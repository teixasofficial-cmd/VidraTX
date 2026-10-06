CREATE TABLE foto (
    id BIGINT NOT NULL AUTO_INCREMENT,
    orcamento_id BIGINT NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    url VARCHAR(500) NOT NULL,
    descricao VARCHAR(500),
    criado_em DATETIME NOT NULL,

    CONSTRAINT pk_foto PRIMARY KEY (id),

    CONSTRAINT fk_foto_orcamento
        FOREIGN KEY (orcamento_id)
            REFERENCES orcamento(id)
);

CREATE INDEX idx_foto_orcamento
    ON foto (orcamento_id);
