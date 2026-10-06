package br.com.vidratx.mapper;

import br.com.vidratx.calculo.AlertaCalculo;
import br.com.vidratx.dto.AlertaCalculoResponse;
import br.com.vidratx.dto.OrcamentoItemResponse;
import br.com.vidratx.dto.OrcamentoLinhaResponse;
import br.com.vidratx.dto.OrcamentoPecaResponse;
import br.com.vidratx.entity.OrcamentoItem;
import br.com.vidratx.entity.OrcamentoLinha;
import br.com.vidratx.entity.OrcamentoPeca;
import br.com.vidratx.util.DinheiroUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class OrcamentoItemMapper {

    public OrcamentoItemResponse toResponse(
            OrcamentoItem item,
            List<OrcamentoPeca> pecas,
            List<OrcamentoLinha> linhas,
            long custoItemCentavos,
            List<AlertaCalculo> alertas) {

        OrcamentoItemResponse response = new OrcamentoItemResponse();

        response.setId(item.getId());
        response.setOrcamentoId(item.getOrcamento().getId());
        response.setTipologiaId(item.getTipologia().getId());
        response.setTipologiaNome(item.getTipologia().getNome());
        response.setAmbiente(item.getAmbiente());
        response.setLarguraVaoMm(item.getLarguraVaoMm());
        response.setAlturaVaoMm(item.getAlturaVaoMm());
        response.setLarguraVao2Mm(item.getLarguraVao2Mm());
        response.setAlturaVao2Mm(item.getAlturaVao2Mm());
        response.setMedidaTextoOriginal(item.getMedidaTextoOriginal());
        response.setMedidaAproximada(item.getMedidaAproximada());
        response.setTipoVidro(item.getTipoVidro());
        response.setEspessuraMm(item.getEspessuraMm());
        response.setCor(item.getCor());
        response.setAcabamento(item.getAcabamento());
        response.setCorFerragem(item.getCorFerragem());
        response.setQuantidade(item.getQuantidade());
        response.setObservacoes(item.getObservacoes());
        response.setOrdem(item.getOrdem());
        response.setCustoItem(DinheiroUtils.paraReais(custoItemCentavos));

        response.setPecas(pecas.stream().map(this::toPecaResponse).toList());
        response.setLinhas(linhas.stream().map(this::toLinhaResponse).toList());
        response.setAlertas(
                alertas.stream().map(a -> new AlertaCalculoResponse(a.codigo(), a.mensagem())).toList()
        );

        return response;
    }

    private OrcamentoPecaResponse toPecaResponse(OrcamentoPeca peca) {

        OrcamentoPecaResponse response = new OrcamentoPecaResponse();

        response.setId(peca.getId());
        response.setDescricao(peca.getDescricao());
        response.setLarguraCorteMm(peca.getLarguraCorteMm());
        response.setAlturaCorteMm(peca.getAlturaCorteMm());
        response.setQuantidade(peca.getQuantidade());
        response.setExcedeTamanhoMaximo(peca.getExcedeTamanhoMaximo());

        BigDecimal area = BigDecimal.valueOf((long) peca.getLarguraCorteMm() * peca.getAlturaCorteMm())
                .divide(BigDecimal.valueOf(1_000_000L), 4, RoundingMode.HALF_UP);

        response.setAreaM2(area);

        return response;
    }

    private OrcamentoLinhaResponse toLinhaResponse(OrcamentoLinha linha) {

        OrcamentoLinhaResponse response = new OrcamentoLinhaResponse();

        response.setId(linha.getId());
        response.setTipo(linha.getTipoComponente());
        response.setDescricao(linha.getDescricao());
        response.setComponenteDescricao(linha.getComponenteDescricao());
        response.setComponenteQuantidade(linha.getComponenteQuantidade());
        response.setQuantidade(linha.getQuantidade());
        response.setTabelaPrecoId(linha.getTabelaPreco() != null ? linha.getTabelaPreco().getId() : null);
        response.setValorUnitario(
                linha.getValorUnitarioCentavos() != null
                        ? DinheiroUtils.paraReais(linha.getValorUnitarioCentavos()) : null
        );
        response.setValorSugerido(DinheiroUtils.paraReais(linha.getValorSugeridoCentavos()));
        response.setValorFinal(
                linha.getValorFinalCentavos() != null
                        ? DinheiroUtils.paraReais(linha.getValorFinalCentavos()) : null
        );
        response.setValorExibido(DinheiroUtils.paraReais(linha.getValorExibidoCentavos()));
        response.setOrigemSugestao(linha.getOrigemSugestao());
        response.setEditado(linha.getEditado());
        response.setEditadoPorNome(linha.getEditadoPor() != null ? linha.getEditadoPor().getNome() : null);
        response.setEditadoEm(linha.getEditadoEm());

        return response;
    }
}
