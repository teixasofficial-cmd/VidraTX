package br.com.vidratx.security;

import br.com.vidratx.entity.AdministradorGlobal;
import br.com.vidratx.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private static final Logger log =
            LoggerFactory.getLogger(JwtService.class);

    private final SecretKey chave;
    private final Duration expiracao;

    public JwtService(
            @Value("${vidratx.jwt.secret}") String secret,
            @Value("${vidratx.jwt.expiration}") Duration expiracao) {

        if (secret == null || secret.isBlank()) {

            throw new IllegalArgumentException(
                    "vidratx.jwt.secret não configurado"
            );
        }

        byte[] secretBytes =
                secret.getBytes(StandardCharsets.UTF_8);

        if (secretBytes.length < 32) {

            throw new IllegalArgumentException(
                    "A chave JWT deve possuir pelo menos 32 bytes"
            );
        }

        this.chave =
                Keys.hmacShaKeyFor(secretBytes);

        if (expiracao == null
                || expiracao.isZero()
                || expiracao.isNegative()) {

            throw new IllegalArgumentException(
                    "A expiração do JWT deve ser maior que zero"
            );
        }

        this.expiracao = expiracao;
    }

    public String gerarToken(Usuario usuario) {

        Instant agora = Instant.now();

        return Jwts.builder()

                .subject(
                        usuario.getId().toString()
                )

                .claim(
                        "empresaId",
                        usuario.getEmpresa().getId()
                )

                .claim(
                        "perfil",
                        usuario.getPerfil().name()
                )

                .issuedAt(
                        Date.from(agora)
                )

                .expiration(
                        Date.from(
                                agora.plus(expiracao)
                        )
                )

                .signWith(chave)

                .compact();
    }

    public String gerarTokenSuperAdmin(AdministradorGlobal administrador) {

        Instant agora = Instant.now();

        return Jwts.builder()

                .subject(
                        administrador.getId().toString()
                )

                .claim(
                        "tipo",
                        "SUPER_ADMIN"
                )

                .issuedAt(
                        Date.from(agora)
                )

                .expiration(
                        Date.from(
                                agora.plus(expiracao)
                        )
                )

                .signWith(chave)

                .compact();
    }

    public boolean isTokenSuperAdmin(String token) {

        return "SUPER_ADMIN".equals(
                extrairClaims(token).get("tipo", String.class)
        );
    }

    public Claims extrairClaims(String token) {

        return Jwts.parser()
                .verifyWith(chave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long extrairUsuarioId(String token) {

        String subject =
                extrairClaims(token).getSubject();

        if (subject == null || subject.isBlank()) {

            throw new IllegalArgumentException(
                    "Token sem identificação do usuário"
            );
        }

        return Long.valueOf(subject);
    }

    public Long extrairEmpresaId(String token) {

        Number empresaId =
                extrairClaims(token)
                        .get("empresaId", Number.class);

        if (empresaId == null
                || empresaId.longValue() <= 0) {

            throw new IllegalArgumentException(
                    "Token sem identificação da empresa"
            );
        }

        return empresaId.longValue();
    }

    public boolean tokenValido(String token) {

        try {

            Claims claims =
                    extrairClaims(token);

            Date expiracaoToken =
                    claims.getExpiration();

            return expiracaoToken != null
                    && expiracaoToken.after(
                    new Date()
            );

        } catch (Exception ex) {

            log.debug(
                    "Token JWT inválido: {} ({})",
                    ex.getMessage(),
                    ex.getClass().getSimpleName()
            );

            return false;
        }
    }
}
