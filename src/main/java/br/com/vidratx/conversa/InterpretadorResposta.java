package br.com.vidratx.conversa;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class InterpretadorResposta {

    private static final Map<String, String> EMOJIS = Map.ofEntries(
            Map.entry("👍", " joinha "),
            Map.entry("👌", " joinha "),
            Map.entry("✅", " joinha "),
            Map.entry("✔", " joinha "),
            Map.entry("🙏", " obrigado "),
            Map.entry("👎", " nao "),
            Map.entry("❌", " nao "),
            Map.entry("🚫", " nao ")
    );

    private static final Set<String> POSITIVAS = Set.of(
            "sim", "s", "ss", "sss", "simm", "yes", "pode", "ok", "okay", "oks", "okk", "okey",
            "blz", "beleza", "combinado", "combinamos", "fechado", "fechou", "confirmo",
            "confirmado", "confirmada", "confirmar", "aceito", "aceita", "aceitamos", "aprovo",
            "aprovado", "aprovada", "certo", "certinho", "perfeito", "otimo", "show", "claro",
            "isso", "bora", "top", "bom", "joia", "joinha", "positivo", "correto", "exato",
            "maravilha", "excelente", "tranquilo", "demais", "manda", "marcado", "agendado", "bem"
    );

    private static final Set<String> NEUTRAS = Set.of(
            "ser", "vir", "marcar", "fechar", "agendar", "entao", "por", "favor", "obrigado",
            "obrigada", "valeu", "brigado", "brigada", "muito", "ta", "esta", "e", "eu", "a", "o",
            "de", "pra", "para", "com", "certeza", "mesmo", "pode", "sim", "ai", "la", "gente",
            "amigo", "amiga", "moco", "moca", "senhor", "senhora", "dona", "aqui", "tudo",
            "pois", "ja", "fica", "ficou", "assim", "vamos", "vou", "estarei", "estaremos", "em",
            "casa", "voces", "voce", "ne", "hein", "entendi", "perfeitamente", "que", "legal",
            "orcamento", "fazer", "seguir", "servico", "numero"
    );

    private static final Set<String> NEGACOES = Set.of(
            "nao", "n", "nn", "nop", "nope", "negativo", "nunca", "jamais", "nem", "recuso",
            "recusado", "recusar", "cancela", "cancelar", "cancelado", "desisto", "desistir"
    );

    private static final Set<String> COMPLEMENTOS_RECUSA = Set.of(
            "posso", "pode", "consigo", "da", "dar", "vai", "quero", "queremos", "tenho", "temos",
            "interesse", "esse", "essa", "nesse", "nessa", "neste", "nesta", "dia", "data", "horario",
            "hora", "agora", "momento", "enquanto", "mais", "preciso", "precisa", "fazer", "seguir",
            "fechar", "orcamento", "servico", "gostei", "rola", "mim", "no", "na", "nao", "vou", "obg",
            "desculpa", "desculpe", "infelizmente", "aceitar", "aprovar", "confirmar", "isso", "pro", "prefiro",
            "melhor", "deixa", "deixar", "pra", "la", "ok", "por", "hoje", "valeu", "certo"
    );

    private static final List<Pattern> IDIOMAS_POSITIVOS = Stream.of(
                    "sem problema", "nao tem problema", "nao ha problema", "nao tem erro", "nao vejo problema",
                    "nao tem como nao", "sem duvida")
            .map(idioma -> Pattern.compile("\\b" + idioma + "s?\\b"))
            .toList();

    private static final List<String> INCERTEZAS = List.of(
            "nao sei", "sei la", "talvez", "vou ver", "vou verificar", "depois te falo", "depois eu vejo",
            "deixa eu ver", "nao tenho certeza", "vou confirmar", "preciso ver", "preciso verificar",
            "vou pensar", "nao decidi", "vou falar com", "vou conversar com", "vou consultar", "vou perguntar",
            "preciso falar com", "preciso conversar com"
    );

    private static final List<String> ADIAMENTOS = List.of(
            "agora nao", "nao agora", "por enquanto", "no momento nao", "nao no momento", "ainda nao",
            "mais pra frente", "mais para frente", "mais tarde", "outra hora", "outro momento"
    );

    private static final Set<String> CORTESIAS = Set.of(
            "obrigado", "obrigada", "valeu", "brigado", "brigada", "grato", "grata", "agradeco",
            "ok", "okay", "blz", "beleza", "show", "top", "joinha", "tmj", "certo", "entendi",
            "muito", "de", "nada", "por", "favor", "bom", "dia", "tarde", "noite", "ta", "tudo", "bem"
    );

    private static final List<String> SAUDACOES = List.of(
            "oi", "ola", "bom dia", "boa tarde", "boa noite", "e ai", "eai", "opa", "alo", "hey", "oie", "oii"
    );

    private static final List<String> PEDIDOS_ATENDENTE = List.of(
            "atendente", "humano", "falar com alguem", "falar com uma pessoa", "falar com pessoa",
            "falar com voce", "falar com voces", "pessoa de verdade", "me liga", "me ligue", "ligar pra mim",
            "ligar para mim", "falar com o dono", "falar com a dona", "falar com o responsavel",
            "falar com vendedor", "falar com um vendedor", "quero atendimento"
    );

    private static final List<String> PEDIDOS_REMARCAR = List.of(
            "remarc", "reagend", "mudar a data", "mudar o dia", "mudar o horario", "mudar de data",
            "mudar de dia", "mudar de horario", "trocar a data", "trocar o dia", "trocar o horario",
            "outro dia", "outra data", "outro horario", "adiar", "desmarc", "nao vou poder", "nao vou conseguir",
            "surgiu um imprevisto", "tive um imprevisto"
    );

    private static final List<String> PEDIDOS_ALTERACAO = List.of(
            "desconto", "mais barato", "baixar", "abaixar", "muito caro", "ta caro", "esta caro", "caro demais",
            "alterar", "mudar o orcamento", "mudar o valor", "parcel", "negoci", "revisar", "rever o valor",
            "outro valor", "melhorar o valor", "melhor preco", "melhorar o preco", "fazer por", "faz por"
    );

    private static final Pattern DATA = Pattern.compile(
            "\\b\\d{1,2}/\\d{1,2}(/\\d{2,4})?\\b"
                    + "|\\bdia\\s+\\d{1,2}\\b"
                    + "|\\b\\d{1,2}\\s*(h|hs|hr|hrs|horas?)\\b"
                    + "|\\b\\d{1,2}h\\d{2}\\b"
                    + "|\\b\\d{1,2}:\\d{2}\\b"
                    + "|\\b(segunda|terca|quarta|quinta|sexta|sabado|domingo)\\b"
                    + "|\\bamanha\\b|\\bsemana que vem\\b|\\bproxima semana\\b|\\bfim de semana\\b"
                    + "|\\bfinal de semana\\b|\\bmeio dia\\b|\\bmeio-dia\\b"
                    + "|\\bde manha\\b|\\bpela manha\\b|\\ba tarde\\b|\\bpela tarde\\b|\\bde tarde\\b|\\ba noite\\b"
                    + "|\\bdepois das\\s+\\d{1,2}\\b|\\bantes das\\s+\\d{1,2}\\b|\\bapos as\\s+\\d{1,2}\\b"
    );

    private static final Pattern NUMERO_ABREVIADO = Pattern.compile("(?i)\\bn\\.?\\s*[º°]\\.?");

    private static final Pattern OPCAO = Pattern.compile(
            "^(?:opcao|opção|numero|número|op)?\\s*([0-9])\\s*[.)\\-]?$"
    );

    private InterpretadorResposta() {
    }

    public static Interpretacao interpretar(String textoOriginal) {

        String texto = textoOriginal == null ? "" : textoOriginal.trim();
        String leve = normalizarLeve(texto);
        String palavras = normalizarPalavras(leve);

        String semIdiomas = palavras;

        for (Pattern idioma : IDIOMAS_POSITIVOS) {
            semIdiomas = idioma.matcher(semIdiomas).replaceAll("sim");
        }

        List<String> tokens = semIdiomas.isEmpty() ? List.of() : Arrays.asList(semIdiomas.split(" "));

        Integer opcao = extrairOpcao(leve);
        boolean pergunta = texto.contains("?");
        boolean incerteza = INCERTEZAS.stream().anyMatch(palavras::contains);
        boolean adiamento = ADIAMENTOS.stream().anyMatch(palavras::contains);
        boolean negacao = !incerteza && tokens.stream().anyMatch(NEGACOES::contains);
        boolean negacaoClara = negacao
                && !pergunta
                && tokens.stream().allMatch(t -> NEGACOES.contains(t) || NEUTRAS.contains(t)
                        || POSITIVAS.contains(t) || COMPLEMENTOS_RECUSA.contains(t) || CORTESIAS.contains(t));
        boolean contemData = DATA.matcher(leve).find();
        boolean pedeAtendente = opcao != null && opcao == 9
                || PEDIDOS_ATENDENTE.stream().anyMatch(palavras::contains);
        boolean pedeRemarcar = PEDIDOS_REMARCAR.stream().anyMatch(palavras::contains);
        boolean pedeAlteracao = PEDIDOS_ALTERACAO.stream().anyMatch(palavras::contains);

        boolean positiva = !tokens.isEmpty()
                && !negacao
                && !incerteza
                && !pergunta
                && tokens.stream().allMatch(t -> POSITIVAS.contains(t) || NEUTRAS.contains(t))
                && tokens.stream().anyMatch(POSITIVAS::contains);

        boolean confirmacao = positiva && !adiamento && !pedeRemarcar && !pedeAlteracao && !pedeAtendente;

        boolean cortesia = !tokens.isEmpty()
                && !negacao
                && tokens.stream().allMatch(CORTESIAS::contains)
                && tokens.stream().noneMatch(t -> t.equals("bom") || t.equals("dia") || t.equals("tarde") || t.equals("noite"))
                || tokens.size() <= 3 && tokens.stream().anyMatch(t -> t.startsWith("obrigad") || t.equals("valeu"));

        boolean saudacao = tokens.size() <= 4 && SAUDACOES.stream().anyMatch(s ->
                palavras.equals(s) || palavras.startsWith(s + " "));

        return new Interpretacao(
                texto, palavras, opcao, confirmacao, negacao, negacaoClara, contemData, pedeAtendente,
                pedeRemarcar, pedeAlteracao, cortesia && !confirmacao, saudacao, pergunta, incerteza, adiamento
        );
    }

    public static String palavras(String texto) {
        return texto == null ? "" : normalizarPalavras(normalizarLeve(texto));
    }

    static String normalizarLeve(String texto) {

        String comEmojis = texto;

        for (Map.Entry<String, String> emoji : EMOJIS.entrySet()) {
            comEmojis = comEmojis.replace(emoji.getKey(), emoji.getValue());
        }

        comEmojis = NUMERO_ABREVIADO.matcher(comEmojis).replaceAll(" numero ");

        String semAcento = Normalizer.normalize(comEmojis, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        return semAcento.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").trim();
    }

    static String normalizarPalavras(String leve) {

        String limpo = leve.replaceAll("[^a-z0-9 ]", " ").replaceAll("\\s+", " ").trim();

        if (limpo.isEmpty()) {
            return limpo;
        }

        StringBuilder resultado = new StringBuilder();

        for (String token : limpo.split(" ")) {

            String reduzido = token.replaceAll("([a-z])\\1{2,}$", "$1");

            if (!resultado.isEmpty()) {
                resultado.append(' ');
            }

            resultado.append(reduzido);
        }

        return resultado.toString();
    }

    private static Integer extrairOpcao(String leve) {

        Matcher matcher = OPCAO.matcher(leve.replaceAll("[!*]", "").trim());

        return matcher.matches() ? Integer.valueOf(matcher.group(1)) : null;
    }
}
