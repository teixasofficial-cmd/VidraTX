CREATE TABLE whatsapp_instancia (
    id BIGINT NOT NULL AUTO_INCREMENT,
    empresa_id BIGINT NOT NULL,
    numero VARCHAR(20),
    webhook_token VARCHAR(64) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DESCONECTADO',
    conectado_em DATETIME,
    criado_em DATETIME NOT NULL,
    atualizado_em DATETIME NOT NULL,

    CONSTRAINT pk_whatsapp_instancia PRIMARY KEY (id),

    CONSTRAINT fk_whatsapp_instancia_empresa
        FOREIGN KEY (empresa_id)
            REFERENCES empresa(id),

    CONSTRAINT uk_whatsapp_instancia_empresa
        UNIQUE (empresa_id),

    CONSTRAINT uk_whatsapp_instancia_token
        UNIQUE (webhook_token)
);
