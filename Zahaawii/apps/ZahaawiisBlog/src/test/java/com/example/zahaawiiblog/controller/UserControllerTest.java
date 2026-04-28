package com.example.zahaawiiblog.controller;

import com.example.zahaawiiblog.DTO.AuthResponseDTO;
import com.example.zahaawiiblog.DTO.SignupRequest;
import com.example.zahaawiiblog.logginFeature.service.LoggingService;
import com.example.zahaawiiblog.securityFeature.DTO.AuthResponse;
import com.example.zahaawiiblog.securityFeature.DTO.CurrentUserResponse;
import com.example.zahaawiiblog.securityFeature.Entity.AuthRequest;
import com.example.zahaawiiblog.securityFeature.Entity.UserInfo;
import com.example.zahaawiiblog.securityFeature.service.JwtService;
import com.example.zahaawiiblog.securityFeature.service.UserInfoService;
import com.example.zahaawiiblog.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;

import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserControllerTest {

    private StubUserService userService;
    private StubUserInfoService userInfoService;
    private StubJwtService jwtService;
    private StubAuthenticationManager authenticationManager;
    private NoOpLoggingService loggingService;
    private UserController controller;

    @BeforeEach
    void setUp() {
        userService = new StubUserService();
        userInfoService = new StubUserInfoService();
        jwtService = new StubJwtService();
        authenticationManager = new StubAuthenticationManager();
        loggingService = new NoOpLoggingService();
        controller = new UserController(userService, userInfoService, jwtService, authenticationManager, loggingService);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createUserSetsCookieAndReturnsAuthResponse() {
        SignupRequest request = new SignupRequest("alice", "alice@example.com", "secret");

        ResponseEntity<?> response = controller.createUser(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(AuthResponseDTO.class, response.getBody());

        AuthResponseDTO body = (AuthResponseDTO) response.getBody();
        assertEquals("alice", body.name());
        assertEquals(1800L, body.expiresIn());
        assertEquals("AUTH_TOKEN=jwt-token; Path=/", response.getHeaders().getFirst(HttpHeaders.SET_COOKIE));

        UserInfo saved = userInfoService.lastSavedUser;
        assertNotNull(saved);
        assertEquals("alice", saved.getName());
        assertEquals("alice@example.com", saved.getEmail());
        assertEquals("USER", saved.getRoles());
    }

    @Test
    void deleteUserRejectsAnonymousRequest() {
        ResponseEntity<?> response = controller.deleteUserById(1);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("User not logged in", response.getBody());
    }

    @Test
    void deleteUserRejectsDeletingAnotherAccount() {
        UserInfo currentUser = user(1L, "owner");
        authenticate("owner");
        userService.usersByName.put("owner", currentUser);

        ResponseEntity<?> response = controller.deleteUserById(2);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("You are not allowed to delete this user", response.getBody());
    }

    @Test
    void loginSetsCookieAndReturnsUsername() {
        AuthRequest request = new AuthRequest("alice", "secret");
        authenticationManager.nextAuthentication = new UsernamePasswordAuthenticationToken("alice", "secret", List.of());
        userService.usersByName.put("alice", user(10L, "alice"));

        ResponseEntity<AuthResponse> response = controller.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("AUTH_TOKEN=jwt-token; Path=/", response.getHeaders().getFirst(HttpHeaders.SET_COOKIE));
        assertEquals("alice", response.getBody().username());
        assertEquals(1800L, response.getBody().expiresIn());
    }

    @Test
    void logoutClearsCookieAndReturnsNoContent() {
        UserInfo user = user(11L, "alice");
        userService.usersByName.put("alice", user);
        authenticate("alice");

        ResponseEntity<Void> response = controller.logout();

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertEquals("AUTH_TOKEN=; Path=/", response.getHeaders().getFirst(HttpHeaders.SET_COOKIE));
    }

    @Test
    void currentUserReturnsAuthenticatedProfile() {
        UserInfo user = user(5L, "alice");
        user.setImgPath("alice.jpeg");
        userService.usersByName.put("alice", user);
        authenticate("alice");

        ResponseEntity<CurrentUserResponse> response = controller.currentUser();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(5L, response.getBody().userId());
        assertEquals("alice", response.getBody().username());
        assertEquals("alice.jpeg", response.getBody().imgPath());
    }

    @Test
    void csrfEndpointReturnsExpectedShape() {
        CsrfToken token = new StubCsrfToken("token-value", "X-XSRF-TOKEN", "_csrf");

        ResponseEntity<Map<String, String>> response = controller.csrf(token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("token-value", response.getBody().get("token"));
        assertEquals("X-XSRF-TOKEN", response.getBody().get("headerName"));
        assertEquals("_csrf", response.getBody().get("parameterName"));
    }

    private static UserInfo user(Long id, String username) {
        return UserInfo.builder()
                .userId(id)
                .name(username)
                .password("encoded")
                .createdDate(Date.valueOf(LocalDate.now()))
                .roles("USER")
                .build();
    }

    private static void authenticate(String username) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(username, "password", List.of()));
        SecurityContextHolder.setContext(context);
    }

    private static final class StubUserService extends UserService {
        private final Map<String, UserInfo> usersByName = new HashMap<>();
        private long deleteResult = 1;

        private StubUserService() {
            super(null);
        }

        @Override
        public Optional<UserInfo> findUserByUsername(String username) {
            return Optional.ofNullable(usersByName.get(username));
        }

        @Override
        public long deleteUserById(long userId) {
            return deleteResult;
        }
    }

    private static final class StubUserInfoService extends UserInfoService {
        private UserInfo lastSavedUser;

        private StubUserInfoService() {
            super(null, null);
        }

        @Override
        public String addUser(UserInfo userInfo) {
            userInfo.setUserId(99L);
            lastSavedUser = userInfo;
            return "ok";
        }
    }

    private static final class StubJwtService extends JwtService {
        @Override
        public String generateToken(String username) {
            return "jwt-token";
        }

        @Override
        public long getExpirationSeconds() {
            return 1800L;
        }

        @Override
        public ResponseCookie buildAuthCookie(String token) {
            return ResponseCookie.from("AUTH_TOKEN", token).path("/").build();
        }

        @Override
        public ResponseCookie clearAuthCookie() {
            return ResponseCookie.from("AUTH_TOKEN", "").path("/").build();
        }
    }

    private static final class StubAuthenticationManager implements AuthenticationManager {
        private Authentication nextAuthentication;

        @Override
        public Authentication authenticate(Authentication authentication) {
            return nextAuthentication;
        }
    }

    private static final class NoOpLoggingService extends LoggingService {
        private NoOpLoggingService() {
            super(null);
        }

        @Override
        public void log(Long userId, String test, String username, Long postId) {
        }
    }

    private record StubCsrfToken(String token, String headerName, String parameterName) implements CsrfToken {
        @Override
        public String getHeaderName() {
            return headerName;
        }

        @Override
        public String getParameterName() {
            return parameterName;
        }

        @Override
        public String getToken() {
            return token;
        }
    }
}
