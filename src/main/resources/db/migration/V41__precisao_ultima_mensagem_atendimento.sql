ALTER TABLE atendimento_whatsapp
    MODIFY COLUMN ultima_mensagem_cliente_em DATETIME(6) NULL,
    MODIFY COLUMN ultima_mensagem_empresa_em DATETIME(6) NULL;
