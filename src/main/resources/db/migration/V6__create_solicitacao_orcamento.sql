CREATE TABLE solicitacao_orcamento (
                                       id BIGINT NOT NULL AUTO_INCREMENT,

                                       empresa_id BIGINT NOT NULL,

                                       cliente_id BIGINT NOT NULL,

                                       canal VARCHAR(20) NOT NULL,

                                       status VARCHAR(30) NOT NULL,

                                       descricao TEXT,

                                       criado_em DATETIME NOT NULL,

                                       atualizado_em DATETIME NOT NULL,

                                       CONSTRAINT pk_solicitacao_orcamento
                                           PRIMARY KEY (id),

                                       CONSTRAINT fk_solicitacao_orcamento_empresa
                                           FOREIGN KEY (empresa_id)
                                               REFERENCES empresa(id),

                                       CONSTRAINT fk_solicitacao_orcamento_cliente
                                           FOREIGN KEY (cliente_id)
                                               REFERENCES cliente(id)
);

CREATE INDEX idx_solicitacao_empresa
    ON solicitacao_orcamento (empresa_id);

CREATE INDEX idx_solicitacao_empresa_status
    ON solicitacao_orcamento (empresa_id, status);

CREATE INDEX idx_solicitacao_cliente
    ON solicitacao_orcamento (cliente_id);
