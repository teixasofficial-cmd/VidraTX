package br.com.vidratx.mapper;

import br.com.vidratx.dto.TabelaPrecoRequest;
import br.com.vidratx.dto.TabelaPrecoResponse;
import br.com.vidratx.entity.TabelaPreco;
import br.com.vidratx.enums.OrigemPreco;
import br.com.vidratx.util.DinheiroUtils;
import org.springframework.stereotype.Component;

@Component
public class TabelaPrecoMapper {

    public TabelaPreco toEntity(TabelaPrecoRequest request) {

        TabelaPreco tabelaPreco = new TabelaPreco();

        aplicarCampos(tabelaPreco, request);

        return tabelaPreco;
    }

    public void updateEntity(TabelaPreco tabelaPreco, TabelaPrecoRequest request) {
        aplicarCampos(tabelaPreco, request);
    }

    public TabelaPrecoResponse toResponse(TabelaPreco tabelaPreco) {

        TabelaPrecoResponse response = new TabelaPrecoResponse();

        response.setId(tabelaPreco.getId());
        response.setEmpresaId(tabelaPreco.getEmpresa().getId());
        response.setCategoria(tabelaPreco.getCategoria());
        response.setDescricao(tabelaPreco.getDescricao());
        response.setTipoVidro(tabelaPreco.getTipoVidro());
        response.setEspessuraMm(tabelaPreco.getEspessuraMm());
        response.setCor(tabelaPreco.getCor());
        response.setAcabamento(tabelaPreco.getAcabamento());
        response.setUnidade(tabelaPreco.getUnidade());
        response.setCusto(
                tabelaPreco.getCustoCentavos() != null
                        ? DinheiroUtils.paraReais(tabelaPreco.getCustoCentavos())
                        : null
        );
        response.setPrecoVenda(DinheiroUtils.paraReais(tabelaPreco.getPrecoVendaCentavos()));
        response.setPrecoAbaixoDoCusto(
                tabelaPreco.getCustoCentavos() != null
                        && tabelaPreco.getPrecoVendaCentavos() != null
                        && tabelaPreco.getCustoCentavos() > tabelaPreco.getPrecoVendaCentavos()
        );
        response.setFornecedor(tabelaPreco.getFornecedor());
        response.setOrigem(tabelaPreco.getOrigem());
        response.setFormulaOrigem(tabelaPreco.getFormulaOrigem());
        response.setAtivo(tabelaPreco.getAtivo());
        response.setCriadoEm(tabelaPreco.getCriadoEm());
        response.setAtualizadoEm(tabelaPreco.getAtualizadoEm());

        return response;
    }

    private void aplicarCampos(TabelaPreco tabelaPreco, TabelaPrecoRequest request) {

        tabelaPreco.setCategoria(request.getCategoria());
        tabelaPreco.setDescricao(request.getDescricao().trim());
        tabelaPreco.setTipoVidro(request.getTipoVidro());
        tabelaPreco.setEspessuraMm(request.getEspessuraMm());
        tabelaPreco.setCor(request.getCor());
        tabelaPreco.setAcabamento(request.getAcabamento());
        tabelaPreco.setUnidade(request.getUnidade());
        tabelaPreco.setCustoCentavos(
                request.getCusto() != null ? DinheiroUtils.paraCentavos(request.getCusto()) : null
        );
        tabelaPreco.setPrecoVendaCentavos(DinheiroUtils.paraCentavos(request.getPrecoVenda()));
        tabelaPreco.setFornecedor(normalizar(request.getFornecedor()));
        tabelaPreco.setAtivo(request.getAtivo() == null ? Boolean.TRUE : request.getAtivo());
        tabelaPreco.setOrigem(request.getOrigem() == null ? OrigemPreco.MANUAL : request.getOrigem());
        tabelaPreco.setFormulaOrigem(normalizar(request.getFormulaOrigem()));
    }

    private String normalizar(String valor) {

        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor.trim();
    }
}
