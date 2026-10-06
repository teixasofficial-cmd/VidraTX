CREATE TABLE usuario (
                         id BIGINT NOT NULL AUTO_INCREMENT,
                         empresa_id BIGINT NOT NULL,
                         nome VARCHAR(150) NOT NULL,
                         email VARCHAR(150) NOT NULL,
                         senha VARCHAR(255) NOT NULL,
                         perfil VARCHAR(30) NOT NULL,
                         ativo BOOLEAN NOT NULL DEFAULT TRUE,
                         criado_em DATETIME NOT NULL,
                         atualizado_em DATETIME NOT NULL,

                         CONSTRAINT pk_usuario PRIMARY KEY (id),

                         CONSTRAINT fk_usuario_empresa
                             FOREIGN KEY (empresa_id)
                                 REFERENCES empresa(id),

                         CONSTRAINT uk_usuario_empresa_email
                             UNIQUE (empresa_id, email)
);
