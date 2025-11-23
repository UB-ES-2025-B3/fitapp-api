package com.fitnessapp.fitapp_api.stats.service;

import com.fitnessapp.fitapp_api.stats.dto.EvolutionKcalResponseDTO;

public interface EvolutionKcalService {
    EvolutionKcalResponseDTO getEvolutionKcal(String email, int days);
}
