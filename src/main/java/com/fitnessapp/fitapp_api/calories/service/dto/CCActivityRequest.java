package com.fitnessapp.fitapp_api.calories.service.dto;

public record CCActivityRequest(
    String activityType,
    long duration
) {
}
