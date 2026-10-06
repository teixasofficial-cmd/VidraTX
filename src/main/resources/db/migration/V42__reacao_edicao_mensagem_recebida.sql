ALTER TABLE mensagem_recebida
    ADD COLUMN reacao_a_mensagem_id VARCHAR(128) NULL,
    ADD COLUMN edita_mensagem_id VARCHAR(128) NULL,
    ADD COLUMN apaga_mensagem_id VARCHAR(128) NULL;
