package br.com.vidratx.calculo;

public record AlertaCalculo(
        String codigo,
        String mensagem
) {

    public static final String REQUER_VIDRO_SEGURANCA = "REQUER_VIDRO_SEGURANCA";
    public static final String REQUER_LAMINADO = "REQUER_LAMINADO";
    public static final String PECA_EXCEDE_CHAPA = "PECA_EXCEDE_CHAPA";
    public static final String ABAIXO_MARGEM_MINIMA = "ABAIXO_MARGEM_MINIMA";
}
