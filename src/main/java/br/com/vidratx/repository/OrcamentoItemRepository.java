package br.com.vidratx.repository;

import br.com.vidratx.entity.OrcamentoItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrcamentoItemRepository extends JpaRepository<OrcamentoItem, Long> {

    Optional<OrcamentoItem> findByIdAndOrcamentoId(Long id, Long orcamentoId);

    List<OrcamentoItem> findAllByOrcamentoIdOrderByOrdemAsc(Long orcamentoId);

    long countByOrcamentoId(Long orcamentoId);
}
