package br.com.vidratx.service;

import br.com.vidratx.dto.LoginRequest;
import br.com.vidratx.dto.LoginResponse;
import br.com.vidratx.entity.Empresa;
import br.com.vidratx.entity.Usuario;
import br.com.vidratx.exception.CredenciaisInvalidasException;
import br.com.vidratx.exception.EmpresaInativaException;
import br.com.vidratx.exception.MuitasTentativasException;
import br.com.vidratx.exception.UsuarioInativoException;
import br.com.vidratx.repository.EmpresaRepository;
import br.com.vidratx.repository.UsuarioRepository;
import br.com.vidratx.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private static final int MAX_TENTATIVAS_FALHAS = 5;
    private static final Duration JANELA_BLOQUEIO = Duration.ofMinutes(15);

    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private final ConcurrentHashMap<String, RegistroTentativas> tentativasLogin =
            new ConcurrentHashMap<>();

    public AuthService(
            EmpresaRepository empresaRepository,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.empresaRepository = empresaRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(
            LoginRequest request) {

        String slug =
                normalizarSlug(
                        request.getEmpresaSlug()
                );

        String email =
                normalizarEmail(
                        request.getEmail()
                );

        String chaveTentativas = slug + "|" + email;

        verificarBloqueio(chaveTentativas);

        try {

            LoginResponse response =
                    autenticar(request, slug, email);

            tentativasLogin.remove(chaveTentativas);

            return response;

        } catch (CredenciaisInvalidasException ex) {

            registrarFalha(chaveTentativas);

            throw ex;
        }
    }

    private LoginResponse autenticar(
            LoginRequest request,
            String slug,
            String email) {

        Empresa empresa =
                empresaRepository
                        .findBySlug(slug)
                        .orElseThrow(() ->
                                new CredenciaisInvalidasException(
                                        "E-mail ou senha inválidos"
                                )
                        );

        if (!Boolean.TRUE.equals(empresa.getAtiva())) {

            throw new EmpresaInativaException(
                    "A empresa está inativa"
            );
        }

        Usuario usuario =
                usuarioRepository
                        .findByEmpresaIdAndEmail(
                                empresa.getId(),
                                email
                        )
                        .orElseThrow(() ->
                                new CredenciaisInvalidasException(
                                        "E-mail ou senha inválidos"
                                )
                        );

        if (!Boolean.TRUE.equals(
                usuario.getAtivo())) {

            throw new UsuarioInativoException(
                    "O usuário está inativo"
            );
        }

        if (!passwordEncoder.matches(
                request.getSenha(),
                usuario.getSenha()
        )) {

            throw new CredenciaisInvalidasException(
                    "E-mail ou senha inválidos"
            );
        }

        return montarResposta(usuario, empresa);
    }

    @Transactional(readOnly = true)
    public LoginResponse renovar(Long usuarioId) {

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new CredenciaisInvalidasException("Sessão inválida"));

        if (!Boolean.TRUE.equals(usuario.getAtivo()) || !Boolean.TRUE.equals(usuario.getEmpresa().getAtiva())) {
            throw new CredenciaisInvalidasException("Sessão inválida");
        }

        return montarResposta(usuario, usuario.getEmpresa());
    }

    private LoginResponse montarResposta(Usuario usuario, Empresa empresa) {

        String token =
                jwtService.gerarToken(usuario);

        LoginResponse response =
                new LoginResponse();

        response.setToken(token);
        response.setTipo("Bearer");

        response.setUsuarioId(
                usuario.getId()
        );

        response.setEmpresaId(
                empresa.getId()
        );

        response.setEmpresaSlug(
                empresa.getSlug()
        );

        response.setEmpresaNomeFantasia(
                empresa.getNomeFantasia()
        );

        response.setNome(
                usuario.getNome()
        );

        response.setEmail(
                usuario.getEmail()
        );

        response.setPerfil(
                usuario.getPerfil()
        );

        return response;
    }

    private String normalizarSlug(String slug) {

        if (slug == null) {
            return null;
        }

        return slug.trim().toLowerCase();
    }

    private String normalizarEmail(
            String email) {

        if (email == null) {
            return null;
        }

        return email
                .trim()
                .toLowerCase();
    }

    private void verificarBloqueio(String chave) {

        RegistroTentativas registro =
                tentativasLogin.get(chave);

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
                    registro != null
                            ? registro
                            : new RegistroTentativas();

            atual.falhas++;

            if (atual.falhas >= MAX_TENTATIVAS_FALHAS) {

                atual.desbloqueiaEm =
                        Instant.now().plus(JANELA_BLOQUEIO);

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
