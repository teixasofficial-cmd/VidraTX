package br.com.vidratx.service;

import br.com.vidratx.dto.TabelaPrecoRequest;
import br.com.vidratx.dto.TabelaPrecoResponse;
import br.com.vidratx.entity.Empresa;
import br.com.vidratx.entity.TabelaPreco;
import br.com.vidratx.enums.TipoEventoHistorico;
import br.com.vidratx.exception.EmpresaNaoEncontradaException;
import br.com.vidratx.exception.TabelaPrecoNaoEncontradaException;
import br.com.vidratx.mapper.TabelaPrecoMapper;
import br.com.vidratx.repository.EmpresaRepository;
import br.com.vidratx.repository.TabelaPrecoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TabelaPrecoService {

    private final TabelaPrecoRepository tabelaPrecoRepository;
    private final EmpresaRepository empresaRepository;
    private final TabelaPrecoMapper tabelaPrecoMapper;
    private final HistoricoService historicoService;

    public TabelaPrecoService(
            TabelaPrecoRepository tabelaPrecoRepository,
            EmpresaRepository empresaRepository,
            TabelaPrecoMapper tabelaPrecoMapper,
            HistoricoService historicoService) {

        this.tabelaPrecoRepository = tabelaPrecoRepository;
        this.empresaRepository = empresaRepository;
        this.tabelaPrecoMapper = tabelaPrecoMapper;
        this.historicoService = historicoService;
    }

    @Transactional(readOnly = true)
    public List<TabelaPrecoResponse> listarPorEmpresa(Long empresaId, boolean apenasAtivos) {

        verificarEmpresa(empresaId);

        List<TabelaPreco> itens = apenasAtivos
                ? tabelaPrecoRepository.findAllByEmpresaIdAndAtivoTrueOrderByCategoriaAscDescricaoAsc(empresaId)
                : tabelaPrecoRepository.findAllByEmpresaIdOrderByCategoriaAscDescricaoAsc(empresaId);

        return itens.stream().map(tabelaPrecoMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public TabelaPrecoResponse buscarPorId(Long empresaId, Long tabelaPrecoId) {
        return tabelaPrecoMapper.toResponse(buscarTabelaPreco(empresaId, tabelaPrecoId));
    }

    @Transactional
    public TabelaPrecoResponse salvar(Long empresaId, TabelaPrecoRequest request) {

        Empresa empresa = buscarEmpresa(empresaId);

        TabelaPreco tabelaPreco = tabelaPrecoMapper.toEntity(request);
        tabelaPreco.setEmpresa(empresa);

        TabelaPreco salvo = tabelaPrecoRepository.save(tabelaPreco);

        historicoService.registrarEventoEmpresa(
                empresa, TipoEventoHistorico.TABELA_PRECO_ATUALIZADA,
                "Item adicionado à Tabela de Preços: " + salvo.getDescricao()
        );

        return tabelaPrecoMapper.toResponse(salvo);
    }

    @Transactional
    public TabelaPrecoResponse atualizar(Long empresaId, Long tabelaPrecoId, TabelaPrecoRequest request) {

        TabelaPreco tabelaPreco = buscarTabelaPreco(empresaId, tabelaPrecoId);

        tabelaPrecoMapper.updateEntity(tabelaPreco, request);

        TabelaPreco salvo = tabelaPrecoRepository.save(tabelaPreco);

        historicoService.registrarEventoEmpresa(
                salvo.getEmpresa(), TipoEventoHistorico.TABELA_PRECO_ATUALIZADA,
                "Item da Tabela de Preços atualizado: " + salvo.getDescricao()
        );

        return tabelaPrecoMapper.toResponse(salvo);
    }

    @Transactional
    public void excluir(Long empresaId, Long tabelaPrecoId) {

        TabelaPreco tabelaPreco = buscarTabelaPreco(empresaId, tabelaPrecoId);

        tabelaPrecoRepository.delete(tabelaPreco);

        historicoService.registrarEventoEmpresa(
                tabelaPreco.getEmpresa(), TipoEventoHistorico.TABELA_PRECO_ATUALIZADA,
                "Item removido da Tabela de Preços: " + tabelaPreco.getDescricao()
        );
    }

    private Empresa buscarEmpresa(Long empresaId) {

        return empresaRepository
                .findById(empresaId)
                .orElseThrow(() -> new EmpresaNaoEncontradaException("Empresa não encontrada"));
    }

    private void verificarEmpresa(Long empresaId) {
        buscarEmpresa(empresaId);
    }

    private TabelaPreco buscarTabelaPreco(Long empresaId, Long tabelaPrecoId) {

        return tabelaPrecoRepository
                .findByIdAndEmpresaId(tabelaPrecoId, empresaId)
                .orElseThrow(() -> new TabelaPrecoNaoEncontradaException("Item da tabela de preços não encontrado"));
    }
}
