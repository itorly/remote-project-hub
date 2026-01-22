package com.itorly.rph.auth.refresh;

import com.itorly.rph.common.exception.UnauthorizedException;
import com.itorly.rph.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private RefreshTokenService refreshTokenService;
    private final Map<String, RefreshToken> tokenStore = new HashMap<>();

    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenService(refreshTokenRepository, 60_000L);

        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> {
                    RefreshToken token = invocation.getArgument(0);
                    tokenStore.put(token.getTokenHash(), token);
                    return token;
                });

        when(refreshTokenRepository.findByTokenHash(any(String.class)))
                .thenAnswer(invocation -> {
                    String hash = invocation.getArgument(0);
                    return Optional.ofNullable(tokenStore.get(hash));
                });
    }

    @Test
    void rotateToken_issuesNewToken_andRevokesOld() {
        User user = new User();
        user.setId(1L);
        user.setEmail("rotator@example.com");

        String rawToken = refreshTokenService.issueToken(user, "UnitTest", "127.0.0.1");
        String oldHash = tokenStore.keySet().iterator().next();

        RefreshTokenService.RotationResult result = refreshTokenService.rotateToken(
                rawToken,
                "UnitTest",
                "127.0.0.1"
        );

        assertThat(result.refreshToken()).isNotEqualTo(rawToken);
        assertThat(tokenStore).hasSize(2);
        RefreshToken oldToken = tokenStore.get(oldHash);
        assertThat(oldToken.getRevokedAt()).isNotNull();
    }

    @Test
    void revokeToken_marksTokenRevoked() {
        User user = new User();
        user.setId(2L);
        user.setEmail("logout@example.com");

        String rawToken = refreshTokenService.issueToken(user, "UnitTest", "127.0.0.1");
        String tokenHash = tokenStore.keySet().iterator().next();

        refreshTokenService.revokeToken(rawToken);

        RefreshToken storedToken = tokenStore.get(tokenHash);
        assertThat(storedToken.getRevokedAt()).isNotNull();
    }

    @Test
    void rotateToken_rejectsExpiredToken() {
        User user = new User();
        user.setId(3L);
        user.setEmail("expired@example.com");

        String rawToken = refreshTokenService.issueToken(user, "UnitTest", "127.0.0.1");
        String tokenHash = tokenStore.keySet().iterator().next();
        RefreshToken storedToken = tokenStore.get(tokenHash);
        storedToken.setExpiresAt(Instant.now().minusSeconds(10));

        assertThrows(UnauthorizedException.class, () ->
                refreshTokenService.rotateToken(rawToken, "UnitTest", "127.0.0.1")
        );
    }
}
