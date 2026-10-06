CREATE INDEX idx_usuario_empresa_ativo
    ON usuario (empresa_id, ativo);

CREATE INDEX idx_empresa_ativa
    ON empresa (ativa);
