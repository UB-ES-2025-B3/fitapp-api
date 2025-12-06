package com.fitnessapp.fitapp_api.gamification.service;

import com.fitnessapp.fitapp_api.gamification.dto.PCActivityRequestDTO;
import com.fitnessapp.fitapp_api.routeexecution.model.RouteExecution;

public interface PointsCalculationService {
    long calculatePoints(PCActivityRequestDTO pcActivityRequestDTO);
}
