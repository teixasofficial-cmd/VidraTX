package br.com.vidratx.repository;

import br.com.vidratx.entity.OrcamentoLinha;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrcamentoLinhaRepository extends JpaRepository<OrcamentoLinha, Long> {

    Optional<OrcamentoLinha> findByIdAndOrcamentoItemId(Long id, Long orcamentoItemId);

    List<OrcamentoLinha> findAllByOrcamentoItemIdOrderByIdAsc(Long orcamentoItemId);

    List<OrcamentoLinha> findAllByOrcamentoItemOrcamentoIdOrderByIdAsc(Long orcamentoId);

    void deleteAllByOrcamentoItemId(Long orcamentoItemId);
}
