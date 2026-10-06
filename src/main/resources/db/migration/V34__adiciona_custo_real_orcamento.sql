ALTER TABLE orcamento
    ADD COLUMN custo_real_total_centavos BIGINT NULL,
    ADD COLUMN margem_sobre_custo_real_percentual DECIMAL(5,2) NULL,
    ADD COLUMN custo_real_incompleto TINYINT(1) NOT NULL DEFAULT 0;
