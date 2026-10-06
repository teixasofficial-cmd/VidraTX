package br.com.vidratx.repository;

import br.com.vidratx.entity.MaterialNecessario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MaterialNecessarioRepository
        extends JpaRepository<MaterialNecessario, Long> {

    Optional<MaterialNecessario> findByIdAndOrcamentoId(
            Long id,
            Long orcamentoId
    );

    List<MaterialNecessario> findAllByOrcamentoIdOrderByCriadoEmAsc(
            Long orcamentoId
    );
}
