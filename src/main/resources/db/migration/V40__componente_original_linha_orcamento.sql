ALTER TABLE orcamento_linha
    ADD COLUMN componente_descricao VARCHAR(200) NULL,
    ADD COLUMN componente_quantidade DECIMAL(10,3) NULL;

UPDATE orcamento_linha
SET componente_descricao = descricao,
    componente_quantidade = quantidade
WHERE tipo_componente NOT IN ('VIDRO', 'PERDAS');
