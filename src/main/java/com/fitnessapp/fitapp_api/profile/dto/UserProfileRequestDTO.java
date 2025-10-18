package com.fitnessapp.fitapp_api.profile.dto;

import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UserProfileRequestDTO(

        //@NotBlank(message = "Email is required")
        //@Email(message = "Email format is invalid")
        //String email,

        @NotBlank(message = "First name is required")
        String firstName,

        @NotBlank(message = "Last name is required")
        String lastName,

        @Past(message = "Birth date must be in the past, you are not a time traveler!")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate birthDate,

        @DecimalMin(value = "0.0", inclusive = false, message = "Height must be greater than 0")
        @Digits(integer = 3, fraction = 2, message = "Height must be a valid number with up to 3 digits and 2 decimal places")
        BigDecimal heightCm,

        @DecimalMin(value = "0.0", inclusive = false, message = "Weight must be greater than 0")
        @Digits(integer = 3, fraction = 2, message = "Weight must be a valid number with up to 3 digits and 2 decimal places")
        BigDecimal weightKg
) {
}
