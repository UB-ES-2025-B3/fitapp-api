package com.fitnessapp.fitapp_api.profile.dto;

import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UserProfileRequestDTO(

        @NotBlank(message = "First name is required")
        String firstName,

        @NotBlank(message = "Last name is required")
        String lastName,

        @NotNull(message = "Birth date is required")
        @Past(message = "Birth date must be in the past, you are not a time traveler!")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate birthDate,

        @NotNull(message = "Height is required")
        @DecimalMin(value = "50.0", message = "Height must be at least 50 cm")
        @DecimalMax(value = "300.0", message = "Height cannot exceed 300 cm")
        @Digits(integer = 3, fraction = 2, message = "Height must be a valid number with up to 3 digits and 2 decimal places")
        BigDecimal heightCm,

        @NotNull(message = "Weight is required")
        @DecimalMin(value = "20.0", message = "Weight must be at least 20 kg")
        @DecimalMax(value = "500.0", message = "Weight cannot exceed 500 kg")
        @Digits(integer = 3, fraction = 2, message = "Weight must be a valid number with up to 3 digits and 2 decimal places")
        BigDecimal weightKg
) {
}