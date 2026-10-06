package br.com.vidratx.service;

import br.com.vidratx.dto.AdministradorRequest;
import br.com.vidratx.dto.OnboardingRequest;
import br.com.vidratx.dto.OnboardingResponse;
import br.com.vidratx.entity.Empresa;
import br.com.vidratx.entity.Usuario;
import br.com.vidratx.enums.Perfil;
import br.com.vidratx.exception.CnpjDuplicadoException;
import br.com.vidratx.exception.EmailUsuarioDuplicadoException;
import br.com.vidratx.exception.SlugDuplicadoException;
import br.com.vidratx.repository.EmpresaRepository;
import br.com.vidratx.repository.UsuarioRepository;
import br.com.vidratx.util.SlugUtils;
import br.com.vidratx.validator.CnpjValidator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OnboardingService {

    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TipologiaSeedService tipologiaSeedService;

    public OnboardingService(
            EmpresaRepository empresaRepository,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            TipologiaSeedService tipologiaSeedService) {

        this.empresaRepository =
                empresaRepository;

        this.usuarioRepository =
                usuarioRepository;

        this.passwordEncoder =
                passwordEncoder;

        this.tipologiaSeedService = tipologiaSeedService;
    }

    @Transactional
    public OnboardingResponse cadastrar(
            OnboardingRequest request) {

        String cnpj =
                normalizarCnpj(request.getCnpj());

        validarCnpj(cnpj);

        if (empresaRepository.existsByCnpj(cnpj)) {

            throw new CnpjDuplicadoException(
                    "Já existe uma empresa cadastrada com este CNPJ"
            );
        }

        AdministradorRequest administrador =
                request.getAdministrador();

        String emailAdministrador =
                normalizarEmail(
                        administrador.getEmail()
                );

        String slug =
                resolverSlug(
                        request.getSlug(),
                        request.getNomeFantasia()
                );

        Empresa empresa = new Empresa();

        empresa.setRazaoSocial(
                request.getRazaoSocial().trim()
        );

        empresa.setNomeFantasia(
                request.getNomeFantasia().trim()
        );

        empresa.setCnpj(cnpj);

        empresa.setSlug(slug);

        empresa.setEmail(
                normalizar(request.getEmailEmpresa())
        );

        empresa.setTelefone(
                normalizar(request.getTelefone())
        );

        empresa.setAtiva(true);

        Empresa empresaSalva =
                empresaRepository.save(empresa);

        tipologiaSeedService.seedPadrao(empresaSalva);

        if (usuarioRepository
                .existsByEmpresaIdAndEmail(
                        empresaSalva.getId(),
                        emailAdministrador
                )) {

            throw new EmailUsuarioDuplicadoException(
                    "Já existe um usuário com este e-mail"
            );
        }

        Usuario usuario = new Usuario();

        usuario.setEmpresa(
                empresaSalva
        );

        usuario.setNome(
                administrador.getNome().trim()
        );

        usuario.setEmail(
                emailAdministrador
        );

        usuario.setSenha(
                passwordEncoder.encode(
                        administrador.getSenha()
                )
        );

        usuario.setPerfil(
                Perfil.ADMIN
        );

        usuario.setAtivo(true);

        Usuario usuarioSalvo =
                usuarioRepository.save(usuario);

        OnboardingResponse response =
                new OnboardingResponse();

        response.setEmpresaId(
                empresaSalva.getId()
        );

        response.setUsuarioId(
                usuarioSalvo.getId()
        );

        response.setNomeFantasia(
                empresaSalva.getNomeFantasia()
        );

        response.setSlug(
                empresaSalva.getSlug()
        );

        response.setNomeAdministrador(
                usuarioSalvo.getNome()
        );

        response.setEmailAdministrador(
                usuarioSalvo.getEmail()
        );

        response.setMensagem(
                "Empresa cadastrada com sucesso"
        );

        return response;
    }

    private String resolverSlug(
            String slugInformado,
            String nomeFantasia) {

        String slugNormalizado =
                normalizar(slugInformado);

        if (slugNormalizado == null) {
            return gerarSlugUnico(nomeFantasia);
        }

        slugNormalizado = slugNormalizado.toLowerCase();

        if (!SlugUtils.formatoValido(slugNormalizado)) {

            throw new IllegalArgumentException(
                    "Identificador inválido. Use apenas letras minúsculas, "
                            + "números e hífen (ex.: vidracaria-silva)"
            );
        }

        if (SlugUtils.reservado(slugNormalizado)) {

            throw new IllegalArgumentException(
                    "Este identificador é reservado pelo sistema. Escolha outro."
            );
        }

        if (empresaRepository.existsBySlug(slugNormalizado)) {

            throw new SlugDuplicadoException(
                    "Este identificador já está em uso por outra empresa"
            );
        }

        return slugNormalizado;
    }

    private String gerarSlugUnico(String nomeFantasia) {

        String base =
                SlugUtils.gerar(nomeFantasia);

        String candidato = base;
        int sufixo = 2;

        while (empresaRepository.existsBySlug(candidato)
                || SlugUtils.reservado(candidato)) {

            candidato = base + "-" + sufixo;
            sufixo++;
        }

        return candidato;
    }

    private String normalizarCnpj(
            String cnpj) {

        if (cnpj == null) {
            return null;
        }

        return cnpj.replaceAll(
                "\\D",
                ""
        );
    }

    private void validarCnpj(
            String cnpj) {

        if (!CnpjValidator.isValid(cnpj)) {

            throw new IllegalArgumentException(
                    "CNPJ inválido"
            );
        }
    }

    private String normalizar(
            String valor) {

        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor.trim();
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

}
