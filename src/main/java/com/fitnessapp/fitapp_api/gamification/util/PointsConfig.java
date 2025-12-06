package com.fitnessapp.fitapp_api.gamification.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PointsConfig {

    // Correr (mayor esfuerzo = más puntos)
    RUNNING_SLOW_PER_KM(12),
    RUNNING_MODERATE_PER_KM(15),
    RUNNING_INTENSE_PER_KM(18),

    // Ciclismo (menos esfuerzo por km que correr)
    CYCLING_SLOW_PER_KM(4),
    CYCLING_MODERATE_PER_KM(6),
    CYCLING_INTENSE_PER_KM(8),

    // Caminar (menor intensidad)
    WALKING_SLOW_PER_KM(3),
    WALKING_MODERATE_PER_KM(5),
    WALKING_INTENSE_PER_KM(7),

    // Bonus y límites
    ROUTE_COMPLETED_BONUS(10),
    DAILY_GOAL_BONUS(50),
    MAX_PER_SESSION(300),
    MAX_PER_DAY(800),
    MIN_DURATION_MINUTES(5),
    MAX_SESSIONS_PER_DAY(10);

    private final int value;

    public static int getPointsPerKm(String activityType) {
        return switch (activityType.toUpperCase()) {
            case "RUNNING_SLOW" -> RUNNING_SLOW_PER_KM.value;
            case "RUNNING_MODERATE" -> RUNNING_MODERATE_PER_KM.value;
            case "RUNNING_INTENSE" -> RUNNING_INTENSE_PER_KM.value;
            case "CYCLING_SLOW" -> CYCLING_SLOW_PER_KM.value;
            case "CYCLING_MODERATE" -> CYCLING_MODERATE_PER_KM.value;
            case "CYCLING_INTENSE" -> CYCLING_INTENSE_PER_KM.value;
            case "WALKING_SLOW" -> WALKING_SLOW_PER_KM.value;
            case "WALKING_MODERATE" -> WALKING_MODERATE_PER_KM.value;
            case "WALKING_INTENSE" -> WALKING_INTENSE_PER_KM.value;
            default -> 5;
        };
    }
}
