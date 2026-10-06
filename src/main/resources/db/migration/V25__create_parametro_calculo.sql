CREATE TABLE parametro_calculo (
    id BIGINT NOT NULL AUTO_INCREMENT,
    empresa_id BIGINT NOT NULL,

    modo_precificacao VARCHAR(10) NOT NULL DEFAULT 'CUSTO',

    multiplo_arredondamento_mm SMALLINT NOT NULL DEFAULT 50,
    area_minima_m2 DECIMAL(6,4) NOT NULL DEFAULT 0.2500,
    percentual_perdas DECIMAL(5,2) NOT NULL DEFAULT 3.00,

    percentual_impostos DECIMAL(5,2) NOT NULL DEFAULT 6.00,
    percentual_comissao DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    percentual_margem_desejada DECIMAL(5,2) NOT NULL DEFAULT 25.00,
    percentual_margem_minima DECIMAL(5,2) NOT NULL DEFAULT 15.00,
    taxa_cartao_parcelas JSON NULL,

    arredondamento_comercial VARCHAR(20) NOT NULL DEFAULT 'NENHUM',

    variacao_pre_orcamento_min_pct DECIMAL(5,2) NOT NULL DEFAULT -10.00,
    variacao_pre_orcamento_max_pct DECIMAL(5,2) NOT NULL DEFAULT 15.00,

    valor_visita_tecnica_centavos BIGINT NULL,
    regra_deslocamento VARCHAR(20) NOT NULL DEFAULT 'FIXO',
    valor_deslocamento_centavos BIGINT NULL,

    validade_padrao_dias INT NOT NULL DEFAULT 7,

    tamanho_maximo_chapa_largura_mm INT NOT NULL DEFAULT 3210,
    tamanho_maximo_chapa_altura_mm INT NOT NULL DEFAULT 2250,

    tolerancia_prumo_nivel_mm SMALLINT NOT NULL DEFAULT 5,

    regime_tributario VARCHAR(20) NULL,

    criado_em DATETIME NOT NULL,
    atualizado_em DATETIME NOT NULL,

    CONSTRAINT pk_parametro_calculo PRIMARY KEY (id),

    CONSTRAINT fk_parametro_calculo_empresa
        FOREIGN KEY (empresa_id)
            REFERENCES empresa(id),

    CONSTRAINT uk_parametro_calculo_empresa
        UNIQUE (empresa_id)
);
