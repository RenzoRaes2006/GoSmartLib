// package ap.be.backend.security;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertFalse;
// import static org.junit.jupiter.api.Assertions.assertTrue;

// import org.junit.jupiter.api.Test;
// import org.springframework.test.util.ReflectionTestUtils;

// import ap.be.backend.config.JwtService;

// public class JwtServiceTest {

//     private static final String TEST_SECRET = "dit_is_een_zeer_geheime_sleutel_voor_gosmartbib_die_lang_genoeg_is";

//     private JwtService buildJwtService() {
//         JwtService jwtService = new JwtService();
//         ReflectionTestUtils.setField(jwtService, "jwtSecret", TEST_SECRET);
//         return jwtService;
//     }

//     /*** Verifieert dat een gegenereerde JWT opnieuw gelezen kan worden. */
//     @Test
//     void givenGeneratedToken_whenExtractSmartschoolId_thenReturnsId() {
//         JwtService jwtService = buildJwtService();
//         String token = jwtService.generateToken("ss-123", "STUDENT", "Test Gebruiker");

//         String extracted = jwtService.extractSmartschoolId(token);

//         assertEquals("ss-123", extracted);
//         assertTrue(jwtService.isTokenValid(token));
//     }

//     /*** Verifieert dat een ongeldig token wordt afgewezen. */
//     @Test
//     void givenInvalidToken_whenIsTokenValid_thenReturnsFalse() {
//         JwtService jwtService = buildJwtService();

//         assertFalse(jwtService.isTokenValid("niet.echt.token"));
//     }
// }
