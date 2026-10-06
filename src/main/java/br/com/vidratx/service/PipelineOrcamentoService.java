package br.com.vidratx.service;

import br.com.vidratx.entity.Orcamento;
import br.com.vidratx.entity.Usuario;
import br.com.vidratx.enums.StatusOrcamento;
import br.com.vidratx.enums.TipoEventoHistorico;
import br.com.vidratx.repository.OrcamentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.Set;

@Service
public class PipelineOrcamentoService {

    public static final Set<StatusOrcamento> STATUS_EM_ELABORACAO = EnumSet.of(
            StatusOrcamento.NOVO_CONTATO, StatusOrcamento.PRE_ORCAMENTO,
            StatusOrcamento.VISITA_AGENDADA, StatusOrcamento.MEDIDO, StatusOrcamento.ORCAMENTO_FINAL
    );

    private final OrcamentoRepository orcamentoRepository;
    private final HistoricoService historicoService;

    public PipelineOrcamentoService(
            OrcamentoRepository orcamentoRepository,
            HistoricoService historicoService) {

        this.orcamentoRepository = orcamentoRepository;
        this.historicoService = historicoService;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public boolean moverSeEstiverEm(
            Orcamento orcamento,
            Set<StatusOrcamento> de,
            StatusOrcamento para,
            String motivo,
            Usuario responsavel) {

        if (!de.contains(orcamento.getStatus()) || orcamento.getStatus() == para) {
            return false;
        }

        StatusOrcamento anterior = orcamento.getStatus();

        orcamento.setStatus(para);
        orcamentoRepository.save(orcamento);

        historicoService.registrarEventoOrcamento(
                orcamento, TipoEventoHistorico.ORCAMENTO_MOVIDO_PIPELINE,
                "Orçamento movido de " + anterior + " para " + para + ": " + motivo,
                responsavel
        );

        return true;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void visitaConfirmada(Orcamento orcamento, Usuario responsavel) {

        moverSeEstiverEm(
                orcamento, EnumSet.of(StatusOrcamento.NOVO_CONTATO, StatusOrcamento.PRE_ORCAMENTO),
                StatusOrcamento.VISITA_AGENDADA, "visita de medição confirmada", responsavel
        );
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void visitaDesmarcada(Orcamento orcamento, Usuario responsavel, String motivo) {

        moverSeEstiverEm(
                orcamento, EnumSet.of(StatusOrcamento.VISITA_AGENDADA),
                StatusOrcamento.PRE_ORCAMENTO, motivo, responsavel
        );
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void medicaoRealizada(Orcamento orcamento, Usuario responsavel) {

        moverSeEstiverEm(
                orcamento,
                EnumSet.of(StatusOrcamento.NOVO_CONTATO, StatusOrcamento.PRE_ORCAMENTO, StatusOrcamento.VISITA_AGENDADA),
                StatusOrcamento.MEDIDO, "medição realizada", responsavel
        );
    }
}
