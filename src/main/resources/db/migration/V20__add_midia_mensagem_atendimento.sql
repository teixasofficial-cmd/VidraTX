ALTER TABLE mensagem_atendimento
    MODIFY COLUMN conteudo TEXT NULL,
    ADD COLUMN tipo VARCHAR(20) NOT NULL DEFAULT 'TEXTO' AFTER remetente,
    ADD COLUMN midia_url VARCHAR(500) NULL AFTER conteudo,
    ADD COLUMN midia_content_type VARCHAR(100) NULL AFTER midia_url;
