package com.fitnessapp.fitapp_api.route.dto;

import java.math.BigDecimal;
import java.util.List;

public record RouteResponseDTO(
        Long id,
        String name,
        String startPoint,
        String endPoint,
        BigDecimal distanceKm,
        String userEmail,
        List<CheckpointResponseDTO> checkpoints
) {
}
