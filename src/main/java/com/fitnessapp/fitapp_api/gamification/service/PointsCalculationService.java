package com.fitnessapp.fitapp_api.gamification.service;

import com.fitnessapp.fitapp_api.gamification.dto.PCActivityRequestDTO;

public interface PointsCalculationService {
    long calculatePoints(PCActivityRequestDTO pcActivityRequestDTO);
}
