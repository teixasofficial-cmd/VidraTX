CREATE TABLE material (
    id BIGINT NOT NULL AUTO_INCREMENT,
    empresa_id BIGINT NOT NULL,
    nome VARCHAR(150) NOT NULL,
    descricao TEXT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em DATETIME NOT NULL,
    atualizado_em DATETIME NOT NULL,

    CONSTRAINT pk_material PRIMARY KEY (id),

    CONSTRAINT fk_material_empresa
        FOREIGN KEY (empresa_id)
            REFERENCES empresa(id),

    CONSTRAINT uk_material_empresa_nome
        UNIQUE (empresa_id, nome)
);

CREATE INDEX idx_material_empresa_ativo
    ON material (empresa_id, ativo);

CREATE TABLE material_necessario (
    id BIGINT NOT NULL AUTO_INCREMENT,
    orcamento_id BIGINT NOT NULL,
    material_id BIGINT NOT NULL,
    quantidade DECIMAL(10,3) NOT NULL,
    unidade VARCHAR(20),
    observacao VARCHAR(500),
    criado_em DATETIME NOT NULL,
    atualizado_em DATETIME NOT NULL,

    CONSTRAINT pk_material_necessario PRIMARY KEY (id),

    CONSTRAINT fk_material_necessario_orcamento
        FOREIGN KEY (orcamento_id)
            REFERENCES orcamento(id),

    CONSTRAINT fk_material_necessario_material
        FOREIGN KEY (material_id)
            REFERENCES material(id)
);

CREATE INDEX idx_material_necessario_orcamento
    ON material_necessario (orcamento_id);
