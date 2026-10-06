CREATE TABLE historico (
    id BIGINT NOT NULL AUTO_INCREMENT,
    empresa_id BIGINT NOT NULL,
    cliente_id BIGINT,
    atendimento_id BIGINT,
    orcamento_id BIGINT,
    instalacao_id BIGINT,
    usuario_id BIGINT,
    tipo VARCHAR(40) NOT NULL,
    descricao TEXT NOT NULL,
    criado_em DATETIME NOT NULL,

    CONSTRAINT pk_historico PRIMARY KEY (id),

    CONSTRAINT fk_historico_empresa
        FOREIGN KEY (empresa_id)
            REFERENCES empresa(id),

    CONSTRAINT fk_historico_cliente
        FOREIGN KEY (cliente_id)
            REFERENCES cliente(id),

    CONSTRAINT fk_historico_atendimento
        FOREIGN KEY (atendimento_id)
            REFERENCES atendimento_whatsapp(id),

    CONSTRAINT fk_historico_orcamento
        FOREIGN KEY (orcamento_id)
            REFERENCES orcamento(id),

    CONSTRAINT fk_historico_instalacao
        FOREIGN KEY (instalacao_id)
            REFERENCES instalacao(id),

    CONSTRAINT fk_historico_usuario
        FOREIGN KEY (usuario_id)
            REFERENCES usuario(id)
);

CREATE INDEX idx_historico_empresa_cliente
    ON historico (empresa_id, cliente_id);

CREATE INDEX idx_historico_atendimento
    ON historico (atendimento_id);

CREATE INDEX idx_historico_orcamento
    ON historico (orcamento_id);

CREATE INDEX idx_historico_instalacao
    ON historico (instalacao_id);
