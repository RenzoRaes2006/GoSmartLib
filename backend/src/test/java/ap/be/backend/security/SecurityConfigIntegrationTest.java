package ap.be.backend.security;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Verifieert dat SecurityConfig de juiste regels toepast op endpoints:
 * - publieke endpoints werken zonder authenticatie
 * - beschermde endpoints geven 401 zonder auth
 * - rol-gebaseerde endpoints geven 403 zonder de juiste rol
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void givenGeenAuth_whenGetPublicBoekEndpoint_thenToegestaan() throws Exception {
        mockMvc.perform(get("/api/boeken"))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    void givenGeenAuth_whenGetBeschermEndpoint_thenNiet2xx() throws Exception {
        // Met oauth2Login geconfigureerd wordt unauthenticated request omgeleid (302)
        // i.p.v. 401 — beide zijn aanvaardbaar, zolang er geen 2xx terugkomt.
        int status = mockMvc.perform(get("/api/gebruiker/aanvragen"))
                .andReturn().getResponse().getStatus();
        assertNotEquals(2, status / 100, "Endpoint mag niet bereikbaar zijn zonder auth");
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void givenStudentRol_whenGetLeerkrachtEndpoint_thenForbidden() throws Exception {
        mockMvc.perform(get("/api/gebruiker/aanvragen"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void givenStudentRol_whenGetBeheerderEndpoint_thenForbidden() throws Exception {
        mockMvc.perform(get("/api/reports"))
                .andExpect(status().isForbidden());
    }
}
