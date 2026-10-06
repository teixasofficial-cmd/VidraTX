package br.com.vidratx;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:vidratx_auth_test;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "vidratx.jwt.secret=chave-de-teste-com-pelo-menos-32-bytes-0000",
        "vidratx.jwt.expiration=PT1H",
        "vidratx.cadastro-publico.habilitado=true"
})
class AutenticacaoIntegracaoTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    private String cadastrarEmpresaELogar(String slug) throws Exception {

        String cadastroBody = """
                {
                  "razaoSocial": "Vidraçaria %1$s LTDA",
                  "nomeFantasia": "Vidraçaria %1$s",
                  "cnpj": "%2$s",
                  "slug": "%1$s",
                  "emailEmpresa": "contato@%1$s.com.br",
                  "telefone": "11999990000",
                  "administrador": {
                    "nome": "Admin",
                    "email": "admin@%1$s.com.br",
                    "senha": "SenhaForte@123"
                  }
                }
                """.formatted(slug, cnpjFalsoUnico());

        mockMvc.perform(post("/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cadastroBody))
                .andExpect(status().isCreated());

        String loginBody = """
                {
                  "empresaSlug": "%1$s",
                  "email": "admin@%1$s.com.br",
                  "senha": "SenhaForte@123"
                }
                """.formatted(slug);

        String loginResponse = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(loginResponse);
        return json.get("token").asText();
    }

    private static int contadorCnpj = 0;

    private static synchronized String cnpjFalsoUnico() {

        contadorCnpj++;

        String base = String.format("1122233%05d", contadorCnpj);

        int d1 = calcularDigitoVerificador(base);
        int d2 = calcularDigitoVerificador(base + d1);

        return base + d1 + d2;
    }

    private static int calcularDigitoVerificador(String cnpj) {

        int[] pesos = cnpj.length() == 12
                ? new int[]{5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2}
                : new int[]{6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

        int soma = 0;
        for (int i = 0; i < cnpj.length(); i++) {
            soma += Character.digit(cnpj.charAt(i), 10) * pesos[i];
        }

        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    @Test
    void tokenValidoAcessaRotaProtegidaDeLeitura() throws Exception {

        String token = cadastrarEmpresaELogar("vidracaria-leitura");

        mockMvc.perform(get("/api/empresa")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("vidracaria-leitura"));
    }

    @Test
    void tokenValidoAcessaRotaProtegidaDeEscrita() throws Exception {

        String token = cadastrarEmpresaELogar("vidracaria-escrita");

        String clienteBody = """
                {
                  "nome": "Cliente Teste",
                  "telefone": "11988887777",
                  "whatsapp": "11988887777",
                  "email": "cliente.teste@exemplo.com",
                  "endereco": "Rua Exemplo, 123",
                  "observacoes": "Cliente criado pelo teste"
                }
                """;

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(clienteBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Cliente Teste"));
    }

    @Test
    void webhookSemTokenRetorna401ENao500() throws Exception {

        mockMvc.perform(post("/api/whatsapp/webhook/mensagens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void semTokenRotaProtegidaRetorna403() throws Exception {

        mockMvc.perform(get("/api/empresa"))
                .andExpect(status().isForbidden());
    }

    @Test
    void tokenDeUmaEmpresaNaoEnxergaDadosDeOutra() throws Exception {

        String tokenEmpresaA = cadastrarEmpresaELogar("vidracaria-a");
        cadastrarEmpresaELogar("vidracaria-b");

        mockMvc.perform(get("/api/empresa")
                        .header("Authorization", "Bearer " + tokenEmpresaA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("vidracaria-a"));
    }

    @Test
    void corpoJsonMalformadoRetorna400ENao500() throws Exception {

        String token = cadastrarEmpresaELogar("vidracaria-corpo-invalido");

        String corpoMalformado = """
                {
                  "clienteId": undefined,
                  "observacoes": "Orçamento de teste"
                }
                """;

        mockMvc.perform(post("/api/orcamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(corpoMalformado))
                .andExpect(status().isBadRequest());
    }
}
