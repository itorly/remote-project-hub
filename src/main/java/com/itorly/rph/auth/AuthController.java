package com.itorly.rph.auth;

import com.itorly.rph.auth.dto.AuthResponse;
import com.itorly.rph.auth.dto.AuthTokenResponse;
import com.itorly.rph.auth.dto.LoginRequest;
import com.itorly.rph.auth.dto.RefreshTokenRequest;
import com.itorly.rph.auth.dto.RegisterRequest;
import com.itorly.rph.common.exception.UnauthorizedException;
import com.itorly.rph.security.JwtTokenProvider;
import com.itorly.rph.user.User;
import com.itorly.rph.user.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication and token management endpoints.")
public class AuthController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    public AuthController(
            UserService userService,
            JwtTokenProvider jwtTokenProvider,
            PasswordEncoder passwordEncoder,
            RefreshTokenService refreshTokenService
    ) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a user", description = "Create a user account and return an access token.")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpServletRequest
    ) {
        User user = userService.registerUser(
                request.getEmail(),
                request.getPassword(),
                request.getDisplayName(),
                request.getTimezone()
        );

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail());
        String refreshToken = refreshTokenService.issueToken(
                user,
                httpServletRequest.getHeader("User-Agent"),
                httpServletRequest.getRemoteAddr()
        );

        AuthResponse response = new AuthResponse(
                token,
                refreshToken,
                user.getId(),
                user.getEmail(),
                user.getDisplayName()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate with email and password to receive an access token.")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpServletRequest
    ) {
        User user = userService.findByEmailOrThrow(request.getEmail());

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            // In a real app, you’d use a custom exception and 401 status
            throw new UnauthorizedException("Invalid email or password");
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail());
        String refreshToken = refreshTokenService.issueToken(
                user,
                httpServletRequest.getHeader("User-Agent"),
                httpServletRequest.getRemoteAddr()
        );

        AuthResponse response = new AuthResponse(
                token,
                refreshToken,
                user.getId(),
                user.getEmail(),
                user.getDisplayName()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Rotate refresh token and issue a new access token.")
    public ResponseEntity<AuthTokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        RefreshTokenService.RefreshTokenRotation rotation = refreshTokenService.rotateToken(request.getRefreshToken());
        String token = jwtTokenProvider.generateToken(rotation.user().getId(), rotation.user().getEmail());
        return ResponseEntity.ok(new AuthTokenResponse(token, rotation.refreshToken()));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout current session", description = "Revoke the refresh token for the current session.")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        refreshTokenService.revokeToken(request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }
}
