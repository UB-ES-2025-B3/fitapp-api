package com.fitnessapp.fitapp_api.profile.dto;

import com.fitnessapp.fitapp_api.profile.util.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UserProfileRequestDTO(

        @Schema(
                description = "Primer nombre del usuario",
                example = "Pedro",
                minLength = 1,
                maxLength = 100,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name must be 100 characters or less")
        String firstName,

        @Schema(
                description = "Apellido del usuario",
                example = "González",
                minLength = 1,
                maxLength = 100,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Last name is required")
        @Size(max = 100, message = "Last name must be 100 characters or less")
        String lastName,

        @Schema(
                description = "Género biologico del usuario",
                example = "MALE",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Gender is required")
        Gender gender,

        @Schema(
                description = "Fecha de nacimiento del usuario (formato AAAA-MM-DD)",
                example = "2004-11-17",
                type = "string",
                format = "date",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Birth date is required")
        @Past(message = "Birth date must be in the past, you are not a time traveler!")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate birthDate,

        @Schema(
                description = "Altura del usuario en centímetros",
                example = "175.50",
                minimum = "50.0",
                maximum = "300.0",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Height is required")
        @DecimalMin(value = "50.0", message = "Height must be at least 50 cm")
        @DecimalMax(value = "300.0", message = "Height cannot exceed 300 cm")
        @Digits(integer = 3, fraction = 2, message = "Height must be a valid number with up to 3 digits and 2 decimal places")
        BigDecimal heightCm,

        @Schema(
                description = "Peso del usuario en kilogramos",
                example = "72.35",
                minimum = "20.0",
                maximum = "500.0",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Weight is required")
        @DecimalMin(value = "20.0", message = "Weight must be at least 20 kg")
        @DecimalMax(value = "500.0", message = "Weight cannot exceed 500 kg")
        @Digits(integer = 3, fraction = 2, message = "Weight must be a valid number with up to 3 digits and 2 decimal places")
        BigDecimal weightKg
) {
}