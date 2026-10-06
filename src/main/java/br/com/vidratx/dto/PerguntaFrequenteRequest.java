package br.com.vidratx.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PerguntaFrequenteRequest {

    @NotBlank(message = "Informe a pergunta")
    @Size(max = 200, message = "A pergunta deve ter no máximo 200 caracteres")
    private String pergunta;

    @NotBlank(message = "Informe pelo menos uma palavra-chave")
    @Size(max = 500, message = "As palavras-chave devem ter no máximo 500 caracteres")
    private String palavrasChave;

    @NotBlank(message = "Informe a resposta")
    @Size(max = 2000, message = "A resposta deve ter no máximo 2000 caracteres")
    private String resposta;

    private Boolean ativa;

    public String getPergunta() {
        return pergunta;
    }

    public void setPergunta(String pergunta) {
        this.pergunta = pergunta;
    }

    public String getPalavrasChave() {
        return palavrasChave;
    }

    public void setPalavrasChave(String palavrasChave) {
        this.palavrasChave = palavrasChave;
    }

    public String getResposta() {
        return resposta;
    }

    public void setResposta(String resposta) {
        this.resposta = resposta;
    }

    public Boolean getAtiva() {
        return ativa;
    }

    public void setAtiva(Boolean ativa) {
        this.ativa = ativa;
    }
}
