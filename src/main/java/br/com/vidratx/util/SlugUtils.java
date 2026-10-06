package br.com.vidratx.util;

import java.text.Normalizer;
import java.util.Set;
import java.util.regex.Pattern;

public final class SlugUtils {

    private static final Pattern FORMATO_VALIDO =
            Pattern.compile("^[a-z0-9](?:[a-z0-9-]{1,58}[a-z0-9])?$");

    private static final Set<String> RESERVADOS = Set.of(
            "www", "api", "app", "admin", "painel",
            "auth", "public", "static", "assets",
            "mail", "ftp", "blog", "suporte", "help",
            "vidratx", "sistema", "cdn", "docs", "status"
    );

    private SlugUtils() {
    }

    public static String gerar(String textoBase) {

        if (textoBase == null || textoBase.isBlank()) {

            throw new IllegalArgumentException(
                    "Não é possível gerar um identificador a partir de um texto vazio"
            );
        }

        String semAcento =
                Normalizer
                        .normalize(textoBase, Normalizer.Form.NFD)
                        .replaceAll("\\p{M}", "");

        String slug = semAcento
                .toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("[\\s-]+", "-")
                .replaceAll("^-|-$", "");

        if (slug.length() > 60) {

            slug = slug
                    .substring(0, 60)
                    .replaceAll("-$", "");
        }

        if (slug.isBlank()) {

            throw new IllegalArgumentException(
                    "Não foi possível gerar um identificador válido a partir do nome informado"
            );
        }

        return slug;
    }

    public static boolean formatoValido(String slug) {

        return slug != null
                && FORMATO_VALIDO.matcher(slug).matches();
    }

    public static boolean reservado(String slug) {

        return slug != null
                && RESERVADOS.contains(slug.toLowerCase());
    }
}
