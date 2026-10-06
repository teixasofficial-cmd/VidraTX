CREATE TABLE orcamento (
    id BIGINT NOT NULL AUTO_INCREMENT,
    empresa_id BIGINT NOT NULL,
    cliente_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    observacoes TEXT,
    valido_ate DATE,
    valor_total DECIMAL(12,2) NOT NULL DEFAULT 0,
    criado_em DATETIME NOT NULL,
    atualizado_em DATETIME NOT NULL,
    enviado_em DATETIME,
    respondido_em DATETIME,

    CONSTRAINT pk_orcamento PRIMARY KEY (id),

    CONSTRAINT fk_orcamento_empresa
        FOREIGN KEY (empresa_id)
            REFERENCES empresa(id),

    CONSTRAINT fk_orcamento_cliente
        FOREIGN KEY (cliente_id)
            REFERENCES cliente(id)
);

CREATE INDEX idx_orcamento_empresa_status
    ON orcamento (empresa_id, status);

CREATE INDEX idx_orcamento_cliente
    ON orcamento (cliente_id);

CREATE TABLE orcamento_item (
    id BIGINT NOT NULL AUTO_INCREMENT,
    orcamento_id BIGINT NOT NULL,
    servico_id BIGINT NOT NULL,
    descricao TEXT,
    largura DECIMAL(8,3),
    altura DECIMAL(8,3),
    observacoes_medicao TEXT,
    beneficiamentos TEXT,
    quantidade INT NOT NULL DEFAULT 1,
    custo_material DECIMAL(12,2) NOT NULL DEFAULT 0,
    custo_mao_de_obra DECIMAL(12,2) NOT NULL DEFAULT 0,
    custo_instalacao DECIMAL(12,2) NOT NULL DEFAULT 0,
    deslocamento DECIMAL(12,2) NOT NULL DEFAULT 0,
    outros_custos DECIMAL(12,2) NOT NULL DEFAULT 0,
    total_custos DECIMAL(12,2) NOT NULL DEFAULT 0,
    margem_percentual DECIMAL(5,2) NOT NULL DEFAULT 0,
    preco_venda DECIMAL(12,2) NOT NULL DEFAULT 0,
    desconto_valor DECIMAL(12,2) NOT NULL DEFAULT 0,
    valor_final DECIMAL(12,2) NOT NULL DEFAULT 0,
    criado_em DATETIME NOT NULL,
    atualizado_em DATETIME NOT NULL,

    CONSTRAINT pk_orcamento_item PRIMARY KEY (id),

    CONSTRAINT fk_orcamento_item_orcamento
        FOREIGN KEY (orcamento_id)
            REFERENCES orcamento(id),

    CONSTRAINT fk_orcamento_item_servico
        FOREIGN KEY (servico_id)
            REFERENCES servico(id)
);

CREATE INDEX idx_orcamento_item_orcamento
    ON orcamento_item (orcamento_id);
