package br.com.vidratx.service;

import br.com.vidratx.entity.MensagemRecebida;
import br.com.vidratx.entity.WhatsappInstancia;
import br.com.vidratx.enums.StatusMensagemRecebida;
import br.com.vidratx.enums.TipoMensagemRecebida;
import br.com.vidratx.repository.MensagemRecebidaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class WhatsappInboxService {

    private final MensagemRecebidaRepository mensagemRecebidaRepository;
    private final WhatsappContatoService whatsappContatoService;

    public WhatsappInboxService(
            MensagemRecebidaRepository mensagemRecebidaRepository,
            WhatsappContatoService whatsappContatoService) {

        this.mensagemRecebidaRepository = mensagemRecebidaRepository;
        this.whatsappContatoService = whatsappContatoService;
    }

    public record DadosRecebidos(
            String telefone,
            String whatsappMensagemId,
            LocalDateTime enviadaEm,
            TipoMensagemRecebida tipo,
            String conteudo,
            String midiaUrl,
            String midiaContentType,
            String reacaoA,
            String editaMensagemId,
            String apagaMensagemId) {

        public static DadosRecebidos simples(
                String telefone, String whatsappMensagemId, LocalDateTime enviadaEm, TipoMensagemRecebida tipo,
                String conteudo, String midiaUrl, String midiaContentType) {

            return new DadosRecebidos(telefone, whatsappMensagemId, enviadaEm, tipo, conteudo, midiaUrl,
                    midiaContentType, null, null, null);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<Long> registrar(WhatsappInstancia instancia, DadosRecebidos dados) {

        Long empresaId = instancia.getEmpresa().getId();

        if (dados.whatsappMensagemId() != null
                && mensagemRecebidaRepository
                        .findByEmpresaIdAndWhatsappMensagemId(empresaId, dados.whatsappMensagemId())
                        .isPresent()) {

            return Optional.empty();
        }

        whatsappContatoService.garantirExistente(empresaId, dados.telefone());

        MensagemRecebida mensagem = new MensagemRecebida();

        mensagem.setEmpresa(instancia.getEmpresa());
        mensagem.setTelefone(dados.telefone());
        mensagem.setWhatsappMensagemId(dados.whatsappMensagemId());
        mensagem.setEnviadaEm(dados.enviadaEm());
        mensagem.setTipo(dados.tipo());
        mensagem.setConteudo(dados.conteudo());
        mensagem.setMidiaUrl(dados.midiaUrl());
        mensagem.setMidiaContentType(dados.midiaContentType());
        mensagem.setReacaoAMensagemId(dados.reacaoA());
        mensagem.setEditaMensagemId(dados.editaMensagemId());
        mensagem.setApagaMensagemId(dados.apagaMensagemId());
        mensagem.setStatus(StatusMensagemRecebida.RECEBIDA);

        return Optional.of(mensagemRecebidaRepository.saveAndFlush(mensagem).getId());
    }

    public static final int MAX_TENTATIVAS_PROCESSAMENTO = 5;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean registrarFalha(Long mensagemId, String erro, boolean definitivo) {

        MensagemRecebida mensagem = mensagemRecebidaRepository.findById(mensagemId).orElse(null);

        if (mensagem == null || mensagem.getStatus() != StatusMensagemRecebida.RECEBIDA) {
            return false;
        }

        mensagem.setTentativas(mensagem.getTentativas() + 1);
        mensagem.setErro(erro != null && erro.length() > 2000 ? erro.substring(0, 2000) : erro);

        boolean encerrada = definitivo || mensagem.getTentativas() >= MAX_TENTATIVAS_PROCESSAMENTO;

        if (encerrada) {
            mensagem.setStatus(StatusMensagemRecebida.ERRO);
        }

        mensagemRecebidaRepository.save(mensagem);

        return encerrada;
    }
}
