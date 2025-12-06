package com.fitnessapp.fitapp_api.point_calculation;

import com.fitnessapp.fitapp_api.gamification.dto.PCActivityRequestDTO;
import com.fitnessapp.fitapp_api.gamification.service.implementation.PointsCalculationServiceImpl;
import com.fitnessapp.fitapp_api.gamification.util.PointsConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PointsCalculationServiceUnitTest {
    private final PointsCalculationServiceImpl service = new PointsCalculationServiceImpl();

    @Test
    @DisplayName("No otorga puntos si la duración es menor al mínimo")
    void calculatePoints_DurationTooShort() {
        PCActivityRequestDTO req = new PCActivityRequestDTO(2.0, 60L, "RUNNING_MODERATE", false);
        long points = service.calculatePoints(req);
        assertEquals(0, points);
    }

    @Test
    @DisplayName("No otorga puntos si el ritmo es implausible")
    void calculatePoints_ImplausiblePace() {
        // 10 km en 10 minutos corriendo: velocidad 60 km/h (implausible)
        PCActivityRequestDTO req = new PCActivityRequestDTO(10.0, 600L, "RUNNING_MODERATE", false);
        long points = service.calculatePoints(req);
        assertEquals(0, points);
    }

    @Test
    @DisplayName("Otorga puntos normales para actividad válida sin bonus")
    void calculatePoints_NormalActivity_NoBonus() {
        // 5 km en 40 minutos corriendo moderado
        PCActivityRequestDTO req = new PCActivityRequestDTO(5.0, 2400L, "RUNNING_MODERATE", false);
        int pointsPerKm = PointsConfig.getPointsPerKm("RUNNING_MODERATE");
        double expected = 5.0 * pointsPerKm + PointsConfig.ROUTE_COMPLETED_BONUS.getValue();
        long points = service.calculatePoints(req);
        assertEquals((long) expected, points);
    }

    @Test
    @DisplayName("Otorga puntos con bonus por goal diario cumplido")
    void calculatePoints_WithDailyGoalBonus() {
        PCActivityRequestDTO req = new PCActivityRequestDTO(4.0, 1800L, "CYCLING_INTENSE", true);
        int pointsPerKm = PointsConfig.getPointsPerKm("CYCLING_INTENSE");
        double expected = 4.0 * pointsPerKm + PointsConfig.ROUTE_COMPLETED_BONUS.getValue() + PointsConfig.DAILY_GOAL_BONUS.getValue();
        long points = service.calculatePoints(req);
        assertEquals((long) expected, points);
    }

    @Test
    @DisplayName("Limita los puntos al máximo por día")
    void calculatePoints_MaxPerDayLimit() {
        // 200 km en 600 minutos ciclismo intenso, debería superar el máximo
        PCActivityRequestDTO req = new PCActivityRequestDTO(200.0, 36000L, "CYCLING_INTENSE", true);
        long points = service.calculatePoints(req);
        assertEquals(PointsConfig.MAX_PER_DAY.getValue(), points);
    }

    @Test
    @DisplayName("Otorga puntos para caminar lento dentro de rango válido")
    void calculatePoints_WalkingSlow_Valid() {
        PCActivityRequestDTO req = new PCActivityRequestDTO(2.0, 60*30L, "WALKING_SLOW", false);
        int pointsPerKm = PointsConfig.getPointsPerKm("WALKING_SLOW");
        double expected = 2.0 * pointsPerKm + PointsConfig.ROUTE_COMPLETED_BONUS.getValue();
        long points = service.calculatePoints(req);
        assertEquals((long) expected, points);
    }
}
