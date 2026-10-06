package br.com.vidratx.conversa;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InterpretadorRespostaTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "sim", "Sim!", "Sim.", "SIM", "ok", "Ok!", "pode ser", "Pode sim", "👍", "✅", "combinado",
            "fechado", "sim, pode ser", "confirmo", "pode vir", "tá bom", "beleza", "simmm", "Sim, obrigado!",
            "perfeito, pode marcar", "sem problema"
    })
    void variacoesComunsDeSimSaoConfirmacao(String texto) {

        Interpretacao i = InterpretadorResposta.interpretar(texto);

        assertTrue(i.confirmacao(), texto);
        assertFalse(i.negacao(), texto);
    }

    @ParameterizedTest
    @ValueSource(strings = {"não aceito", "não aprovo", "não confirmo", "nao aceito", "Não, não aprovo"})
    void negacaoAntesDaPalavraPositivaNuncaEhAceite(String texto) {

        Interpretacao i = InterpretadorResposta.interpretar(texto);

        assertFalse(i.confirmacao(), texto);
        assertTrue(i.negacao(), texto);
        assertTrue(i.recusaSemData(), texto);
    }

    @ParameterizedTest
    @ValueSource(strings = {"não posso", "Não.", "nao da", "não consigo nesse dia", "esse dia não", "👎", "não, obrigado",
            "não tenho interesse", "agora não"})
    void recusasComunsSaoRecusaSemData(String texto) {

        Interpretacao i = InterpretadorResposta.interpretar(texto);

        assertTrue(i.recusaSemData(), texto);
        assertFalse(i.confirmacao(), texto);
    }

    @Test
    void perguntaForaDeContextoNaoEhDecisao() {

        Interpretacao i = InterpretadorResposta.interpretar("qual o valor do box?");

        assertTrue(i.pergunta());
        assertFalse(i.confirmacao());
        assertFalse(i.negacao());
        assertFalse(i.contemData());
    }

    @ParameterizedTest
    @ValueSource(strings = {"tenho garagem sim, pode vir de carro", "sim, mas quero mudar o endereço",
            "tá, vou ver com meu marido", "não entendi o valor"})
    void frasesAmbiguasNaoSaoDecisao(String texto) {

        Interpretacao i = InterpretadorResposta.interpretar(texto);

        assertFalse(i.confirmacao(), texto);
        assertFalse(i.recusaSemData(), texto);
    }

    @ParameterizedTest
    @ValueSource(strings = {"não sei", "vou ver", "talvez", "deixa eu ver com minha esposa"})
    void incertezaNaoEhRecusa(String texto) {

        Interpretacao i = InterpretadorResposta.interpretar(texto);

        assertTrue(i.incerteza(), texto);
        assertFalse(i.negacao(), texto);
        assertFalse(i.confirmacao(), texto);
    }

    @ParameterizedTest
    @ValueSource(strings = {"dia 20 às 10h", "pode ser sexta às 14h?", "12/10", "amanhã de tarde", "não posso, só dia 20",
            "semana que vem", "15:30"})
    void reconheceSugestaoDeData(String texto) {
        assertTrue(InterpretadorResposta.interpretar(texto).contemData(), texto);
    }

    @ParameterizedTest
    @ValueSource(strings = {"9", "quero falar com alguém", "atendente por favor", "me liga", "falar com uma pessoa"})
    void reconhecePedidoDeAtendente(String texto) {
        assertTrue(InterpretadorResposta.interpretar(texto).pedeAtendente(), texto);
    }

    @ParameterizedTest
    @ValueSource(strings = {"preciso mudar a data da medição", "dá pra remarcar?", "surgiu um imprevisto",
            "quero outro dia"})
    void reconhecePedidoDeRemarcar(String texto) {

        Interpretacao i = InterpretadorResposta.interpretar(texto);

        assertTrue(i.pedeRemarcar(), texto);
        assertFalse(i.confirmacao(), texto);
    }

    @Test
    void opcoesNumeradas() {

        assertEquals(1, InterpretadorResposta.interpretar("1").opcao());
        assertEquals(2, InterpretadorResposta.interpretar("opção 2").opcao());
        assertEquals(3, InterpretadorResposta.interpretar(" 3. ").opcao());
        assertEquals(null, InterpretadorResposta.interpretar("12").opcao());
    }

    @Test
    void pedidoDeDescontoEhAlteracao() {

        Interpretacao i = InterpretadorResposta.interpretar("consegue fazer um desconto?");

        assertTrue(i.pedeAlteracao());
        assertFalse(i.confirmacao());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Sem dúvida não vou fechar com esse preço",
            "Sem problema, mas nesse dia não vou estar em casa",
            "Não tem problema, mas não vou aprovar agora"
    })
    void expressaoPositivaNaoAnulaNegacao(String texto) {

        Interpretacao i = InterpretadorResposta.interpretar(texto);

        assertFalse(i.confirmacao(), texto);
        assertTrue(i.negacao(), texto);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Sem problema, vou pensar e te falo",
            "Sem problema eu pagar metade na entrega?",
            "sem dúvida, só preciso ver com meu marido",
            "sem problema, vou falar com a minha esposa"
    })
    void expressaoPositivaNaoAnulaDuvidaNemPergunta(String texto) {
        assertFalse(InterpretadorResposta.interpretar(texto).confirmacao(), texto);
    }

    @ParameterizedTest
    @ValueSource(strings = {"sem problemas", "Sem dúvida!", "não tem problema, pode vir", "sem problema, pode marcar",
            "aprovo o orçamento", "pode fazer", "pode seguir com o serviço"})
    void expressoesPositivasContinuamConfirmando(String texto) {

        Interpretacao i = InterpretadorResposta.interpretar(texto);

        assertTrue(i.confirmacao(), texto);
        assertFalse(i.negacao(), texto);
    }

    @ParameterizedTest
    @ValueSource(strings = {"agora não", "não por enquanto", "ainda não", "no momento não", "mais pra frente"})
    void adiamentoEhSinalizado(String texto) {

        Interpretacao i = InterpretadorResposta.interpretar(texto);

        assertTrue(i.adiamento(), texto);
        assertFalse(i.confirmacao(), texto);
    }

    @ParameterizedTest
    @ValueSource(strings = {"não", "não tenho interesse", "não, obrigado", "4"})
    void recusaDefinitivaNaoEhAdiamento(String texto) {
        assertFalse(InterpretadorResposta.interpretar(texto).adiamento(), texto);
    }

    @Test
    void numeroAbreviadoNaoEhNegacao() {

        Interpretacao aprovo = InterpretadorResposta.interpretar("aprovo o orçamento nº 553");

        assertFalse(aprovo.negacao());
        assertFalse(aprovo.negacaoClara());
        assertFalse(aprovo.confirmacao());
        assertEquals(2, InterpretadorResposta.interpretar("nº 2").opcao());
        assertEquals(2, InterpretadorResposta.interpretar("N° 2").opcao());

        Interpretacao data = InterpretadorResposta.interpretar("posso no 15/10 às 14h");

        assertTrue(data.contemData());
        assertFalse(data.negacao());
    }
}
