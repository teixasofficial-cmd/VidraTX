package br.com.vidratx.repository;

import br.com.vidratx.entity.Orcamento;
import br.com.vidratx.enums.StatusOrcamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OrcamentoRepository
        extends JpaRepository<Orcamento, Long> {

    long countByEmpresaIdAndStatus(
            Long empresaId,
            StatusOrcamento status
    );

    long countByEmpresaIdAndStatusIn(
            Long empresaId,
            Collection<StatusOrcamento> status
    );

    @Query(
            "select coalesce(sum(o.valorTotal), 0) "
                    + "from Orcamento o "
                    + "where o.empresa.id = :empresaId "
                    + "and o.status = :status"
    )
    BigDecimal somarValorPorEmpresaEStatus(
            @Param("empresaId") Long empresaId,
            @Param("status") StatusOrcamento status
    );

    Optional<Orcamento> findByIdAndEmpresaId(
            Long id,
            Long empresaId
    );

    List<Orcamento> findAllByEmpresaIdOrderByCriadoEmDesc(
            Long empresaId
    );

    List<Orcamento> findAllByEmpresaIdAndStatusOrderByCriadoEmDesc(
            Long empresaId,
            StatusOrcamento status
    );

    List<Orcamento> findAllByEmpresaIdAndClienteIdOrderByCriadoEmDesc(
            Long empresaId,
            Long clienteId
    );

    List<Orcamento> findAllByEmpresaIdAndStatusAndClienteIdOrderByCriadoEmDesc(
            Long empresaId,
            StatusOrcamento status,
            Long clienteId
    );

    Optional<Orcamento> findBySolicitacaoOrcamentoId(
            Long solicitacaoOrcamentoId
    );

    List<Orcamento> findAllByEmpresaIdAndStatusIn(
            Long empresaId,
            Collection<StatusOrcamento> status
    );

    long countByEmpresaIdAndEnviadoEmIsNotNull(Long empresaId);

    @Query(
            "select coalesce(sum(o.valorTotal), 0) "
                    + "from Orcamento o "
                    + "where o.empresa.id = :empresaId "
                    + "and o.enviadoEm is not null"
    )
    BigDecimal somarValorEnviado(@Param("empresaId") Long empresaId);

    List<Orcamento> findAllByStatusAndValidoAteBefore(
            StatusOrcamento status,
            LocalDate hoje
    );

    List<Orcamento> findAllByStatusAndEnviadoEmBeforeAndLembreteEnviadoEmIsNull(
            StatusOrcamento status,
            LocalDateTime limite
    );

    List<Orcamento> findAllByStatusAndValidoAteIsNullAndEnviadoEmBefore(
            StatusOrcamento status,
            LocalDateTime limite
    );
}
