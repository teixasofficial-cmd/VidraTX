ALTER TABLE orcamento
    ADD COLUMN status_antes_perda VARCHAR(30) NULL;

ALTER TABLE medicao
    ADD COLUMN cancelamento_solicitado_em DATETIME(6) NULL,
    ADD COLUMN cancelamento_solicitado_texto TEXT NULL;

ALTER TABLE instalacao
    ADD COLUMN cancelamento_solicitado_em DATETIME(6) NULL,
    ADD COLUMN cancelamento_solicitado_texto TEXT NULL;

ALTER TABLE atendimento_whatsapp
    ADD COLUMN cliente_aguardando_desde DATETIME(6) NULL,
    ADD COLUMN aviso_espera_enviado_em DATETIME(6) NULL;

UPDATE atendimento_whatsapp
SET cliente_aguardando_desde = ultima_mensagem_cliente_em
WHERE ultima_mensagem_cliente_em IS NOT NULL;
