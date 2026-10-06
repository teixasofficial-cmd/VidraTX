package br.com.vidratx.repository;

import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import br.com.vidratx.entity.MensagemRecebida;
import br.com.vidratx.enums.StatusMensagemRecebida;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MensagemRecebidaRepository
        extends JpaRepository<MensagemRecebida, Long> {

    Optional<MensagemRecebida> findByEmpresaIdAndWhatsappMensagemId(
            Long empresaId,
            String whatsappMensagemId
    );

    List<MensagemRecebida> findTop50ByStatusInAndRecebidaEmBeforeAndTentativasLessThanOrderByIdAsc(
            Collection<StatusMensagemRecebida> status,
            LocalDateTime limite,
            Integer maxTentativas
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from MensagemRecebida m where m.id = :id")
    Optional<MensagemRecebida> travar(@Param("id") Long id);

    List<MensagemRecebida> findAllByEmpresaIdAndTelefoneAndStatusAndIdLessThanAndTentativasLessThanOrderByIdAsc(
            Long empresaId,
            String telefone,
            StatusMensagemRecebida status,
            Long id,
            Integer tentativas
    );

    List<MensagemRecebida> findAllByEmpresaIdAndTelefoneAndStatusAndEnviadaEmAfter(
            Long empresaId,
            String telefone,
            StatusMensagemRecebida status,
            LocalDateTime enviadaEm
    );
}
