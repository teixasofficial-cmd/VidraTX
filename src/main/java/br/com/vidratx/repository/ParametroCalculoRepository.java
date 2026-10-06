package br.com.vidratx.repository;

import br.com.vidratx.entity.ParametroCalculo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParametroCalculoRepository extends JpaRepository<ParametroCalculo, Long> {

    Optional<ParametroCalculo> findByEmpresaId(Long empresaId);
}
