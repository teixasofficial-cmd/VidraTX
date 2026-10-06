package br.com.vidratx.repository;

import br.com.vidratx.entity.OrcamentoPeca;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrcamentoPecaRepository extends JpaRepository<OrcamentoPeca, Long> {

    List<OrcamentoPeca> findAllByOrcamentoItemIdOrderByIdAsc(Long orcamentoItemId);

    void deleteAllByOrcamentoItemId(Long orcamentoItemId);
}
