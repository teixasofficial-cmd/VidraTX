ALTER TABLE empresa
    ADD COLUMN slug           VARCHAR(60)  NOT NULL,
    ADD COLUMN whatsapp       VARCHAR(20)  NULL,
    ADD COLUMN logo_url       VARCHAR(500) NULL,
    ADD COLUMN cor_primaria   VARCHAR(7)   NULL,
    ADD COLUMN cor_secundaria VARCHAR(7)   NULL,
    ADD COLUMN endereco       VARCHAR(255) NULL,
    ADD COLUMN sobre          TEXT         NULL;

ALTER TABLE empresa
    ADD CONSTRAINT uk_empresa_slug UNIQUE (slug);
