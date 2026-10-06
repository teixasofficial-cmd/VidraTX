package br.com.vidratx.repository;

import java.time.LocalDateTime;
import br.com.vidratx.entity.PerguntaPendente;
import br.com.vidratx.enums.StatusPergunta;
import br.com.vidratx.enums.TipoPergunta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PerguntaPendenteRepository
        extends JpaRepository<PerguntaPendente, Long> {

    List<PerguntaPendente> findAllByEmpresaIdAndTelefoneAndStatusOrderByCriadaEmAscIdAsc(
            Long empresaId,
            String telefone,
            StatusPergunta status
    );

    List<PerguntaPendente> findAllByTipoInAndReferenciaIdAndStatus(
            Collection<TipoPergunta> tipos,
            Long referenciaId,
            StatusPergunta status
    );

    Optional<PerguntaPendente> findFirstByEmpresaIdAndTelefoneAndStatusNotAndAtualizadoEmAfterOrderByAtualizadoEmDescIdDesc(
            Long empresaId,
            String telefone,
            StatusPergunta status,
            LocalDateTime desde
    );
}
