ALTER TABLE whatsapp_instancia
    ADD COLUMN desconectado_em DATETIME(6) NULL,
    ADD COLUMN alerta_desconexao_em DATETIME(6) NULL;

UPDATE whatsapp_instancia
SET desconectado_em = atualizado_em
WHERE status <> 'CONECTADO'
  AND numero IS NOT NULL;
