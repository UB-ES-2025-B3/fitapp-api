package com.fitnessapp.fitapp_api.stats.dto;

import java.util.List;

public record EvolutionKcalResponseDTO(
        List<DailyKcalResponseDTO> checkpoints
) {
}
