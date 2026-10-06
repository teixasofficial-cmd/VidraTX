package br.com.vidratx.repository;

import br.com.vidratx.entity.AdministradorGlobal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdministradorGlobalRepository
        extends JpaRepository<AdministradorGlobal, Long> {

    Optional<AdministradorGlobal> findByEmailAndAtivoTrue(String email);

    Optional<AdministradorGlobal> findByIdAndAtivoTrue(Long id);
}
