import { readFileSync } from 'node:fs';
import { rename, writeFile } from 'node:fs/promises';
import { config } from './config.js';

const VALIDADE_MS = 24 * 60 * 60 * 1000;

type Envio = { mensagemId: string; em: number };

const envios = new Map<string, Envio>(carregar());

const emAndamento = new Map<string, Promise<string | null>>();

let gravacao: Promise<void> = Promise.resolve();

function carregar(): [string, Envio][] {

    try {

        const dados = JSON.parse(readFileSync(config.arquivoIdempotencia, 'utf8')) as Record<string, Envio>;
        const agora = Date.now();

        return Object.entries(dados).filter(([, envio]) =>
            envio && typeof envio.mensagemId === 'string' && agora - envio.em < VALIDADE_MS);

    } catch {
        return [];
    }
}

function limparVencidos(): void {

    const agora = Date.now();

    for (const [chave, envio] of envios) {
        if (agora - envio.em > VALIDADE_MS) {
            envios.delete(chave);
        }
    }
}

async function gravar(): Promise<void> {

    const temporario = `${config.arquivoIdempotencia}.tmp`;

    await writeFile(temporario, JSON.stringify(Object.fromEntries(envios)));
    await rename(temporario, config.arquivoIdempotencia);
}

export async function enviarUmaVez(
    chave: string | undefined,
    enviar: () => Promise<string | null>
): Promise<string | null> {

    if (!chave) {
        return enviar();
    }

    limparVencidos();

    const anterior = envios.get(chave);

    if (anterior) {
        return anterior.mensagemId;
    }

    const andamento = emAndamento.get(chave);

    if (andamento) {
        return andamento;
    }

    const promessa = (async () => {

        const mensagemId = await enviar();

        if (mensagemId) {

            envios.set(chave, { mensagemId, em: Date.now() });

            gravacao = gravacao
                .then(gravar)
                .catch((erro) => console.error('[idempotencia] falha ao gravar o registro de envios', erro));

            await gravacao;
        }

        return mensagemId;
    })();

    emAndamento.set(chave, promessa);

    try {
        return await promessa;
    } finally {
        emAndamento.delete(chave);
    }
}
