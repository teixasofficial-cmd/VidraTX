package br.com.vidratx.security;

import br.com.vidratx.config.SecurityConfig;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public final class GeradorHashSenha {

    public static final String COMANDO = "gerar-hash-senha";

    private static final int MINIMO_CARACTERES = 8;

    private static final int MAXIMO_BYTES = 72;

    private GeradorHashSenha() {
    }

    public static int executar(InputStream entrada, PrintStream saida, PrintStream erro) {

        String senha;

        try {
            senha = new BufferedReader(new InputStreamReader(entrada, StandardCharsets.UTF_8)).readLine();
        } catch (IOException ex) {
            erro.println("Não foi possível ler a senha: " + ex.getMessage());
            return 1;
        }

        if (senha != null && senha.endsWith("\r")) {
            senha = senha.substring(0, senha.length() - 1);
        }

        if (senha == null || senha.length() < MINIMO_CARACTERES) {
            erro.println("A senha precisa ter pelo menos " + MINIMO_CARACTERES + " caracteres.");
            return 2;
        }

        if (senha.getBytes(StandardCharsets.UTF_8).length > MAXIMO_BYTES) {
            erro.println("A senha pode ter no máximo " + MAXIMO_BYTES + " bytes (limite do bcrypt).");
            return 2;
        }

        saida.println(new BCryptPasswordEncoder(SecurityConfig.CUSTO_BCRYPT).encode(senha));
        return 0;
    }
}
