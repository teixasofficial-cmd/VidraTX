export const config = {
    port: Number(process.env.PORT ?? 3333),
    gatewayToken: process.env.GATEWAY_TOKEN ?? '',
    springBaseUrl: process.env.SPRING_BASE_URL ?? 'http://localhost:8080',
    authDir: process.env.AUTH_STATE_DIR ?? 'auth_sessions',
    filaDir: process.env.FILA_DIR ?? 'fila_webhook',
    timeoutVersaoMs: Number(process.env.TIMEOUT_VERSAO_MS ?? 5000),
    janelaMensagensOfflineHoras: Number(process.env.JANELA_MENSAGENS_OFFLINE_HORAS ?? 72),
    arquivoIdempotencia: process.env.ARQUIVO_IDEMPOTENCIA ?? 'envios-idempotencia.json',
};

if (!config.gatewayToken) {
    console.warn(
        '[config] GATEWAY_TOKEN não definido — nenhuma chamada do backend Spring ' +
        'vai ser aceita. Use o mesmo valor configurado em WHATSAPP_GATEWAY_TOKEN ' +
        'no backend.'
    );
}
