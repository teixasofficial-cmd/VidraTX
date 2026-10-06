CREATE TABLE servico (
    id BIGINT NOT NULL AUTO_INCREMENT,
    empresa_id BIGINT NOT NULL,
    nome VARCHAR(150) NOT NULL,
    descricao TEXT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em DATETIME NOT NULL,
    atualizado_em DATETIME NOT NULL,

    CONSTRAINT pk_servico PRIMARY KEY (id),

    CONSTRAINT fk_servico_empresa
        FOREIGN KEY (empresa_id)
            REFERENCES empresa(id),

    CONSTRAINT uk_servico_empresa_nome
        UNIQUE (empresa_id, nome)
);

CREATE INDEX idx_servico_empresa_ativo
    ON servico (empresa_id, ativo);
