CREATE TABLE tabela_preco (
    id BIGINT NOT NULL AUTO_INCREMENT,
    empresa_id BIGINT NOT NULL,

    categoria VARCHAR(20) NOT NULL,
    descricao VARCHAR(200) NOT NULL,

    tipo_vidro VARCHAR(20) NULL,
    espessura_mm SMALLINT NULL,
    cor VARCHAR(20) NULL,
    acabamento VARCHAR(20) NULL,

    unidade VARCHAR(10) NOT NULL,

    custo_centavos BIGINT NULL,
    preco_venda_centavos BIGINT NOT NULL,

    fornecedor VARCHAR(150) NULL,

    origem VARCHAR(20) NOT NULL DEFAULT 'MANUAL',
    formula_origem VARCHAR(255) NULL,

    ativo BOOLEAN NOT NULL DEFAULT TRUE,

    criado_em DATETIME NOT NULL,
    atualizado_em DATETIME NOT NULL,

    CONSTRAINT pk_tabela_preco PRIMARY KEY (id),

    CONSTRAINT fk_tabela_preco_empresa
        FOREIGN KEY (empresa_id)
            REFERENCES empresa(id)
);

CREATE INDEX idx_tabela_preco_empresa_categoria
    ON tabela_preco (empresa_id, categoria);

CREATE INDEX idx_tabela_preco_empresa_ativo
    ON tabela_preco (empresa_id, ativo);
