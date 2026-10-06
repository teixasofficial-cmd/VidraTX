import path from 'node:path';
import { readdir, rm, stat } from 'node:fs/promises';
import type { Boom } from '@hapi/boom';
import pino from 'pino';
import QRCode from 'qrcode';
import qrcodeTerminal from 'qrcode-terminal';
import makeWASocket, {
    DisconnectReason,
    downloadMediaMessage,
    fetchLatestBaileysVersion,
    jidNormalizedUser,
    proto,
    useMultiFileAuthState,
    type WAMessage,
    type WASocket,
} from '@whiskeysockets/baileys';
import { config } from './config.js';
import { enviarUmaVez } from './idempotencia.js';
import { dentroDaJanela, montarRepasse, ordenarPorEnvio } from './repasse.js';
import {
    notificarConexao,
    notificarMensagemRecebida,
    notificarMidiaRecebida,
    notificarStatusEntrega,
} from './springClient.js';

const logger = pino({ level: 'silent' });

type EstadoSessao = 'CONECTANDO' | 'CONECTADO' | 'DESCONECTADO';

type Sessao = {
    sock: WASocket;
    estado: EstadoSessao;
    qr?: string;
    tentativasReconexao: number;
    reconexaoAgendada?: NodeJS.Timeout;
    filaEntrega: Promise<void>;
};

const sessoes = new Map<string, Sessao>();

const cacheNumeros = new Map<string, { jid: string | null; em: number }>();
const VALIDADE_CACHE_NUMERO_MS = 6 * 60 * 60 * 1000;

const ESPERAS_RECONEXAO_MS = [2000, 5000, 15000, 30000, 60000];

let versaoWhatsapp: [number, number, number] | undefined;
let versaoConsultada = false;

export class NumeroSemWhatsappError extends Error {
}

function mascarar(instanciaToken: string): string {
    return instanciaToken.length <= 8 ? '***' : `${instanciaToken.slice(0, 8)}…`;
}

async function versao(): Promise<[number, number, number] | undefined> {

    if (versaoConsultada) {
        return versaoWhatsapp;
    }

    versaoConsultada = true;

    try {

        const resultado = await Promise.race([
            fetchLatestBaileysVersion(),
            new Promise<never>((_, rejeitar) =>
                setTimeout(() => rejeitar(new Error('timeout')), config.timeoutVersaoMs)),
        ]);

        versaoWhatsapp = resultado.version as [number, number, number];

    } catch {

        console.warn('[sessions] não foi possível consultar a versão do WhatsApp Web — usando a embutida no Baileys');
    }

    return versaoWhatsapp;
}

export function estadoSessao(instanciaToken: string): EstadoSessao {
    return sessoes.get(instanciaToken)?.estado ?? 'DESCONECTADO';
}

export function resumoSessoes(): { total: number; conectadas: number } {

    let conectadas = 0;

    for (const sessao of sessoes.values()) {
        if (sessao.estado === 'CONECTADO') {
            conectadas++;
        }
    }

    return { total: sessoes.size, conectadas };
}

export async function qrDaSessao(instanciaToken: string): Promise<string | null> {

    const sessao = sessoes.get(instanciaToken);

    if (!sessao?.qr || sessao.estado === 'CONECTADO') {
        return null;
    }

    return QRCode.toDataURL(sessao.qr, { margin: 1, width: 300 });
}

export async function restaurarSessoes(): Promise<void> {

    let pastas: string[];

    try {
        pastas = await readdir(config.authDir);
    } catch {
        return;
    }

    for (const pasta of pastas) {

        try {

            await stat(path.join(config.authDir, pasta, 'creds.json'));

            console.log(`[sessions] restaurando a sessão ${mascarar(pasta)}`);
            await iniciarSessao(pasta);

        } catch {
        }
    }
}

