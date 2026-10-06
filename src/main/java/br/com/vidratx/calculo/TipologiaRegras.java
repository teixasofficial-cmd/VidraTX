package br.com.vidratx.calculo;

import java.util.List;

public record TipologiaRegras(
        int numeroFolhas,
        FormulaPecas formulaPecas,
        int descontoLarguraMm,
        int descontoAlturaMm,
        int transpasseMm,
        List<String> alertasNormativos
) {

    public TipologiaRegras {
        alertasNormativos = alertasNormativos == null ? List.of() : List.copyOf(alertasNormativos);
    }
}
