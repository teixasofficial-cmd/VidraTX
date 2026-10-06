import { test } from 'node:test';
import assert from 'node:assert/strict';
import { execFileSync } from 'node:child_process';
import { mkdtempSync, readFileSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import path from 'node:path';

const pasta = mkdtempSync(path.join(tmpdir(), 'idempotencia-'));
const arquivo = path.join(pasta, 'envios.json');

process.env.ARQUIVO_IDEMPOTENCIA = arquivo;

const { enviarUmaVez } = await import('../src/idempotencia.js');

function emOutroProcesso(chave: string, idNovo: string): { id: string | null; enviou: boolean } {

    const script = `
        const { enviarUmaVez } = await import(${JSON.stringify(path.resolve('src/idempotencia.ts'))});
        let enviou = false;
        const id = await enviarUmaVez(${JSON.stringify(chave)}, async () => { enviou = true; return ${JSON.stringify(idNovo)}; });
        console.log(JSON.stringify({ id, enviou }));
    `;

    const saida = execFileSync(process.execPath, ['--import', 'tsx', '--input-type=module', '-e', script], {
        env: { ...process.env, ARQUIVO_IDEMPOTENCIA: arquivo },
        encoding: 'utf8',
    });

    return JSON.parse(saida.trim().split('\n').pop()!);
}

test('A3: repetição da mesma chave depois de um reinício não envia de novo', async () => {

    let envios = 0;

    const id = await enviarUmaVez('empresa:saida-1', async () => { envios++; return 'WA-1'; });

    assert.equal(id, 'WA-1');
    assert.equal(envios, 1);
    assert.equal(JSON.parse(readFileSync(arquivo, 'utf8'))['empresa:saida-1'].mensagemId, 'WA-1');

    assert.deepEqual(emOutroProcesso('empresa:saida-1', 'WA-DUPLICADA'), { id: 'WA-1', enviou: false });
});

test('A3: duas chamadas simultâneas com a mesma chave enviam uma vez', async () => {

    let envios = 0;
    const enviar = async () => { envios++; await new Promise((r) => setTimeout(r, 50)); return 'WA-2'; };

    const [a, b] = await Promise.all([enviarUmaVez('empresa:saida-2', enviar), enviarUmaVez('empresa:saida-2', enviar)]);

    assert.equal(a, 'WA-2');
    assert.equal(b, 'WA-2');
    assert.equal(envios, 1);
});

test('A3: envio sem sessão (null) não é registrado e pode ser repetido', async () => {

    assert.equal(await enviarUmaVez('empresa:saida-3', async () => null), null);
    assert.equal(await enviarUmaVez('empresa:saida-3', async () => 'WA-3'), 'WA-3');
});

test('A3: registro com mais de 24 h não vale mais', () => {

    const dados = JSON.parse(readFileSync(arquivo, 'utf8'));
    dados['empresa:velha'] = { mensagemId: 'WA-VELHA', em: Date.now() - 25 * 3600 * 1000 };
    writeFileSync(arquivo, JSON.stringify(dados));

    assert.deepEqual(emOutroProcesso('empresa:velha', 'WA-NOVA'), { id: 'WA-NOVA', enviou: true });
});
