package br.com.vidratx.security;

import br.com.vidratx.entity.Usuario;
import br.com.vidratx.enums.Perfil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class UsuarioAutenticado {

    private UsuarioAutenticado() {
    }

    public static Usuario get() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal()
                instanceof Usuario usuario)) {

            throw new IllegalStateException(
                    "Usuário não autenticado"
            );
        }

        return usuario;
    }

    public static Long getUsuarioId() {
        return get().getId();
    }

    public static Long getEmpresaId() {

        Usuario usuario = get();

        if (usuario.getEmpresa() == null
                || usuario.getEmpresa().getId() == null) {

            throw new IllegalStateException(
                    "Usuário não possui empresa vinculada"
            );
        }

        return usuario.getEmpresa().getId();
    }

    public static Perfil getPerfil() {
        return get().getPerfil();
    }

    public static boolean isAdmin() {
        return getPerfil() == Perfil.ADMIN;
    }

    public static boolean isGerente() {
        return getPerfil() == Perfil.GERENTE;
    }

    public static boolean isFuncionario() {
        return getPerfil() == Perfil.FUNCIONARIO;
    }

    public static boolean isAdminOuGerente() {

        Perfil perfil = getPerfil();

        return perfil == Perfil.ADMIN
                || perfil == Perfil.GERENTE;
    }
}
