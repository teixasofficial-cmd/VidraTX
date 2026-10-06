package br.com.vidratx.service;

import br.com.vidratx.config.FusoEmpresa;
import br.com.vidratx.entity.Cliente;
import br.com.vidratx.entity.Empresa;
import br.com.vidratx.entity.PerguntaPendente;
import br.com.vidratx.enums.StatusPergunta;
import br.com.vidratx.enums.TipoPergunta;
import br.com.vidratx.repository.PerguntaPendenteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Service
public class PerguntaPendenteService {

    private static final Set<TipoPergunta> PERGUNTAS_MEDICAO =
            Set.of(TipoPergunta.CONFIRMAR_MEDICAO, TipoPergunta.SUGERIR_DATA_MEDICAO);

    private static final Set<TipoPergunta> PERGUNTAS_INSTALACAO =
            Set.of(TipoPergunta.CONFIRMAR_INSTALACAO, TipoPergunta.SUGERIR_DATA_INSTALACAO);

    private static final Set<TipoPergunta> PERGUNTAS_ORCAMENTO =
            Set.of(TipoPergunta.APROVAR_ORCAMENTO);

    private final PerguntaPendenteRepository perguntaPendenteRepository;
    private final Clock clock;
    private final FusoEmpresa fuso;

    public PerguntaPendenteService(
            PerguntaPendenteRepository perguntaPendenteRepository,
            Clock clock) {

        this.perguntaPendenteRepository = perguntaPendenteRepository;
        this.clock = clock;
        this.fuso = new FusoEmpresa(clock);
    }

    public static Set<TipoPergunta> tiposDaMesmaReferencia(TipoPergunta tipo) {

        return switch (tipo.getReferencia()) {
            case MEDICAO -> PERGUNTAS_MEDICAO;
            case INSTALACAO -> PERGUNTAS_INSTALACAO;
            case ORCAMENTO -> PERGUNTAS_ORCAMENTO;
        };
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public PerguntaPendente abrir(
            Empresa empresa,
            String telefone,
            Cliente cliente,
            TipoPergunta tipo,
            Long referenciaId,
            int versao,
            String resumo,
            String textoPergunta,
            LocalDateTime expiraEm) {

        encerrarDaReferencia(tiposDaMesmaReferencia(tipo), referenciaId, StatusPergunta.SUBSTITUIDA);

        PerguntaPendente pergunta = new PerguntaPendente();

        pergunta.setEmpresa(empresa);
        pergunta.setTelefone(telefone);
        pergunta.setCliente(cliente);
        pergunta.setTipo(tipo);
        pergunta.setReferenciaId(referenciaId);
        pergunta.setVersao(versao);
        pergunta.setResumo(resumo.length() > 255 ? resumo.substring(0, 255) : resumo);
        pergunta.setTextoPergunta(textoPergunta);
        pergunta.setExpiraEm(expiraEm);
        pergunta.setCriadaEm(LocalDateTime.now(clock));

        return perguntaPendenteRepository.save(pergunta);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void encerrarDaMedicao(Long medicaoId, StatusPergunta status) {
        encerrarDaReferencia(PERGUNTAS_MEDICAO, medicaoId, status);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void encerrarDaInstalacao(Long instalacaoId, StatusPergunta status) {
        encerrarDaReferencia(PERGUNTAS_INSTALACAO, instalacaoId, status);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void encerrarDoOrcamento(Long orcamentoId, StatusPergunta status) {
        encerrarDaReferencia(PERGUNTAS_ORCAMENTO, orcamentoId, status);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void encerrarDaReferencia(Collection<TipoPergunta> tipos, Long referenciaId, StatusPergunta status) {

        for (PerguntaPendente ativa : perguntaPendenteRepository
                .findAllByTipoInAndReferenciaIdAndStatus(tipos, referenciaId, StatusPergunta.ATIVA)) {

            encerrar(ativa, status);
        }
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void encerrar(PerguntaPendente pergunta, StatusPergunta status) {

        if (!pergunta.ativa()) {
            return;
        }

        pergunta.setStatus(status);

        if (status == StatusPergunta.RESPONDIDA) {
            pergunta.setRespondidaEm(LocalDateTime.now(clock));
        }

        perguntaPendenteRepository.save(pergunta);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public List<PerguntaPendente> ativas(Long empresaId, String telefone) {

        List<PerguntaPendente> perguntas = perguntaPendenteRepository
                .findAllByEmpresaIdAndTelefoneAndStatusOrderByCriadaEmAscIdAsc(empresaId, telefone, StatusPergunta.ATIVA);

        return perguntas.stream()
                .filter(p -> {
                    if (p.getExpiraEm() != null && p.getExpiraEm().isBefore(fuso.agora(p.getEmpresa()))) {
                        encerrar(p, StatusPergunta.EXPIRADA);
                        return false;
                    }
                    return true;
                })
                .toList();
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void registrarTentativa(PerguntaPendente pergunta) {

        pergunta.setTentativas(pergunta.getTentativas() + 1);
        perguntaPendenteRepository.save(pergunta);
    }
}
