package br.com.vidratx.repository;

import br.com.vidratx.entity.AtendimentoWhatsapp;
import br.com.vidratx.enums.StatusAtendimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AtendimentoWhatsappRepository
        extends JpaRepository<AtendimentoWhatsapp, Long> {

    Optional<AtendimentoWhatsapp> findByIdAndEmpresaId(
            Long id,
            Long empresaId
    );

    Optional<AtendimentoWhatsapp> findFirstByEmpresaIdAndTelefoneAndStatusNotOrderByCriadoEmDesc(
            Long empresaId,
            String telefone,
            StatusAtendimento status
    );

    List<AtendimentoWhatsapp> findAllByEmpresaIdOrderByAtualizadoEmDesc(
            Long empresaId
    );

    List<AtendimentoWhatsapp> findAllByEmpresaIdAndStatusOrderByAtualizadoEmDesc(
            Long empresaId,
            StatusAtendimento status
    );

    long countByEmpresaIdAndStatus(
            Long empresaId,
            StatusAtendimento status
    );

    Optional<AtendimentoWhatsapp> findBySolicitacaoOrcamentoId(
            Long solicitacaoOrcamentoId
    );

    List<AtendimentoWhatsapp> findAllByEmpresaIdAndTelefoneInAndClienteIsNullAndStatusNot(
            Long empresaId,
            Collection<String> telefones,
            StatusAtendimento status
    );

    List<AtendimentoWhatsapp> findTop200ByStatusAndAtualizadoEmBefore(
            StatusAtendimento status,
            LocalDateTime limite
    );

    List<AtendimentoWhatsapp> findAllByEmpresaIdAndStatusIn(
            Long empresaId,
            Collection<StatusAtendimento> status
    );

    @Query("select count(a) from AtendimentoWhatsapp a where a.empresa.id = :empresaId "
            + "and a.status in :status and a.ultimaMensagemClienteEm is not null "
            + "and (a.ultimaMensagemEmpresaEm is null or a.ultimaMensagemClienteEm > a.ultimaMensagemEmpresaEm)")
    long contarClienteAguardandoResposta(
            @Param("empresaId") Long empresaId,
            @Param("status") Collection<StatusAtendimento> status
    );

    @Query("select a from AtendimentoWhatsapp a where a.empresa.id = :empresaId "
            + "and a.status in :status and a.ultimaMensagemClienteEm is not null "
            + "and (a.ultimaMensagemEmpresaEm is null or a.ultimaMensagemClienteEm > a.ultimaMensagemEmpresaEm) "
            + "order by coalesce(a.clienteAguardandoDesde, a.ultimaMensagemClienteEm) asc")
    List<AtendimentoWhatsapp> listarClienteAguardandoResposta(
            @Param("empresaId") Long empresaId,
            @Param("status") Collection<StatusAtendimento> status
    );

    @Query("select a from AtendimentoWhatsapp a where a.status = :status "
            + "and a.ultimaMensagemClienteEm is not null "
            + "and (a.ultimaMensagemEmpresaEm is null or a.ultimaMensagemClienteEm > a.ultimaMensagemEmpresaEm) "
            + "and coalesce(a.clienteAguardandoDesde, a.ultimaMensagemClienteEm) < :limite")
    List<AtendimentoWhatsapp> listarEsperandoDesdeAntesDe(
            @Param("status") StatusAtendimento status,
            @Param("limite") LocalDateTime limite
    );

    List<AtendimentoWhatsapp> findTop200ByStatusInAndAtualizadoEmBefore(
            Collection<StatusAtendimento> status,
            LocalDateTime limite
    );
}
