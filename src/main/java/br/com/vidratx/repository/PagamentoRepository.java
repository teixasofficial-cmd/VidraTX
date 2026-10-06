package br.com.vidratx.repository;

import br.com.vidratx.entity.Pagamento;
import br.com.vidratx.enums.SituacaoPagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PagamentoRepository
        extends JpaRepository<Pagamento, Long> {

    Optional<Pagamento> findByIdAndOrcamentoId(
            Long id,
            Long orcamentoId
    );

    List<Pagamento> findAllByOrcamentoIdOrderByCriadoEmAsc(
            Long orcamentoId
    );

    @Query(
            "select coalesce(sum(p.valor), 0) "
                    + "from Pagamento p "
                    + "where p.orcamento.id = :orcamentoId "
                    + "and p.situacao = :situacao"
    )
    BigDecimal somarPorOrcamentoESituacao(
            @Param("orcamentoId") Long orcamentoId,
            @Param("situacao") SituacaoPagamento situacao
    );
}
