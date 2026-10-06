package br.com.vidratx.service;

import br.com.vidratx.dto.PagamentoRequest;
import br.com.vidratx.dto.PagamentoResponse;
import br.com.vidratx.dto.ResumoFinanceiroResponse;
import br.com.vidratx.entity.Orcamento;
import br.com.vidratx.entity.Pagamento;
import br.com.vidratx.enums.SituacaoPagamento;
import br.com.vidratx.exception.OrcamentoNaoAprovadoException;
import br.com.vidratx.exception.OrcamentoNaoEncontradoException;
import br.com.vidratx.exception.PagamentoNaoEditavelException;
import br.com.vidratx.exception.PagamentoNaoEncontradoException;
import br.com.vidratx.mapper.PagamentoMapper;
import br.com.vidratx.repository.OrcamentoRepository;
import br.com.vidratx.repository.PagamentoRepository;
import br.com.vidratx.enums.StatusOrcamento;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class PagamentoService {

    private final PagamentoRepository pagamentoRepository;
    private final OrcamentoRepository orcamentoRepository;
    private final PagamentoMapper pagamentoMapper;

    public PagamentoService(
            PagamentoRepository pagamentoRepository,
            OrcamentoRepository orcamentoRepository,
            PagamentoMapper pagamentoMapper) {

        this.pagamentoRepository = pagamentoRepository;
        this.orcamentoRepository = orcamentoRepository;
        this.pagamentoMapper = pagamentoMapper;
    }

    @Transactional(readOnly = true)
    public List<PagamentoResponse> listar(
            Long empresaId,
            Long orcamentoId) {

        buscarOrcamento(empresaId, orcamentoId);

        return pagamentoRepository
                .findAllByOrcamentoIdOrderByCriadoEmAsc(orcamentoId)
                .stream()
                .map(pagamentoMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ResumoFinanceiroResponse resumo(
            Long empresaId,
            Long orcamentoId) {

        Orcamento orcamento =
                buscarOrcamento(empresaId, orcamentoId);

        BigDecimal recebido =
                pagamentoRepository.somarPorOrcamentoESituacao(
                        orcamentoId, SituacaoPagamento.PAGO
                );

        ResumoFinanceiroResponse resumo =
                new ResumoFinanceiroResponse();

        resumo.setOrcamentoId(orcamentoId);
        resumo.setValorContratado(orcamento.getValorTotal());
        resumo.setValorRecebido(recebido);
        resumo.setValorAReceber(
                orcamento.getValorTotal().subtract(recebido)
        );

        return resumo;
    }

    @Transactional
    public PagamentoResponse adicionar(
            Long empresaId,
            Long orcamentoId,
            PagamentoRequest request) {

        Orcamento orcamento =
                buscarOrcamento(empresaId, orcamentoId);

        garantirAprovado(orcamento);

        Pagamento pagamento =
                pagamentoMapper.toEntity(request);

        pagamento.setOrcamento(orcamento);

        Pagamento salvo =
                pagamentoRepository.save(pagamento);

        return pagamentoMapper.toResponse(salvo);
    }

    @Transactional
    public PagamentoResponse marcarComoPago(
            Long empresaId,
            Long orcamentoId,
            Long pagamentoId) {

        buscarOrcamento(empresaId, orcamentoId);

        Pagamento pagamento =
                buscarPagamento(orcamentoId, pagamentoId);

        pagamento.setSituacao(SituacaoPagamento.PAGO);
        pagamento.setPagoEm(LocalDate.now());

        return pagamentoMapper.toResponse(
                pagamentoRepository.save(pagamento)
        );
    }

    @Transactional
    public void remover(
            Long empresaId,
            Long orcamentoId,
            Long pagamentoId) {

        buscarOrcamento(empresaId, orcamentoId);

        Pagamento pagamento =
                buscarPagamento(orcamentoId, pagamentoId);

        if (pagamento.getSituacao() == SituacaoPagamento.PAGO) {

            throw new PagamentoNaoEditavelException(
                    "Não é possível excluir um pagamento já registrado como pago"
            );
        }

        pagamentoRepository.delete(pagamento);
    }

    private void garantirAprovado(Orcamento orcamento) {

        if (orcamento.getStatus() != StatusOrcamento.APROVADO) {

            throw new OrcamentoNaoAprovadoException(
                    "Só é possível registrar pagamentos em orçamentos aprovados"
            );
        }
    }

    private Orcamento buscarOrcamento(
            Long empresaId,
            Long orcamentoId) {

        return orcamentoRepository
                .findByIdAndEmpresaId(orcamentoId, empresaId)
                .orElseThrow(() ->
                        new OrcamentoNaoEncontradoException(
                                "Orçamento não encontrado"
                        )
                );
    }

    private Pagamento buscarPagamento(
            Long orcamentoId,
            Long pagamentoId) {

        return pagamentoRepository
                .findByIdAndOrcamentoId(pagamentoId, orcamentoId)
                .orElseThrow(() ->
                        new PagamentoNaoEncontradoException(
                                "Pagamento não encontrado"
                        )
                );
    }
}
