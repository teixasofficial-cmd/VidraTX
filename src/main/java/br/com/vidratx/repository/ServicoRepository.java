package br.com.vidratx.repository;

import br.com.vidratx.entity.Servico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServicoRepository
        extends JpaRepository<Servico, Long> {

    Optional<Servico> findByIdAndEmpresaId(
            Long id,
            Long empresaId
    );

    List<Servico> findAllByEmpresaIdOrderByNomeAsc(
            Long empresaId
    );

    List<Servico> findAllByEmpresaIdAndAtivoTrueOrderByNomeAsc(
            Long empresaId
    );

    boolean existsByEmpresaIdAndNome(
            Long empresaId,
            String nome
    );

    boolean existsByEmpresaIdAndNomeAndIdNot(
            Long empresaId,
            String nome,
            Long id
    );
}
