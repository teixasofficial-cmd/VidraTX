INSERT INTO tipologia (empresa_id, codigo, nome, categoria, regras_json, ativo, criado_em, atualizado_em)
SELECT id, 'box_frontal_2f', 'Box frontal (2 folhas)', 'BOX',
    '{"numeroFolhas":2,"formulaPecas":"DIVIDIR_LARGURA_IGUAL","descontoLarguraMm":12,"descontoAlturaMm":0,"transpasseMm":0,"alertasNormativos":["REQUER_VIDRO_SEGURANCA"]}',
    TRUE, NOW(), NOW()
FROM empresa;

INSERT INTO tipologia (empresa_id, codigo, nome, categoria, regras_json, ativo, criado_em, atualizado_em)
SELECT id, 'box_canto_4f', 'Box de canto (4 folhas)', 'BOX',
    '{"numeroFolhas":4,"formulaPecas":"DOIS_VAOS_DIVIDIDOS","descontoLarguraMm":12,"descontoAlturaMm":0,"transpasseMm":0,"alertasNormativos":["REQUER_VIDRO_SEGURANCA"]}',
    TRUE, NOW(), NOW()
FROM empresa;

INSERT INTO tipologia (empresa_id, codigo, nome, categoria, regras_json, ativo, criado_em, atualizado_em)
SELECT id, 'box_abrir_pivotante', 'Box de abrir/pivotante', 'BOX',
    '{"numeroFolhas":1,"formulaPecas":"PECA_UNICA","descontoLarguraMm":10,"descontoAlturaMm":10,"transpasseMm":0,"alertasNormativos":["REQUER_VIDRO_SEGURANCA"]}',
    TRUE, NOW(), NOW()
FROM empresa;

INSERT INTO tipologia (empresa_id, codigo, nome, categoria, regras_json, ativo, criado_em, atualizado_em)
SELECT id, 'espelho', 'Espelho', 'ESPELHO',
    '{"numeroFolhas":1,"formulaPecas":"PECA_UNICA","descontoLarguraMm":0,"descontoAlturaMm":0,"transpasseMm":0,"alertasNormativos":[]}',
    TRUE, NOW(), NOW()
FROM empresa;

INSERT INTO tipologia (empresa_id, codigo, nome, categoria, regras_json, ativo, criado_em, atualizado_em)
SELECT id, 'porta_pivotante', 'Porta de vidro temperado (pivotante)', 'PORTA',
    '{"numeroFolhas":1,"formulaPecas":"PECA_UNICA","descontoLarguraMm":10,"descontoAlturaMm":15,"transpasseMm":0,"alertasNormativos":["REQUER_VIDRO_SEGURANCA"]}',
    TRUE, NOW(), NOW()
FROM empresa;

INSERT INTO tipologia (empresa_id, codigo, nome, categoria, regras_json, ativo, criado_em, atualizado_em)
SELECT id, 'porta_correr', 'Porta de vidro temperado (de correr)', 'PORTA',
    '{"numeroFolhas":2,"formulaPecas":"DIVIDIR_LARGURA_IGUAL","descontoLarguraMm":12,"descontoAlturaMm":0,"transpasseMm":40,"alertasNormativos":["REQUER_VIDRO_SEGURANCA"]}',
    TRUE, NOW(), NOW()
FROM empresa;

INSERT INTO tipologia (empresa_id, codigo, nome, categoria, regras_json, ativo, criado_em, atualizado_em)
SELECT id, 'janela_2f', 'Janela de vidro temperado (2 folhas)', 'JANELA',
    '{"numeroFolhas":2,"formulaPecas":"DIVIDIR_LARGURA_IGUAL","descontoLarguraMm":10,"descontoAlturaMm":0,"transpasseMm":30,"alertasNormativos":["REQUER_VIDRO_SEGURANCA"]}',
    TRUE, NOW(), NOW()
FROM empresa;

INSERT INTO tipologia (empresa_id, codigo, nome, categoria, regras_json, ativo, criado_em, atualizado_em)
SELECT id, 'janela_4f', 'Janela de vidro temperado (4 folhas)', 'JANELA',
    '{"numeroFolhas":4,"formulaPecas":"DIVIDIR_LARGURA_IGUAL","descontoLarguraMm":10,"descontoAlturaMm":0,"transpasseMm":30,"alertasNormativos":["REQUER_VIDRO_SEGURANCA"]}',
    TRUE, NOW(), NOW()
FROM empresa;

INSERT INTO tipologia (empresa_id, codigo, nome, categoria, regras_json, ativo, criado_em, atualizado_em)
SELECT id, 'vidro_avulso', 'Troca de vidro / vidro avulso', 'VIDRO_AVULSO',
    '{"numeroFolhas":1,"formulaPecas":"PECA_UNICA","descontoLarguraMm":0,"descontoAlturaMm":0,"transpasseMm":0,"alertasNormativos":[]}',
    TRUE, NOW(), NOW()
FROM empresa;

INSERT INTO tipologia (empresa_id, codigo, nome, categoria, regras_json, ativo, criado_em, atualizado_em)
SELECT id, 'guarda_corpo', 'Guarda-corpo', 'GUARDA_CORPO',
    '{"numeroFolhas":1,"formulaPecas":"PECA_UNICA","descontoLarguraMm":0,"descontoAlturaMm":0,"transpasseMm":0,"alertasNormativos":["REQUER_LAMINADO"]}',
    TRUE, NOW(), NOW()
FROM empresa;

INSERT INTO tipologia (empresa_id, codigo, nome, categoria, regras_json, ativo, criado_em, atualizado_em)
SELECT id, 'envidracamento_sacada', 'Envidraçamento de sacada', 'ENVIDRACAMENTO_SACADA',
    '{"numeroFolhas":1,"formulaPecas":"PECA_UNICA","descontoLarguraMm":0,"descontoAlturaMm":0,"transpasseMm":0,"alertasNormativos":["REQUER_VIDRO_SEGURANCA"]}',
    TRUE, NOW(), NOW()
FROM empresa;

INSERT INTO tipologia (empresa_id, codigo, nome, categoria, regras_json, ativo, criado_em, atualizado_em)
SELECT id, 'tampo_prateleira', 'Tampo de mesa e prateleira', 'TAMPO_PRATELEIRA',
    '{"numeroFolhas":1,"formulaPecas":"PECA_UNICA","descontoLarguraMm":0,"descontoAlturaMm":0,"transpasseMm":0,"alertasNormativos":[]}',
    TRUE, NOW(), NOW()
FROM empresa;

INSERT INTO tipologia (empresa_id, codigo, nome, categoria, regras_json, ativo, criado_em, atualizado_em)
SELECT id, 'item_livre', 'Item livre', 'ITEM_LIVRE',
    '{"numeroFolhas":0,"formulaPecas":"SEM_PECAS","descontoLarguraMm":0,"descontoAlturaMm":0,"transpasseMm":0,"alertasNormativos":[]}',
    TRUE, NOW(), NOW()
FROM empresa;
