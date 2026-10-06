package br.com.vidratx.security;

import br.com.vidratx.entity.AdministradorGlobal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SuperAdminAutenticado {

    private SuperAdminAutenticado() {
    }

    public static AdministradorGlobal get() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal()
                instanceof AdministradorGlobal administrador)) {

            throw new IllegalStateException(
                    "Super admin não autenticado"
            );
        }

        return administrador;
    }

    public static Long getId() {
        return get().getId();
    }
}
