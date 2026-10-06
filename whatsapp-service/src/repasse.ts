import {
    isJidUser,
    isLidUser,
    jidNormalizedUser,
    normalizeMessageContent,
    proto,
    type WAMessage,
} from '@whiskeysockets/baileys';
import type { RecebidaMidia, RecebidaTexto } from './springClient.js';

export type Repasse =
    | { tipo: 'texto'; corpo: RecebidaTexto }
    | { tipo: 'imagem'; corpo: RecebidaMidia }
    | { tipo: 'ignorar'; aviso?: string };

const IGNORAR: Repasse = { tipo: 'ignorar' };

export function dentroDaJanela(mensagem: WAMessage, agoraMs: number, horas: number): boolean {

    const timestamp = Number(mensagem.messageTimestamp ?? 0);

    return timestamp <= 0 || timestamp * 1000 >= agoraMs - horas * 3600 * 1000;
}

export function ordenarPorEnvio(mensagens: WAMessage[]): WAMessage[] {

    return [...mensagens].sort(
        (a, b) => Number(a.messageTimestamp ?? 0) - Number(b.messageTimestamp ?? 0)
    );
}

function telefoneDoRemetente(mensagem: WAMessage): string | null {

    const remoteJid = mensagem.key.remoteJid ?? undefined;

    if (isJidUser(remoteJid)) {
        return jidNormalizedUser(remoteJid).split('@')[0] ?? null;
    }

    if (isLidUser(remoteJid) && mensagem.key.senderPn && isJidUser(mensagem.key.senderPn)) {
        return jidNormalizedUser(mensagem.key.senderPn).split('@')[0] ?? null;
    }

    return null;
}

function textoDe(conteudo: proto.IMessage | null | undefined): string | null {

    return conteudo?.conversation
        ?? conteudo?.extendedTextMessage?.text
        ?? conteudo?.buttonsResponseMessage?.selectedDisplayText
        ?? conteudo?.templateButtonReplyMessage?.selectedDisplayText
        ?? conteudo?.listResponseMessage?.title
        ?? null;
}

export function montarRepasse(mensagem: WAMessage): Repasse {

    if (mensagem.key.fromMe || !mensagem.message || !mensagem.key.id) {
        return IGNORAR;
    }

    const remoteJid = mensagem.key.remoteJid ?? '';

    if (!isJidUser(remoteJid) && !isLidUser(remoteJid)) {
        return IGNORAR;
    }

    const telefone = telefoneDoRemetente(mensagem);

    if (!telefone) {
        return {
            tipo: 'ignorar',
            aviso: 'mensagem de um contato @lid sem número informado — não é possível identificar o cliente',
        };
    }

    const mensagemId = mensagem.key.id;
    const timestamp = Number(mensagem.messageTimestamp ?? 0) || undefined;

    const conteudo = normalizeMessageContent(mensagem.message);

    if (!conteudo) {
        return IGNORAR;
    }

    const protocolo = conteudo.protocolMessage;

    if (protocolo?.key?.id && protocolo.type === proto.Message.ProtocolMessage.Type.MESSAGE_EDIT) {

        const textoEditado = textoDe(normalizeMessageContent(protocolo.editedMessage ?? undefined));

        if (!textoEditado) {
            return IGNORAR;
        }

        const momentoEdicao = protocolo.timestampMs
            ? Math.floor(Number(protocolo.timestampMs) / 1000)
            : timestamp;

        return {
            tipo: 'texto',
            corpo: {
                telefone, mensagem: textoEditado, mensagemId, timestamp: momentoEdicao,
                editaMensagemId: protocolo.key.id,
            },
        };
    }

    if (protocolo?.key?.id && protocolo.type === proto.Message.ProtocolMessage.Type.REVOKE) {

        return {
            tipo: 'texto',
            corpo: { telefone, mensagem: '', mensagemId, timestamp, apagaMensagemId: protocolo.key.id },
        };
    }

    if (protocolo || conteudo.senderKeyDistributionMessage && Object.keys(conteudo).length === 1) {
        return IGNORAR;
    }

    if (conteudo.imageMessage) {

        return {
            tipo: 'imagem',
            corpo: {
                telefone,
                mensagemId,
                timestamp,
                mimetype: conteudo.imageMessage.mimetype ?? 'image/jpeg',
                legenda: conteudo.imageMessage.caption ?? undefined,
            },
        };
    }

    if (conteudo.reactionMessage) {

        const reacao = conteudo.reactionMessage;

        if (!reacao.text) {
            return IGNORAR;
        }

        return {
            tipo: 'texto',
            corpo: { telefone, mensagem: reacao.text, mensagemId, timestamp, reacaoA: reacao.key?.id ?? undefined },
        };
    }

    const texto = textoDe(conteudo);

    if (texto) {
        return { tipo: 'texto', corpo: { telefone, mensagem: texto, mensagemId, timestamp } };
    }

    const naoSuportada =
        conteudo.audioMessage ? 'AUDIO'
            : conteudo.videoMessage || conteudo.ptvMessage ? 'VIDEO'
                : conteudo.stickerMessage ? 'FIGURINHA'
                    : conteudo.documentMessage ? 'DOCUMENTO'
                        : conteudo.locationMessage || conteudo.liveLocationMessage ? 'LOCALIZACAO'
                            : conteudo.contactMessage || conteudo.contactsArrayMessage ? 'CONTATO'
                                : 'OUTRO';

    const legenda = conteudo.videoMessage?.caption ?? conteudo.documentMessage?.caption ?? '';

    return {
        tipo: 'texto',
        corpo: { telefone, mensagem: legenda, mensagemId, timestamp, midiaNaoSuportada: naoSuportada },
    };
}
