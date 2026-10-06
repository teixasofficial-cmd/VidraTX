CREATE TABLE administrador_global (
    id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(150) NOT NULL,
    senha VARCHAR(255) NOT NULL,
    criado_em DATETIME NOT NULL,
    atualizado_em DATETIME NOT NULL,

    CONSTRAINT pk_administrador_global PRIMARY KEY (id),

    CONSTRAINT uk_administrador_global_email UNIQUE (email)
);

INSERT INTO administrador_global (email, senha, criado_em, atualizado_em)
VALUES (
    'guiteixeiras2018@hotmail.com',
    '$2a$12$Mg1KF8O6y4CUwPOULTJ59uMZOckiz9IqQVk646GTjw7beouk/Sm6K',
    NOW(),
    NOW()
);
