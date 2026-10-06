package br.com.vidratx.service;

import br.com.vidratx.entity.AtendimentoWhatsapp;
import br.com.vidratx.entity.Cliente;
import br.com.vidratx.entity.Empresa;
import br.com.vidratx.entity.MensagemAtendimento;
import br.com.vidratx.entity.MensagemRecebida;
import br.com.vidratx.entity.Usuario;
import br.com.vidratx.enums.RemetenteMensagem;
import br.com.vidratx.enums.StatusAtendimento;
import br.com.vidratx.enums.TipoEventoHistorico;
import br.com.vidratx.enums.TipoMensagem;
import br.com.vidratx.enums.TipoMensagemRecebida;
import br.com.vidratx.repository.AtendimentoWhatsappRepository;
import br.com.vidratx.repository.ClienteRepository;
import br.com.vidratx.repository.MensagemAtendimentoRepository;
import br.com.vidratx.util.TelefoneUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ConversaWhatsappService {

    public static final String MOTIVO_INATIVIDADE = "INATIVIDADE";
    public static final String MOTIVO_PROCESSO_CONCLUIDO = "PROCESSO_CONCLUIDO";
    public static final String MOTIVO_MANUAL = "MANUAL";

    private final AtendimentoWhatsappRepository atendimentoWhatsappRepository;
    private final MensagemAtendimentoRepository mensagemAtendimentoRepository;
    private final ClienteRepository clienteRepository;
    private final HistoricoService historicoService;
    private final Clock clock;
    private final long horasInatividade;
    private final long horasInatividadeHumano;

    public ConversaWhatsappService(
            AtendimentoWhatsappRepository atendimentoWhatsappRepository,
            MensagemAtendimentoRepository mensagemAtendimentoRepository,
            ClienteRepository clienteRepository,
            HistoricoService historicoService,
            Clock clock,
            @Value("${vidratx.whatsapp.conversa.inatividade-horas:24}") long horasInatividade,
            @Value("${vidratx.whatsapp.conversa.inatividade-humano-horas:72}") long horasInatividadeHumano) {

        this.atendimentoWhatsappRepository = atendimentoWhatsappRepository;
        this.mensagemAtendimentoRepository = mensagemAtendimentoRepository;
        this.clienteRepository = clienteRepository;
        this.historicoService = historicoService;
        this.clock = clock;
        this.horasInatividade = horasInatividade;
        this.horasInatividadeHumano = horasInatividadeHumano;
    }

    public record ConversaAberta(AtendimentoWhatsapp atendimento, boolean nova) {
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public ConversaAberta obterConversaAberta(Empresa empresa, String telefone, Cliente clienteConhecido) {

        AtendimentoWhatsapp atendimento = atendimentoWhatsappRepository
                .findFirstByEmpresaIdAndTelefoneAndStatusNotOrderByCriadoEmDesc(
                        empresa.getId(), telefone, StatusAtendimento.ENCERRADO
                )
                .orElse(null);

        if (atendimento != null && estaInativa(atendimento)) {

            encerrar(atendimento, MOTIVO_INATIVIDADE, null,
                    "Conversa encerrada automaticamente por inatividade");

            atendimento = null;
        }

        if (atendimento != null) {

            if (atendimento.getCliente() == null) {
                atendimento.setCliente(clienteConhecido != null
                        ? clienteConhecido
                        : buscarClientePorTelefone(empresa.getId(), telefone).orElse(null));
            }

            return new ConversaAberta(atendimento, false);
        }

        AtendimentoWhatsapp nova = new AtendimentoWhatsapp();

        nova.setEmpresa(empresa);
        nova.setTelefone(telefone);
        nova.setCliente(clienteConhecido != null
                ? clienteConhecido
                : buscarClientePorTelefone(empresa.getId(), telefone).orElse(null));

        return new ConversaAberta(atendimentoWhatsappRepository.save(nova), true);
    }

    public Optional<Cliente> buscarClientePorTelefone(Long empresaId, String telefone) {

        List<Cliente> clientes = clienteRepository.findAllByEmpresaIdAndWhatsappInOrderByIdAsc(
                empresaId, TelefoneUtils.variantes(telefone)
        );

        return clientes.stream().findFirst();
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public MensagemAtendimento registrarMensagemCliente(AtendimentoWhatsapp atendimento, MensagemRecebida recebida) {

        MensagemAtendimento mensagem = new MensagemAtendimento();

        mensagem.setAtendimento(atendimento);
        mensagem.setRemetente(RemetenteMensagem.CLIENTE);
        mensagem.setWhatsappMensagemId(recebida.getWhatsappMensagemId());

        if (recebida.getTipo() == TipoMensagemRecebida.IMAGEM) {

            mensagem.setTipo(TipoMensagem.IMAGEM);
            mensagem.setMidiaUrl(recebida.getMidiaUrl());
            mensagem.setMidiaContentType(recebida.getMidiaContentType());
            mensagem.setConteudo(recebida.getConteudo());

        } else {

            mensagem.setTipo(TipoMensagem.TEXTO);
            mensagem.setConteudo(recebida.getConteudo());
        }

        if (recebida.getEnviadaEm() != null) {
            mensagem.setEnviadoEm(recebida.getEnviadaEm());
        }

        if (!recebida.ehReacao()) {

            LocalDateTime agora = LocalDateTime.now(clock);

            if (!atendimento.clienteAguardandoResposta()) {
                atendimento.setClienteAguardandoDesde(agora);
            }

            atendimento.setUltimaMensagemClienteEm(agora);
            atendimentoWhatsappRepository.save(atendimento);
        }

        return mensagemAtendimentoRepository.save(mensagem);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void escalar(AtendimentoWhatsapp atendimento, String descricao) {

        if (atendimento.getStatus() != StatusAtendimento.EM_ATENDIMENTO_HUMANO) {

            atendimento.setStatus(StatusAtendimento.AGUARDANDO_ATENDENTE);
            atendimento.setTentativasErro(0);
            atendimentoWhatsappRepository.save(atendimento);
        }

        historicoService.registrarEventoAtendimento(
                atendimento, TipoEventoHistorico.ATENDIMENTO_ESCALADO_ATENDENTE, descricao, null
        );
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void encerrar(AtendimentoWhatsapp atendimento, String motivo, Usuario responsavel, String descricao) {

        atendimento.setStatus(StatusAtendimento.ENCERRADO);
        atendimento.setEncerradoEm(LocalDateTime.now(clock));
        atendimento.setMotivoEncerramento(motivo);

        atendimentoWhatsappRepository.save(atendimento);

        historicoService.registrarEventoAtendimento(
                atendimento, TipoEventoHistorico.ATENDIMENTO_ENCERRADO, descricao, responsavel
        );
    }

    private boolean estaInativa(AtendimentoWhatsapp atendimento) {

        return (atendimento.getStatus() == StatusAtendimento.EM_FLUXO_BOT
                && atendimento.getAtualizadoEm() != null
                && atendimento.getAtualizadoEm().isBefore(LocalDateTime.now(clock).minusHours(horasInatividade)))
                || humanaInativa(atendimento);
    }

    public boolean humanaInativa(AtendimentoWhatsapp atendimento) {

        return atendimento.comPessoa()
                && !atendimento.clienteAguardandoResposta()
                && atendimento.getAtualizadoEm() != null
                && atendimento.getAtualizadoEm().isBefore(LocalDateTime.now(clock).minusHours(horasInatividadeHumano));
    }

    public long getHorasInatividade() {
        return horasInatividade;
    }

    public long getHorasInatividadeHumano() {
        return horasInatividadeHumano;
    }
}
