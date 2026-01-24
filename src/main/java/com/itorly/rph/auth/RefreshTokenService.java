package com.itorly.rph.auth;

import com.itorly.rph.common.exception.UnauthorizedException;
import com.itorly.rph.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final long refreshValidityInMillis;
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            @Value("${security.jwt.refresh-validity-ms:1209600000}") long refreshValidityInMillis
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshValidityInMillis = refreshValidityInMillis;
    }

    public String issueToken(User user, String deviceInfo, String ip) {
        String rawToken = generateRawToken();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(hashToken(rawToken));
        refreshToken.setExpiresAt(Instant.now().plusMillis(refreshValidityInMillis));
        refreshToken.setDeviceInfo(deviceInfo);
        refreshToken.setIp(ip);

        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }

    @Transactional
    public RefreshTokenRotation rotateToken(String rawToken) {
        RefreshToken existingToken = getValidToken(rawToken);
        existingToken.setRevokedAt(Instant.now());
        refreshTokenRepository.save(existingToken);

        String newToken = issueToken(
                existingToken.getUser(),
                existingToken.getDeviceInfo(),
                existingToken.getIp()
        );
        return new RefreshTokenRotation(existingToken.getUser(), newToken);
    }

    @Transactional
    public void revokeToken(String rawToken) {
        RefreshToken existingToken = getValidToken(rawToken);
        existingToken.setRevokedAt(Instant.now());
        refreshTokenRepository.save(existingToken);
    }

    private RefreshToken getValidToken(String rawToken) {
        String tokenHash = hashToken(rawToken);
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (refreshToken.getRevokedAt() != null) {
            throw new UnauthorizedException("Refresh token revoked");
        }

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new UnauthorizedException("Refresh token expired");
        }

        return refreshToken;
    }

    private String generateRawToken() {
        byte[] bytes = new byte[64];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to hash refresh token", ex);
        }
    }

    public record RefreshTokenRotation(User user, String refreshToken) {
    }
}
