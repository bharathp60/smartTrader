package com.smarttrader.controller;

import com.smarttrader.dto.auth.LoginRequest;
import com.smarttrader.dto.auth.LoginResponse;
import com.smarttrader.security.JwtTokenProvider;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final Clock clock;

    public AuthController(AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider, Clock clock) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.clock = clock;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        String jwt = tokenProvider.generateToken(authentication.getName(), roles);
        
        Instant expiresAt = clock.instant().plus(24, ChronoUnit.HOURS);

        return ResponseEntity.ok(new LoginResponse(jwt, authentication.getName(), roles, expiresAt));
    }
}
