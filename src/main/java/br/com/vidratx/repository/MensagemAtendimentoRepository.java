package br.com.vidratx.repository;

import br.com.vidratx.entity.MensagemAtendimento;
import br.com.vidratx.enums.TipoMensagem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MensagemAtendimentoRepository
        extends JpaRepository<MensagemAtendimento, Long> {

    List<MensagemAtendimento> findAllByAtendimentoIdAndTipoOrderByEnviadoEmAsc(
            Long atendimentoId,
            TipoMensagem tipo
    );

    List<MensagemAtendimento> findAllByMensagemSaidaId(
            Long mensagemSaidaId
    );

    boolean existsByAtendimentoIdAndWhatsappMensagemId(
            Long atendimentoId,
            String whatsappMensagemId
    );

    Optional<MensagemAtendimento> findByIdAndAtendimento_Empresa_Id(
            Long id,
            Long empresaId
    );

    Optional<MensagemAtendimento> findFirstByAtendimentoIdAndIdLessThanOrderByIdDesc(
            Long atendimentoId,
            Long id
    );

    @EntityGraph(attributePaths = "mensagemSaida")
    List<MensagemAtendimento> findAllByAtendimentoIdOrderByEnviadoEmAscIdAsc(Long atendimentoId);
}
