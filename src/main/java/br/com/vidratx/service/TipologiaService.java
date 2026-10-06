package br.com.vidratx.service;

import br.com.vidratx.dto.TipologiaAjusteRequest;
import br.com.vidratx.dto.TipologiaResponse;
import br.com.vidratx.entity.Tipologia;
import br.com.vidratx.enums.TipoEventoHistorico;
import br.com.vidratx.exception.EmpresaNaoEncontradaException;
import br.com.vidratx.exception.TipologiaNaoEncontradaException;
import br.com.vidratx.mapper.TipologiaMapper;
import br.com.vidratx.repository.EmpresaRepository;
import br.com.vidratx.repository.TipologiaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TipologiaService {

    private final TipologiaRepository tipologiaRepository;
    private final EmpresaRepository empresaRepository;
    private final TipologiaMapper tipologiaMapper;
    private final HistoricoService historicoService;

    public TipologiaService(
            TipologiaRepository tipologiaRepository,
            EmpresaRepository empresaRepository,
            TipologiaMapper tipologiaMapper,
            HistoricoService historicoService) {

        this.tipologiaRepository = tipologiaRepository;
        this.empresaRepository = empresaRepository;
        this.tipologiaMapper = tipologiaMapper;
        this.historicoService = historicoService;
    }

    @Transactional(readOnly = true)
    public List<TipologiaResponse> listarPorEmpresa(Long empresaId, boolean apenasAtivas) {

        if (!empresaRepository.existsById(empresaId)) {
            throw new EmpresaNaoEncontradaException("Empresa não encontrada");
        }

        List<Tipologia> tipologias = apenasAtivas
                ? tipologiaRepository.findAllByEmpresaIdAndAtivoTrueOrderByNomeAsc(empresaId)
                : tipologiaRepository.findAllByEmpresaIdOrderByNomeAsc(empresaId);

        return tipologias.stream().map(tipologiaMapper::toResponse).toList();
    }

    @Transactional
    public TipologiaResponse ajustarFolgas(Long empresaId, Long tipologiaId, TipologiaAjusteRequest request) {

        Tipologia tipologia = buscarTipologia(empresaId, tipologiaId);

        tipologiaMapper.aplicarAjuste(tipologia, request);

        Tipologia salvo = tipologiaRepository.save(tipologia);

        historicoService.registrarEventoEmpresa(
                salvo.getEmpresa(), TipoEventoHistorico.TABELA_PRECO_ATUALIZADA,
                "Folgas da tipologia \"" + salvo.getNome() + "\" ajustadas"
        );

        return tipologiaMapper.toResponse(salvo);
    }

    private Tipologia buscarTipologia(Long empresaId, Long tipologiaId) {

        return tipologiaRepository.findByIdAndEmpresaId(tipologiaId, empresaId)
                .orElseThrow(() -> new TipologiaNaoEncontradaException("Tipologia não encontrada"));
    }
}
