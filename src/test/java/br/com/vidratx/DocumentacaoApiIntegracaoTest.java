package br.com.vidratx;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DocumentacaoApiIntegracaoTest {

    private static final String H2 = "spring.datasource.url=jdbc:h2:mem:vidratx_docs_test;MODE=MySQL;DB_CLOSE_DELAY=-1";

    abstract static class Base {

        @Autowired
        private WebApplicationContext webApplicationContext;

        MockMvc mockMvc;

        @BeforeEach
        void setUp() {
            mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                    .apply(springSecurity())
                    .build();
        }
    }

    @Nested
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
    @TestPropertySource(properties = {
            H2,
            "spring.datasource.driver-class-name=org.h2.Driver",
            "spring.datasource.username=sa",
            "spring.datasource.password=",
            "spring.flyway.enabled=false",
            "spring.jpa.hibernate.ddl-auto=create-drop",
            "vidratx.jwt.secret=chave-de-teste-com-pelo-menos-32-bytes-0000",
            "vidratx.jwt.expiration=PT1H"
    })
    class ConfiguracaoPadrao extends Base {

        @Test
        void documentacaoNaoEstaDisponivel() throws Exception {

            mockMvc.perform(get("/v3/api-docs"))
                    .andExpect(status().isNotFound());

            mockMvc.perform(get("/swagger-ui/index.html"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
    @TestPropertySource(properties = {
            H2,
            "spring.datasource.driver-class-name=org.h2.Driver",
            "spring.datasource.username=sa",
            "spring.datasource.password=",
            "spring.flyway.enabled=false",
            "spring.jpa.hibernate.ddl-auto=create-drop",
            "vidratx.jwt.secret=chave-de-teste-com-pelo-menos-32-bytes-0000",
            "vidratx.jwt.expiration=PT1H",
            "springdoc.api-docs.enabled=true",
            "springdoc.swagger-ui.enabled=true"
    })
    class LigadaEmDesenvolvimento extends Base {

        @Test
        void documentacaoResponde() throws Exception {

            mockMvc.perform(get("/v3/api-docs"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.info.title").value("VidraTX API"));
        }
    }
}
