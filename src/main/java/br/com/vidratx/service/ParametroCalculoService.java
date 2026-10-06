package br.com.vidratx.service;

import br.com.vidratx.calculo.ParametrosCalculoInput;
import br.com.vidratx.dto.ParametroCalculoRequest;
import br.com.vidratx.dto.ParametroCalculoResponse;
import br.com.vidratx.entity.Empresa;
import br.com.vidratx.entity.ParametroCalculo;
import br.com.vidratx.enums.TipoEventoHistorico;
import br.com.vidratx.event.ParametrosCalculoAlteradosEvent;
import br.com.vidratx.exception.EmpresaNaoEncontradaException;
import br.com.vidratx.mapper.ParametroCalculoMapper;
import br.com.vidratx.repository.EmpresaRepository;
import br.com.vidratx.repository.ParametroCalculoRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class ParametroCalculoService {

    private final ParametroCalculoRepository parametroCalculoRepository;
    private final EmpresaRepository empresaRepository;
    private final ParametroCalculoMapper parametroCalculoMapper;
    private final HistoricoService historicoService;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    public ParametroCalculoService(
            ParametroCalculoRepository parametroCalculoRepository,
            EmpresaRepository empresaRepository,
            ParametroCalculoMapper parametroCalculoMapper,
            HistoricoService historicoService,
            ObjectMapper objectMapper,
            ApplicationEventPublisher eventPublisher) {

        this.parametroCalculoRepository = parametroCalculoRepository;
        this.empresaRepository = empresaRepository;
        this.parametroCalculoMapper = parametroCalculoMapper;
        this.historicoService = historicoService;
        this.objectMapper = objectMapper;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public ParametroCalculoResponse buscarPorEmpresa(Long empresaId) {
        return parametroCalculoMapper.toResponse(buscarOuCriarPadrao(empresaId));
    }

    @Transactional
    public ParametroCalculoResponse atualizar(Long empresaId, ParametroCalculoRequest request) {

        ParametroCalculo entidade = buscarOuCriarPadrao(empresaId);

        parametroCalculoMapper.updateEntity(entidade, request);

        validarSomaPercentuais(entidade);

        ParametroCalculo salvo = parametroCalculoRepository.save(entidade);

        historicoService.registrarEventoEmpresa(
                salvo.getEmpresa(), TipoEventoHistorico.PARAMETRO_CALCULO_ATUALIZADO,
                "Parâmetros de cálculo atualizados"
        );

        eventPublisher.publishEvent(new ParametrosCalculoAlteradosEvent(empresaId));

        return parametroCalculoMapper.toResponse(salvo);
    }

    @Transactional
    public ParametroCalculo buscarOuCriarPadrao(Long empresaId) {

        return parametroCalculoRepository.findByEmpresaId(empresaId)
                .orElseGet(() -> {

                    Empresa empresa = empresaRepository.findById(empresaId)
                            .orElseThrow(() -> new EmpresaNaoEncontradaException("Empresa não encontrada"));

                    ParametroCalculo novo = new ParametroCalculo();
                    novo.setEmpresa(empresa);

                    return parametroCalculoRepository.save(novo);
                });
    }

    public ParametrosCalculoInput paraMotor(ParametroCalculo entidade, Integer parcelas) {

        Map<Integer, BigDecimal> taxas = lerTaxas(entidade.getTaxaCartaoParcelas());
        BigDecimal percentualTaxaCartao = taxas.getOrDefault(
                parcelas != null ? parcelas : 1, BigDecimal.ZERO
        );

        return new ParametrosCalculoInput(
                entidade.getMultiploArredondamentoMm(),
                entidade.getAreaMinimaM2(),
                entidade.getPercentualPerdas(),
                entidade.getPercentualImpostos(),
                percentualTaxaCartao,
                entidade.getPercentualComissao(),
                entidade.getPercentualMargemDesejada(),
                entidade.getPercentualMargemMinima(),
                entidade.getArredondamentoComercial(),
                entidade.getTamanhoMaximoChapaLarguraMm(),
                entidade.getTamanhoMaximoChapaAlturaMm(),
                entidade.getModoPrecificacao()
        );
    }

    public boolean taxaConfigurada(ParametroCalculo entidade, Integer parcelas) {
        return parcelas == null || lerTaxas(entidade.getTaxaCartaoParcelas()).containsKey(parcelas);
    }

    private void validarSomaPercentuais(ParametroCalculo entidade) {

        BigDecimal soma = paraMotor(entidade, null).somaPercentuaisPrecificacao();

        if (soma.compareTo(BigDecimal.valueOf(100)) >= 0) {

            throw new IllegalArgumentException(
                    "A soma de impostos, taxa de cartão (à vista), comissão e margem desejada "
                            + "está em " + soma + "%, igual ou acima de 100% — não seria possível "
                            + "calcular um preço com esses parâmetros. Reduza algum dos percentuais."
            );
        }
    }

    private Map<Integer, BigDecimal> lerTaxas(String json) {

        if (json == null || json.isBlank()) {
            return Map.of();
        }

        try {
            return objectMapper.readValue(json, new TypeReference<Map<Integer, BigDecimal>>() {
            });
        } catch (Exception excecao) {
            return Map.of();
        }
    }
}
