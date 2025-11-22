package com.fitnessapp.fitapp_api.stats.dto;

public record DailyKcalResponseDTO(
        String date,
        double caloriesKcal
) {
}
