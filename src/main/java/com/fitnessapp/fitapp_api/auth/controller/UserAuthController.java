package com.fitnessapp.fitapp_api.auth.controller;

import com.fitnessapp.fitapp_api.auth.dto.RegisterUserRequestDTO;
import com.fitnessapp.fitapp_api.auth.dto.UserAuthResponseDTO;
import com.fitnessapp.fitapp_api.auth.service.IUserAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class UserAuthController {

    private final IUserAuthService userAuthService;

    @Operation(
            summary = "Registrar nuevo usuario",
            description = "Registra un nuevo usuario y retorna un token JWT para autenticación.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content (
                            mediaType = "application/json",
                            schema = @Schema(implementation = RegisterUserRequestDTO.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Usuario registrado exitosamente",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = UserAuthResponseDTO.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "El email ya está en uso",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Datos de entrada inválidos",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    // Endpoint for user registration
    @PostMapping("/register")
    public ResponseEntity<UserAuthResponseDTO> register(@Valid @RequestBody RegisterUserRequestDTO registerUserRequestDTO) {
        return new ResponseEntity<>(this.userAuthService.register(registerUserRequestDTO), HttpStatus.CREATED);
    }
}
