UPDATE cliente
SET whatsapp = CONCAT('55', whatsapp)
WHERE whatsapp REGEXP '^[0-9]{10,11}$';

UPDATE cliente
SET telefone = CONCAT('55', telefone)
WHERE telefone REGEXP '^[0-9]{10,11}$';

UPDATE cliente
SET whatsapp = CONCAT(SUBSTRING(whatsapp, 1, 4), '9', SUBSTRING(whatsapp, 5))
WHERE whatsapp REGEXP '^55[0-9]{2}[6-9][0-9]{7}$';

UPDATE atendimento_whatsapp
SET telefone = CONCAT(SUBSTRING(telefone, 1, 4), '9', SUBSTRING(telefone, 5))
WHERE telefone REGEXP '^55[0-9]{2}[6-9][0-9]{7}$';
