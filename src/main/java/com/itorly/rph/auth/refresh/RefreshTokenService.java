package com.itorly.rph.auth.refresh;

import com.itorly.rph.common.exception.UnauthorizedException;
import com.itorly.rph.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class RefreshTokenService {

    private static final int TOKEN_BYTES = 64;

    private final RefreshTokenRepository refreshTokenRepository;
    private final long validityInMillis;
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            @Value("${security.refresh-token.validity-ms:1209600000}") long validityInMillis
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.validityInMillis = validityInMillis;
    }

    public String issueToken(User user, String deviceInfo, String ip) {
        String rawToken = generateToken();
        String hash = hashToken(rawToken);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(hash);
        refreshToken.setExpiresAt(Instant.now().plusMillis(validityInMillis));
        refreshToken.setDeviceInfo(deviceInfo);
        refreshToken.setIp(ip);
        refreshTokenRepository.save(refreshToken);

        return rawToken;
    }

    @Transactional
    public RotationResult rotateToken(String rawToken, String deviceInfo, String ip) {
        RefreshToken existing = getValidToken(rawToken);
        existing.setRevokedAt(Instant.now());
        refreshTokenRepository.save(existing);

        String newToken = issueToken(existing.getUser(), deviceInfo, ip);
        return new RotationResult(existing.getUser(), newToken);
    }

    @Transactional
    public void revokeToken(String rawToken) {
        RefreshToken refreshToken = refreshTokenRepository
                .findByTokenHash(hashToken(rawToken))
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (refreshToken.getRevokedAt() == null) {
            refreshToken.setRevokedAt(Instant.now());
            refreshTokenRepository.save(refreshToken);
        }
    }

    private RefreshToken getValidToken(String rawToken) {
        RefreshToken refreshToken = refreshTokenRepository
                .findByTokenHash(hashToken(rawToken))
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (refreshToken.getRevokedAt() != null) {
            throw new UnauthorizedException("Refresh token has been revoked");
        }

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new UnauthorizedException("Refresh token has expired");
        }

        return refreshToken;
    }

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    public record RotationResult(User user, String refreshToken) {
    }
}
