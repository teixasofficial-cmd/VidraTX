import { test } from 'node:test';
import assert from 'node:assert/strict';
import { proto, type WAMessage } from '@whiskeysockets/baileys';
import { dentroDaJanela, montarRepasse, ordenarPorEnvio } from '../src/repasse.js';

const CLIENTE = '5511988887777@s.whatsapp.net';

function msg(message: proto.IMessage, extra: Partial<WAMessage['key']> = {}, timestamp = 1_790_000_000): WAMessage {
    return {
        key: { remoteJid: CLIENTE, fromMe: false, id: 'MSG1', ...extra },
        message,
        messageTimestamp: timestamp,
    } as WAMessage;
}

const TIPO = proto.Message.ProtocolMessage.Type;

test('texto comum vai como texto', () => {

    const r = montarRepasse(msg({ conversation: '1' }));

    assert.deepEqual(r, {
        tipo: 'texto',
        corpo: { telefone: '5511988887777', mensagem: '1', mensagemId: 'MSG1', timestamp: 1_790_000_000 },
    });
});

test('A1: reação vai com o id da mensagem reagida', () => {

    const r = montarRepasse(msg({ reactionMessage: { text: '👍', key: { id: 'PROPOSTA9', fromMe: true } } }));

    assert.equal(r.tipo, 'texto');
    assert.equal(r.tipo === 'texto' && r.corpo.reacaoA, 'PROPOSTA9');
    assert.equal(r.tipo === 'texto' && r.corpo.mensagem, '👍');
});

test('A1: reação removida não é mensagem', () => {
    assert.equal(montarRepasse(msg({ reactionMessage: { text: '', key: { id: 'X' } } })).tipo, 'ignorar');
});

test('A4: edição vai com o id da original e o texto novo', () => {

    const edicao = {
        protocolMessage: {
            type: TIPO.MESSAGE_EDIT,
            key: { id: 'ORIGINAL', remoteJid: CLIENTE, fromMe: false },
            editedMessage: { conversation: '2' },
            timestampMs: 1_790_000_060_000,
        },
    };

    for (const mensagem of [edicao, { editedMessage: { message: edicao } }]) {

        const r = montarRepasse(msg(mensagem, { id: 'EDICAO1' }));

        assert.equal(r.tipo, 'texto');
        assert.deepEqual(r.tipo === 'texto' && r.corpo, {
            telefone: '5511988887777', mensagem: '2', mensagemId: 'EDICAO1', timestamp: 1_790_000_060,
            editaMensagemId: 'ORIGINAL',
        });
    }
});

test('A4: "apagar para todos" vai com o id da apagada', () => {

    const r = montarRepasse(msg({ protocolMessage: { type: TIPO.REVOKE, key: { id: 'APAGADA' } } }, { id: 'REVOKE1' }));

    assert.equal(r.tipo, 'texto');
    assert.equal(r.tipo === 'texto' && r.corpo.apagaMensagemId, 'APAGADA');
    assert.equal(r.tipo === 'texto' && r.corpo.mensagem, '');
});

test('outras protocolMessage continuam ignoradas', () => {
    assert.equal(montarRepasse(msg({ protocolMessage: { type: TIPO.EPHEMERAL_SETTING, ephemeralExpiration: 86400 } })).tipo,
        'ignorar');
});

test('mensagem do próprio número, de grupo ou sem conteúdo é ignorada', () => {

    assert.equal(montarRepasse(msg({ conversation: 'oi' }, { fromMe: true })).tipo, 'ignorar');
    assert.equal(montarRepasse(msg({ conversation: 'oi' }, { remoteJid: '1203630@g.us' })).tipo, 'ignorar');
    assert.equal(montarRepasse({ key: { remoteJid: CLIENTE, id: 'X' } } as WAMessage).tipo, 'ignorar');
});

test('áudio avisa o tipo ao backend', () => {

    const r = montarRepasse(msg({ audioMessage: { seconds: 5 } }));

    assert.equal(r.tipo === 'texto' && r.corpo.midiaNaoSuportada, 'AUDIO');
});

test('mensagem temporária é desembrulhada', () => {

    const r = montarRepasse(msg({ ephemeralMessage: { message: { extendedTextMessage: { text: 'sim' } } } }));

    assert.equal(r.tipo === 'texto' && r.corpo.mensagem, 'sim');
});

test('A2: janela das mensagens recebidas com o serviço fora do ar', () => {

    const agora = 1_790_000_000_000;

    assert.equal(dentroDaJanela(msg({ conversation: 'a' }, {}, agora / 1000 - 3600), agora, 72), true);
    assert.equal(dentroDaJanela(msg({ conversation: 'a' }, {}, agora / 1000 - 80 * 3600), agora, 72), false);
    assert.equal(dentroDaJanela({ key: {}, message: { conversation: 'a' } } as WAMessage, agora, 72), true);
});

test('A2: lote da reconexão sai em ordem de envio', () => {

    const lote = [msg({ conversation: 'c' }, { id: 'C' }, 30), msg({ conversation: 'a' }, { id: 'A' }, 10),
        msg({ conversation: 'b' }, { id: 'B' }, 20)];

    assert.deepEqual(ordenarPorEnvio(lote).map((m) => m.key.id), ['A', 'B', 'C']);
});
