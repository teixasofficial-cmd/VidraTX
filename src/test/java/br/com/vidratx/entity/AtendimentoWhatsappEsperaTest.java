package br.com.vidratx.entity;

import br.com.vidratx.enums.StatusAtendimento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AtendimentoWhatsappEsperaTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 9, 22, 15, 0);

    private AtendimentoWhatsapp atendimento;

    @BeforeEach
    void montar() {

        Empresa empresa = new Empresa();
        empresa.setPrazoRespostaAtendenteMinutos(30);

        atendimento = new AtendimentoWhatsapp();
        atendimento.setEmpresa(empresa);
        atendimento.setStatus(StatusAtendimento.AGUARDANDO_ATENDENTE);
        atendimento.setUltimaMensagemEmpresaEm(AGORA.minusHours(2));
    }

    private void clienteEscreveu(LocalDateTime quando) {

        if (!atendimento.clienteAguardandoResposta()) {
            atendimento.setClienteAguardandoDesde(quando);
        }

        atendimento.setUltimaMensagemClienteEm(quando);
    }

    @Test
    void esperaContaDaPrimeiraMensagemSemResposta() {

        clienteEscreveu(AGORA.minusMinutes(50));
        clienteEscreveu(AGORA.minusMinutes(25));
        clienteEscreveu(AGORA.minusMinutes(5));

        assertEquals(50, atendimento.minutosEsperandoResposta(AGORA));
        assertTrue(atendimento.respostaAtrasada(AGORA));
    }

    @Test
    void dentroDoPrazoNaoEstaAtrasada() {

        clienteEscreveu(AGORA.minusMinutes(29));

        assertEquals(29, atendimento.minutosEsperandoResposta(AGORA));
        assertFalse(atendimento.respostaAtrasada(AGORA));
    }

    @Test
    void prazoDaEmpresaValeNoCalculo() {

        atendimento.getEmpresa().setPrazoRespostaAtendenteMinutos(10);
        clienteEscreveu(AGORA.minusMinutes(12));

        assertTrue(atendimento.respostaAtrasada(AGORA));
    }

    @Test
    void respostaDaEmpresaEncerraAEspera() {

        clienteEscreveu(AGORA.minusMinutes(50));
        atendimento.setUltimaMensagemEmpresaEm(AGORA.minusMinutes(40));

        assertEquals(0, atendimento.minutosEsperandoResposta(AGORA));
        assertFalse(atendimento.respostaAtrasada(AGORA));

        clienteEscreveu(AGORA.minusMinutes(3));

        assertEquals(3, atendimento.minutosEsperandoResposta(AGORA));
    }

    @Test
    void comOBotNaoHaEsperaPorPessoa() {

        atendimento.setStatus(StatusAtendimento.EM_FLUXO_BOT);
        clienteEscreveu(AGORA.minusMinutes(50));

        assertEquals(0, atendimento.minutosEsperandoResposta(AGORA));
        assertFalse(atendimento.respostaAtrasada(AGORA));
    }

    @Test
    void avisoDeEsperaUmaVezPorEspera() {

        clienteEscreveu(AGORA.minusMinutes(50));
        assertFalse(atendimento.avisoDeEsperaJaEnviado());

        atendimento.setAvisoEsperaEnviadoEm(AGORA.minusMinutes(15));
        clienteEscreveu(AGORA.minusMinutes(5));

        assertTrue(atendimento.avisoDeEsperaJaEnviado());

        atendimento.setUltimaMensagemEmpresaEm(AGORA.minusMinutes(4));
        clienteEscreveu(AGORA.minusMinutes(2));

        assertFalse(atendimento.avisoDeEsperaJaEnviado());
    }

    @Test
    void conversaAntigaSemInicioDaEsperaUsaAUltimaMensagem() {

        atendimento.setUltimaMensagemClienteEm(AGORA.minusMinutes(40));

        assertEquals(40, atendimento.minutosEsperandoResposta(AGORA));
    }
}
