package com.fitnessapp.fitapp_api.calories.service;

import com.fitnessapp.fitapp_api.calories.service.dto.CCActivityRequest;
import com.fitnessapp.fitapp_api.profile.model.UserProfile;


public interface CalorieCalculationService {
    int calculateCalories(UserProfile profile, CCActivityRequest activity);
}
