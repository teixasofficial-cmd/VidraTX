package br.com.vidratx.repository;

import br.com.vidratx.entity.PropostaAgendamento;
import br.com.vidratx.enums.OrigemProposta;
import br.com.vidratx.enums.StatusProposta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PropostaAgendamentoRepository
        extends JpaRepository<PropostaAgendamento, Long> {

    List<PropostaAgendamento> findAllByMedicaoIdOrderByIdAsc(Long medicaoId);

    List<PropostaAgendamento> findAllByInstalacaoIdOrderByIdAsc(Long instalacaoId);

    List<PropostaAgendamento> findAllByMedicaoIdAndStatus(Long medicaoId, StatusProposta status);

    List<PropostaAgendamento> findAllByInstalacaoIdAndStatus(Long instalacaoId, StatusProposta status);

    Optional<PropostaAgendamento> findFirstByMedicaoIdAndOrigemOrderByIdDesc(Long medicaoId, OrigemProposta origem);

    Optional<PropostaAgendamento> findFirstByInstalacaoIdAndOrigemOrderByIdDesc(Long instalacaoId, OrigemProposta origem);
}
