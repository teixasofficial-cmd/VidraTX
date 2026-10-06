package br.com.vidratx.repository;

import br.com.vidratx.entity.Tipologia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TipologiaRepository extends JpaRepository<Tipologia, Long> {

    Optional<Tipologia> findByIdAndEmpresaId(Long id, Long empresaId);

    List<Tipologia> findAllByEmpresaIdOrderByNomeAsc(Long empresaId);

    List<Tipologia> findAllByEmpresaIdAndAtivoTrueOrderByNomeAsc(Long empresaId);

    boolean existsByEmpresaId(Long empresaId);
}
