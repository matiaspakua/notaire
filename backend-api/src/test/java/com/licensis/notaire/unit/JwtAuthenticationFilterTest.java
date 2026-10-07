package com.licensis.notaire.unit;

import com.licensis.notaire.config.AuthCookieService;
import com.licensis.notaire.config.JwtAuthenticationFilter;
import com.licensis.notaire.config.JwtTokenService;
import com.licensis.notaire.config.TokenRevocationService;
import com.licensis.notaire.security.UserAuthorityResolver;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter — cookie or Bearer dual-read (issue #1051)")
class JwtAuthenticationFilterTest {

    private static final String COOKIE_NAME = "notaire-auth-token";

    @Mock
    private JwtTokenService jwtTokenService;
    @Mock
    private AuthCookieService authCookieService;
    @Mock
    private UserAuthorityResolver userAuthorityResolver;
    @Mock
    private TokenRevocationService tokenRevocationService;
    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        lenient().when(authCookieService.cookieName()).thenReturn(COOKIE_NAME);
        lenient().when(userAuthorityResolver.resolve(anyString()))
                .thenReturn(Optional.of(List.of(new SimpleGrantedAuthority("ROLE_USER"))));
        filter = new JwtAuthenticationFilter(jwtTokenService, authCookieService, userAuthorityResolver,
                tokenRevocationService);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("should authenticate from auth cookie when Authorization is absent")
    void shouldAuthenticateFromCookieWithoutBearer() throws Exception {
        when(jwtTokenService.isValid("cookie-jwt")).thenReturn(true);
        when(jwtTokenService.extractUsername("cookie-jwt")).thenReturn("admin");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie(COOKIE_NAME, "cookie-jwt"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("admin");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("should authenticate from Bearer when cookie is absent")
    void shouldAuthenticateFromBearerWithoutCookie() throws Exception {
        when(jwtTokenService.isValid("bearer-jwt")).thenReturn(true);
        when(jwtTokenService.extractUsername("bearer-jwt")).thenReturn("admin");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer bearer-jwt");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("admin");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("should prefer Bearer over cookie when both are present")
    void shouldPreferBearerOverCookie() throws Exception {
        when(jwtTokenService.isValid("bearer-jwt")).thenReturn(true);
        when(jwtTokenService.extractUsername("bearer-jwt")).thenReturn("bearer-user");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer bearer-jwt");
        request.setCookies(new Cookie(COOKIE_NAME, "cookie-jwt"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("bearer-user");
    }

    @Test
    @DisplayName("should not authenticate when the user cannot be resolved (issue #559)")
    void shouldNotAuthenticateWhenTheUserCannotBeResolved() throws Exception {
        when(jwtTokenService.isValid("orphan-jwt")).thenReturn(true);
        when(jwtTokenService.extractUsername("orphan-jwt")).thenReturn("ghost");
        when(userAuthorityResolver.resolve("ghost")).thenReturn(Optional.empty());

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer orphan-jwt");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }
    @Test
    @DisplayName("should not authenticate a token revoked by logout (issue #676)")
    void shouldNotAuthenticateRevokedToken() throws Exception {
        when(jwtTokenService.isValid("revoked-jwt")).thenReturn(true);
        when(tokenRevocationService.isRevoked("revoked-jwt")).thenReturn(true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer revoked-jwt");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }
}
