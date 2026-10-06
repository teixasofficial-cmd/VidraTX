CREATE TABLE mensagem_atendimento (
    id BIGINT NOT NULL AUTO_INCREMENT,
    atendimento_id BIGINT NOT NULL,
    remetente VARCHAR(20) NOT NULL,
    conteudo TEXT NOT NULL,
    enviado_em DATETIME NOT NULL,

    CONSTRAINT pk_mensagem_atendimento PRIMARY KEY (id),

    CONSTRAINT fk_mensagem_atendimento_atendimento
        FOREIGN KEY (atendimento_id)
            REFERENCES atendimento_whatsapp(id)
);

CREATE INDEX idx_mensagem_atendimento_atendimento
    ON mensagem_atendimento (atendimento_id);
