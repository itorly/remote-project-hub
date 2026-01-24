package com.itorly.rph.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorly.rph.auth.dto.LoginRequest;
import com.itorly.rph.auth.dto.RefreshTokenRequest;
import com.itorly.rph.auth.dto.RegisterRequest;
import com.itorly.rph.security.CustomUserDetailsService;
import com.itorly.rph.security.JwtTokenProvider;
import com.itorly.rph.user.User;
import com.itorly.rph.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = AuthController.class,
        excludeAutoConfiguration = { SecurityAutoConfiguration.class }
)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private RefreshTokenService refreshTokenService;

    @Test
    void register_returnsAuthResponse() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("alice@example.com");
        request.setPassword("password123");
        request.setDisplayName("Alice");
        request.setTimezone("UTC");

        User createdUser = new User();
        createdUser.setId(5L);
        createdUser.setEmail("alice@example.com");
        createdUser.setDisplayName("Alice");
        createdUser.setTimezone("UTC");

        when(userService.registerUser(
                request.getEmail(),
                request.getPassword(),
                request.getDisplayName(),
                request.getTimezone()
        )).thenReturn(createdUser);

        when(jwtTokenProvider.generateToken(5L, "alice@example.com"))
                .thenReturn("jwt-token");
        when(refreshTokenService.issueToken(createdUser, "TestAgent", "127.0.0.1"))
                .thenReturn("refresh-token");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("User-Agent", "TestAgent")
                        .with(requestBuilder -> {
                            requestBuilder.setRemoteAddr("127.0.0.1");
                            return requestBuilder;
                        })
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.userId").value(5L))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.displayName").value("Alice"));
    }

    @Test
    void login_returnsAuthResponse_whenCredentialsValid() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("bob@example.com");
        request.setPassword("secret");

        User user = new User();
        user.setId(7L);
        user.setEmail("bob@example.com");
        user.setDisplayName("Bob");
        user.setPasswordHash("encoded-secret");

        when(userService.findByEmailOrThrow("bob@example.com")).thenReturn(user);
        when(passwordEncoder.matches("secret", "encoded-secret")).thenReturn(true);
        when(jwtTokenProvider.generateToken(7L, "bob@example.com")).thenReturn("token-123");
        when(refreshTokenService.issueToken(user, "TestAgent", "127.0.0.1"))
                .thenReturn("refresh-token");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("User-Agent", "TestAgent")
                        .with(requestBuilder -> {
                            requestBuilder.setRemoteAddr("127.0.0.1");
                            return requestBuilder;
                        })
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("token-123"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.userId").value(7L))
                .andExpect(jsonPath("$.email").value("bob@example.com"))
                .andExpect(jsonPath("$.displayName").value("Bob"));
    }

    @Test
    void login_returnsUnauthorized_whenPasswordInvalid() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("charlie@example.com");
        request.setPassword("wrong");

        User user = new User();
        user.setId(9L);
        user.setEmail("charlie@example.com");
        user.setDisplayName("Charlie");
        user.setPasswordHash("encoded-password");

        when(userService.findByEmailOrThrow("charlie@example.com")).thenReturn(user);
        when(passwordEncoder.matches("wrong", "encoded-password")).thenReturn(false);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Invalid email or password"))
                .andExpect(jsonPath("$.path").value("/api/auth/login"));
    }

    @Test
    void login_returnsBadRequest_whenMissingCredentials() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors").value(containsInAnyOrder(
                        "email must not be blank",
                        "password must not be blank"
                )));
    }

    @Test
    void refresh_returnsAuthTokenResponse() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("refresh-token");

        User user = new User();
        user.setId(11L);
        user.setEmail("dana@example.com");

        when(refreshTokenService.rotateToken("refresh-token"))
                .thenReturn(new RefreshTokenService.RefreshTokenRotation(user, "new-refresh-token"));
        when(jwtTokenProvider.generateToken(11L, "dana@example.com"))
                .thenReturn("new-access-token");

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));
    }

    @Test
    void logout_revokesRefreshToken() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("refresh-token");

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }
}
