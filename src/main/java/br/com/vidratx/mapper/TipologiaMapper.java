package br.com.vidratx.mapper;

import br.com.vidratx.calculo.FormulaPecas;
import br.com.vidratx.dto.TipologiaAjusteRequest;
import br.com.vidratx.dto.TipologiaResponse;
import br.com.vidratx.entity.Tipologia;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TipologiaMapper {

    private final ObjectMapper objectMapper;

    public TipologiaMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record RegrasJson(
            int numeroFolhas,
            String formulaPecas,
            int descontoLarguraMm,
            int descontoAlturaMm,
            int transpasseMm,
            List<String> alertasNormativos
    ) {
    }

    public TipologiaResponse toResponse(Tipologia tipologia) {

        RegrasJson regras = ler(tipologia.getRegrasJson());

        TipologiaResponse response = new TipologiaResponse();

        response.setId(tipologia.getId());
        response.setEmpresaId(tipologia.getEmpresa().getId());
        response.setCodigo(tipologia.getCodigo());
        response.setNome(tipologia.getNome());
        response.setCategoria(tipologia.getCategoria());
        response.setNumeroFolhas(regras.numeroFolhas());
        response.setFormulaPecas(regras.formulaPecas());
        response.setDescontoLarguraMm(regras.descontoLarguraMm());
        response.setDescontoAlturaMm(regras.descontoAlturaMm());
        response.setTranspasseMm(regras.transpasseMm());
        response.setAlertasNormativos(regras.alertasNormativos());
        response.setAtivo(tipologia.getAtivo());
        response.setCriadoEm(tipologia.getCriadoEm());
        response.setAtualizadoEm(tipologia.getAtualizadoEm());

        return response;
    }

    public void aplicarAjuste(Tipologia tipologia, TipologiaAjusteRequest request) {

        RegrasJson atual = ler(tipologia.getRegrasJson());

        RegrasJson novo = new RegrasJson(
                atual.numeroFolhas(),
                atual.formulaPecas(),
                request.getDescontoLarguraMm(),
                request.getDescontoAlturaMm(),
                request.getTranspasseMm(),
                atual.alertasNormativos()
        );

        tipologia.setRegrasJson(escrever(novo));

        if (request.getAtivo() != null) {
            tipologia.setAtivo(request.getAtivo());
        }
    }

    public br.com.vidratx.calculo.TipologiaRegras paraRegrasCalculo(Tipologia tipologia) {

        RegrasJson regras = ler(tipologia.getRegrasJson());

        return new br.com.vidratx.calculo.TipologiaRegras(
                regras.numeroFolhas(),
                FormulaPecas.valueOf(regras.formulaPecas()),
                regras.descontoLarguraMm(),
                regras.descontoAlturaMm(),
                regras.transpasseMm(),
                regras.alertasNormativos()
        );
    }

    private RegrasJson ler(String json) {

        try {
            return objectMapper.readValue(json, RegrasJson.class);
        } catch (JsonProcessingException excecao) {
            throw new IllegalStateException("regras_json inválido na tipologia: " + excecao.getMessage());
        }
    }

    private String escrever(RegrasJson regras) {

        try {
            return objectMapper.writeValueAsString(regras);
        } catch (JsonProcessingException excecao) {
            throw new IllegalStateException("Não foi possível serializar as regras da tipologia");
        }
    }
}
