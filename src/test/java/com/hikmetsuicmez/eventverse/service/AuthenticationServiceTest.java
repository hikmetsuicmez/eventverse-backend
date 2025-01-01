package com.hikmetsuicmez.eventverse.service;

import com.hikmetsuicmez.eventverse.dto.request.AuthenticationRequest;
import com.hikmetsuicmez.eventverse.dto.request.RegisterRequest;
import com.hikmetsuicmez.eventverse.dto.response.AuthenticationResponse;
import com.hikmetsuicmez.eventverse.entity.User;
import com.hikmetsuicmez.eventverse.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService authenticationService;

    private RegisterRequest registerRequest;
    private AuthenticationRequest authenticationRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .email("test@example.com")
                .password("Test123!")
                .firstName("Test")
                .lastName("User")
                .phoneNumber("+905555555555")
                .build();

        authenticationRequest = AuthenticationRequest.builder()
                .email("test@example.com")
                .password("Test123!")
                .build();

        testUser = User.builder()
                .email(registerRequest.getEmail())
                .password("encodedPassword")
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .phoneNumber(registerRequest.getPhoneNumber())
                .build();
    }

    @Test
    void register_ShouldCreateUserAndReturnTokens() {
        // Arrange
        String token = "test.jwt.token";
        String refreshToken = "test.refresh.token";
        
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtService.generateToken(any(User.class))).thenReturn(token);
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn(refreshToken);

        // Act
        AuthenticationResponse response = authenticationService.register(registerRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo(token);
        assertThat(response.getRefreshToken()).isEqualTo(refreshToken);
        
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(any(User.class));
        verify(jwtService).generateRefreshToken(any(User.class));
    }

    @Test
    void authenticate_ShouldAuthenticateAndReturnTokens() {
        // Arrange
        String token = "test.jwt.token";
        String refreshToken = "test.refresh.token";
        
        when(userRepository.findByEmail(authenticationRequest.getEmail()))
                .thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(testUser)).thenReturn(token);
        when(jwtService.generateRefreshToken(testUser)).thenReturn(refreshToken);

        // Act
        AuthenticationResponse response = authenticationService.authenticate(authenticationRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo(token);
        assertThat(response.getRefreshToken()).isEqualTo(refreshToken);
        
        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken(
                        authenticationRequest.getEmail(),
                        authenticationRequest.getPassword()
                )
        );
    }

    @Test
    void refreshToken_ShouldGenerateNewAccessToken() {
        // Arrange
        String oldRefreshToken = "old.refresh.token";
        String newAccessToken = "new.access.token";
        String email = "test@example.com";
        
        when(jwtService.extractUsername(oldRefreshToken)).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(jwtService.isTokenValid(oldRefreshToken, testUser)).thenReturn(true);
        when(jwtService.generateToken(testUser)).thenReturn(newAccessToken);

        // Act
        AuthenticationResponse response = authenticationService.refreshToken(oldRefreshToken);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo(newAccessToken);
        assertThat(response.getRefreshToken()).isEqualTo(oldRefreshToken);
        
        verify(jwtService).extractUsername(oldRefreshToken);
        verify(jwtService).isTokenValid(oldRefreshToken, testUser);
        verify(jwtService).generateToken(testUser);
    }
} 