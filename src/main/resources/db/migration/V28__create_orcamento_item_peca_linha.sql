CREATE TABLE orcamento_item (
    id BIGINT NOT NULL AUTO_INCREMENT,
    orcamento_id BIGINT NOT NULL,
    tipologia_id BIGINT NOT NULL,

    ambiente VARCHAR(100) NULL,

    largura_vao_mm INT NULL,
    altura_vao_mm INT NULL,
    largura_vao_2_mm INT NULL,
    altura_vao_2_mm INT NULL,
    medida_texto_original VARCHAR(100) NULL,
    medida_aproximada BOOLEAN NOT NULL DEFAULT FALSE,

    tipo_vidro VARCHAR(20) NULL,
    espessura_mm SMALLINT NULL,
    cor VARCHAR(20) NULL,
    acabamento VARCHAR(20) NULL,
    cor_ferragem VARCHAR(30) NULL,

    quantidade INT NOT NULL DEFAULT 1,
    observacoes TEXT NULL,
    ordem INT NOT NULL DEFAULT 0,

    criado_em DATETIME NOT NULL,
    atualizado_em DATETIME NOT NULL,

    CONSTRAINT pk_orcamento_item PRIMARY KEY (id),

    CONSTRAINT fk_orcamento_item_orcamento
        FOREIGN KEY (orcamento_id)
            REFERENCES orcamento(id),

    CONSTRAINT fk_orcamento_item_tipologia
        FOREIGN KEY (tipologia_id)
            REFERENCES tipologia(id)
);

CREATE INDEX idx_orcamento_item_orcamento
    ON orcamento_item (orcamento_id);

CREATE TABLE orcamento_peca (
    id BIGINT NOT NULL AUTO_INCREMENT,
    orcamento_item_id BIGINT NOT NULL,

    descricao VARCHAR(100) NOT NULL,
    largura_corte_mm INT NOT NULL,
    altura_corte_mm INT NOT NULL,
    quantidade INT NOT NULL DEFAULT 1,
    excede_tamanho_maximo BOOLEAN NOT NULL DEFAULT FALSE,

    criado_em DATETIME NOT NULL,

    CONSTRAINT pk_orcamento_peca PRIMARY KEY (id),

    CONSTRAINT fk_orcamento_peca_item
        FOREIGN KEY (orcamento_item_id)
            REFERENCES orcamento_item(id)
);

CREATE INDEX idx_orcamento_peca_item
    ON orcamento_peca (orcamento_item_id);

CREATE TABLE orcamento_linha (
    id BIGINT NOT NULL AUTO_INCREMENT,
    orcamento_item_id BIGINT NOT NULL,
    tabela_preco_id BIGINT NULL,

    tipo_componente VARCHAR(20) NOT NULL,
    descricao VARCHAR(200) NOT NULL,
    quantidade DECIMAL(10,3) NOT NULL DEFAULT 1,

    valor_unitario_centavos BIGINT NULL,
    valor_sugerido_centavos BIGINT NOT NULL,
    valor_final_centavos BIGINT NULL,

    origem_sugestao VARCHAR(20) NOT NULL,
    editado BOOLEAN NOT NULL DEFAULT FALSE,
    editado_por_id BIGINT NULL,
    editado_em DATETIME NULL,

    criado_em DATETIME NOT NULL,
    atualizado_em DATETIME NOT NULL,

    CONSTRAINT pk_orcamento_linha PRIMARY KEY (id),

    CONSTRAINT fk_orcamento_linha_item
        FOREIGN KEY (orcamento_item_id)
            REFERENCES orcamento_item(id),

    CONSTRAINT fk_orcamento_linha_tabela_preco
        FOREIGN KEY (tabela_preco_id)
            REFERENCES tabela_preco(id),

    CONSTRAINT fk_orcamento_linha_editor
        FOREIGN KEY (editado_por_id)
            REFERENCES usuario(id)
);

CREATE INDEX idx_orcamento_linha_item
    ON orcamento_linha (orcamento_item_id);
