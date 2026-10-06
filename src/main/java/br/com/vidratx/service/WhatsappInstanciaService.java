package br.com.vidratx.service;

import br.com.vidratx.dto.WhatsappInstanciaResponse;
import br.com.vidratx.entity.Empresa;
import br.com.vidratx.entity.WhatsappInstancia;
import br.com.vidratx.enums.StatusInstanciaWhatsapp;
import br.com.vidratx.enums.StatusMensagemSaida;
import br.com.vidratx.exception.CredenciaisInvalidasException;
import br.com.vidratx.exception.EmpresaNaoEncontradaException;
import br.com.vidratx.mapper.WhatsappInstanciaMapper;
import br.com.vidratx.repository.EmpresaRepository;
import br.com.vidratx.repository.MensagemSaidaRepository;
import br.com.vidratx.repository.WhatsappInstanciaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class WhatsappInstanciaService {

    private final WhatsappInstanciaRepository whatsappInstanciaRepository;
    private final EmpresaRepository empresaRepository;
    private final MensagemSaidaRepository mensagemSaidaRepository;
    private final WhatsappInstanciaMapper whatsappInstanciaMapper;
    private final WhatsappGatewayClient whatsappGatewayClient;
    private final WhatsappSaidaDespachante despachante;
    private final TransactionTemplate transactionTemplate;
    private final Clock clock;

    public WhatsappInstanciaService(
            WhatsappInstanciaRepository whatsappInstanciaRepository,
            EmpresaRepository empresaRepository,
            MensagemSaidaRepository mensagemSaidaRepository,
            WhatsappInstanciaMapper whatsappInstanciaMapper,
            WhatsappGatewayClient whatsappGatewayClient,
            WhatsappSaidaDespachante despachante,
            PlatformTransactionManager transactionManager,
            Clock clock) {

        this.whatsappInstanciaRepository = whatsappInstanciaRepository;
        this.empresaRepository = empresaRepository;
        this.mensagemSaidaRepository = mensagemSaidaRepository;
        this.whatsappInstanciaMapper = whatsappInstanciaMapper;
        this.whatsappGatewayClient = whatsappGatewayClient;
        this.despachante = despachante;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.clock = clock;
    }

    @Transactional
    public WhatsappInstanciaResponse status(Long empresaId) {
        return montarResponse(buscarOuCriar(empresaId), null);
    }

    public WhatsappInstanciaResponse statusComQr(Long empresaId) {

        WhatsappInstancia instancia = transactionTemplate.execute(tx -> buscarOuCriar(empresaId));

        String qr = instancia.getStatus() != StatusInstanciaWhatsapp.CONECTADO
                ? whatsappGatewayClient.buscarQr(instancia.getWebhookToken())
                : null;

        return transactionTemplate.execute(tx -> montarResponse(
                whatsappInstanciaRepository.findById(instancia.getId()).orElse(instancia), qr));
    }

    public WhatsappInstanciaResponse conectar(Long empresaId) {

        WhatsappInstancia instancia = transactionTemplate.execute(tx -> {

            WhatsappInstancia atual = buscarOuCriar(empresaId);

            if (atual.getStatus() != StatusInstanciaWhatsapp.CONECTADO) {
                atual.alterarStatus(StatusInstanciaWhatsapp.CONECTANDO, LocalDateTime.now(clock));
                whatsappInstanciaRepository.save(atual);
            }

            return atual;
        });

        whatsappGatewayClient.iniciarSessao(instancia.getWebhookToken());

        return statusComQr(empresaId);
    }

    @Transactional(readOnly = true)
    public WhatsappInstancia buscarPorTokenOuFalhar(String webhookToken) {

        return whatsappInstanciaRepository
                .findByWebhookToken(webhookToken)
                .orElseThrow(() -> new CredenciaisInvalidasException("Token do webhook inválido"));
    }

    @Transactional
    public void atualizarStatusConexao(String webhookToken, StatusInstanciaWhatsapp status, String numero) {

        WhatsappInstancia instancia = buscarPorTokenOuFalhar(webhookToken);

        boolean reconectou = status == StatusInstanciaWhatsapp.CONECTADO
                && instancia.getStatus() != StatusInstanciaWhatsapp.CONECTADO;

        instancia.alterarStatus(status, LocalDateTime.now(clock));

        if (status == StatusInstanciaWhatsapp.CONECTADO) {

            instancia.setNumero(numero);

            if (reconectou || instancia.getConectadoEm() == null) {
                instancia.setConectadoEm(LocalDateTime.now(clock));
            }

        } else {

            instancia.setConectadoEm(null);
        }

        whatsappInstanciaRepository.save(instancia);

        if (status == StatusInstanciaWhatsapp.CONECTADO) {

            Long empresaId = instancia.getEmpresa().getId();

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

                @Override
                public void afterCommit() {
                    despachante.despacharPendentesDaEmpresa(empresaId);
                }
            });
        }
    }

    private WhatsappInstanciaResponse montarResponse(WhatsappInstancia instancia, String qr) {

        WhatsappInstanciaResponse response = whatsappInstanciaMapper.toResponse(instancia);
        Long empresaId = instancia.getEmpresa().getId();

        response.setQrCode(qr);
        response.setMensagensNaFila(mensagemSaidaRepository.countByEmpresaIdAndStatus(empresaId, StatusMensagemSaida.PENDENTE));
        response.setMensagensComFalha(mensagemSaidaRepository.countByEmpresaIdAndStatusAndCriadoEmAfter(
                empresaId, StatusMensagemSaida.FALHOU, LocalDateTime.now(clock).minusDays(7)));

        return response;
    }

    private WhatsappInstancia buscarOuCriar(Long empresaId) {

        return whatsappInstanciaRepository
                .findByEmpresaId(empresaId)
                .orElseGet(() -> criar(empresaId));
    }

    private WhatsappInstancia criar(Long empresaId) {

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new EmpresaNaoEncontradaException("Empresa não encontrada"));

        WhatsappInstancia instancia = new WhatsappInstancia();

        instancia.setEmpresa(empresa);
        instancia.setWebhookToken(gerarToken());
        instancia.setStatus(StatusInstanciaWhatsapp.DESCONECTADO);

        return whatsappInstanciaRepository.save(instancia);
    }

    private String gerarToken() {
        return UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
    }
}
