ALTER TABLE empresa
    ADD COLUMN fuso_horario VARCHAR(40) NULL,
    ADD COLUMN antecedencia_minima_minutos INT NOT NULL DEFAULT 60,
    ADD COLUMN prazo_resposta_atendente_minutos INT NOT NULL DEFAULT 30;
