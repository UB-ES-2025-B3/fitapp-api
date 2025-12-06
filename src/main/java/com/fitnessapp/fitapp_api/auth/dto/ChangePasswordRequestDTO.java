package com.fitnessapp.fitapp_api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequestDTO(

        @Schema(
                description = "Contraseña actual del usuario",
                example = "OldPass123!",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Current password is required")
        String currentPassword,

        @Schema(
                description = "Nueva contraseña con mínimo 8 caracteres, al menos una letra, un número y un símbolo",
                example = "NewPass123!",
                minLength = 8,
                pattern = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d]).+$",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "New password is required")
        @Size(min = 8, message = "New password must be at least 8 characters long")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d]).+$",
                message = "Password must contain at least one letter, one number, and one symbol"
        )
        String newPassword,

        @Schema(
                description = "Confirmación de la nueva contraseña. Debe coincidir con 'newPassword'",
                example = "NewPass123!",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Password confirmation is required")
        String confirmPassword
) {
}
