CREATE TABLE atendimento_whatsapp (
    id BIGINT NOT NULL AUTO_INCREMENT,
    empresa_id BIGINT NOT NULL,
    cliente_id BIGINT,
    solicitacao_orcamento_id BIGINT,
    atendente_id BIGINT,
    telefone VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'EM_FLUXO_BOT',
    etapa_fluxo VARCHAR(30),
    tentativas_erro INT NOT NULL DEFAULT 0,
    dados_coletados TEXT,
    criado_em DATETIME NOT NULL,
    atualizado_em DATETIME NOT NULL,
    encerrado_em DATETIME,

    CONSTRAINT pk_atendimento_whatsapp PRIMARY KEY (id),

    CONSTRAINT fk_atendimento_whatsapp_empresa
        FOREIGN KEY (empresa_id)
            REFERENCES empresa(id),

    CONSTRAINT fk_atendimento_whatsapp_cliente
        FOREIGN KEY (cliente_id)
            REFERENCES cliente(id),

    CONSTRAINT fk_atendimento_whatsapp_solicitacao
        FOREIGN KEY (solicitacao_orcamento_id)
            REFERENCES solicitacao_orcamento(id),

    CONSTRAINT fk_atendimento_whatsapp_atendente
        FOREIGN KEY (atendente_id)
            REFERENCES usuario(id)
);

CREATE INDEX idx_atendimento_whatsapp_empresa_status
    ON atendimento_whatsapp (empresa_id, status);

CREATE INDEX idx_atendimento_whatsapp_empresa_telefone
    ON atendimento_whatsapp (empresa_id, telefone);
