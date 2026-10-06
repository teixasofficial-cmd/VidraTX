CREATE TABLE proposta_agendamento (
    id BIGINT NOT NULL AUTO_INCREMENT,
    empresa_id BIGINT NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    medicao_id BIGINT NULL,
    instalacao_id BIGINT NULL,
    versao INT NOT NULL,
    origem VARCHAR(20) NOT NULL,
    data_proposta DATETIME NULL,
    texto_cliente TEXT NULL,
    equipe VARCHAR(150) NULL,
    status VARCHAR(20) NOT NULL,
    motivo TEXT NULL,
    criado_por_id BIGINT NULL,
    respondido_pelo_cliente BOOLEAN NOT NULL DEFAULT FALSE,
    respondido_por_id BIGINT NULL,
    mensagem_saida_id BIGINT NULL,
    criado_em DATETIME NOT NULL,
    respondido_em DATETIME NULL,

    CONSTRAINT pk_proposta_agendamento PRIMARY KEY (id),
    CONSTRAINT fk_proposta_agendamento_empresa FOREIGN KEY (empresa_id) REFERENCES empresa(id),
    CONSTRAINT fk_proposta_agendamento_medicao FOREIGN KEY (medicao_id) REFERENCES medicao(id),
    CONSTRAINT fk_proposta_agendamento_instalacao FOREIGN KEY (instalacao_id) REFERENCES instalacao(id),
    CONSTRAINT fk_proposta_agendamento_criado_por FOREIGN KEY (criado_por_id) REFERENCES usuario(id),
    CONSTRAINT fk_proposta_agendamento_respondido_por FOREIGN KEY (respondido_por_id) REFERENCES usuario(id),
    CONSTRAINT fk_proposta_agendamento_saida FOREIGN KEY (mensagem_saida_id) REFERENCES mensagem_saida(id)
);

CREATE INDEX idx_proposta_agendamento_medicao ON proposta_agendamento (medicao_id, versao);
CREATE INDEX idx_proposta_agendamento_instalacao ON proposta_agendamento (instalacao_id, versao);

ALTER TABLE medicao ADD COLUMN versao_proposta INT NOT NULL DEFAULT 0;
ALTER TABLE instalacao ADD COLUMN versao_proposta INT NOT NULL DEFAULT 0;

UPDATE medicao SET versao_proposta = 1;
UPDATE instalacao SET versao_proposta = 1;

INSERT INTO proposta_agendamento
    (empresa_id, tipo, medicao_id, versao, origem, data_proposta, status, respondido_pelo_cliente, criado_em, respondido_em)
SELECT o.empresa_id, 'MEDICAO', m.id, 1, 'EMPRESA', m.data_agendada,
       CASE m.status
           WHEN 'PROPOSTA_ENVIADA' THEN 'PENDENTE'
           WHEN 'AGENDADA' THEN 'ACEITA'
           WHEN 'REALIZADA' THEN 'ACEITA'
           WHEN 'CANCELADA' THEN 'CANCELADA'
           ELSE 'RECUSADA'
       END,
       m.status IN ('AGENDADA', 'REALIZADA', 'RECUSADA_CLIENTE', 'CONTRAPROPOSTA_CLIENTE'),
       m.criado_em,
       CASE WHEN m.status = 'PROPOSTA_ENVIADA' THEN NULL ELSE m.atualizado_em END
FROM medicao m
JOIN orcamento o ON o.id = m.orcamento_id;

INSERT INTO proposta_agendamento
    (empresa_id, tipo, medicao_id, versao, origem, texto_cliente, status, criado_em)
SELECT o.empresa_id, 'MEDICAO', m.id, 1, 'CLIENTE', m.contraproposta_texto, 'PENDENTE', m.atualizado_em
FROM medicao m
JOIN orcamento o ON o.id = m.orcamento_id
WHERE m.status = 'CONTRAPROPOSTA_CLIENTE';

INSERT INTO proposta_agendamento
    (empresa_id, tipo, instalacao_id, versao, origem, data_proposta, equipe, status, respondido_pelo_cliente, criado_em, respondido_em)
