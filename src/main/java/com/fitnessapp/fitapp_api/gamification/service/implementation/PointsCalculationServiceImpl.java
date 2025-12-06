package com.fitnessapp.fitapp_api.gamification.service.implementation;

import com.fitnessapp.fitapp_api.gamification.dto.PCActivityRequestDTO;
import com.fitnessapp.fitapp_api.gamification.service.PointsCalculationService;
import com.fitnessapp.fitapp_api.gamification.util.PointsConfig;
import org.springframework.stereotype.Service;

@Service
public class PointsCalculationServiceImpl implements PointsCalculationService {
    @Override
    public long calculatePoints(PCActivityRequestDTO pcActivityRequestDTO) {
        double durationMinutes = pcActivityRequestDTO.durationSec() / 60.0;

        if (durationMinutes < PointsConfig.MIN_DURATION_MINUTES.getValue()) {
            return 0;
        }

        if (!isPlausiblePace(pcActivityRequestDTO.distanceKm(), durationMinutes, pcActivityRequestDTO.activityType())) {
            return 0;
        }

        int pointsPerKm = PointsConfig.getPointsPerKm(pcActivityRequestDTO.activityType());
        double distancePoints = pcActivityRequestDTO.distanceKm() * pointsPerKm;

        double totalPoints = distancePoints + PointsConfig.ROUTE_COMPLETED_BONUS.getValue();

        if (pcActivityRequestDTO.dailyGoalCompleted()) {
            totalPoints += PointsConfig.DAILY_GOAL_BONUS.getValue();
        }

        return (long) Math.min(totalPoints, PointsConfig.MAX_PER_DAY.getValue());
    }

    private boolean isPlausiblePace(double distanceKm, double durationMinutes, String activityType) {
        double speedKmH = (distanceKm / durationMinutes) * 60;

        return switch (activityType.toUpperCase()) {
            case "RUNNING_SLOW", "RUNNING_MODERATE", "RUNNING_INTENSE" -> speedKmH >= 4 && speedKmH <= 25;
            case "CYCLING_SLOW", "CYCLING_MODERATE", "CYCLING_INTENSE" -> speedKmH >= 8 && speedKmH <= 50;
            case "WALKING_SLOW", "WALKING_MODERATE", "WALKING_INTENSE" -> speedKmH >= 2 && speedKmH <= 10;
            default -> speedKmH >= 1 && speedKmH <= 30;
        };
    }
}
