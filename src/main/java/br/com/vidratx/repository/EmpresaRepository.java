package br.com.vidratx.repository;

import br.com.vidratx.entity.Empresa;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EmpresaRepository
        extends JpaRepository<Empresa, Long> {

    boolean existsByCnpj(String cnpj);

    boolean existsByCnpjAndIdNot(
            String cnpj,
            Long id
    );

    boolean existsBySlug(String slug);

    Optional<Empresa> findBySlug(String slug);

    long countByAtiva(Boolean ativa);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Empresa e where e.id = :id")
    Optional<Empresa> travarPorId(@Param("id") Long id);
}
