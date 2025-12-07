package com.fitnessapp.fitapp_api.calories.service.implementation;

import com.fitnessapp.fitapp_api.calories.service.CalorieCalculationService;
import com.fitnessapp.fitapp_api.calories.dto.CCActivityRequest;
import com.fitnessapp.fitapp_api.core.exception.UserProfileNotCompletedException;
import com.fitnessapp.fitapp_api.profile.model.UserProfile;
import com.fitnessapp.fitapp_api.profile.service.UserProfileService;
import com.fitnessapp.fitapp_api.routeexecution.model.RouteExecution;
import com.fitnessapp.fitapp_api.routeexecution.repository.RouteExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CalorieCalculationServiceImpl implements CalorieCalculationService {
    private final UserProfileService userProfileService;
    private final RouteExecutionRepository routeExecutionRepository;

    public double calculateCalories(UserProfile profile, CCActivityRequest activity) {
        if (!userProfileService.isProfileComplete(profile)) {
            throw new UserProfileNotCompletedException("User profile is not completed.");
        }
        double bmr = calculateBMR(
                profile.getGender().toString(),
                profile.getWeightKg().doubleValue(),
                profile.getHeightCm().doubleValue(),
                getAge(profile.getBirthDate())
        );

        double met = getMET(activity.activityType());

        return calculateCaloriesByActivity(bmr, met, activity.duration());
    }

    private double calculateBMR(String gender, double weight, double height, int age) {
        if (gender.equals("MALE")) {
            return (10 * weight) + (6.25 * height) - (5 * age) + 5;
        } else if (gender.equals("FEMALE")) {
            return (10 * weight) + (6.25 * height) - (5 * age) - 161;
        } else {
            throw new IllegalArgumentException("Invalid gender: " + gender);
        }
    }

    private double getMET(String activityType) {
        return switch (activityType.toUpperCase()) {
            case "RUNNING_SLOW" -> 8.3;
            case "RUNNING_MODERATE" -> 9.8;
            case "RUNNING_INTENSE" -> 11.8;
            case "CYCLING_SLOW" -> 4.3;
            case "CYCLING_MODERATE" -> 7.0;
            case "CYCLING_INTENSE" -> 9.0;
            case "WALKING_SLOW" -> 2.0;
            case "WALKING_MODERATE" -> 3.5;
            case "WALKING_INTENSE" -> 5.0;
            default -> throw new IllegalArgumentException("Unknown activity type: " + activityType);
        };
    }

    private double calculateCaloriesByActivity(double bmr, double met, long duration) {
        double bmrMinutes = (bmr / 24) / 60;
        double durationMinutes = duration / 60.0;
        return bmrMinutes * met * durationMinutes;
    }

    private int getAge(LocalDate birthDate) {
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    @Override
    public boolean hasReachedDailyGoal(UserProfile profile) {
        if (profile == null
                || profile.getUser() == null
                || profile.getUser().getEmail() == null
                || profile.getGoalKcalDaily() == null) {
            return false;
        }

        double todayCalories = getTodayCalories(profile);
        return todayCalories >= profile.getGoalKcalDaily();
    }

    private double getTodayCalories(UserProfile profile) {
        ZoneId userZone = profile.getTimeZone() != null ? profile.getTimeZone() : ZoneId.systemDefault();
        ZoneId systemZone = ZoneId.systemDefault();

        ZonedDateTime userStart = LocalDate.now(userZone).atStartOfDay(userZone);
        ZonedDateTime userEnd = userStart.plusDays(1);

        ZonedDateTime systemStart = userStart.withZoneSameInstant(systemZone);
        ZonedDateTime systemEnd = userEnd.withZoneSameInstant(systemZone);

        List<RouteExecution> userRoutesExecutions = routeExecutionRepository.findAllByUserEmailAndEndTimeBetweenAndStatus(
                profile.getUser().getEmail(),
                systemStart.toLocalDateTime(),
                systemEnd.toLocalDateTime(),
                RouteExecution.RouteExecutionStatus.FINISHED
        );

        return userRoutesExecutions.stream()
                .filter(routeExecution ->
                        routeExecution.getStatus() == RouteExecution.RouteExecutionStatus.FINISHED &&
                        routeExecution.getEndTime() != null &&
                        routeExecution.getCalories() != null &&
                        routeExecution.getEndTime()
                                .atZone(systemZone)
                                .withZoneSameInstant(userZone)
                                .toLocalDate()
                                .equals(userStart.toLocalDate())
                )
                .mapToDouble(routeExecution -> routeExecution.getCalories().doubleValue())
                .sum();
    }
}
