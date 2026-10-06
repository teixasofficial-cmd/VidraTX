CREATE TABLE tipologia (
    id BIGINT NOT NULL AUTO_INCREMENT,
    empresa_id BIGINT NOT NULL,

    codigo VARCHAR(40) NOT NULL,
    nome VARCHAR(100) NOT NULL,
    categoria VARCHAR(30) NOT NULL,

    regras_json JSON NOT NULL,

    ativo BOOLEAN NOT NULL DEFAULT TRUE,

    criado_em DATETIME NOT NULL,
    atualizado_em DATETIME NOT NULL,

    CONSTRAINT pk_tipologia PRIMARY KEY (id),

    CONSTRAINT fk_tipologia_empresa
        FOREIGN KEY (empresa_id)
            REFERENCES empresa(id),

    CONSTRAINT uk_tipologia_empresa_codigo
        UNIQUE (empresa_id, codigo)
);
