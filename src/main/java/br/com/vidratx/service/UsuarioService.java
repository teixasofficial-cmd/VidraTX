package br.com.vidratx.service;

import br.com.vidratx.dto.TrocarSenhaRequest;
import br.com.vidratx.dto.UsuarioRequest;
import br.com.vidratx.dto.UsuarioResponse;
import br.com.vidratx.dto.UsuarioUpdateRequest;
import br.com.vidratx.entity.Empresa;
import br.com.vidratx.entity.Usuario;
import br.com.vidratx.enums.Perfil;
import br.com.vidratx.exception.CredenciaisInvalidasException;
import br.com.vidratx.exception.EmailUsuarioDuplicadoException;
import br.com.vidratx.exception.EmpresaNaoEncontradaException;
import br.com.vidratx.exception.UltimoAdministradorException;
import br.com.vidratx.exception.UsuarioNaoEncontradoException;
import br.com.vidratx.mapper.UsuarioMapper;
import br.com.vidratx.repository.EmpresaRepository;
import br.com.vidratx.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            EmpresaRepository empresaRepository,
            UsuarioMapper usuarioMapper,
            PasswordEncoder passwordEncoder) {

        this.usuarioRepository = usuarioRepository;
        this.empresaRepository = empresaRepository;
        this.usuarioMapper = usuarioMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarPorEmpresa(
            Long empresaId) {

        verificarEmpresa(empresaId);

        return usuarioRepository
                .findAllByEmpresaId(empresaId)
                .stream()
                .map(usuarioMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(
            Long empresaId,
            Long usuarioId) {

        Usuario usuario =
                buscarUsuario(
                        empresaId,
                        usuarioId
                );

        return usuarioMapper.toResponse(usuario);
    }

    @Transactional
    public UsuarioResponse salvar(
            Long empresaId,
            UsuarioRequest request) {

        Empresa empresa =
                buscarEmpresa(empresaId);

        String email =
                normalizarEmail(
                        request.getEmail()
                );

        if (usuarioRepository
                .existsByEmpresaIdAndEmail(
                        empresaId,
                        email
                )) {

            throw new EmailUsuarioDuplicadoException(
                    "Já existe um usuário com este e-mail nesta empresa"
            );
        }

        Usuario usuario =
                usuarioMapper.toEntity(request);

        usuario.setEmpresa(empresa);

        Usuario salvo =
                usuarioRepository.save(usuario);

        return usuarioMapper.toResponse(salvo);
    }

    @Transactional
    public UsuarioResponse atualizar(
            Long empresaId,
            Long usuarioId,
            UsuarioUpdateRequest request) {

        Usuario usuario =
                buscarUsuario(
                        empresaId,
                        usuarioId
                );

        String email =
                normalizarEmail(
                        request.getEmail()
                );

        if (usuarioRepository
                .existsByEmpresaIdAndEmailAndIdNot(
                        empresaId,
                        email,
                        usuarioId
                )) {

            throw new EmailUsuarioDuplicadoException(
                    "Já existe um usuário com este e-mail nesta empresa"
            );
        }

        boolean deixaDeSerAdminAtivo =
                Perfil.ADMIN.equals(usuario.getPerfil())
                        && Boolean.TRUE.equals(usuario.getAtivo())
                        && (!Perfil.ADMIN.equals(request.getPerfil())
                        || !Boolean.TRUE.equals(request.getAtivo()));

        if (deixaDeSerAdminAtivo) {
            garantirQueNaoRemoveUltimoAdmin(empresaId);
        }

        usuarioMapper.updateEntity(
                usuario,
                request
        );

        return usuarioMapper.toResponse(
                usuarioRepository.save(usuario)
        );
    }

    @Transactional
    public void excluir(
            Long empresaId,
            Long usuarioId) {

        Usuario usuario =
                buscarUsuario(
                        empresaId,
                        usuarioId
                );

        if (Perfil.ADMIN.equals(usuario.getPerfil())
                && Boolean.TRUE.equals(usuario.getAtivo())) {

            garantirQueNaoRemoveUltimoAdmin(empresaId);
        }

        usuarioRepository.delete(usuario);
    }

    @Transactional
    public void redefinirSenha(Long empresaId, Long usuarioId, String novaSenha) {

        Usuario usuario = buscarUsuario(empresaId, usuarioId);

        usuario.setSenha(passwordEncoder.encode(novaSenha));

        usuarioRepository.save(usuario);
    }

    @Transactional
    public void trocarSenha(
            Long empresaId,
            Long usuarioId,
            TrocarSenhaRequest request) {

        Usuario usuario =
                buscarUsuario(
                        empresaId,
                        usuarioId
                );

        if (!passwordEncoder.matches(
                request.getSenhaAtual(),
                usuario.getSenha()
        )) {

            throw new CredenciaisInvalidasException(
                    "Senha atual incorreta"
            );
        }

        usuario.setSenha(
                passwordEncoder.encode(
                        request.getNovaSenha()
                )
        );

        usuarioRepository.save(usuario);
    }

    private void garantirQueNaoRemoveUltimoAdmin(
            Long empresaId) {

        long adminsAtivos =
                usuarioRepository
                        .countByEmpresaIdAndPerfilAndAtivoTrue(
                                empresaId,
                                Perfil.ADMIN
                        );

        if (adminsAtivos <= 1) {

            throw new UltimoAdministradorException(
                    "A empresa precisa ter pelo menos um administrador ativo"
            );
        }
    }

    private Empresa buscarEmpresa(Long empresaId) {

        return empresaRepository
                .findById(empresaId)
                .orElseThrow(() ->
                        new EmpresaNaoEncontradaException(
                                "Empresa não encontrada"
                        )
                );
    }

    private void verificarEmpresa(Long empresaId) {
        buscarEmpresa(empresaId);
    }

    private Usuario buscarUsuario(
            Long empresaId,
            Long usuarioId) {

        return usuarioRepository
                .findByIdAndEmpresaId(
                        usuarioId,
                        empresaId
                )
                .orElseThrow(() ->
                        new UsuarioNaoEncontradoException(
                                "Usuário não encontrado"
                        )
                );
    }

    private String normalizarEmail(String email) {

        if (email == null) {
            return null;
        }

        return email
                .trim()
                .toLowerCase();
    }
}
