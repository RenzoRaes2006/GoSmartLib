package ap.be.backend.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

import ap.be.backend.config.JwtService;
import ap.be.backend.config.OAuth2LoginSuccessHandler;
import ap.be.backend.entity.User;
import ap.be.backend.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
public class OAuth2LoginSuccessHandlerTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OAuth2LoginSuccessHandler successHandler;

    /*** Verifieert dat een nieuwe gebruiker wordt opgeslagen en doorgestuurd met token. */
    @Test
    void givenNewUser_whenOnAuthenticationSuccess_thenUserSavedAndRedirected() throws Exception {
        ReflectionTestUtils.setField(successHandler, "frontendBaseUrl", "http://localhost:4200");

        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttribute("id")).thenReturn("ss-123");
        when(oAuth2User.getAttribute("role")).thenReturn("STUDENT");
        when(oAuth2User.getAttribute("name")).thenReturn("Test Gebruiker");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(oAuth2User);

        when(userRepository.findBySmartschoolId("ss-123")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken("ss-123", "STUDENT", "Test Gebruiker")).thenReturn("jwt-token");

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        successHandler.onAuthenticationSuccess(request, response, authentication);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());
        assertEquals("ss-123", userCaptor.getValue().getSmartschoolId());
        verify(response, times(1)).sendRedirect("http://localhost:4200/login-success?token=jwt-token");
    }

    /*** Verifieert dat een bestaande gebruiker niet opnieuw wordt opgeslagen. */
    @Test
    void givenExistingUser_whenOnAuthenticationSuccess_thenNoSaveAndRedirected() throws Exception {
        ReflectionTestUtils.setField(successHandler, "frontendBaseUrl", "http://localhost:4200");

        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttribute("id")).thenReturn("ss-456");
        when(oAuth2User.getAttribute("role")).thenReturn("ADMIN");
        when(oAuth2User.getAttribute("name")).thenReturn("Admin User");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(oAuth2User);

        when(userRepository.findBySmartschoolId("ss-456")).thenReturn(Optional.of(new User("ss-456")));
        when(jwtService.generateToken("ss-456", "ADMIN", "Admin User")).thenReturn("jwt-token-2");

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        successHandler.onAuthenticationSuccess(request, response, authentication);

        verify(userRepository, never()).save(any(User.class));
        verify(response, times(1)).sendRedirect("http://localhost:4200/login-success?token=jwt-token-2");
    }
}
