package br.com.vidratx.repository;

import br.com.vidratx.entity.Foto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FotoRepository
        extends JpaRepository<Foto, Long> {

    Optional<Foto> findByIdAndOrcamentoId(
            Long id,
            Long orcamentoId
    );

    List<Foto> findAllByOrcamentoIdOrderByCriadoEmAsc(
            Long orcamentoId
    );
}
