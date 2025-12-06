package com.fitnessapp.fitapp_api.gamification.dto;

public record PCActivityRequestDTO(
        double distanceKm,
        long durationSec,
        String activityType,
        boolean dailyGoalCompleted
) {}
