CREATE TABLE instalacao (
    id BIGINT NOT NULL AUTO_INCREMENT,
    ordem_servico_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    data_agendada DATETIME NOT NULL,
    data_realizada DATETIME,
    endereco VARCHAR(255),
    checklist TEXT,
    observacoes TEXT,
    criado_em DATETIME NOT NULL,
    atualizado_em DATETIME NOT NULL,

    CONSTRAINT pk_instalacao PRIMARY KEY (id),

    CONSTRAINT fk_instalacao_ordem_servico
        FOREIGN KEY (ordem_servico_id)
            REFERENCES ordem_servico(id),

    CONSTRAINT uk_instalacao_ordem_servico
        UNIQUE (ordem_servico_id)
);

CREATE INDEX idx_instalacao_data_agendada
    ON instalacao (data_agendada);
