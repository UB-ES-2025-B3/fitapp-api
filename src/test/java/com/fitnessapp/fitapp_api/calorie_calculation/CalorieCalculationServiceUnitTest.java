package com.fitnessapp.fitapp_api.calorie_calculation;


import com.fitnessapp.fitapp_api.calories.service.dto.CCActivityRequest;
import com.fitnessapp.fitapp_api.calories.service.implementation.CalorieCalculationServiceImpl;
import com.fitnessapp.fitapp_api.core.exception.UserProfileNotCompletedException;
import com.fitnessapp.fitapp_api.profile.model.UserProfile;
import com.fitnessapp.fitapp_api.profile.service.UserProfileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.fitnessapp.fitapp_api.profile.util.Gender;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalorieCalculationServiceUnitTest {

    @Mock
    UserProfileService userProfileService;

    @Mock
    UserProfile userProfile;

    @InjectMocks
    CalorieCalculationServiceImpl service;

    private static final double DELTA = 1e-4;

    @BeforeEach
    void setUp() {
        // Por defecto considerar perfil completo; tests individuales pueden sobreescribirlo.
        when(userProfileService.isProfileComplete(userProfile)).thenReturn(true);
    }

    @Test
    @DisplayName("Calcula calorias para hombre con RUNNING_MODERATE 1 hora")
    void calculateCaloriesMaleRunningModerate() {
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
}
