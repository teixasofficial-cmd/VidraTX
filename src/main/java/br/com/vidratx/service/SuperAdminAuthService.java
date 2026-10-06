package br.com.vidratx.service;

import br.com.vidratx.dto.SuperAdminLoginRequest;
import br.com.vidratx.dto.SuperAdminLoginResponse;
import br.com.vidratx.dto.TrocarSenhaRequest;
import br.com.vidratx.entity.AdministradorGlobal;
import br.com.vidratx.exception.CredenciaisInvalidasException;
import br.com.vidratx.exception.MuitasTentativasException;
import br.com.vidratx.repository.AdministradorGlobalRepository;
import br.com.vidratx.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SuperAdminAuthService {

    private static final int MAX_TENTATIVAS_FALHAS = 5;
    private static final Duration JANELA_BLOQUEIO = Duration.ofMinutes(15);

    private final AdministradorGlobalRepository administradorGlobalRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private final ConcurrentHashMap<String, RegistroTentativas> tentativasLogin =
            new ConcurrentHashMap<>();

    public SuperAdminAuthService(
            AdministradorGlobalRepository administradorGlobalRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.administradorGlobalRepository = administradorGlobalRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public SuperAdminLoginResponse login(SuperAdminLoginRequest request) {

        String email = normalizarEmail(request.getEmail());

        verificarBloqueio(email);

        try {

            SuperAdminLoginResponse response = autenticar(request, email);

            tentativasLogin.remove(email);

            return response;

        } catch (CredenciaisInvalidasException ex) {

            registrarFalha(email);

            throw ex;
        }
    }

    private SuperAdminLoginResponse autenticar(
            SuperAdminLoginRequest request,
            String email) {

        AdministradorGlobal administrador =
                administradorGlobalRepository
                        .findByEmailAndAtivoTrue(email)
                        .orElseThrow(() ->
                                new CredenciaisInvalidasException(
                                        "E-mail ou senha inválidos"
                                )
                        );

        if (!passwordEncoder.matches(request.getSenha(), administrador.getSenha())) {

            throw new CredenciaisInvalidasException(
                    "E-mail ou senha inválidos"
            );
        }

        String token = jwtService.gerarTokenSuperAdmin(administrador);

        SuperAdminLoginResponse response = new SuperAdminLoginResponse();

        response.setToken(token);
        response.setTipo("Bearer");
        response.setAdministradorId(administrador.getId());
        response.setEmail(administrador.getEmail());

        return response;
    }

    @Transactional
    public void trocarSenha(
            Long administradorId,
            TrocarSenhaRequest request) {

        AdministradorGlobal administrador =
                administradorGlobalRepository
                        .findByIdAndAtivoTrue(administradorId)
                        .orElseThrow(() ->
                                new CredenciaisInvalidasException(
                                        "Administrador não encontrado"
                                )
                        );

        if (!passwordEncoder.matches(
                request.getSenhaAtual(),
                administrador.getSenha()
        )) {

            throw new CredenciaisInvalidasException(
                    "Senha atual incorreta"
            );
        }

        administrador.setSenha(
                passwordEncoder.encode(
                        request.getNovaSenha()
                )
        );

        administradorGlobalRepository.save(administrador);
    }

    private String normalizarEmail(String email) {

        if (email == null) {
            return null;
        }

        return email.trim().toLowerCase();
    }

    private void verificarBloqueio(String chave) {

        RegistroTentativas registro = tentativasLogin.get(chave);

        if (registro != null
                && registro.desbloqueiaEm != null
                && Instant.now().isBefore(registro.desbloqueiaEm)) {

            throw new MuitasTentativasException(
                    "Muitas tentativas de login. Tente novamente em alguns minutos."
            );
        }
    }

    private void registrarFalha(String chave) {

        tentativasLogin.compute(chave, (chaveIgnorada, registro) -> {

            RegistroTentativas atual =
                    registro != null ? registro : new RegistroTentativas();

            atual.falhas++;

            if (atual.falhas >= MAX_TENTATIVAS_FALHAS) {

                atual.desbloqueiaEm = Instant.now().plus(JANELA_BLOQUEIO);
                atual.falhas = 0;
            }

            return atual;
        });
    }

    private static final class RegistroTentativas {

        private int falhas;
        private Instant desbloqueiaEm;
    }
}
