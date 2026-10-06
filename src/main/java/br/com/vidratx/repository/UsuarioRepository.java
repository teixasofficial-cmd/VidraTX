package br.com.vidratx.repository;

import br.com.vidratx.entity.Usuario;
import br.com.vidratx.enums.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository
        extends JpaRepository<Usuario, Long> {

    boolean existsByEmpresaIdAndEmail(
            Long empresaId,
            String email
    );

    boolean existsByEmpresaIdAndEmailAndIdNot(
            Long empresaId,
            String email,
            Long id
    );

    @Query(
            "select u from Usuario u "
                    + "join fetch u.empresa "
                    + "where u.id = :id and u.empresa.id = :empresaId"
    )
    Optional<Usuario> findByIdAndEmpresaId(
            @Param("id") Long id,
            @Param("empresaId") Long empresaId
    );

    List<Usuario> findAllByEmpresaId(
            Long empresaId
    );

    List<Usuario> findAllByEmpresaIdAndAtivoTrue(
            Long empresaId
    );

    Optional<Usuario> findByEmpresaIdAndEmail(
            Long empresaId,
            String email
    );

    long countByEmpresaIdAndPerfilAndAtivoTrue(
            Long empresaId,
            Perfil perfil
    );

    long countByEmpresaId(Long empresaId);

}