SELECT os.empresa_id, 'INSTALACAO', i.id, 1, 'EMPRESA', i.data_agendada, i.equipe_responsavel,
       CASE i.status
           WHEN 'PROPOSTA_ENVIADA' THEN 'PENDENTE'
           WHEN 'AGENDADA' THEN 'ACEITA'
           WHEN 'REALIZADA' THEN 'ACEITA'
           WHEN 'CANCELADA' THEN 'CANCELADA'
           ELSE 'RECUSADA'
       END,
       i.status IN ('AGENDADA', 'REALIZADA', 'RECUSADA_CLIENTE', 'CONTRAPROPOSTA_CLIENTE'),
       i.criado_em,
       CASE WHEN i.status = 'PROPOSTA_ENVIADA' THEN NULL ELSE i.atualizado_em END
FROM instalacao i
JOIN ordem_servico os ON os.id = i.ordem_servico_id;

INSERT INTO proposta_agendamento
    (empresa_id, tipo, instalacao_id, versao, origem, texto_cliente, status, criado_em)
SELECT os.empresa_id, 'INSTALACAO', i.id, 1, 'CLIENTE', i.contraproposta_texto, 'PENDENTE', i.atualizado_em
FROM instalacao i
JOIN ordem_servico os ON os.id = i.ordem_servico_id
WHERE i.status = 'CONTRAPROPOSTA_CLIENTE';

INSERT INTO pergunta_pendente
    (empresa_id, telefone, cliente_id, tipo, referencia_id, versao, status, resumo, texto_pergunta, criada_em, atualizado_em)
SELECT o.empresa_id, c.whatsapp, c.id,
       CASE WHEN m.status = 'PROPOSTA_ENVIADA' THEN 'CONFIRMAR_MEDICAO' ELSE 'SUGERIR_DATA_MEDICAO' END,
       m.id, 1, 'ATIVA',
       CONCAT('Visita de medição de ', DATE_FORMAT(m.data_agendada, '%d/%m/%Y às %H:%i')),
       CONCAT('Podemos fazer a visita de medição em ', DATE_FORMAT(m.data_agendada, '%d/%m/%Y às %H:%i'), '?'),
       m.atualizado_em, m.atualizado_em
FROM medicao m
JOIN orcamento o ON o.id = m.orcamento_id
JOIN cliente c ON c.id = o.cliente_id
WHERE m.status IN ('PROPOSTA_ENVIADA', 'RECUSADA_CLIENTE', 'CONTRAPROPOSTA_CLIENTE')
  AND c.whatsapp IS NOT NULL;

INSERT INTO pergunta_pendente
    (empresa_id, telefone, cliente_id, tipo, referencia_id, versao, status, resumo, texto_pergunta, criada_em, atualizado_em)
SELECT os.empresa_id, c.whatsapp, c.id,
       CASE WHEN i.status = 'PROPOSTA_ENVIADA' THEN 'CONFIRMAR_INSTALACAO' ELSE 'SUGERIR_DATA_INSTALACAO' END,
       i.id, 1, 'ATIVA',
       CONCAT('Instalação de ', DATE_FORMAT(i.data_agendada, '%d/%m/%Y às %H:%i')),
       CONCAT('Podemos fazer a instalação em ', DATE_FORMAT(i.data_agendada, '%d/%m/%Y às %H:%i'), '?'),
       i.atualizado_em, i.atualizado_em
FROM instalacao i
JOIN ordem_servico os ON os.id = i.ordem_servico_id
JOIN orcamento o ON o.id = os.orcamento_id
JOIN cliente c ON c.id = o.cliente_id
WHERE i.status IN ('PROPOSTA_ENVIADA', 'RECUSADA_CLIENTE', 'CONTRAPROPOSTA_CLIENTE')
  AND c.whatsapp IS NOT NULL;

INSERT INTO pergunta_pendente
    (empresa_id, telefone, cliente_id, tipo, referencia_id, versao, status, resumo, texto_pergunta, criada_em, atualizado_em)
SELECT o.empresa_id, c.whatsapp, c.id, 'APROVAR_ORCAMENTO', o.id, 1, 'ATIVA',
       CONCAT('Orçamento nº ', o.id, ' (R$ ', REPLACE(FORMAT(o.valor_total, 2), ',', '#'), ')'),
       CONCAT('Você aprova o orçamento nº ', o.id, '?'),
       COALESCE(o.enviado_em, o.atualizado_em), COALESCE(o.enviado_em, o.atualizado_em)
FROM orcamento o
JOIN cliente c ON c.id = o.cliente_id
WHERE o.status = 'ENVIADO'
  AND c.whatsapp IS NOT NULL;

UPDATE pergunta_pendente
SET resumo = REPLACE(REPLACE(REPLACE(resumo, '.', ','), '#', '.'), ',', ',')
WHERE tipo = 'APROVAR_ORCAMENTO';
