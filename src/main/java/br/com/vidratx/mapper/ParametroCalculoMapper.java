package br.com.vidratx.mapper;

import br.com.vidratx.dto.ParametroCalculoRequest;
import br.com.vidratx.dto.ParametroCalculoResponse;
import br.com.vidratx.entity.ParametroCalculo;
import br.com.vidratx.util.DinheiroUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class ParametroCalculoMapper {

    private static final Logger log = LoggerFactory.getLogger(ParametroCalculoMapper.class);
    private static final TypeReference<Map<Integer, BigDecimal>> TIPO_MAPA_TAXAS = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;

    public ParametroCalculoMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void updateEntity(ParametroCalculo entidade, ParametroCalculoRequest request) {

        entidade.setModoPrecificacao(request.getModoPrecificacao());
        entidade.setMultiploArredondamentoMm(request.getMultiploArredondamentoMm());
        entidade.setAreaMinimaM2(request.getAreaMinimaM2());
        entidade.setPercentualPerdas(request.getPercentualPerdas());
        entidade.setPercentualImpostos(request.getPercentualImpostos());
        entidade.setPercentualComissao(request.getPercentualComissao());
        entidade.setPercentualMargemDesejada(request.getPercentualMargemDesejada());
        entidade.setPercentualMargemMinima(request.getPercentualMargemMinima());
        entidade.setTaxaCartaoParcelas(escreverJson(request.getTaxaCartaoParcelas()));
        entidade.setArredondamentoComercial(request.getArredondamentoComercial());
        entidade.setVariacaoPreOrcamentoMinPct(request.getVariacaoPreOrcamentoMinPct());
        entidade.setVariacaoPreOrcamentoMaxPct(request.getVariacaoPreOrcamentoMaxPct());
        entidade.setValorVisitaTecnicaCentavos(
                request.getValorVisitaTecnica() != null
                        ? DinheiroUtils.paraCentavos(request.getValorVisitaTecnica()) : null
        );
        entidade.setRegraDeslocamento(request.getRegraDeslocamento());
        entidade.setValorDeslocamentoCentavos(
                request.getValorDeslocamento() != null
                        ? DinheiroUtils.paraCentavos(request.getValorDeslocamento()) : null
        );
        entidade.setValidadePadraoDias(request.getValidadePadraoDias());
        entidade.setTamanhoMaximoChapaLarguraMm(request.getTamanhoMaximoChapaLarguraMm());
        entidade.setTamanhoMaximoChapaAlturaMm(request.getTamanhoMaximoChapaAlturaMm());
        entidade.setToleranciaPrumoNivelMm(request.getToleranciaPrumoNivelMm());
        entidade.setRegimeTributario(request.getRegimeTributario());
    }

    public ParametroCalculoResponse toResponse(ParametroCalculo entidade) {

        ParametroCalculoResponse response = new ParametroCalculoResponse();

        response.setId(entidade.getId());
        response.setEmpresaId(entidade.getEmpresa().getId());
        response.setModoPrecificacao(entidade.getModoPrecificacao());
        response.setMultiploArredondamentoMm(entidade.getMultiploArredondamentoMm());
        response.setAreaMinimaM2(entidade.getAreaMinimaM2());
        response.setPercentualPerdas(entidade.getPercentualPerdas());
        response.setPercentualImpostos(entidade.getPercentualImpostos());
        response.setPercentualComissao(entidade.getPercentualComissao());
        response.setPercentualMargemDesejada(entidade.getPercentualMargemDesejada());
        response.setPercentualMargemMinima(entidade.getPercentualMargemMinima());
        response.setTaxaCartaoParcelas(lerJson(entidade.getTaxaCartaoParcelas()));
        response.setArredondamentoComercial(entidade.getArredondamentoComercial());
        response.setVariacaoPreOrcamentoMinPct(entidade.getVariacaoPreOrcamentoMinPct());
        response.setVariacaoPreOrcamentoMaxPct(entidade.getVariacaoPreOrcamentoMaxPct());
        response.setValorVisitaTecnica(DinheiroUtils.paraReais(entidade.getValorVisitaTecnicaCentavos()));
        response.setRegraDeslocamento(entidade.getRegraDeslocamento());
        response.setValorDeslocamento(DinheiroUtils.paraReais(entidade.getValorDeslocamentoCentavos()));
        response.setValidadePadraoDias(entidade.getValidadePadraoDias());
        response.setTamanhoMaximoChapaLarguraMm(entidade.getTamanhoMaximoChapaLarguraMm());
        response.setTamanhoMaximoChapaAlturaMm(entidade.getTamanhoMaximoChapaAlturaMm());
        response.setToleranciaPrumoNivelMm(entidade.getToleranciaPrumoNivelMm());
        response.setRegimeTributario(entidade.getRegimeTributario());

        return response;
    }

    private String escreverJson(Map<Integer, BigDecimal> mapa) {

        if (mapa == null || mapa.isEmpty()) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(mapa);
        } catch (JsonProcessingException excecao) {
            throw new IllegalArgumentException("Taxa de cartão por parcela inválida");
        }
    }

    private Map<Integer, BigDecimal> lerJson(String json) {

        if (json == null || json.isBlank()) {
            return Map.of();
        }

        try {
            return objectMapper.readValue(json, TIPO_MAPA_TAXAS);
        } catch (JsonProcessingException excecao) {

            log.warn("taxa_cartao_parcelas inválido no banco, ignorando: {}", excecao.getMessage());
            return Map.of();
        }
    }
}
