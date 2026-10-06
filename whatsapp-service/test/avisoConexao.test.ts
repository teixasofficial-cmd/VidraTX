import { test } from 'node:test';
import assert from 'node:assert/strict';
import { createServer } from 'node:http';
import type { AddressInfo } from 'node:net';

let backendNoAr = false;
const recebidos: { token: string; corpo: unknown }[] = [];

const servidor = createServer((req, res) => {

    let corpo = '';
    req.on('data', (parte) => { corpo += parte; });
    req.on('end', () => {
        if (!backendNoAr) {
            res.writeHead(503).end();
            return;
        }
        recebidos.push({ token: req.headers.authorization ?? '', corpo: JSON.parse(corpo) });
        res.writeHead(200).end();
    });
});

await new Promise<void>((resolve) => servidor.listen(0, '127.0.0.1', resolve));
process.env.SPRING_BASE_URL = `http://127.0.0.1:${(servidor.address() as AddressInfo).port}`;
process.env.GATEWAY_TOKEN = 'teste';

const { notificarConexao, reenviarAvisosConexao } = await import('../src/springClient.js');

test('aviso de conexão que não entrou é reenviado quando o backend volta, só o mais recente', async () => {

    await notificarConexao('instancia-a', 'DESCONECTADO');
    await notificarConexao('instancia-a', 'CONECTADO', '5511999990000');
    await reenviarAvisosConexao();
    assert.equal(recebidos.length, 0);

    backendNoAr = true;
    await reenviarAvisosConexao();

    assert.deepEqual(recebidos, [
        { token: 'Bearer instancia-a', corpo: { status: 'CONECTADO', numero: '5511999990000' } },
    ]);

    await reenviarAvisosConexao();
    assert.equal(recebidos.length, 1);

    servidor.close();
});
