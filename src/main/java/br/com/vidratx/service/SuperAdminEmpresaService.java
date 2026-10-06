package br.com.vidratx.service;

import br.com.vidratx.dto.EmpresaSuperAdminAtualizarRequest;
import br.com.vidratx.dto.EmpresaSuperAdminRequest;
import br.com.vidratx.dto.EmpresaSuperAdminResponse;
import br.com.vidratx.dto.SuperAdminResumoResponse;
import br.com.vidratx.entity.Empresa;
import br.com.vidratx.entity.Usuario;
import br.com.vidratx.entity.WhatsappInstancia;
import br.com.vidratx.enums.Perfil;
import br.com.vidratx.enums.StatusInstanciaWhatsapp;
import br.com.vidratx.exception.CnpjDuplicadoException;
import br.com.vidratx.exception.EmpresaNaoEncontradaException;
import br.com.vidratx.exception.UsuarioNaoEncontradoException;
import br.com.vidratx.repository.EmpresaRepository;
import br.com.vidratx.repository.UsuarioRepository;
import br.com.vidratx.repository.WhatsappInstanciaRepository;
import br.com.vidratx.util.SlugUtils;
import br.com.vidratx.validator.CnpjValidator;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SuperAdminEmpresaService {

    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TipologiaSeedService tipologiaSeedService;
    private final WhatsappInstanciaRepository whatsappInstanciaRepository;

    public SuperAdminEmpresaService(
            EmpresaRepository empresaRepository,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            TipologiaSeedService tipologiaSeedService,
            WhatsappInstanciaRepository whatsappInstanciaRepository) {

        this.empresaRepository = empresaRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tipologiaSeedService = tipologiaSeedService;
        this.whatsappInstanciaRepository = whatsappInstanciaRepository;
    }

    @Transactional(readOnly = true)
    public List<EmpresaSuperAdminResponse> listar() {

        Map<Long, WhatsappInstancia> instancias = whatsappInstanciaRepository.findAll().stream()
                .collect(Collectors.toMap(i -> i.getEmpresa().getId(), Function.identity()));

        return empresaRepository
                .findAll(Sort.by(Sort.Direction.DESC, "criadoEm"))
                .stream()
                .map(empresa -> toResponse(empresa, instancias.get(empresa.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public SuperAdminResumoResponse resumo() {

        long total = empresaRepository.count();
        long ativas = empresaRepository.countByAtiva(true);

        long whatsappDesconectados = whatsappInstanciaRepository
                .countByStatusNotAndDesconectadoEmIsNotNullAndEmpresaAtivaTrue(StatusInstanciaWhatsapp.CONECTADO);

        return new SuperAdminResumoResponse(total, ativas, total - ativas, whatsappDesconectados);
    }

    @Transactional
    public EmpresaSuperAdminResponse criar(EmpresaSuperAdminRequest request) {

        String cnpj = normalizarCnpj(request.getCnpj());

        validarCnpj(cnpj);

        if (empresaRepository.existsByCnpj(cnpj)) {

            throw new CnpjDuplicadoException(
                    "Já existe uma empresa cadastrada com este CNPJ"
            );
        }

        String nome = request.getNome().trim();
        String email = normalizarEmail(request.getEmail());

        Empresa empresa = new Empresa();

        empresa.setRazaoSocial(nome);
        empresa.setNomeFantasia(nome);
        empresa.setCnpj(cnpj);
        empresa.setSlug(gerarSlugUnico(nome));
        empresa.setEmail(email);
        empresa.setTelefone(request.getTelefone().trim());
        empresa.setEndereco(request.getEndereco().trim());
        empresa.setAtiva(true);

        Empresa empresaSalva = empresaRepository.save(empresa);

        tipologiaSeedService.seedPadrao(empresaSalva);

        Usuario usuario = new Usuario();

        usuario.setEmpresa(empresaSalva);
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode(request.getSenha()));
        usuario.setPerfil(Perfil.ADMIN);
        usuario.setAtivo(true);

        usuarioRepository.save(usuario);

        return toResponse(empresaSalva);
    }

    @Transactional
    public EmpresaSuperAdminResponse atualizar(
            Long id,
            EmpresaSuperAdminAtualizarRequest request) {

        Empresa empresa = buscarEmpresa(id);

        String cnpj = normalizarCnpj(request.getCnpj());

        validarCnpj(cnpj);

        if (empresaRepository.existsByCnpjAndIdNot(cnpj, id)) {

            throw new CnpjDuplicadoException("CNPJ já cadastrado");
        }

        String emailAntigo = empresa.getEmail();
        String emailNovo = normalizarEmail(request.getEmail());

        empresa.setRazaoSocial(request.getNome().trim());
        empresa.setNomeFantasia(request.getNome().trim());
        empresa.setCnpj(cnpj);
        empresa.setEmail(emailNovo);
        empresa.setTelefone(request.getTelefone().trim());
        empresa.setEndereco(request.getEndereco().trim());

        Empresa salva = empresaRepository.save(empresa);

        sincronizarLoginComEmailDaEmpresa(id, emailAntigo, emailNovo);

        return toResponse(salva);
    }

    private void sincronizarLoginComEmailDaEmpresa(
            Long empresaId,
            String emailAntigo,
            String emailNovo) {

        if (emailAntigo == null
                || emailAntigo.equals(emailNovo)
                || usuarioRepository.existsByEmpresaIdAndEmail(empresaId, emailNovo)) {

            return;
        }

        usuarioRepository
                .findByEmpresaIdAndEmail(empresaId, emailAntigo)
                .filter(usuario -> usuario.getPerfil() == Perfil.ADMIN)
                .ifPresent(admin -> {

                    admin.setEmail(emailNovo);
                    usuarioRepository.save(admin);
                });
    }

    @Transactional
    public EmpresaSuperAdminResponse alternarStatus(Long id) {

        Empresa empresa = buscarEmpresa(id);

        empresa.setAtiva(!Boolean.TRUE.equals(empresa.getAtiva()));

        return toResponse(empresaRepository.save(empresa));
    }

    @Transactional
    public void redefinirSenha(Long id, String novaSenha) {

        Empresa empresa = buscarEmpresa(id);

        Usuario admin =
                usuarioRepository
                        .findByEmpresaIdAndEmail(id, empresa.getEmail())
                        .orElseThrow(() ->
                                new UsuarioNaoEncontradoException(
                                        "Login desta empresa não encontrado"
                                )
                        );

        admin.setSenha(passwordEncoder.encode(novaSenha));

        usuarioRepository.save(admin);
    }

    private String gerarSlugUnico(String nome) {

        String base = SlugUtils.gerar(nome);

        String candidato = base;
        int sufixo = 2;

        while (empresaRepository.existsBySlug(candidato) || SlugUtils.reservado(candidato)) {

            candidato = base + "-" + sufixo;
            sufixo++;
        }

        return candidato;
    }

    private EmpresaSuperAdminResponse toResponse(Empresa empresa) {
        return toResponse(empresa, whatsappInstanciaRepository.findByEmpresaId(empresa.getId()).orElse(null));
    }

    private EmpresaSuperAdminResponse toResponse(Empresa empresa, WhatsappInstancia instancia) {

        EmpresaSuperAdminResponse response = new EmpresaSuperAdminResponse();

        response.setId(empresa.getId());
        response.setNome(empresa.getNomeFantasia());
        response.setCnpj(empresa.getCnpj());
        response.setSlug(empresa.getSlug());
        response.setEmail(empresa.getEmail());
        response.setTelefone(empresa.getTelefone());
        response.setEndereco(empresa.getEndereco());
        response.setAtiva(empresa.getAtiva());
        response.setTotalUsuarios(usuarioRepository.countByEmpresaId(empresa.getId()));
        response.setCriadoEm(empresa.getCriadoEm());
        response.setWhatsappStatus(instancia == null ? "NAO_CONFIGURADO" : instancia.getStatus().name());
        response.setWhatsappDesconectadoDesde(instancia == null ? null : instancia.getDesconectadoEm());

        return response;
    }

    private Empresa buscarEmpresa(Long id) {

        return empresaRepository
                .findById(id)
                .orElseThrow(() ->
                        new EmpresaNaoEncontradaException("Empresa não encontrada")
                );
    }

    private String normalizarCnpj(String cnpj) {

        return cnpj == null ? null : cnpj.replaceAll("\\D", "");
    }

    private void validarCnpj(String cnpj) {

        if (!CnpjValidator.isValid(cnpj)) {
            throw new IllegalArgumentException("CNPJ inválido");
        }
    }

    private String normalizarEmail(String email) {

        return email == null ? null : email.trim().toLowerCase();
    }
}
