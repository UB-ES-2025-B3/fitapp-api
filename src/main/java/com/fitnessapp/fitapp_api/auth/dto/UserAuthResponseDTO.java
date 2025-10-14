package com.fitnessapp.fitapp_api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserAuthResponseDTO(
        @Schema(
                description = "JWT generado para el usuario autenticado",
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        )
        String token,

        @Schema(
                description = "Indica si el usuario ya tiene perfil creado",
                example = "false"
        )
        boolean profileExists
) {}
