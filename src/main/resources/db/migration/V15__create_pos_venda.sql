CREATE TABLE pos_venda (
    id BIGINT NOT NULL AUTO_INCREMENT,
    ordem_servico_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ABERTA',
    problema TEXT NOT NULL,
    atendimento TEXT,
    solucao TEXT,
    criado_em DATETIME NOT NULL,
    atualizado_em DATETIME NOT NULL,
    resolvido_em DATETIME,
    encerrado_em DATETIME,

    CONSTRAINT pk_pos_venda PRIMARY KEY (id),

    CONSTRAINT fk_pos_venda_ordem_servico
        FOREIGN KEY (ordem_servico_id)
            REFERENCES ordem_servico(id)
);

CREATE INDEX idx_pos_venda_ordem_servico_status
    ON pos_venda (ordem_servico_id, status);
