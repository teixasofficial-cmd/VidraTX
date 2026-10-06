ALTER TABLE empresa
    ADD COLUMN duracao_medicao_minutos INT NOT NULL DEFAULT 60,
    ADD COLUMN duracao_instalacao_minutos INT NOT NULL DEFAULT 240,
    ADD COLUMN medicoes_simultaneas INT NOT NULL DEFAULT 1,
    ADD COLUMN hora_inicio_mensagens INT NOT NULL DEFAULT 8,
    ADD COLUMN hora_fim_mensagens INT NOT NULL DEFAULT 20;

ALTER TABLE orcamento
    ADD COLUMN preco_final_manual_centavos BIGINT NULL,
    ADD COLUMN parcelas_cartao INT NULL,
    ADD COLUMN revisao_envio INT NOT NULL DEFAULT 0,
    ADD COLUMN alteracao_solicitada_em DATETIME NULL,
    ADD COLUMN alteracao_solicitada_texto TEXT NULL,
    ADD COLUMN estimativa_enviada_em DATETIME NULL;

UPDATE orcamento
SET preco_final_manual_centavos = valor_total_centavos
WHERE ajuste_comercial_centavos <> 0
  AND valor_total_centavos IS NOT NULL;

UPDATE orcamento
SET revisao_envio = 1
WHERE enviado_em IS NOT NULL;
