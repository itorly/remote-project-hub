package com.itorly.rph.auth;

import com.itorly.rph.auth.dto.AuthResponse;
import com.itorly.rph.auth.dto.LoginRequest;
import com.itorly.rph.auth.dto.LogoutRequest;
import com.itorly.rph.auth.dto.RefreshRequest;
import com.itorly.rph.auth.dto.RegisterRequest;
import com.itorly.rph.auth.refresh.RefreshTokenService;
import com.itorly.rph.common.exception.UnauthorizedException;
import com.itorly.rph.security.JwtTokenProvider;
import com.itorly.rph.user.User;
import com.itorly.rph.user.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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
    @Operation(summary = "Register a user", description = "Create a user account and return access and refresh tokens.")
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

        return getAuthResponseResponseEntity(httpServletRequest, user);
    }

    private ResponseEntity<AuthResponse> getAuthResponseResponseEntity(HttpServletRequest httpServletRequest, User user) {
        String accessToken = jwtTokenProvider.generateToken(user.getId(), user.getEmail());
        String refreshToken = refreshTokenService.issueToken(
                user,
                httpServletRequest.getHeader("User-Agent"),
                httpServletRequest.getRemoteAddr()
        );

        AuthResponse response = new AuthResponse(
                accessToken,
                refreshToken,
                user.getId(),
                user.getEmail(),
                user.getDisplayName()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate with email and password to receive tokens.")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpServletRequest
    ) {
        User user = userService.findByEmailOrThrow(request.getEmail());

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        return getAuthResponseResponseEntity(httpServletRequest, user);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Rotate refresh token and issue a new access token.")
    public ResponseEntity<AuthResponse> refresh(
            @Valid @RequestBody RefreshRequest request,
            HttpServletRequest httpServletRequest
    ) {
        RefreshTokenService.RotationResult rotationResult = refreshTokenService.rotateToken(
                request.getRefreshToken(),
                httpServletRequest.getHeader("User-Agent"),
                httpServletRequest.getRemoteAddr()
        );

        User user = rotationResult.user();
        String accessToken = jwtTokenProvider.generateToken(user.getId(), user.getEmail());

        AuthResponse response = new AuthResponse(
                accessToken,
                rotationResult.refreshToken(),
                user.getId(),
                user.getEmail(),
                user.getDisplayName()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Revoke the current refresh token.")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request) {
        refreshTokenService.revokeToken(request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }
}
