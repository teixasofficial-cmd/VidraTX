DROP TABLE orcamento_item;

ALTER TABLE orcamento
    ADD COLUMN solicitacao_orcamento_id BIGINT,
    ADD CONSTRAINT fk_orcamento_solicitacao
        FOREIGN KEY (solicitacao_orcamento_id)
            REFERENCES solicitacao_orcamento(id);

CREATE INDEX idx_orcamento_solicitacao
    ON orcamento (solicitacao_orcamento_id);
