CREATE TABLE medicao (
    id BIGINT NOT NULL AUTO_INCREMENT,
    orcamento_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    data_agendada DATETIME NOT NULL,
    data_realizada DATETIME,
    endereco VARCHAR(255),
    observacoes TEXT,
    criado_em DATETIME NOT NULL,
    atualizado_em DATETIME NOT NULL,

    CONSTRAINT pk_medicao PRIMARY KEY (id),

    CONSTRAINT fk_medicao_orcamento
        FOREIGN KEY (orcamento_id)
            REFERENCES orcamento(id),

    CONSTRAINT uk_medicao_orcamento
        UNIQUE (orcamento_id)
);

CREATE INDEX idx_medicao_data_agendada
    ON medicao (data_agendada);
