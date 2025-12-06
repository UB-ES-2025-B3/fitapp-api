package com.fitnessapp.fitapp_api.calories.dto;

public record CCActivityRequest(
    String activityType,
    long duration
) {
}
