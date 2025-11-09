package com.fitnessapp.fitapp_api.profile.dto;

import com.fitnessapp.fitapp_api.profile.util.Gender;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UserProfileResponseDTO(
        String email,
        String firstName,
        String lastName,
        Gender gender,
        LocalDate birthDate,
        BigDecimal heightCm,
        BigDecimal weightKg
) {
}