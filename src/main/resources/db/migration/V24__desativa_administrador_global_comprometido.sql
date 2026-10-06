ALTER TABLE administrador_global
    ADD COLUMN ativo BOOLEAN NOT NULL DEFAULT TRUE;

UPDATE administrador_global
SET ativo = FALSE,
    senha = '$2a$12$EpRMkh/ipLp4UKvUob1uee5fvmiAGg/bAgRIe2wfVdqYrUepqh7VS',
    atualizado_em = NOW()
WHERE email = 'guiteixeiras2018@hotmail.com';

INSERT INTO administrador_global (email, senha, ativo, criado_em, atualizado_em)
VALUES (
    'admin@vidratx.local',
    '$2a$12$TQPVY9XJ/8r7dAOpg2WYjOJNIZ3lzGwCYw4whvXoMWZJs7sWpRLJe',
    TRUE,
    NOW(),
    NOW()
);
