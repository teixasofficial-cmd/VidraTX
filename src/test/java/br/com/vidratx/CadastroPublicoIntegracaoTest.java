package br.com.vidratx;

import br.com.vidratx.repository.EmpresaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:vidratx_cadastro_test;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "vidratx.jwt.secret=chave-de-teste-com-pelo-menos-32-bytes-0000",
        "vidratx.jwt.expiration=PT1H"
})
class CadastroPublicoIntegracaoTest {

    private static final String CADASTRO_VALIDO = """
            {
              "razaoSocial": "Vidraçaria Fechada LTDA",
              "nomeFantasia": "Vidraçaria Fechada",
              "cnpj": "11222333000181",
              "slug": "vidracaria-fechada",
              "emailEmpresa": "contato@fechada.com.br",
              "telefone": "11999990000",
              "administrador": {
                "nome": "Admin",
                "email": "admin@fechada.com.br",
                "senha": "SenhaForte@123"
              }
            }
            """;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private EmpresaRepository empresaRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void cadastroPublicoFechadoPorPadrao() throws Exception {

        long empresasAntes = empresaRepository.count();

        mockMvc.perform(post("/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CADASTRO_VALIDO))
                .andExpect(status().isForbidden());

        assertEquals(empresasAntes, empresaRepository.count());
    }

    @Test
    void loginContinuaPublico() throws Exception {

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "empresaSlug": "nao-existe",
                                  "email": "alguem@exemplo.com",
                                  "senha": "qualquer-senha"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }
}
