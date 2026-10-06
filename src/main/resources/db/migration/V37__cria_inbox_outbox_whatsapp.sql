CREATE TABLE whatsapp_contato (
    id BIGINT NOT NULL AUTO_INCREMENT,
    empresa_id BIGINT NOT NULL,
    telefone VARCHAR(20) NOT NULL,
    pergunta_foco_id BIGINT NULL,
    escolha_pendente BOOLEAN NOT NULL DEFAULT FALSE,
    tentativas_escolha INT NOT NULL DEFAULT 0,
    criado_em DATETIME NOT NULL,
    atualizado_em DATETIME NOT NULL,

    CONSTRAINT pk_whatsapp_contato PRIMARY KEY (id),
    CONSTRAINT uk_whatsapp_contato_empresa_telefone UNIQUE (empresa_id, telefone),
    CONSTRAINT fk_whatsapp_contato_empresa FOREIGN KEY (empresa_id) REFERENCES empresa(id)
);

CREATE TABLE mensagem_recebida (
    id BIGINT NOT NULL AUTO_INCREMENT,
    empresa_id BIGINT NOT NULL,
    telefone VARCHAR(20) NOT NULL,
    whatsapp_mensagem_id VARCHAR(128) NULL,
    enviada_em DATETIME NULL,
    recebida_em DATETIME NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    conteudo TEXT NULL,
    midia_url VARCHAR(500) NULL,
    midia_content_type VARCHAR(100) NULL,
    status VARCHAR(20) NOT NULL,
    tentativas INT NOT NULL DEFAULT 0,
    erro TEXT NULL,
    processada_em DATETIME NULL,

    CONSTRAINT pk_mensagem_recebida PRIMARY KEY (id),
    CONSTRAINT uk_mensagem_recebida_whatsapp UNIQUE (empresa_id, whatsapp_mensagem_id),
    CONSTRAINT fk_mensagem_recebida_empresa FOREIGN KEY (empresa_id) REFERENCES empresa(id)
);

CREATE INDEX idx_mensagem_recebida_status ON mensagem_recebida (status, recebida_em);

CREATE TABLE pergunta_pendente (
    id BIGINT NOT NULL AUTO_INCREMENT,
    empresa_id BIGINT NOT NULL,
    telefone VARCHAR(20) NOT NULL,
    cliente_id BIGINT NULL,
    tipo VARCHAR(40) NOT NULL,
    referencia_id BIGINT NOT NULL,
    versao INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    resumo VARCHAR(255) NOT NULL,
    texto_pergunta TEXT NOT NULL,
    tentativas INT NOT NULL DEFAULT 0,
    criada_em DATETIME NOT NULL,
    respondida_em DATETIME NULL,
    expira_em DATETIME NULL,
    atualizado_em DATETIME NOT NULL,

    CONSTRAINT pk_pergunta_pendente PRIMARY KEY (id),
    CONSTRAINT fk_pergunta_pendente_empresa FOREIGN KEY (empresa_id) REFERENCES empresa(id),
    CONSTRAINT fk_pergunta_pendente_cliente FOREIGN KEY (cliente_id) REFERENCES cliente(id)
);

CREATE INDEX idx_pergunta_pendente_contato ON pergunta_pendente (empresa_id, telefone, status);
CREATE INDEX idx_pergunta_pendente_referencia ON pergunta_pendente (tipo, referencia_id, status);

CREATE TABLE mensagem_saida (
    id BIGINT NOT NULL AUTO_INCREMENT,
    empresa_id BIGINT NOT NULL,
    atendimento_id BIGINT NULL,
    telefone VARCHAR(20) NOT NULL,
    conteudo TEXT NOT NULL,
    categoria VARCHAR(30) NOT NULL,
    referencia_tipo VARCHAR(30) NULL,
    referencia_id BIGINT NULL,
    pergunta_id BIGINT NULL,
    status VARCHAR(20) NOT NULL,
    tentativas INT NOT NULL DEFAULT 0,
    proxima_tentativa_em DATETIME NULL,
    ultimo_erro TEXT NULL,
    whatsapp_mensagem_id VARCHAR(128) NULL,
    enviada_em DATETIME NULL,
    entregue_em DATETIME NULL,
    lida_em DATETIME NULL,
    criado_em DATETIME NOT NULL,
    atualizado_em DATETIME NOT NULL,

    CONSTRAINT pk_mensagem_saida PRIMARY KEY (id),
    CONSTRAINT fk_mensagem_saida_empresa FOREIGN KEY (empresa_id) REFERENCES empresa(id),
    CONSTRAINT fk_mensagem_saida_atendimento FOREIGN KEY (atendimento_id) REFERENCES atendimento_whatsapp(id),
    CONSTRAINT fk_mensagem_saida_pergunta FOREIGN KEY (pergunta_id) REFERENCES pergunta_pendente(id)
);

CREATE INDEX idx_mensagem_saida_fila ON mensagem_saida (status, proxima_tentativa_em);
CREATE INDEX idx_mensagem_saida_whatsapp ON mensagem_saida (empresa_id, whatsapp_mensagem_id);
CREATE INDEX idx_mensagem_saida_referencia ON mensagem_saida (referencia_tipo, referencia_id);

ALTER TABLE mensagem_atendimento
    ADD COLUMN whatsapp_mensagem_id VARCHAR(128) NULL,
    ADD COLUMN mensagem_saida_id BIGINT NULL,
    ADD CONSTRAINT fk_mensagem_atendimento_saida FOREIGN KEY (mensagem_saida_id) REFERENCES mensagem_saida(id);

ALTER TABLE atendimento_whatsapp
    ADD COLUMN versao BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN ultima_mensagem_cliente_em DATETIME NULL,
    ADD COLUMN ultima_mensagem_empresa_em DATETIME NULL,
    ADD COLUMN motivo_encerramento VARCHAR(40) NULL;

UPDATE atendimento_whatsapp a
SET a.ultima_mensagem_cliente_em = (
        SELECT MAX(m.enviado_em) FROM mensagem_atendimento m
        WHERE m.atendimento_id = a.id AND m.remetente = 'CLIENTE'
    ),
    a.ultima_mensagem_empresa_em = (
        SELECT MAX(m.enviado_em) FROM mensagem_atendimento m
        WHERE m.atendimento_id = a.id AND m.remetente = 'ATENDENTE'
    );
