package br.com.vidratx.repository;

import br.com.vidratx.entity.TabelaPreco;
import br.com.vidratx.enums.AcabamentoVidro;
import br.com.vidratx.enums.CorVidro;
import br.com.vidratx.enums.TipoVidro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TabelaPrecoRepository extends JpaRepository<TabelaPreco, Long> {

    Optional<TabelaPreco> findByIdAndEmpresaId(Long id, Long empresaId);

    List<TabelaPreco> findAllByEmpresaIdOrderByCategoriaAscDescricaoAsc(Long empresaId);

    List<TabelaPreco> findAllByEmpresaIdAndAtivoTrueOrderByCategoriaAscDescricaoAsc(Long empresaId);

    Optional<TabelaPreco> findFirstByEmpresaIdAndAtivoTrueAndTipoVidroAndEspessuraMmAndCorAndAcabamento(
            Long empresaId,
            TipoVidro tipoVidro,
            Short espessuraMm,
            CorVidro cor,
            AcabamentoVidro acabamento
    );
}
