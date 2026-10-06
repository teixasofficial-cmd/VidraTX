package br.com.vidratx.repository;

import br.com.vidratx.entity.Instalacao;
import br.com.vidratx.enums.StatusAgendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface InstalacaoRepository
        extends JpaRepository<Instalacao, Long> {

    Optional<Instalacao> findByOrdemServicoId(
            Long ordemServicoId
    );

    @Query("select i from Instalacao i where i.ordemServico.empresa.id = :empresaId "
            + "and i.status = br.com.vidratx.enums.StatusAgendamento.AGENDADA "
            + "and i.id <> :ignorarId and lower(trim(i.equipeResponsavel)) = :equipe "
            + "and i.dataAgendada > :inicio and i.dataAgendada < :fim")
    List<Instalacao> findConfirmadasDaEquipeNaJanela(
            @Param("empresaId") Long empresaId,
            @Param("ignorarId") Long ignorarId,
            @Param("equipe") String equipe,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );

    List<Instalacao> findAllByOrdemServicoEmpresaIdAndStatusInOrderByDataAgendadaAsc(
            Long empresaId,
            Collection<StatusAgendamento> status
    );

    List<Instalacao> findAllByOrdemServicoEmpresaIdAndStatusAndDataAgendadaBetweenOrderByDataAgendadaAsc(
            Long empresaId,
            StatusAgendamento status,
            LocalDateTime de,
            LocalDateTime ate
    );

    List<Instalacao> findTop200ByStatusAndDataAgendadaBeforeOrderByDataAgendadaAsc(
            StatusAgendamento status,
            LocalDateTime limite
    );

    List<Instalacao> findAllByOrdemServicoOrcamentoClienteIdAndStatus(
            Long clienteId,
            StatusAgendamento status
    );

    List<Instalacao> findAllByOrdemServicoEmpresaIdAndStatusInAndDataAgendadaBetweenOrderByDataAgendadaAsc(
            Long empresaId,
            Collection<StatusAgendamento> status,
            LocalDateTime de,
            LocalDateTime ate
    );
}
