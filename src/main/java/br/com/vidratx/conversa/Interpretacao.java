package br.com.vidratx.conversa;

public record Interpretacao(
        String textoOriginal,
        String textoNormalizado,
        Integer opcao,
        boolean confirmacao,
        boolean negacao,
        boolean negacaoClara,
        boolean contemData,
        boolean pedeAtendente,
        boolean pedeRemarcar,
        boolean pedeAlteracao,
        boolean cortesia,
        boolean saudacao,
        boolean pergunta,
        boolean incerteza,
        boolean adiamento) {

    public boolean opcao(int numero) {
        return opcao != null && opcao == numero;
    }

    public boolean recusaSemData() {
        return negacaoClara && !contemData;
    }

    public boolean vazia() {
        return textoNormalizado == null || textoNormalizado.isBlank();
    }
}
