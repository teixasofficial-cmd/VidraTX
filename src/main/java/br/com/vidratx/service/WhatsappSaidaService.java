package br.com.vidratx.service;

import br.com.vidratx.entity.AtendimentoWhatsapp;
import br.com.vidratx.entity.Cliente;
import br.com.vidratx.entity.Empresa;
import br.com.vidratx.entity.MensagemAtendimento;
import br.com.vidratx.entity.MensagemSaida;
import br.com.vidratx.entity.PerguntaPendente;
import br.com.vidratx.enums.CategoriaMensagemSaida;
import br.com.vidratx.enums.RemetenteMensagem;
import br.com.vidratx.enums.StatusMensagemSaida;
import br.com.vidratx.enums.TipoMensagem;
import br.com.vidratx.repository.AtendimentoWhatsappRepository;
import br.com.vidratx.repository.MensagemAtendimentoRepository;
import br.com.vidratx.repository.MensagemSaidaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class WhatsappSaidaService {

    private final MensagemSaidaRepository mensagemSaidaRepository;
    private final MensagemAtendimentoRepository mensagemAtendimentoRepository;
    private final AtendimentoWhatsappRepository atendimentoWhatsappRepository;
    private final WhatsappContatoService whatsappContatoService;
    private final ConversaWhatsappService conversaWhatsappService;
    private final WhatsappSaidaDespachante despachante;
    private final Clock clock;

    public WhatsappSaidaService(
            MensagemSaidaRepository mensagemSaidaRepository,
            MensagemAtendimentoRepository mensagemAtendimentoRepository,
            AtendimentoWhatsappRepository atendimentoWhatsappRepository,
            WhatsappContatoService whatsappContatoService,
            ConversaWhatsappService conversaWhatsappService,
            WhatsappSaidaDespachante despachante,
            Clock clock) {

        this.mensagemSaidaRepository = mensagemSaidaRepository;
        this.mensagemAtendimentoRepository = mensagemAtendimentoRepository;
        this.atendimentoWhatsappRepository = atendimentoWhatsappRepository;
        this.whatsappContatoService = whatsappContatoService;
        this.conversaWhatsappService = conversaWhatsappService;
        this.despachante = despachante;
        this.clock = clock;
    }

    public record NovaMensagem(
            Empresa empresa,
            String telefone,
            Cliente cliente,
            String conteudo,
            CategoriaMensagemSaida categoria,
            RemetenteMensagem remetente,
            String referenciaTipo,
            Long referenciaId,
            PerguntaPendente pergunta,
            AtendimentoWhatsapp atendimento) {

        public static NovaMensagem doBot(AtendimentoWhatsapp atendimento, String conteudo) {
            return new NovaMensagem(
                    atendimento.getEmpresa(), atendimento.getTelefone(), atendimento.getCliente(),
                    conteudo, CategoriaMensagemSaida.BOT, RemetenteMensagem.BOT,
                    null, null, null, atendimento
            );
        }

        public NovaMensagem comPergunta(PerguntaPendente novaPergunta) {
            return new NovaMensagem(empresa, telefone, cliente, conteudo, categoria, remetente,
                    referenciaTipo, referenciaId, novaPergunta, atendimento);
        }

        public NovaMensagem comCategoria(CategoriaMensagemSaida novaCategoria) {
            return new NovaMensagem(empresa, telefone, cliente, conteudo, novaCategoria, remetente,
                    referenciaTipo, referenciaId, pergunta, atendimento);
        }
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public MensagemSaida enfileirar(NovaMensagem nova) {

        if (nova.conteudo() == null || nova.conteudo().isBlank()) {
            throw new IllegalArgumentException("Mensagem vazia não pode ser enviada");
        }

        whatsappContatoService.travar(nova.empresa(), nova.telefone());

        AtendimentoWhatsapp atendimento = nova.atendimento() != null
                ? nova.atendimento()
                : conversaWhatsappService
                        .obterConversaAberta(nova.empresa(), nova.telefone(), nova.cliente())
                        .atendimento();

        MensagemSaida saida = new MensagemSaida();

        saida.setEmpresa(nova.empresa());
        saida.setAtendimento(atendimento);
        saida.setTelefone(nova.telefone());
        saida.setConteudo(nova.conteudo());
        saida.setCategoria(nova.categoria());
        saida.setReferenciaTipo(nova.referenciaTipo());
        saida.setReferenciaId(nova.referenciaId());
        saida.setPergunta(nova.pergunta());
        saida.setStatus(StatusMensagemSaida.PENDENTE);

        MensagemSaida salva = mensagemSaidaRepository.save(saida);

        MensagemAtendimento registro = new MensagemAtendimento();

        registro.setAtendimento(atendimento);
        registro.setRemetente(nova.remetente() != null ? nova.remetente() : RemetenteMensagem.BOT);
        registro.setTipo(TipoMensagem.TEXTO);
        registro.setConteudo(nova.conteudo());
        registro.setMensagemSaida(salva);

        mensagemAtendimentoRepository.save(registro);

        if (nova.categoria().respondeAoCliente()) {
            atendimento.setUltimaMensagemEmpresaEm(LocalDateTime.now(clock));
            atendimentoWhatsappRepository.save(atendimento);
        }

        despachante.despacharAposCommit(salva.getId());

        return salva;
    }

    @Transactional(readOnly = true)
    public Optional<MensagemSaida> ultimaDaReferencia(String referenciaTipo, Long referenciaId) {
        return mensagemSaidaRepository.findFirstByReferenciaTipoAndReferenciaIdOrderByIdDesc(referenciaTipo, referenciaId);
    }
}
