import { mkdir, readdir, readFile, rename, rm, writeFile } from 'node:fs/promises';
import path from 'node:path';
import { randomUUID } from 'node:crypto';
import { config } from './config.js';

export type RecebidaTexto = {
    telefone: string;
    mensagem: string;
    mensagemId: string;
    timestamp?: number;
    midiaNaoSuportada?: string;
    reacaoA?: string;
    editaMensagemId?: string;
    apagaMensagemId?: string;
};

export type RecebidaMidia = {
    telefone: string;
    mensagemId: string;
    timestamp?: number;
    mimetype: string;
    legenda?: string;
};

const MAX_TENTATIVAS = 3;
const ESPERA_ENTRE_TENTATIVAS_MS = [500, 1500, 4000];

type Pendencia =
    | { tipo: 'texto'; instanciaToken: string; corpo: RecebidaTexto }
    | { tipo: 'midia'; instanciaToken: string; corpo: RecebidaMidia; arquivo: string }
    | { tipo: 'status'; instanciaToken: string; corpo: { mensagemId: string; status: string } };

export async function notificarMensagemRecebida(instanciaToken: string, corpo: RecebidaTexto): Promise<void> {

    const entregue = await postJsonComRetentativa('/api/whatsapp/webhook/mensagens', instanciaToken, corpo);

    if (!entregue) {
        await enfileirar({ tipo: 'texto', instanciaToken, corpo });
    }
}

export async function notificarMidiaRecebida(
    instanciaToken: string,
    corpo: RecebidaMidia,
    arquivo: Buffer
): Promise<void> {

    const entregue = await postMidiaComRetentativa(instanciaToken, corpo, arquivo);

    if (!entregue) {

        await mkdir(config.filaDir, { recursive: true });

        const caminho = path.join(config.filaDir, `${randomUUID()}.bin`);
        await writeFile(caminho, arquivo);

        await enfileirar({ tipo: 'midia', instanciaToken, corpo, arquivo: caminho });
    }
}

export async function notificarStatusEntrega(
    instanciaToken: string,
    mensagemId: string,
    status: 'ENTREGUE' | 'LIDA' | 'ERRO'
): Promise<void> {

    const corpo = { mensagemId, status };
    const entregue = await postJsonComRetentativa('/api/whatsapp/webhook/status', instanciaToken, corpo);

    if (!entregue) {
        await enfileirar({ tipo: 'status', instanciaToken, corpo });
    }
}

type AvisoConexao = { status: 'CONECTADO' | 'DESCONECTADO'; numero?: string };

const avisosConexaoPendentes = new Map<string, AvisoConexao>();

export async function notificarConexao(
    instanciaToken: string,
    status: 'CONECTADO' | 'DESCONECTADO',
    numero?: string
): Promise<void> {

    const aviso: AvisoConexao = { status, numero };
    avisosConexaoPendentes.set(instanciaToken, aviso);

    if (await postJsonComRetentativa('/api/whatsapp/webhook/conexao', instanciaToken, aviso)) {
        descartarAvisoEntregue(instanciaToken, aviso);
    }
}

export async function reenviarAvisosConexao(): Promise<void> {

    for (const [instanciaToken, aviso] of [...avisosConexaoPendentes]) {

        if (!await postJson('/api/whatsapp/webhook/conexao', instanciaToken, aviso)) {
            return;
        }

        descartarAvisoEntregue(instanciaToken, aviso);

        console.log(`[springClient] aviso de conexão (${aviso.status}) entregue ao backend`);
    }
}

function descartarAvisoEntregue(instanciaToken: string, aviso: AvisoConexao): void {

    if (avisosConexaoPendentes.get(instanciaToken) === aviso) {
        avisosConexaoPendentes.delete(instanciaToken);
    }
}

let reenviando = false;

async function enfileirar(pendencia: Pendencia): Promise<void> {

    await mkdir(config.filaDir, { recursive: true });

    const nome = `${Date.now().toString().padStart(15, '0')}-${randomUUID()}.json`;
    const temporario = path.join(config.filaDir, `${nome}.tmp`);

    await writeFile(temporario, JSON.stringify(pendencia));
    await rename(temporario, path.join(config.filaDir, nome));

    console.error(
        `[springClient] backend indisponível — ${pendencia.tipo} guardada na fila local para reenvio (${nome})`
    );
}

