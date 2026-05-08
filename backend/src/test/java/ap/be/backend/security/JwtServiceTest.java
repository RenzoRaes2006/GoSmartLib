package ap.be.backend.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import ap.be.backend.config.JwtService;

class JwtServiceTest {

    private static final String TEST_SECRET = "dit_is_een_zeer_geheime_sleutel_voor_gosmartbib_die_lang_genoeg_is";

    private JwtService buildJwtService() {
        JwtService jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", TEST_SECRET);
        return jwtService;
    }

    @Test
    void generatedToken_canBeRead() {
        JwtService jwtService = buildJwtService();

        String token = jwtService.generateToken("ss-123", "student", "Test Gebruiker", "2SOF");

        assertEquals("ss-123", jwtService.extractSmartschoolId(token));
        assertEquals("student", jwtService.extractRole(token));
        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void invalidToken_isRejected() {
        JwtService jwtService = buildJwtService();

        assertFalse(jwtService.isTokenValid("niet.echt.token"));
    }
}
