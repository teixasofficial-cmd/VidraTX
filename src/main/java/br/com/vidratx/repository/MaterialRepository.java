package br.com.vidratx.repository;

import br.com.vidratx.entity.Material;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MaterialRepository
        extends JpaRepository<Material, Long> {

    Optional<Material> findByIdAndEmpresaId(
            Long id,
            Long empresaId
    );

    List<Material> findAllByEmpresaIdOrderByNomeAsc(
            Long empresaId
    );

    List<Material> findAllByEmpresaIdAndAtivoTrueOrderByNomeAsc(
            Long empresaId
    );

    boolean existsByEmpresaIdAndNome(
            Long empresaId,
            String nome
    );

    boolean existsByEmpresaIdAndNomeAndIdNot(
            Long empresaId,
            String nome,
            Long id
    );
}
