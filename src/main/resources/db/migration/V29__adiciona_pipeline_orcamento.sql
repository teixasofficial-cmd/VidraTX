ALTER TABLE orcamento
    ADD COLUMN motivo_perda VARCHAR(30) NULL,
    ADD COLUMN motivo_perda_outro VARCHAR(255) NULL,
    ADD COLUMN custo_total_centavos BIGINT NULL,
    ADD COLUMN preco_sugerido_centavos BIGINT NULL,
    ADD COLUMN valor_total_centavos BIGINT NULL,
    ADD COLUMN ajuste_comercial_centavos BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN margem_real_percentual DECIMAL(5,2) NULL;

UPDATE orcamento SET status = 'ORCAMENTO_FINAL' WHERE status = 'RASCUNHO';

UPDATE orcamento
    SET status = 'PERDIDO', motivo_perda = 'OUTRO'
    WHERE status = 'RECUSADO';
