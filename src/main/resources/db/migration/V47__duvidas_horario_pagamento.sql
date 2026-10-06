CREATE TABLE pergunta_frequente (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    pergunta VARCHAR(200) NOT NULL,
    palavras_chave VARCHAR(500) NOT NULL,
    resposta TEXT NOT NULL,
    ativa BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em DATETIME NOT NULL,
    atualizado_em DATETIME NOT NULL,
    CONSTRAINT fk_pergunta_frequente_empresa FOREIGN KEY (empresa_id) REFERENCES empresa (id),
    INDEX idx_pergunta_frequente_empresa (empresa_id, ativa)
);

ALTER TABLE empresa
    ADD COLUMN mensagem_fora_horario VARCHAR(500) NULL,
    ADD COLUMN condicoes_pagamento VARCHAR(500) NULL,
    ADD COLUMN lembrete_orcamento_ativo BOOLEAN NOT NULL DEFAULT TRUE;
