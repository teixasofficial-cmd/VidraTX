package br.com.vidratx.repository;

import br.com.vidratx.entity.MensagemSaida;
import br.com.vidratx.enums.StatusMensagemSaida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MensagemSaidaRepository
        extends JpaRepository<MensagemSaida, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update MensagemSaida s set s.status = br.com.vidratx.enums.StatusMensagemSaida.ENVIANDO, "
            + "s.atualizadoEm = :agora where s.id = :id and s.status in :status")
    int reivindicar(
            @Param("id") Long id,
            @Param("status") Collection<StatusMensagemSaida> status,
            @Param("agora") LocalDateTime agora
    );

    @Query("select s.id from MensagemSaida s where s.status = br.com.vidratx.enums.StatusMensagemSaida.PENDENTE "
            + "and (s.proximaTentativaEm is null or s.proximaTentativaEm <= :agora) order by s.id")
    List<Long> idsProntosParaEnvio(@Param("agora") LocalDateTime agora);

    @Query("select s.id from MensagemSaida s where s.status = br.com.vidratx.enums.StatusMensagemSaida.PENDENTE "
            + "and s.empresa.id = :empresaId order by s.id")
    List<Long> idsPendentesDaEmpresa(@Param("empresaId") Long empresaId);

    @Query("select count(a) from MensagemSaida a, MensagemSaida s where s.id = :id "
            + "and a.empresa = s.empresa and a.telefone = s.telefone and a.id < s.id "
            + "and (a.status = br.com.vidratx.enums.StatusMensagemSaida.ENVIANDO "
            + "or (a.status = br.com.vidratx.enums.StatusMensagemSaida.PENDENTE "
            + "and (a.proximaTentativaEm is null or a.proximaTentativaEm <= :agora)))")
    long contarAnterioresNaFrente(@Param("id") Long id, @Param("agora") LocalDateTime agora);

    @Query("select a.id from MensagemSaida a, MensagemSaida s where s.id = :id "
            + "and a.empresa = s.empresa and a.telefone = s.telefone and a.id > s.id "
            + "and a.status = br.com.vidratx.enums.StatusMensagemSaida.PENDENTE "
            + "and (a.proximaTentativaEm is null or a.proximaTentativaEm <= :agora) order by a.id")
    List<Long> proximasDoContato(@Param("id") Long id, @Param("agora") LocalDateTime agora);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update MensagemSaida s set s.status = br.com.vidratx.enums.StatusMensagemSaida.PENDENTE "
            + "where s.status = br.com.vidratx.enums.StatusMensagemSaida.ENVIANDO and s.atualizadoEm < :limite")
    int liberarPresas(@Param("limite") LocalDateTime limite);

    Optional<MensagemSaida> findFirstByEmpresaIdAndWhatsappMensagemId(Long empresaId, String whatsappMensagemId);

    Optional<MensagemSaida> findFirstByReferenciaTipoAndReferenciaIdOrderByIdDesc(String referenciaTipo, Long referenciaId);

    List<MensagemSaida> findAllByEmpresaIdAndStatusOrderByIdDesc(Long empresaId, StatusMensagemSaida status);

    Optional<MensagemSaida> findFirstByAtendimentoIdOrderByIdDesc(Long atendimentoId);

    Optional<MensagemSaida> findFirstByPerguntaIdAndEnviadaEmIsNotNullOrderByEnviadaEmAsc(Long perguntaId);

    boolean existsByPerguntaId(Long perguntaId);

    long countByEmpresaIdAndStatus(Long empresaId, StatusMensagemSaida status);

    long countByEmpresaIdAndStatusAndCriadoEmAfter(Long empresaId, StatusMensagemSaida status, LocalDateTime desde);

    List<MensagemSaida> findAllByReferenciaTipoAndReferenciaIdIn(String referenciaTipo, Collection<Long> referenciaIds);

    Optional<MensagemSaida> findFirstByAtendimentoIdAndEnviadaEmLessThanOrderByEnviadaEmDescIdDesc(
            Long atendimentoId, LocalDateTime momento);
}
