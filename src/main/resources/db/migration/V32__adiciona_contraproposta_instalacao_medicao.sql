ALTER TABLE instalacao
    MODIFY COLUMN status VARCHAR(30) NOT NULL,
    ADD COLUMN contraproposta_texto TEXT NULL;

ALTER TABLE medicao
    MODIFY COLUMN status VARCHAR(30) NOT NULL,
    ADD COLUMN contraproposta_texto TEXT NULL;
