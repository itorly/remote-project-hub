package com.itorly.rph.auth;

import com.itorly.rph.common.exception.UnauthorizedException;
import com.itorly.rph.user.User;
import com.itorly.rph.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@Import(RefreshTokenService.class)
@TestPropertySource(properties = "security.jwt.refresh-validity-ms=3600000")
class RefreshTokenServiceTest {

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void refreshTokenRotatesAndRejectsOldToken() {
        User user = createUser("rotator@example.com");
        String token = refreshTokenService.issueToken(user, "Chrome", "127.0.0.1");

        RefreshTokenService.RefreshTokenRotation rotation = refreshTokenService.rotateToken(token);

        assertThat(rotation.refreshToken()).isNotEqualTo(token);
        assertThrows(UnauthorizedException.class, () -> refreshTokenService.rotateToken(token));
        refreshTokenService.rotateToken(rotation.refreshToken());
    }

    @Test
    void revokeTokenPreventsRefresh() {
        User user = createUser("logout@example.com");
        String token = refreshTokenService.issueToken(user, "Firefox", "127.0.0.2");

        refreshTokenService.revokeToken(token);

        assertThrows(UnauthorizedException.class, () -> refreshTokenService.rotateToken(token));
    }

    @Test
    void expiredTokenIsRejected() {
        User user = createUser("expired@example.com");
        String token = refreshTokenService.issueToken(user, "Safari", "127.0.0.3");

        RefreshToken storedToken = refreshTokenRepository.findAll().get(0);
        storedToken.setExpiresAt(Instant.now().minusSeconds(10));
        refreshTokenRepository.save(storedToken);

        assertThrows(UnauthorizedException.class, () -> refreshTokenService.rotateToken(token));
    }

    private User createUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash("hashed");
        user.setDisplayName("Test User");
        user.setTimezone("UTC");
        return userRepository.save(user);
    }
}
