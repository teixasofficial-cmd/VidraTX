package br.com.vidratx.mapper;

import br.com.vidratx.dto.UsuarioRequest;
import br.com.vidratx.dto.UsuarioResponse;
import br.com.vidratx.dto.UsuarioUpdateRequest;
import br.com.vidratx.entity.Usuario;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    private final PasswordEncoder passwordEncoder;

    public UsuarioMapper(
            PasswordEncoder passwordEncoder) {

        this.passwordEncoder =
                passwordEncoder;
    }

    public Usuario toEntity(
            UsuarioRequest request) {

        Usuario usuario =
                new Usuario();

        usuario.setNome(
                request.getNome().trim()
        );

        usuario.setEmail(
                normalizarEmail(
                        request.getEmail()
                )
        );

        usuario.setSenha(
                passwordEncoder.encode(
                        request.getSenha()
                )
        );

        usuario.setPerfil(
                request.getPerfil()
        );

        usuario.setAtivo(
                request.getAtivo() == null
                        ? Boolean.TRUE
                        : request.getAtivo()
        );

        return usuario;
    }

    public void updateEntity(
            Usuario usuario,
            UsuarioUpdateRequest request) {

        usuario.setNome(
                request.getNome().trim()
        );

        usuario.setEmail(
                normalizarEmail(
                        request.getEmail()
                )
        );

        usuario.setPerfil(
                request.getPerfil()
        );

        usuario.setAtivo(
                request.getAtivo()
        );
    }

    public UsuarioResponse toResponse(
            Usuario usuario) {

        UsuarioResponse response =
                new UsuarioResponse();

        response.setId(
                usuario.getId()
        );

        if (usuario.getEmpresa() != null) {

            response.setEmpresaId(
                    usuario.getEmpresa().getId()
            );
        }

        response.setNome(
                usuario.getNome()
        );

        response.setEmail(
                usuario.getEmail()
        );

        response.setPerfil(
                usuario.getPerfil()
        );

        response.setAtivo(
                usuario.getAtivo()
        );

        response.setCriadoEm(
                usuario.getCriadoEm()
        );

        response.setAtualizadoEm(
                usuario.getAtualizadoEm()
        );

        return response;
    }

    private String normalizarEmail(
            String email) {

        if (email == null
                || email.isBlank()) {

            return null;
        }

        return email
                .trim()
                .toLowerCase();
    }
}
