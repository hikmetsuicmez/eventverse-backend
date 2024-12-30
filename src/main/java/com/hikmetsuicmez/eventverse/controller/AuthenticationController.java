package com.hikmetsuicmez.eventverse.controller;

import com.hikmetsuicmez.eventverse.dto.request.AuthenticationRequest;
import com.hikmetsuicmez.eventverse.dto.request.RegisterRequest;
import com.hikmetsuicmez.eventverse.dto.response.AuthenticationResponse;
import com.hikmetsuicmez.eventverse.service.AuthenticationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody @Valid RegisterRequest request
    ) {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody @Valid AuthenticationRequest request
    ) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refresh(
            @RequestHeader("Authorization") String refreshToken
    ) {
        if (refreshToken != null && refreshToken.startsWith("Bearer ")) {
            refreshToken = refreshToken.substring(7);
            return ResponseEntity.ok(authenticationService.refreshToken(refreshToken));
        }
        throw new RuntimeException("Invalid refresh token header");
    }
} 