export async function reenviarFila(): Promise<void> {

    if (reenviando) {
        return;
    }

    reenviando = true;

    try {

        let arquivos: string[];

        try {
            arquivos = (await readdir(config.filaDir)).filter((a) => a.endsWith('.json')).sort();
        } catch {
            return;
        }

        for (const nome of arquivos) {

            const caminho = path.join(config.filaDir, nome);
            const pendencia = JSON.parse(await readFile(caminho, 'utf8')) as Pendencia;

            let entregue: boolean;

            if (pendencia.tipo === 'midia') {
                const arquivo = await readFile(pendencia.arquivo).catch(() => null);
                entregue = arquivo === null
                    ? true
                    : await postMidia(pendencia.instanciaToken, pendencia.corpo, arquivo);
            } else {
                const destino = pendencia.tipo === 'texto'
                    ? '/api/whatsapp/webhook/mensagens'
                    : '/api/whatsapp/webhook/status';
                entregue = await postJson(destino, pendencia.instanciaToken, pendencia.corpo);
            }

            if (!entregue) {
                return;
            }

            if (pendencia.tipo === 'midia') {
                await rm(pendencia.arquivo, { force: true });
            }

            await rm(caminho, { force: true });

            console.log(`[springClient] item da fila local entregue ao backend (${nome})`);
        }

    } finally {
        reenviando = false;
    }
}

function aguardar(ms: number): Promise<void> {
    return new Promise((resolve) => setTimeout(resolve, ms));
}

async function postJsonComRetentativa(caminho: string, instanciaToken: string, corpo: unknown): Promise<boolean> {

    for (let tentativa = 1; tentativa <= MAX_TENTATIVAS; tentativa++) {

        if (await postJson(caminho, instanciaToken, corpo)) {
            return true;
        }

        if (tentativa < MAX_TENTATIVAS) {
            await aguardar(ESPERA_ENTRE_TENTATIVAS_MS[tentativa - 1]);
        }
    }

    return false;
}

async function postJson(caminho: string, instanciaToken: string, corpo: unknown): Promise<boolean> {

    try {

        const resposta = await fetch(`${config.springBaseUrl}${caminho}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${instanciaToken}`,
            },
            body: JSON.stringify(corpo),
            signal: AbortSignal.timeout(15000),
        });

        if (resposta.ok) {
            return true;
        }

        if (recusaDefinitiva(resposta.status)) {
            console.error(`[springClient] ${caminho} recusou definitivamente (${resposta.status}) — descartado`);
            return true;
        }

        console.error(`[springClient] ${caminho} respondeu ${resposta.status}`);

    } catch (erro) {
        console.error(`[springClient] falha ao chamar ${caminho}`, erro);
    }

    return false;
}

async function postMidiaComRetentativa(instanciaToken: string, corpo: RecebidaMidia, arquivo: Buffer): Promise<boolean> {

    for (let tentativa = 1; tentativa <= MAX_TENTATIVAS; tentativa++) {

        if (await postMidia(instanciaToken, corpo, arquivo)) {
            return true;
        }

        if (tentativa < MAX_TENTATIVAS) {
            await aguardar(ESPERA_ENTRE_TENTATIVAS_MS[tentativa - 1]);
        }
    }

    return false;
}

async function postMidia(instanciaToken: string, corpo: RecebidaMidia, arquivo: Buffer): Promise<boolean> {

    const formData = new FormData();

    formData.append('telefone', corpo.telefone);
    formData.append('mensagemId', corpo.mensagemId);
    formData.append('arquivo', new Blob([arquivo], { type: corpo.mimetype }), `midia.${extensaoDe(corpo.mimetype)}`);

    if (corpo.timestamp) {
        formData.append('timestamp', String(corpo.timestamp));
    }

    if (corpo.legenda) {
        formData.append('legenda', corpo.legenda);
    }

    try {

        const resposta = await fetch(`${config.springBaseUrl}/api/whatsapp/webhook/midia`, {
            method: 'POST',
            headers: { Authorization: `Bearer ${instanciaToken}` },
            body: formData,
            signal: AbortSignal.timeout(30000),
        });

        if (resposta.ok) {
            return true;
        }

        if (recusaDefinitiva(resposta.status)) {
            console.error(`[springClient] /api/whatsapp/webhook/midia recusou definitivamente (${resposta.status}) — descartada`);
            return true;
        }

        console.error(`[springClient] /api/whatsapp/webhook/midia respondeu ${resposta.status}`);

    } catch (erro) {
        console.error('[springClient] falha ao chamar /api/whatsapp/webhook/midia', erro);
    }

    return false;
}

function recusaDefinitiva(status: number): boolean {
    return status >= 400 && status < 500 && status !== 408 && status !== 429;
}

function extensaoDe(mimetype: string): string {

    if (mimetype === 'image/png') {
        return 'png';
    }

    if (mimetype === 'image/webp') {
        return 'webp';
    }

    return 'jpg';
}
