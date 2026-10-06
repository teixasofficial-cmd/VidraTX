CREATE TABLE cliente (
                         id BIGINT NOT NULL AUTO_INCREMENT,
                         empresa_id BIGINT NOT NULL,
                         nome VARCHAR(150) NOT NULL,
                         telefone VARCHAR(20),
                         whatsapp VARCHAR(20),
                         email VARCHAR(150),
                         cpf_cnpj VARCHAR(18),
                         endereco VARCHAR(255),
                         observacoes TEXT,
                         criado_em DATETIME NOT NULL,
                         atualizado_em DATETIME NOT NULL,

                         CONSTRAINT pk_cliente PRIMARY KEY (id),

                         CONSTRAINT fk_cliente_empresa
                             FOREIGN KEY (empresa_id)
                                 REFERENCES empresa(id)
);
