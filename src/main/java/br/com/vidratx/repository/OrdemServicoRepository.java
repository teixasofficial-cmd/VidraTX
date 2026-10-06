package br.com.vidratx.repository;

import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import br.com.vidratx.entity.OrdemServico;
import br.com.vidratx.enums.StatusProducao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OrdemServicoRepository
        extends JpaRepository<OrdemServico, Long> {

    Optional<OrdemServico> findByIdAndEmpresaId(
            Long id,
            Long empresaId
    );

    List<OrdemServico> findAllByEmpresaIdOrderByCriadoEmDesc(
            Long empresaId
    );

    List<OrdemServico> findAllByEmpresaIdAndStatusProducaoOrderByCriadoEmDesc(
            Long empresaId,
            StatusProducao statusProducao
    );

    Optional<OrdemServico> findByOrcamentoId(
            Long orcamentoId
    );

    List<OrdemServico> findAllByOrcamentoIdIn(
            Collection<Long> orcamentoIds
    );

    @Query("select o from OrdemServico o where o.empresa.id = :empresaId "
            + "and not exists (select i.id from Instalacao i where i.ordemServico = o) "
            + "order by o.criadoEm asc")
    List<OrdemServico> findSemInstalacao(@Param("empresaId") Long empresaId);
}
