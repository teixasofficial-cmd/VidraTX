package br.com.vidratx.repository;

import br.com.vidratx.entity.Historico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoricoRepository
        extends JpaRepository<Historico, Long> {

    List<Historico> findAllByEmpresaIdAndClienteIdOrderByCriadoEmDesc(
            Long empresaId,
            Long clienteId
    );

    List<Historico> findAllByAtendimentoIdOrderByCriadoEmAsc(
            Long atendimentoId
    );
}
