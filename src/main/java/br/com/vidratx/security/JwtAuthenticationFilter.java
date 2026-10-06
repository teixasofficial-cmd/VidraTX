package br.com.vidratx.security;

import br.com.vidratx.entity.Usuario;
import br.com.vidratx.repository.UsuarioRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private static final Logger log =
            LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UsuarioRepository usuarioRepository) {

        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        try {

            autenticarSePossivel(request);

            filterChain.doFilter(request, response);

        } finally {

            SecurityContextHolder
                    .clearContext();
        }
    }

    private void autenticarSePossivel(
            HttpServletRequest request) {

        try {

            String authorization =
                    request.getHeader("Authorization");

            if (authorization == null
                    || !authorization.startsWith("Bearer ")) {

                return;
            }

            String token =
                    authorization
                            .substring(7)
                            .trim();

            if (token.isBlank()
                    || !jwtService.tokenValido(token)) {

                return;
            }

            Long usuarioId =
                    jwtService.extrairUsuarioId(token);

            Long empresaId =
                    jwtService.extrairEmpresaId(token);

            Claims claims =
                    jwtService.extrairClaims(token);

            String perfilToken =
                    claims.get("perfil", String.class);

            Usuario usuario =
                    usuarioRepository
                            .findByIdAndEmpresaId(
                                    usuarioId,
                                    empresaId
                            )
                            .orElse(null);

            if (usuario == null
                    || !Boolean.TRUE.equals(
                    usuario.getAtivo())
                    || usuario.getEmpresa() == null
                    || !Boolean.TRUE.equals(
                    usuario.getEmpresa().getAtiva())
                    || usuario.getPerfil() == null) {

                return;
            }

            String perfilBanco =
                    usuario.getPerfil().name();

            if (!perfilBanco.equals(perfilToken)) {

                return;
            }

            var autoridade =
                    new SimpleGrantedAuthority(
                            "ROLE_" + perfilBanco
                    );

            var authentication =
                    new UsernamePasswordAuthenticationToken(
                            usuario,
                            null,
                            List.of(autoridade)
                    );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(
                            authentication
                    );

        } catch (Exception ex) {

            log.debug(
                    "Requisição não autenticada: {} ({})",
                    ex.getMessage(),
                    ex.getClass().getSimpleName()
            );

            SecurityContextHolder
                    .clearContext();
        }
    }
}
