package br.com.vidratx.repository;

import br.com.vidratx.entity.SolicitacaoOrcamento;
import br.com.vidratx.enums.StatusSolicitacaoOrcamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SolicitacaoOrcamentoRepository
        extends JpaRepository<SolicitacaoOrcamento, Long> {

    Optional<SolicitacaoOrcamento> findByIdAndEmpresaId(
            Long id,
            Long empresaId
    );

    List<SolicitacaoOrcamento> findAllByEmpresaIdOrderByCriadoEmDesc(
            Long empresaId
    );

    List<SolicitacaoOrcamento> findAllByEmpresaIdAndStatusOrderByCriadoEmDesc(
            Long empresaId,
            StatusSolicitacaoOrcamento status
    );
}
