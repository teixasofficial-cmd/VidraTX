package br.com.vidratx.repository;

import br.com.vidratx.entity.PerguntaFrequente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PerguntaFrequenteRepository extends JpaRepository<PerguntaFrequente, Long> {

    Optional<PerguntaFrequente> findByIdAndEmpresaId(Long id, Long empresaId);

    List<PerguntaFrequente> findAllByEmpresaIdOrderByPerguntaAsc(Long empresaId);

    List<PerguntaFrequente> findAllByEmpresaIdAndAtivaTrue(Long empresaId);
}
