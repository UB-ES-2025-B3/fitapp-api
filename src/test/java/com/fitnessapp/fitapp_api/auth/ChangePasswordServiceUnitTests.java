package com.fitnessapp.fitapp_api.auth;

import com.fitnessapp.fitapp_api.auth.dto.ChangePasswordRequestDTO;
import com.fitnessapp.fitapp_api.auth.dto.UserAuthResponseDTO;
import com.fitnessapp.fitapp_api.auth.model.UserAuth;
import com.fitnessapp.fitapp_api.auth.repository.UserAuthRepository;
import com.fitnessapp.fitapp_api.auth.service.implementation.UserAuthServiceImpl;
import com.fitnessapp.fitapp_api.core.exception.UserAuthNotFoundException;
import com.fitnessapp.fitapp_api.core.util.JwtUtils;
import com.fitnessapp.fitapp_api.profile.repository.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChangePasswordServiceUnitTests {

    @Mock private UserAuthRepository userAuthRepository;
    @Mock private UserProfileRepository userProfileRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtUtils jwtUtils;

    @InjectMocks
    private UserAuthServiceImpl service;

    private UserAuth user;

    @BeforeEach
    void setUp() {
        user = new UserAuth();
        user.setId(1L);
        user.setEmail("tester@example.com");
        user.setPassword("encodedOldPassword");
    }

    // ============================================
    // SUCCESS CASE
    // ============================================

    @Test
    @DisplayName("changePassword — OK → actualiza password y devuelve nuevo token")
    void changePassword_ShouldUpdatePassword() {

        ChangePasswordRequestDTO req = new ChangePasswordRequestDTO(
                "OldPass123!",
                "NewPass123!",
                "NewPass123!"
        );

        when(userAuthRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("OldPass123!", "encodedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode("NewPass123!")).thenReturn("encodedNewPassword");

        Authentication authMock = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authMock);

        when(jwtUtils.createToken(authMock)).thenReturn("NEW_JWT_TOKEN");

        UserAuthResponseDTO result = service.changePassword(user.getEmail(), req);

        assertEquals(1L, result.id());
        assertEquals("NEW_JWT_TOKEN", result.token());
        assertFalse(result.profileExists());

        verify(userAuthRepository).save(user);
        assertEquals("encodedNewPassword", user.getPassword());
    }

    // ============================================
    // USER NOT FOUND
    // ============================================

    @Test
    @DisplayName("changePassword — usuario no existe → lanza UserAuthNotFoundException")
    void changePassword_UserNotFound_ShouldThrow() {

        ChangePasswordRequestDTO req = new ChangePasswordRequestDTO(
                "OldPass123!",
                "NewPass123!",
                "NewPass123!"
        );

        when(userAuthRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());

        assertThrows(UserAuthNotFoundException.class,
                () -> service.changePassword(user.getEmail(), req));
    }

    // ============================================
    // WRONG CURRENT PASSWORD
    // ============================================

    @Test
    @DisplayName("changePassword — contraseña actual incorrecta → IllegalArgumentException")
    void changePassword_WrongCurrentPassword_ShouldThrow() {

        ChangePasswordRequestDTO req = new ChangePasswordRequestDTO(
                "WrongPassword!",
                "NewPass123!",
                "NewPass123!"
        );

        when(userAuthRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPassword!", user.getPassword())).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> service.changePassword(user.getEmail(), req));
    }

    // ============================================
    // NEW PASSWORD CONFIRMATION MISMATCH
    // ============================================

    @Test
    @DisplayName("changePassword — confirmación no coincide → IllegalArgumentException")
    void changePassword_NewPasswordMismatch_ShouldThrow() {

        ChangePasswordRequestDTO req = new ChangePasswordRequestDTO(
                "OldPass123!",
                "NewPass123!",
                "DifferentPass!"
        );

        when(userAuthRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("OldPass123!", user.getPassword())).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> service.changePassword(user.getEmail(), req));
    }

    // ============================================
    // NEW PASSWORD DOES NOT MEET REQUIREMENTS
    // ============================================

    @Test
    @DisplayName("changePassword — nueva password no cumple requisitos → IllegalArgumentException")
    void changePassword_NewPasswordInvalid_ShouldThrow() {

        ChangePasswordRequestDTO req = new ChangePasswordRequestDTO(
                "OldPass123!",
                "short",
                "short"
        );

        when(userAuthRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("OldPass123!", user.getPassword())).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> service.changePassword(user.getEmail(), req));
    }
}
