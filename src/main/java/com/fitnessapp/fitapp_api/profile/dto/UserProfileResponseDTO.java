package com.fitnessapp.fitapp_api.profile.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UserProfileResponseDTO(
        String email,
        String firstName,
        String lastName,
        LocalDate birthDate,
        BigDecimal heightCm,
        BigDecimal weightKg
) {
}