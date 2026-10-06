CREATE TABLE ordem_servico (
    id BIGINT NOT NULL AUTO_INCREMENT,
    empresa_id BIGINT NOT NULL,
    orcamento_id BIGINT NOT NULL,
    necessita_producao BOOLEAN NOT NULL DEFAULT TRUE,
    status_producao VARCHAR(30),
    observacoes TEXT,
    criado_em DATETIME NOT NULL,
    atualizado_em DATETIME NOT NULL,
    producao_iniciada_em DATETIME,
    producao_concluida_em DATETIME,
    producao_conferida_em DATETIME,

    CONSTRAINT pk_ordem_servico PRIMARY KEY (id),

    CONSTRAINT fk_ordem_servico_empresa
        FOREIGN KEY (empresa_id)
            REFERENCES empresa(id),

    CONSTRAINT fk_ordem_servico_orcamento
        FOREIGN KEY (orcamento_id)
            REFERENCES orcamento(id),

    CONSTRAINT uk_ordem_servico_orcamento
        UNIQUE (orcamento_id)
);

CREATE INDEX idx_ordem_servico_empresa_status
    ON ordem_servico (empresa_id, status_producao);
