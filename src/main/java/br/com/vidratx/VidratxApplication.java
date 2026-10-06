package br.com.vidratx;

import br.com.vidratx.config.TempoConfig;
import br.com.vidratx.security.GeradorHashSenha;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VidratxApplication {

    static {
        TempoConfig.fixarFusoPadrao();
    }

    public static void main(String[] args) {

        if (args.length > 0 && GeradorHashSenha.COMANDO.equals(args[0])) {
            System.exit(GeradorHashSenha.executar(System.in, System.out, System.err));
        }

        SpringApplication.run(VidratxApplication.class, args);
    }

}