export async function iniciarSessao(instanciaToken: string): Promise<void> {

    const existente = sessoes.get(instanciaToken);

    if (existente && existente.estado !== 'DESCONECTADO') {
        return;
    }

    if (existente?.reconexaoAgendada) {
        clearTimeout(existente.reconexaoAgendada);
        existente.reconexaoAgendada = undefined;
    }

    const diretorioAuth = path.join(config.authDir, instanciaToken);
    const { state, saveCreds } = await useMultiFileAuthState(diretorioAuth);
    const versaoAtual = await versao();

    const sock = makeWASocket({
        auth: state,
        logger,
        ...(versaoAtual ? { version: versaoAtual } : {}),
        markOnlineOnConnect: false,
        syncFullHistory: false,
    });

    const sessao: Sessao = {
        sock,
        estado: 'CONECTANDO',
        tentativasReconexao: existente?.tentativasReconexao ?? 0,
        filaEntrega: Promise.resolve(),
    };

    sessoes.set(instanciaToken, sessao);

    sock.ev.on('creds.update', saveCreds);

    sock.ev.on('connection.update', async (update) => {

        const { connection, lastDisconnect, qr } = update;

        if (qr) {
            sessao.qr = qr;
            console.log(`\n[sessions] QR disponível no painel (instância ${mascarar(instanciaToken)}):\n`);
            qrcodeTerminal.generate(qr, { small: true });
        }

        if (connection === 'open') {

            sessao.estado = 'CONECTADO';
            sessao.qr = undefined;
            sessao.tentativasReconexao = 0;

            const numero = sock.user?.id ? jidNormalizedUser(sock.user.id).split('@')[0] : undefined;

            console.log(`[sessions] instância ${mascarar(instanciaToken)} conectada (${numero ?? '??'})`);

            await notificarConexao(instanciaToken, 'CONECTADO', numero);
        }

        if (connection === 'close') {

            sessao.estado = 'DESCONECTADO';

            const statusCode = (lastDisconnect?.error as Boom | undefined)?.output?.statusCode;
            const deslogado = statusCode === DisconnectReason.loggedOut;

            await notificarConexao(instanciaToken, 'DESCONECTADO');

            if (deslogado) {

                sessoes.delete(instanciaToken);
                await rm(diretorioAuth, { recursive: true, force: true });

                console.log(
                    `[sessions] instância ${mascarar(instanciaToken)} saiu (logout no aparelho) — ` +
                    'conecte de novo pelo painel e escaneie um QR novo'
                );

                return;
            }

            const espera = ESPERAS_RECONEXAO_MS[Math.min(sessao.tentativasReconexao, ESPERAS_RECONEXAO_MS.length - 1)];
            sessao.tentativasReconexao++;

            console.log(
                `[sessions] conexão da instância ${mascarar(instanciaToken)} caiu (código ${statusCode ?? '?'}); ` +
                `nova tentativa em ${espera / 1000}s`
            );

            sessao.reconexaoAgendada = setTimeout(() => {
                sessao.reconexaoAgendada = undefined;
                iniciarSessao(instanciaToken).catch((erro) =>
                    console.error(`[sessions] falha ao reconectar ${mascarar(instanciaToken)}`, erro));
            }, espera);
        }
    });

    sock.ev.on('messages.upsert', (evento) => {

        for (const mensagem of ordenarPorEnvio(evento.messages)) {

            if (!dentroDaJanela(mensagem, Date.now(), config.janelaMensagensOfflineHoras)) {

                if (!mensagem.key.fromMe) {
                    console.warn(
                        `[sessions] mensagem ${mensagem.key.id} fora da janela de ` +
                        `${config.janelaMensagensOfflineHoras} h — não repassada (${evento.type})`
                    );
                }

                continue;
            }

            sessao.filaEntrega = sessao.filaEntrega
                .then(() => repassarMensagem(instanciaToken, sock, mensagem))
                .catch((erro) => console.error(`[sessions] falha ao repassar mensagem (${mascarar(instanciaToken)})`, erro));
        }
    });

    sock.ev.on('messages.update', (atualizacoes) => {

        for (const { key, update } of atualizacoes) {

            if (!key.fromMe || !key.id || update.status === undefined || update.status === null) {
                continue;
            }

            const status = traduzirRecibo(update.status);

            if (status) {
                notificarStatusEntrega(instanciaToken, key.id, status).catch((erro) =>
                    console.error('[sessions] falha ao repassar recibo', erro));
            }
        }
    });
}

