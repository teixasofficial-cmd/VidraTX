package br.com.vidratx.dto;

import br.com.vidratx.enums.TipoFoto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class FotoRequest {

    @NotNull(message = "Tipo é obrigatório")
    private TipoFoto tipo;

    @NotBlank(message = "URL é obrigatória")
    @Size(
            max = 500,
            message = "URL deve ter no máximo 500 caracteres"
    )
    private String url;

    @Size(
            max = 500,
            message = "Descrição deve ter no máximo 500 caracteres"
    )
    private String descricao;

    public TipoFoto getTipo() {
        return tipo;
    }

    public void setTipo(TipoFoto tipo) {
        this.tipo = tipo;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
