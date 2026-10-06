package br.com.vidratx.security;

import br.com.vidratx.entity.AdministradorGlobal;
import br.com.vidratx.repository.AdministradorGlobalRepository;
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
public class SuperAdminAuthenticationFilter
        extends OncePerRequestFilter {

    private static final Logger log =
            LoggerFactory.getLogger(SuperAdminAuthenticationFilter.class);

    private final JwtService jwtService;
    private final AdministradorGlobalRepository administradorGlobalRepository;

    public SuperAdminAuthenticationFilter(
            JwtService jwtService,
            AdministradorGlobalRepository administradorGlobalRepository) {

        this.jwtService = jwtService;
        this.administradorGlobalRepository = administradorGlobalRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        autenticarSePossivel(request);

        filterChain.doFilter(request, response);
    }

    private void autenticarSePossivel(HttpServletRequest request) {

        try {

            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                return;
            }

            String authorization = request.getHeader("Authorization");

            if (authorization == null || !authorization.startsWith("Bearer ")) {
                return;
            }

            String token = authorization.substring(7).trim();

            if (token.isBlank()
                    || !jwtService.tokenValido(token)
                    || !jwtService.isTokenSuperAdmin(token)) {

                return;
            }

            Long administradorId =
                    Long.valueOf(jwtService.extrairClaims(token).getSubject());

            AdministradorGlobal administrador =
                    administradorGlobalRepository
                            .findByIdAndAtivoTrue(administradorId)
                            .orElse(null);

            if (administrador == null) {
                return;
            }

            var autoridade = new SimpleGrantedAuthority("ROLE_SUPER_ADMIN");

            var authentication =
                    new UsernamePasswordAuthenticationToken(
                            administrador, null, List.of(autoridade)
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception ex) {

            log.debug(
                    "Requisição de super admin não autenticada: {} ({})",
                    ex.getMessage(),
                    ex.getClass().getSimpleName()
            );

            SecurityContextHolder.clearContext();
        }
    }
}
