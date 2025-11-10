package com.fitnessapp.fitapp_api.route.dto;

import java.math.BigDecimal;

public record RouteResponseDTO(
        Long id,
        String name,
        String startPoint,
        String endPoint,
        BigDecimal distanceKm,
        String userEmail
) {
}
