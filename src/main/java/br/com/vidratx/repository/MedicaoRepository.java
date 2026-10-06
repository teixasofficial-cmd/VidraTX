package br.com.vidratx.repository;

import br.com.vidratx.enums.StatusOrcamento;
import br.com.vidratx.entity.Medicao;
import br.com.vidratx.enums.StatusAgendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MedicaoRepository
        extends JpaRepository<Medicao, Long> {

    Optional<Medicao> findByOrcamentoId(
            Long orcamentoId
    );

    List<Medicao> findAllByOrcamentoIdIn(
            Collection<Long> orcamentoIds
    );

    @Query("select m from Medicao m where m.orcamento.empresa.id = :empresaId "
            + "and m.status = br.com.vidratx.enums.StatusAgendamento.AGENDADA "
            + "and m.id <> :ignorarId and m.dataAgendada > :inicio and m.dataAgendada < :fim")
    List<Medicao> findConfirmadasNaJanela(
            @Param("empresaId") Long empresaId,
            @Param("ignorarId") Long ignorarId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );

    List<Medicao> findAllByOrcamentoEmpresaIdAndStatusInOrderByDataAgendadaAsc(
            Long empresaId,
            Collection<StatusAgendamento> status
    );

    List<Medicao> findAllByOrcamentoEmpresaIdAndStatusAndDataAgendadaBetweenOrderByDataAgendadaAsc(
            Long empresaId,
            StatusAgendamento status,
            LocalDateTime de,
            LocalDateTime ate
    );

    List<Medicao> findTop200ByStatusAndDataAgendadaBeforeOrderByDataAgendadaAsc(
            StatusAgendamento status,
            LocalDateTime limite
    );

    List<Medicao> findAllByOrcamentoClienteIdAndStatus(
            Long clienteId,
            StatusAgendamento status
    );

    List<Medicao> findAllByOrcamentoEmpresaIdAndStatusInAndDataAgendadaBetweenOrderByDataAgendadaAsc(
            Long empresaId,
            Collection<StatusAgendamento> status,
            LocalDateTime de,
            LocalDateTime ate
    );

    List<Medicao> findAllByOrcamentoEmpresaIdAndStatusAndOrcamentoStatusIn(
            Long empresaId,
            StatusAgendamento status,
            Collection<StatusOrcamento> statusOrcamento
    );
}
