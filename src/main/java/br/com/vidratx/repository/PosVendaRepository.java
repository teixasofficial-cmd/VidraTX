package br.com.vidratx.repository;

import br.com.vidratx.entity.PosVenda;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PosVendaRepository
        extends JpaRepository<PosVenda, Long> {

    Optional<PosVenda> findByIdAndOrdemServicoId(
            Long id,
            Long ordemServicoId
    );

    List<PosVenda> findAllByOrdemServicoIdOrderByCriadoEmDesc(
            Long ordemServicoId
    );
}
