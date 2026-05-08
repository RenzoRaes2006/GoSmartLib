package ap.be.backend.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

import ap.be.backend.config.JwtService;
import ap.be.backend.config.OAuth2LoginSuccessHandler;
import ap.be.backend.dto.OneRosterUser;
import ap.be.backend.entity.Gebruiker;
import ap.be.backend.entity.User;
import ap.be.backend.repository.GebruikerRepository;
import ap.be.backend.repository.UserRepository;
import ap.be.backend.service.OneRosterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class OAuth2LoginSuccessHandlerTest {

    @Mock
    private JwtService jwtService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private GebruikerRepository gebruikerRepository;
    @Mock
    private OneRosterService oneRosterService;

    @InjectMocks
    private OAuth2LoginSuccessHandler successHandler;

    @Test
    void newUser_isPersistedAndRedirectedWithToken() throws Exception {
        ReflectionTestUtils.setField(successHandler, "frontendBaseUrl", "https://frontend.test");

        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttribute("userID")).thenReturn("ss-123");
        when(oAuth2User.getAttribute("name")).thenReturn("Haitam");
        when(oAuth2User.getAttribute("username")).thenReturn("sof2.haitam");

        OneRosterUser orUser = new OneRosterUser("src-1", "student");
        when(oneRosterService.getUserByUsername("sof2.haitam")).thenReturn(orUser);
        when(oneRosterService.getKlasBySourcedId("src-1")).thenReturn("2SOF");

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(oAuth2User);

        when(userRepository.findBySmartschoolId("ss-123")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(gebruikerRepository.findBySmartschoolId("ss-123")).thenReturn(Optional.empty());
        when(gebruikerRepository.save(any(Gebruiker.class))).thenAnswer(i -> i.getArgument(0));
        when(jwtService.generateToken(eq("ss-123"), eq("student"), eq("Haitam"), eq("2SOF")))
                .thenReturn("jwt-token");

        HttpServletResponse response = mock(HttpServletResponse.class);

        successHandler.onAuthenticationSuccess(mock(HttpServletRequest.class), response, auth);

        verify(userRepository, times(1)).save(any(User.class));
        verify(gebruikerRepository, times(1)).save(any(Gebruiker.class));
        verify(response).sendRedirect("https://frontend.test/login-success?token=jwt-token");
    }

    @Test
    void existingUser_isNotPersistedAgain() throws Exception {
        ReflectionTestUtils.setField(successHandler, "frontendBaseUrl", "https://frontend.test");

        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttribute("userID")).thenReturn("ss-456");
        when(oAuth2User.getAttribute("name")).thenReturn("Admin");
        when(oAuth2User.getAttribute("username")).thenReturn("admin.user");

        when(oneRosterService.getUserByUsername("admin.user")).thenReturn(null);

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(oAuth2User);

        when(userRepository.findBySmartschoolId("ss-456")).thenReturn(Optional.of(new User("ss-456")));
        when(gebruikerRepository.findBySmartschoolId("ss-456")).thenReturn(Optional.of(new Gebruiker()));
        when(jwtService.generateToken(eq("ss-456"), eq("onbekend"), eq("Admin"), eq("onbekend")))
                .thenReturn("jwt-token-2");

        HttpServletResponse response = mock(HttpServletResponse.class);

        successHandler.onAuthenticationSuccess(mock(HttpServletRequest.class), response, auth);

        verify(userRepository, never()).save(any(User.class));
        verify(gebruikerRepository, never()).save(any(Gebruiker.class));
        verify(response).sendRedirect("https://frontend.test/login-success?token=jwt-token-2");
    }
}
