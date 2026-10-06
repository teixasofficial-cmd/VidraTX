CREATE INDEX idx_cliente_empresa_nome
    ON cliente (empresa_id, nome);

ALTER TABLE cliente
    ADD CONSTRAINT uk_cliente_empresa_documento
        UNIQUE (empresa_id, cpf_cnpj);
