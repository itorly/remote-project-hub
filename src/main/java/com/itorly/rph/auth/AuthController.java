package com.itorly.rph.auth;

import com.itorly.rph.auth.dto.AuthResponse;
import com.itorly.rph.auth.dto.LoginRequest;
import com.itorly.rph.auth.dto.RegisterRequest;
import com.itorly.rph.security.JwtTokenProvider;
import com.itorly.rph.user.User;
import com.itorly.rph.user.UserService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public AuthController(
            UserService userService,
            JwtTokenProvider jwtTokenProvider,
            PasswordEncoder passwordEncoder
    ) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.registerUser(
                request.getEmail(),
                request.getPassword(),
                request.getDisplayName(),
                request.getTimezone()
        );

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail());

        AuthResponse response = new AuthResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getDisplayName()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = userService.findByEmailOrThrow(request.getEmail());

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            // In a real app, you’d use a custom exception and 401 status
            throw new IllegalArgumentException("Invalid email or password");
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail());

        AuthResponse response = new AuthResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getDisplayName()
        );
        return ResponseEntity.ok(response);
    }
}
