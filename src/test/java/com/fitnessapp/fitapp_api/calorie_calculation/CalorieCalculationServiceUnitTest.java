package com.fitnessapp.fitapp_api.calorie_calculation;


import com.fitnessapp.fitapp_api.auth.model.UserAuth;
import com.fitnessapp.fitapp_api.calories.dto.CCActivityRequest;
import com.fitnessapp.fitapp_api.calories.service.implementation.CalorieCalculationServiceImpl;
import com.fitnessapp.fitapp_api.core.exception.UserProfileNotCompletedException;
import com.fitnessapp.fitapp_api.profile.model.UserProfile;
import com.fitnessapp.fitapp_api.profile.service.UserProfileService;
import com.fitnessapp.fitapp_api.profile.util.Gender;
import com.fitnessapp.fitapp_api.routeexecution.model.RouteExecution;
import com.fitnessapp.fitapp_api.routeexecution.repository.RouteExecutionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalorieCalculationServiceUnitTest {

    private static final double DELTA = 1e-4;
    @Mock
    UserProfileService userProfileService;
    @Mock
    UserProfile userProfile;
    @Mock
    RouteExecutionRepository routeExecutionRepository;
    @InjectMocks
    CalorieCalculationServiceImpl service;

    @Test
    @DisplayName("Calcula calorias para hombre con RUNNING_MODERATE 1 hora")
    void calculateCaloriesMaleRunningModerate() {
        when(userProfileService.isProfileComplete(userProfile)).thenReturn(true);
        when(userProfile.getGender()).thenReturn(Gender.MALE);
        when(userProfile.getWeightKg()).thenReturn(BigDecimal.valueOf(80));
        when(userProfile.getHeightCm()).thenReturn(BigDecimal.valueOf(180));
        when(userProfile.getBirthDate()).thenReturn(LocalDate.now().minusYears(30));

        CCActivityRequest activity = new CCActivityRequest("RUNNING_MODERATE", 3600L);

        double result = service.calculateCalories(userProfile, activity);

        // Cálculo esperado usando fórmula por minuto y MET aproximado RUNNING_MODERATE = 9.8
        double weight = 80.0;
        double height = 180.0;
        int age = 30;
        double bmr = (10 * weight) + (6.25 * height) - (5 * age) + 5; // hombre: +5
        double bmrMinutes = (bmr / 24.0) / 60.0;
        double minutes = 3600.0 / 60.0; // 60 minutos
        double expected = bmrMinutes * 9.8 * minutes;

        assertEquals(expected, result, DELTA);
    }

    @Test
    @DisplayName("Calcula calorias para mujer con CYCLING_SLOW 30 min")
    void calculateCaloriesFemaleCyclingSlow() {
        when(userProfileService.isProfileComplete(userProfile)).thenReturn(true);
        when(userProfile.getGender()).thenReturn(Gender.FEMALE);
        when(userProfile.getWeightKg()).thenReturn(BigDecimal.valueOf(60.0));
        when(userProfile.getHeightCm()).thenReturn(BigDecimal.valueOf(165.0));
        when(userProfile.getBirthDate()).thenReturn(LocalDate.now().minusYears(25));

        CCActivityRequest activity = new CCActivityRequest("CYCLING_SLOW", 1800L);
        double result = service.calculateCalories(userProfile, activity);

        double bmr = (10 * 60.0) + (6.25 * 165.0) - (5 * 25) - 161;
        double bmrMinutes = (bmr / 24.0) / 60.0;
        double minutes = 1800.0 / 60.0; // 30 minutos
        double expected = bmrMinutes * 4.3 * minutes;
        assertEquals(expected, result, DELTA);
    }

    @Test
    @DisplayName("Lanza IllegalArgumentException por actividad desconocida")
    void unknownActivityThrows() {
        when(userProfileService.isProfileComplete(userProfile)).thenReturn(true);
        when(userProfile.getGender()).thenReturn(Gender.MALE);
        when(userProfile.getWeightKg()).thenReturn(BigDecimal.valueOf(80.0));
        when(userProfile.getHeightCm()).thenReturn(BigDecimal.valueOf(180.0));
        when(userProfile.getBirthDate()).thenReturn(LocalDate.now().minusYears(30));

        CCActivityRequest activity = new CCActivityRequest("UNKNOWN_ACTIVITY", 600L);

        assertThrows(IllegalArgumentException.class,
                () -> service.calculateCalories(userProfile, activity));
    }

    @Test
    @DisplayName("Lanza UserProfileNotCompletedException si el perfil no esta completo")
    void incompleteProfileThrows() {
        when(userProfileService.isProfileComplete(userProfile)).thenReturn(false);
        assertThrows(UserProfileNotCompletedException.class,
                () -> service.calculateCalories(userProfile, new CCActivityRequest("WALKING_SLOW", 600L)));
        verify(userProfileService).isProfileComplete(userProfile);
    }

    @Test
    @DisplayName("hasReachedDailyGoal suma calorías usando la zona horaria adelantada del perfil")
    void hasReachedDailyGoal_UsesAheadTimezone() {
        ZoneId userZone = ZoneId.of("Asia/Tokyo");
        ZoneId systemZone = ZoneId.systemDefault();
        UserProfile profile = buildProfile(userZone, 150, "tz-ahead@example.com");

        ZonedDateTime userStart = LocalDate.now(userZone).atStartOfDay(userZone);
        ZonedDateTime userMidday = userStart.plusHours(12);
        ZonedDateTime previousNight = userStart.minusHours(2);

        RouteExecution todayExec = buildExecution(userMidday.withZoneSameInstant(systemZone).toLocalDateTime(), 200.0);
        RouteExecution previousExec = buildExecution(previousNight.withZoneSameInstant(systemZone).toLocalDateTime(), 500.0);

        when(routeExecutionRepository.findAllByUserEmailAndEndTimeBetweenAndStatus(
                eq(profile.getUser().getEmail()),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(RouteExecution.RouteExecutionStatus.FINISHED.toString())
        )).thenReturn(List.of(todayExec, previousExec));

        assertTrue(service.hasReachedDailyGoal(profile));
    }

    @Test
    @DisplayName("hasReachedDailyGoal ignora ejecuciones fuera del día local en zona atrasada")
    void hasReachedDailyGoal_IgnoresOutsideWindowForBehindTimezone() {
        ZoneId userZone = ZoneId.of("America/Los_Angeles");
        ZoneId systemZone = ZoneId.systemDefault();
        UserProfile profile = buildProfile(userZone, 200, "tz-behind@example.com");

        ZonedDateTime userStart = LocalDate.now(userZone).atStartOfDay(userZone);
        ZonedDateTime evening = userStart.plusHours(20);
        ZonedDateTime nextDay = userStart.plusDays(1).plusHours(3);

        RouteExecution validExec = buildExecution(evening.withZoneSameInstant(systemZone).toLocalDateTime(), 180.0);
        RouteExecution nextDayExec = buildExecution(nextDay.withZoneSameInstant(systemZone).toLocalDateTime(), 400.0);

        when(routeExecutionRepository.findAllByUserEmailAndEndTimeBetweenAndStatus(
                eq(profile.getUser().getEmail()),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(RouteExecution.RouteExecutionStatus.FINISHED.toString())
        )).thenReturn(List.of(validExec, nextDayExec));

        assertFalse(service.hasReachedDailyGoal(profile));
    }

    private UserProfile buildProfile(ZoneId zoneId, int goal, String email) {
        UserAuth user = new UserAuth();
        user.setEmail(email);

        UserProfile profile = new UserProfile();
        profile.setUser(user);
        profile.setTimeZone(zoneId);
        profile.setGoalKcalDaily(goal);
        profile.setPoints(0L);
        return profile;
    }

    private RouteExecution buildExecution(LocalDateTime endTime, double calories) {
        RouteExecution exec = new RouteExecution();
        exec.setStatus(RouteExecution.RouteExecutionStatus.FINISHED);
        exec.setEndTime(endTime);
        exec.setCalories(BigDecimal.valueOf(calories));
        return exec;
    }
}
