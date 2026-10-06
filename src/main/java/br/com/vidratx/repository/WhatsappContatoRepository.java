package br.com.vidratx.repository;

import br.com.vidratx.entity.WhatsappContato;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface WhatsappContatoRepository
        extends JpaRepository<WhatsappContato, Long> {

    Optional<WhatsappContato> findByEmpresaIdAndTelefone(
            Long empresaId,
            String telefone
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from WhatsappContato c where c.empresa.id = :empresaId and c.telefone = :telefone")
    Optional<WhatsappContato> travar(
            @Param("empresaId") Long empresaId,
            @Param("telefone") String telefone
    );
}
