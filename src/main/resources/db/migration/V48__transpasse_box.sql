UPDATE tipologia
SET regras_json = JSON_SET(regras_json, '$.transpasseMm', 50),
    atualizado_em = NOW()
WHERE codigo IN ('box_frontal_2f', 'box_canto_4f')
  AND JSON_EXTRACT(regras_json, '$.transpasseMm') = 0;
