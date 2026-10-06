CREATE TABLE pagamento (
    id BIGINT NOT NULL AUTO_INCREMENT,
    orcamento_id BIGINT NOT NULL,
    valor DECIMAL(12,2) NOT NULL,
    forma_pagamento VARCHAR(20) NOT NULL,
    vencimento DATE,
    situacao VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    pago_em DATE,
    observacao VARCHAR(500),
    criado_em DATETIME NOT NULL,
    atualizado_em DATETIME NOT NULL,

    CONSTRAINT pk_pagamento PRIMARY KEY (id),

    CONSTRAINT fk_pagamento_orcamento
        FOREIGN KEY (orcamento_id)
            REFERENCES orcamento(id)
);

CREATE INDEX idx_pagamento_orcamento_situacao
    ON pagamento (orcamento_id, situacao);