function traduzirRecibo(status: number): 'ENTREGUE' | 'LIDA' | 'ERRO' | null {

    const Status = proto.WebMessageInfo.Status;

    if (status === Status.DELIVERY_ACK) {
        return 'ENTREGUE';
    }

    if (status === Status.READ || status === Status.PLAYED) {
        return 'LIDA';
    }

    if (status === Status.ERROR) {
        return 'ERRO';
    }

    return null;
}

async function repassarMensagem(instanciaToken: string, sock: WASocket, mensagem: WAMessage): Promise<void> {

    const repasse = montarRepasse(mensagem);

    if (repasse.tipo === 'ignorar') {

        if (repasse.aviso) {
            console.warn(`[sessions] ${repasse.aviso} (instância ${mascarar(instanciaToken)})`);
        }

        return;
    }

    if (repasse.tipo === 'imagem') {

        const buffer = (await downloadMediaMessage(
            mensagem, 'buffer', {}, { logger, reuploadRequest: sock.updateMediaMessage }
        )) as Buffer;

        await notificarMidiaRecebida(instanciaToken, repasse.corpo, buffer);
        return;
    }

    await notificarMensagemRecebida(instanciaToken, repasse.corpo);
}

export async function enviarMensagem(
    instanciaToken: string,
    telefone: string,
    mensagem: string,
    chaveIdempotencia?: string
): Promise<string | null> {

    limparCaches();

    const chave = chaveIdempotencia ? `${instanciaToken}:${chaveIdempotencia}` : undefined;

    return enviarUmaVez(chave, async () => {

        const sessao = sessoes.get(instanciaToken);

        if (!sessao || sessao.estado !== 'CONECTADO') {
            console.error(`[sessions] envio pela instância ${mascarar(instanciaToken)} sem sessão conectada`);
            return null;
        }

        const jid = await resolverNumero(sessao.sock, telefone);

        if (!jid) {
            throw new NumeroSemWhatsappError(`O número ${telefone} não tem WhatsApp`);
        }

        const enviada = await sessao.sock.sendMessage(jid, { text: mensagem });

        return enviada?.key?.id ?? '';
    });
}

async function resolverNumero(sock: WASocket, telefone: string): Promise<string | null> {

    const digitos = telefone.replace(/\D/g, '');
    const emCache = cacheNumeros.get(digitos);

    if (emCache && Date.now() - emCache.em < VALIDADE_CACHE_NUMERO_MS) {
        return emCache.jid;
    }

    let jid: string | null = null;

    try {

        const [resultado] = (await sock.onWhatsApp(`${digitos}@s.whatsapp.net`)) ?? [];
        jid = resultado?.exists ? resultado.jid : null;

    } catch (erro) {

        console.warn('[sessions] onWhatsApp falhou — enviando para o endereço direto', erro);
        return `${digitos}@s.whatsapp.net`;
    }

    cacheNumeros.set(digitos, { jid, em: Date.now() });

    return jid;
}

function limparCaches(): void {

    const agora = Date.now();

    for (const [numero, valor] of cacheNumeros) {
        if (agora - valor.em > VALIDADE_CACHE_NUMERO_MS) {
            cacheNumeros.delete(numero);
        }
    }
}

export function encerrarTodas(): void {

    for (const sessao of sessoes.values()) {

        if (sessao.reconexaoAgendada) {
            clearTimeout(sessao.reconexaoAgendada);
        }

        try {
            sessao.sock.end(undefined);
        } catch {
        }
    }
}
