import express, { type NextFunction, type Request, type Response } from 'express';
import { config } from './config.js';
import {
    NumeroSemWhatsappError,
    encerrarTodas,
    enviarMensagem,
    estadoSessao,
    iniciarSessao,
    qrDaSessao,
    restaurarSessoes,
    resumoSessoes,
} from './sessions.js';
import { reenviarAvisosConexao, reenviarFila } from './springClient.js';

const app = express();
app.use(express.json({ limit: '256kb' }));

function exigirGatewayToken(req: Request, res: Response, next: NextFunction): void {

    const autorizacao = req.header('authorization');

    if (!config.gatewayToken || autorizacao !== `Bearer ${config.gatewayToken}`) {
        res.status(401).json({ erro: 'Token inválido' });
        return;
    }

    next();
}

app.get('/health', (_req: Request, res: Response) => {
    res.json({ status: 'UP', sessoes: resumoSessoes() });
});

app.post('/sessoes/:instanciaToken/iniciar', exigirGatewayToken, async (req: Request, res: Response) => {

    try {

        await iniciarSessao(req.params.instanciaToken);
        res.status(202).send();

    } catch (erro) {

        console.error('[index] falha ao iniciar sessão', erro);
        res.status(500).json({ erro: 'Falha ao iniciar sessão' });
    }
});

app.get('/sessoes/:instanciaToken/qr', exigirGatewayToken, async (req: Request, res: Response) => {

    try {

        res.json({
            status: estadoSessao(req.params.instanciaToken),
            qr: await qrDaSessao(req.params.instanciaToken),
        });

    } catch (erro) {

        console.error('[index] falha ao gerar o QR', erro);
        res.status(500).json({ erro: 'Falha ao gerar o QR' });
    }
});

app.get('/sessoes/:instanciaToken/status', exigirGatewayToken, (req: Request, res: Response) => {
    res.json({ status: estadoSessao(req.params.instanciaToken) });
});

app.post('/mensagens/enviar', exigirGatewayToken, async (req: Request, res: Response) => {

    const { instanciaToken, telefone, mensagem, idempotencyKey } = req.body ?? {};

    if (!instanciaToken || !telefone || !mensagem) {
        res.status(400).json({ erro: 'instanciaToken, telefone e mensagem são obrigatórios' });
        return;
    }

    try {

        const mensagemId = await enviarMensagem(instanciaToken, telefone, mensagem, idempotencyKey);

        if (mensagemId === null) {
            res.status(409).json({ erro: 'Nenhuma sessão ativa para esta instância' });
            return;
        }

        res.status(200).json({ mensagemId });

    } catch (erro) {

        if (erro instanceof NumeroSemWhatsappError) {
            res.status(422).json({ erro: 'Este número não tem WhatsApp' });
            return;
        }

        console.error('[index] falha ao enviar mensagem', erro);
        res.status(500).json({ erro: 'Falha ao enviar mensagem' });
    }
});

const servidor = app.listen(config.port, () => {

    console.log(`[index] whatsapp-service ouvindo na porta ${config.port}`);

    restaurarSessoes().catch((erro) => console.error('[index] falha ao restaurar sessões', erro));
});

const reenvio = setInterval(() => {
    reenviarFila().catch((erro) => console.error('[index] falha ao reenviar a fila local', erro));
    reenviarAvisosConexao().catch((erro) => console.error('[index] falha ao reenviar os avisos de conexão', erro));
}, 15000);

function desligar(): void {

    clearInterval(reenvio);
    encerrarTodas();
    servidor.close(() => process.exit(0));

    setTimeout(() => process.exit(0), 5000).unref();
}

process.on('SIGTERM', desligar);
process.on('SIGINT', desligar);
