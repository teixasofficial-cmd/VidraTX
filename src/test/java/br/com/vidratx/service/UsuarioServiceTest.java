package br.com.vidratx.service;

import br.com.vidratx.dto.TrocarSenhaRequest;
import br.com.vidratx.entity.Usuario;
import br.com.vidratx.exception.CredenciaisInvalidasException;
import br.com.vidratx.mapper.UsuarioMapper;
import br.com.vidratx.repository.EmpresaRepository;
import br.com.vidratx.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UsuarioServiceTest {

    private static final Long EMPRESA_ID = 1L;
    private static final Long USUARIO_ID = 7L;

    private UsuarioRepository usuarioRepository;
    private PasswordEncoder passwordEncoder;
    private UsuarioService usuarioService;

    @BeforeEach
    void montarCenario() {

        usuarioRepository = mock(UsuarioRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);

        usuarioService = new UsuarioService(
                usuarioRepository,
                mock(EmpresaRepository.class),
                new UsuarioMapper(passwordEncoder),
                passwordEncoder
        );
    }

    private Usuario montarUsuario() {

        Usuario usuario = new Usuario();
        usuario.setSenha("hash-antigo");
        return usuario;
    }

    @Test
    void trocaSenhaQuandoSenhaAtualConfere() {

        Usuario usuario = montarUsuario();

        TrocarSenhaRequest request = new TrocarSenhaRequest();
        request.setSenhaAtual("senha-certa");
        request.setNovaSenha("senha-nova-123");

        when(usuarioRepository.findByIdAndEmpresaId(USUARIO_ID, EMPRESA_ID))
                .thenReturn(Optional.of(usuario));

        when(passwordEncoder.matches("senha-certa", "hash-antigo"))
                .thenReturn(true);

        when(passwordEncoder.encode("senha-nova-123"))
                .thenReturn("hash-novo");

        usuarioService.trocarSenha(EMPRESA_ID, USUARIO_ID, request);

        assertEquals("hash-novo", usuario.getSenha());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void rejeitaQuandoSenhaAtualNaoConfere() {

        Usuario usuario = montarUsuario();

        TrocarSenhaRequest request = new TrocarSenhaRequest();
        request.setSenhaAtual("senha-errada");
        request.setNovaSenha("senha-nova-123");

        when(usuarioRepository.findByIdAndEmpresaId(USUARIO_ID, EMPRESA_ID))
                .thenReturn(Optional.of(usuario));

        when(passwordEncoder.matches("senha-errada", "hash-antigo"))
                .thenReturn(false);

        assertThrows(
                CredenciaisInvalidasException.class,
                () -> usuarioService.trocarSenha(EMPRESA_ID, USUARIO_ID, request)
        );

        assertEquals("hash-antigo", usuario.getSenha());
        verify(usuarioRepository, org.mockito.Mockito.never()).save(any());
    }
}
