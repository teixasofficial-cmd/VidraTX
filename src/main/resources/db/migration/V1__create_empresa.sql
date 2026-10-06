CREATE TABLE empresa (
                         id BIGINT NOT NULL AUTO_INCREMENT,
                         razao_social VARCHAR(150) NOT NULL,
                         nome_fantasia VARCHAR(150) NOT NULL,
                         cnpj VARCHAR(14) NOT NULL,
                         email VARCHAR(150),
                         telefone VARCHAR(20),
                         ativa BOOLEAN NOT NULL DEFAULT TRUE,
                         criado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         atualizado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                         CONSTRAINT pk_empresa PRIMARY KEY (id),
                         CONSTRAINT uk_empresa_cnpj UNIQUE (cnpj)
);
