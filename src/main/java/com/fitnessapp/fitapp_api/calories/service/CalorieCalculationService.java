package com.fitnessapp.fitapp_api.calories.service;

import com.fitnessapp.fitapp_api.calories.dto.CCActivityRequest;
import com.fitnessapp.fitapp_api.profile.model.UserProfile;


public interface CalorieCalculationService {
    double calculateCalories(UserProfile profile, CCActivityRequest activity);
    /**
     * Checks if the user has reached their daily calorie goal.
     * Calculates the calories burned for the current day according to the user's profile timezone.
     *
     * @param profile the user profile containing the daily goal and timezone information
     * @return true if the calories burned today are greater than or equal to the daily goal, false otherwise
     */
    boolean hasReachedDailyGoal(UserProfile profile);
