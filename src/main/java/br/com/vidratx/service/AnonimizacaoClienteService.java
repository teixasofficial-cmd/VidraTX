package br.com.vidratx.service;

import br.com.vidratx.entity.Cliente;
import br.com.vidratx.exception.ClienteNaoEncontradoException;
import br.com.vidratx.repository.ClienteRepository;
import br.com.vidratx.util.TelefoneUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class AnonimizacaoClienteService {

    static final String NOME_ANONIMO = "Cliente removido (LGPD)";
    static final String TEXTO_REMOVIDO = "[removido a pedido do cliente]";

    @PersistenceContext
    private EntityManager em;

    private final ClienteRepository clienteRepository;
    private final WhatsappMidiaStorage midiaStorage;

    public AnonimizacaoClienteService(ClienteRepository clienteRepository, WhatsappMidiaStorage midiaStorage) {
        this.clienteRepository = clienteRepository;
        this.midiaStorage = midiaStorage;
    }

    @Transactional
    public void anonimizar(Long empresaId, Long clienteId) {

        Cliente cliente = clienteRepository.findByIdAndEmpresaId(clienteId, empresaId)
                .orElseThrow(() -> new ClienteNaoEncontradoException("Cliente não encontrado"));

        String nomeOriginal = cliente.getNome();
        Set<String> telefones = new LinkedHashSet<>();

        for (String bruto : new String[]{cliente.getTelefone(), cliente.getWhatsapp()}) {
            String normalizado = TelefoneUtils.normalizar(bruto);
            if (normalizado != null) {
                telefones.add(normalizado);
            }
        }

        telefones.addAll(lista("select telefone from atendimento_whatsapp where empresa_id = ?1 and cliente_id = ?2",
                empresaId, clienteId));

        List<String> midias = new ArrayList<>();

        if (!telefones.isEmpty()) {

            midias.addAll(lista("select m.midia_url from mensagem_atendimento m join atendimento_whatsapp a"
                    + " on a.id = m.atendimento_id where a.empresa_id = ?1 and a.telefone in ?2"
                    + " and m.midia_url is not null", empresaId, telefones));
            midias.addAll(lista("select midia_url from mensagem_recebida where empresa_id = ?1 and telefone in ?2"
                    + " and midia_url is not null", empresaId, telefones));

            executar("update mensagem_atendimento m join atendimento_whatsapp a on a.id = m.atendimento_id"
                    + " set m.conteudo = ?3, m.midia_url = null where a.empresa_id = ?1 and a.telefone in ?2",
                    empresaId, telefones, TEXTO_REMOVIDO);
            executar("update mensagem_recebida set conteudo = ?3, midia_url = null, telefone = concat('anon-', id)"
                    + " where empresa_id = ?1 and telefone in ?2", empresaId, telefones, TEXTO_REMOVIDO);
            executar("update mensagem_saida set conteudo = ?3, telefone = concat('anon-', id)"
                    + " where empresa_id = ?1 and telefone in ?2", empresaId, telefones, TEXTO_REMOVIDO);
            executar("update pergunta_pendente set texto_pergunta = ?3, telefone = concat('anon-', id)"
                    + " where empresa_id = ?1 and telefone in ?2", empresaId, telefones, TEXTO_REMOVIDO);
            executar("update whatsapp_contato set telefone = concat('anon-', id)"
                    + " where empresa_id = ?1 and telefone in ?2", empresaId, telefones);
            executar("update atendimento_whatsapp set telefone = concat('anon-', id)"
                    + " where empresa_id = ?1 and telefone in ?2", empresaId, telefones);
        }

        executar("update solicitacao_orcamento set descricao = ?3 where empresa_id = ?1 and cliente_id = ?2",
                empresaId, clienteId, TEXTO_REMOVIDO);
        executar("update orcamento set alteracao_solicitada_texto = null where empresa_id = ?1 and cliente_id = ?2",
                empresaId, clienteId);
        executar("update medicao m join orcamento o on o.id = m.orcamento_id set m.endereco = null,"
                + " m.observacoes = null, m.contraproposta_texto = null, m.cancelamento_solicitado_texto = null"
                + " where o.empresa_id = ?1 and o.cliente_id = ?2", empresaId, clienteId);
        executar("update instalacao i join ordem_servico s on s.id = i.ordem_servico_id"
                + " join orcamento o on o.id = s.orcamento_id set i.endereco = null, i.observacoes = null,"
                + " i.contraproposta_texto = null, i.cancelamento_solicitado_texto = null"
                + " where o.empresa_id = ?1 and o.cliente_id = ?2", empresaId, clienteId);
        executar("update proposta_agendamento p join medicao m on m.id = p.medicao_id"
                + " join orcamento o on o.id = m.orcamento_id set p.texto_cliente = null"
                + " where o.empresa_id = ?1 and o.cliente_id = ?2", empresaId, clienteId);
        executar("update proposta_agendamento p join instalacao i on i.id = p.instalacao_id"
                + " join ordem_servico s on s.id = i.ordem_servico_id join orcamento o on o.id = s.orcamento_id"
                + " set p.texto_cliente = null where o.empresa_id = ?1 and o.cliente_id = ?2", empresaId, clienteId);

        if (nomeOriginal != null && !nomeOriginal.isBlank()) {
            executar("update historico set descricao = replace(descricao, ?3, ?4) where empresa_id = ?1"
                    + " and (cliente_id = ?2 or orcamento_id in (select id from orcamento where cliente_id = ?2))",
                    empresaId, clienteId, nomeOriginal, NOME_ANONIMO);
        }

        cliente.setNome(NOME_ANONIMO);
        cliente.setTelefone(null);
        cliente.setWhatsapp(null);
        cliente.setEmail(null);
        cliente.setCpfCnpj(null);
        cliente.setEndereco(null);
        cliente.setObservacoes(null);

        clienteRepository.save(cliente);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                midias.stream().filter(Objects::nonNull).distinct().forEach(midiaStorage::excluir);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private List<String> lista(String sql, Object... parametros) {

        var query = em.createNativeQuery(sql);

        for (int i = 0; i < parametros.length; i++) {
            query.setParameter(i + 1, parametros[i]);
        }

        return ((List<Object>) query.getResultList()).stream().map(String::valueOf).toList();
    }

    private void executar(String sql, Object... parametros) {

        var query = em.createNativeQuery(sql);

        for (int i = 0; i < parametros.length; i++) {
            query.setParameter(i + 1, parametros[i]);
        }

        query.executeUpdate();
    }
}
