package com.fitnessapp.fitapp_api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterUserRequestDTO(
        @Schema(
                description = "Correo electrónico del usuario",
                example = "usuario@email.com",
                minLength = 5,
                maxLength = 255,
                pattern = "^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$",
                required = true
        )
        @NotBlank(message = "Email is required")
        @Email(message = "Email format is invalid")
        String email,

        @Schema(
                description = "Contraseña con mínimo 8 caracteres, al menos una letra, un número y un símbolo",
                example = "Passw0rd!",
                minLength = 8,
                pattern = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d]).+$",
                required = true
        )
        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d]).+$",
                message = "La contraseña debe tener al menos una letra, un número y un símbolo")
        String password
) {}
