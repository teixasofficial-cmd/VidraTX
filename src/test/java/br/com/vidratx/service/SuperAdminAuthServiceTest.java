package br.com.vidratx.service;

import br.com.vidratx.dto.SuperAdminLoginRequest;
import br.com.vidratx.dto.SuperAdminLoginResponse;
import br.com.vidratx.dto.TrocarSenhaRequest;
import br.com.vidratx.entity.AdministradorGlobal;
import br.com.vidratx.exception.CredenciaisInvalidasException;
import br.com.vidratx.repository.AdministradorGlobalRepository;
import br.com.vidratx.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SuperAdminAuthServiceTest {

    private static final Long ADMIN_ID = 1L;

    private AdministradorGlobalRepository administradorGlobalRepository;
    private PasswordEncoder passwordEncoder;
    private SuperAdminAuthService superAdminAuthService;

    @BeforeEach
    void montarCenario() {

        administradorGlobalRepository = mock(AdministradorGlobalRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);

        superAdminAuthService = new SuperAdminAuthService(
                administradorGlobalRepository,
                passwordEncoder,
                mock(JwtService.class)
        );
    }

    private AdministradorGlobal montarAdministrador() {

        AdministradorGlobal administrador = new AdministradorGlobal();
        administrador.setEmail("admin@vidratx.local");
        administrador.setSenha("hash-atual");
        administrador.setAtivo(true);
        return administrador;
    }

    @Test
    void rejeitaLoginDeContaDesativadaMesmoComSenhaCerta() {

        SuperAdminLoginRequest request = new SuperAdminLoginRequest();
        request.setEmail("desativado@vidratx.local");
        request.setSenha("qualquer-coisa");

        when(administradorGlobalRepository
                .findByEmailAndAtivoTrue("desativado@vidratx.local"))
                .thenReturn(Optional.empty());

        assertThrows(
                CredenciaisInvalidasException.class,
                () -> superAdminAuthService.login(request)
        );
    }

    @Test
    void permiteLoginDeContaAtivaComSenhaCerta() {

        AdministradorGlobal administrador = montarAdministrador();

        SuperAdminLoginRequest request = new SuperAdminLoginRequest();
        request.setEmail("admin@vidratx.local");
        request.setSenha("senha-certa");

        when(administradorGlobalRepository.findByEmailAndAtivoTrue("admin@vidratx.local"))
                .thenReturn(Optional.of(administrador));

        when(passwordEncoder.matches("senha-certa", "hash-atual"))
                .thenReturn(true);

        SuperAdminLoginResponse response = superAdminAuthService.login(request);

        assertEquals("admin@vidratx.local", response.getEmail());
    }

    @Test
    void trocaSenhaQuandoSenhaAtualConfere() {

        AdministradorGlobal administrador = montarAdministrador();

        TrocarSenhaRequest request = new TrocarSenhaRequest();
        request.setSenhaAtual("senha-certa");
        request.setNovaSenha("senha-nova-123");

        when(administradorGlobalRepository.findByIdAndAtivoTrue(ADMIN_ID))
                .thenReturn(Optional.of(administrador));

        when(passwordEncoder.matches("senha-certa", "hash-atual"))
                .thenReturn(true);

        when(passwordEncoder.encode("senha-nova-123"))
                .thenReturn("hash-novo");

        superAdminAuthService.trocarSenha(ADMIN_ID, request);

        assertEquals("hash-novo", administrador.getSenha());
        verify(administradorGlobalRepository).save(administrador);
    }

    @Test
    void rejeitaTrocaDeSenhaQuandoSenhaAtualNaoConfere() {

        AdministradorGlobal administrador = montarAdministrador();

        TrocarSenhaRequest request = new TrocarSenhaRequest();
        request.setSenhaAtual("senha-errada");
        request.setNovaSenha("senha-nova-123");

        when(administradorGlobalRepository.findByIdAndAtivoTrue(ADMIN_ID))
                .thenReturn(Optional.of(administrador));

        when(passwordEncoder.matches("senha-errada", "hash-atual"))
                .thenReturn(false);

        assertThrows(
                CredenciaisInvalidasException.class,
                () -> superAdminAuthService.trocarSenha(ADMIN_ID, request)
        );

        assertEquals("hash-atual", administrador.getSenha());
        verify(administradorGlobalRepository, never()).save(any());
    }
}
