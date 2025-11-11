package com.fitnessapp.fitapp_api.home;

import com.fitnessapp.fitapp_api.auth.model.UserAuth;
import com.fitnessapp.fitapp_api.core.exception.UserProfileNotCompletedException;
import com.fitnessapp.fitapp_api.core.exception.UserProfileNotFoundException;
import com.fitnessapp.fitapp_api.home.dto.HomeKpisTodayResponseDTO;
import com.fitnessapp.fitapp_api.home.service.implementation.HomeServiceImpl;
import com.fitnessapp.fitapp_api.profile.model.UserProfile;
import com.fitnessapp.fitapp_api.profile.repository.UserProfileRepository;
import com.fitnessapp.fitapp_api.profile.service.UserProfileService;
import com.fitnessapp.fitapp_api.route.model.Route;
import com.fitnessapp.fitapp_api.routeexecution.model.RouteExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HomeServiceUnitTests {

    @Mock
    private UserProfileService userProfileService;

    @Mock
    private UserProfileRepository userProfileRepository;

    @InjectMocks
    private HomeServiceImpl homeService;

    private String testEmail;
    private UserProfile testProfile;
    private UserAuth testUser;

    @BeforeEach
    void setUp() {
        testEmail = "test@example.com";
        testUser = new UserAuth();
        testUser.setEmail(testEmail);
        testUser.setId(1L);

        testProfile = new UserProfile();
        testProfile.setUser(testUser);
        testProfile.setId(1L);
        // Inicializar lista vacía de RouteExecutions
        //testProfile.setRouteExecutions(new ArrayList<>());
    }

    @Test
    @DisplayName("Debe lanzar UserProfileNotFoundException cuando el perfil no existe")
    void getHomeKpisToday_WhenUserProfileNotFound_ShouldThrowException() {
        // Arrange
        when(userProfileRepository.findByUser_Email(testEmail))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserProfileNotFoundException.class,
                () -> homeService.getHomeKpisToday(testEmail));

        verify(userProfileRepository).findByUser_Email(testEmail);
        verify(userProfileService, never()).isProfileComplete(testProfile);
    }

    @Test
    @DisplayName("Debe lanzar UserProfileNotCompletedException cuando el perfil está incompleto")
    void getHomeKpisToday_WhenProfileIncomplete_ShouldThrowException() {
        // Arrange
        when(userProfileRepository.findByUser_Email(testEmail))
                .thenReturn(Optional.of(testProfile));
        when(userProfileService.isProfileComplete(testProfile))
                .thenReturn(false);

        // Act & Assert
        assertThrows(UserProfileNotCompletedException.class,
                () -> homeService.getHomeKpisToday(testEmail));

        verify(userProfileRepository).findByUser_Email(testEmail);
        verify(userProfileService).isProfileComplete(testProfile);
    }

    @Test
    @DisplayName("Debe retornar KPIs con valores en cero cuando no hay rutas")
    void getHomeKpisToday_WhenNoRoutes_ShouldReturnZeroKpis() {
        // Arrange
        when(userProfileRepository.findByUser_Email(testEmail))
                .thenReturn(Optional.of(testProfile));
        when(userProfileService.isProfileComplete(testProfile))
                .thenReturn(true);

        // Act
        HomeKpisTodayResponseDTO result = homeService.getHomeKpisToday(testEmail);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.routesCompletedToday());
        assertEquals(0L, result.totalDurationSecToday());
        assertEquals(0.0, result.totalDistanceKmToday());
        assertEquals(0.0, result.caloriesKcalToday());
        assertEquals(0, result.activeStreakDays());
        assertFalse(result.hasCreatedRoutes());

        verify(userProfileRepository).findByUser_Email(testEmail);
        verify(userProfileService).isProfileComplete(testProfile);
    }

    @Test
    @DisplayName("Debe calcular correctamente los KPIs con una ruta completada hoy")
    void getHomeKpisToday_WithOneCompletedRouteToday_ShouldCalculateCorrectly() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        List<RouteExecution> routeExecutions = new ArrayList<>();

        routeExecutions.add(createRouteExecution(
                now.minusHours(2),
                1800L, // 30 min
                5.0,
                250.0,
                RouteExecution.RouteExecutionStatus.FINISHED
        ));

        //testProfile.setRouteExecutions(routeExecutions);

        when(userProfileRepository.findByUser_Email(testEmail))
                .thenReturn(Optional.of(testProfile));
        when(userProfileService.isProfileComplete(testProfile))
                .thenReturn(true);

        // Act
        HomeKpisTodayResponseDTO result = homeService.getHomeKpisToday(testEmail);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.routesCompletedToday());
        assertEquals(1800L, result.totalDurationSecToday());
        assertEquals(5.0, result.totalDistanceKmToday(), 0.01);
        assertEquals(250.0, result.caloriesKcalToday(), 0.01);
        assertEquals(1, result.activeStreakDays());
        assertTrue(result.hasCreatedRoutes());
    }

    @Test
    @DisplayName("Debe calcular correctamente los KPIs con múltiples rutas completadas hoy")
    void getHomeKpisToday_WithMultipleCompletedRoutesToday_ShouldCalculateCorrectly() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        List<RouteExecution> routeExecutions = new ArrayList<>();

        routeExecutions.add(createRouteExecution(now.minusHours(3), 1800L, 5.0, 250.0, RouteExecution.RouteExecutionStatus.FINISHED));
        routeExecutions.add(createRouteExecution(now.minusHours(2), 1200L, 3.5, 180.0, RouteExecution.RouteExecutionStatus.FINISHED));
        routeExecutions.add(createRouteExecution(now.minusHours(1), 900L, 2.5, 120.0, RouteExecution.RouteExecutionStatus.FINISHED));

        //testProfile.setRouteExecutions(routeExecutions);

        when(userProfileRepository.findByUser_Email(testEmail))
                .thenReturn(Optional.of(testProfile));
        when(userProfileService.isProfileComplete(testProfile))
                .thenReturn(true);

        // Act
        HomeKpisTodayResponseDTO result = homeService.getHomeKpisToday(testEmail);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.routesCompletedToday());
        assertEquals(3900L, result.totalDurationSecToday()); // 65 min
        assertEquals(11.0, result.totalDistanceKmToday(), 0.01);
        assertEquals(550.0, result.caloriesKcalToday(), 0.01);
        assertEquals(1, result.activeStreakDays());
        assertTrue(result.hasCreatedRoutes());
    }

    @Test
    @DisplayName("No debe contar rutas que no estén finalizadas")
    void getHomeKpisToday_ShouldNotCountNonFinishedRoutes() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        List<RouteExecution> routeExecutions = new ArrayList<>();

        routeExecutions.add(createRouteExecution(now.minusHours(2), 1800L, 5.0, 250.0, RouteExecution.RouteExecutionStatus.FINISHED));
        routeExecutions.add(createRouteExecution(now.minusMinutes(30), 900L, 2.5, 100.0, RouteExecution.RouteExecutionStatus.IN_PROGRESS));
        routeExecutions.add(createRouteExecution(now.minusMinutes(15), 600L, 1.5, 50.0, RouteExecution.RouteExecutionStatus.PAUSED));

        //testProfile.setRouteExecutions(routeExecutions);

        when(userProfileRepository.findByUser_Email(testEmail))
                .thenReturn(Optional.of(testProfile));
        when(userProfileService.isProfileComplete(testProfile))
                .thenReturn(true);

        // Act
        HomeKpisTodayResponseDTO result = homeService.getHomeKpisToday(testEmail);

        // Assert
        assertEquals(1, result.routesCompletedToday());
        assertEquals(1800L, result.totalDurationSecToday());
        assertEquals(5.0, result.totalDistanceKmToday());
        assertEquals(250.0, result.caloriesKcalToday());
    }

    @Test
    @DisplayName("No debe contar rutas de días anteriores en los KPIs de hoy")
    void getHomeKpisToday_ShouldNotCountPreviousDaysRoutes() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        List<RouteExecution> routeExecutions = new ArrayList<>();

        routeExecutions.add(createRouteExecution(now.minusHours(2), 1800L, 5.0, 250.0, RouteExecution.RouteExecutionStatus.FINISHED));
        routeExecutions.add(createRouteExecution(now.minusDays(1).minusHours(2), 2400L, 7.0, 350.0, RouteExecution.RouteExecutionStatus.FINISHED));
        routeExecutions.add(createRouteExecution(now.minusDays(3).minusHours(1), 1200L, 4.0, 200.0, RouteExecution.RouteExecutionStatus.FINISHED));

        //testProfile.setRouteExecutions(routeExecutions);

        when(userProfileRepository.findByUser_Email(testEmail))
                .thenReturn(Optional.of(testProfile));
        when(userProfileService.isProfileComplete(testProfile))
                .thenReturn(true);

        // Act
        HomeKpisTodayResponseDTO result = homeService.getHomeKpisToday(testEmail);

        // Assert
        assertEquals(1, result.routesCompletedToday());
        assertEquals(1800L, result.totalDurationSecToday());
        assertEquals(5.0, result.totalDistanceKmToday());
        assertEquals(250.0, result.caloriesKcalToday());
        assertTrue(result.hasCreatedRoutes());
    }

    @Test
    @DisplayName("Debe calcular correctamente la racha activa de 3 días consecutivos")
    void getHomeKpisToday_ShouldCalculateActiveStreak_ThreeConsecutiveDays() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        List<RouteExecution> routeExecutions = new ArrayList<>();

        routeExecutions.add(createRouteExecution(now.minusHours(1), 1800L, 5.0, 250.0, RouteExecution.RouteExecutionStatus.FINISHED));
        routeExecutions.add(createRouteExecution(now.minusDays(1).minusHours(2), 2400L, 7.0, 350.0, RouteExecution.RouteExecutionStatus.FINISHED));
        routeExecutions.add(createRouteExecution(now.minusDays(2).minusHours(3), 1200L, 4.0, 200.0, RouteExecution.RouteExecutionStatus.FINISHED));
        routeExecutions.add(createRouteExecution(now.minusDays(4).minusHours(1), 1800L, 5.5, 280.0, RouteExecution.RouteExecutionStatus.FINISHED));

        //testProfile.setRouteExecutions(routeExecutions);

        when(userProfileRepository.findByUser_Email(testEmail))
                .thenReturn(Optional.of(testProfile));
        when(userProfileService.isProfileComplete(testProfile))
                .thenReturn(true);

        // Act
        HomeKpisTodayResponseDTO result = homeService.getHomeKpisToday(testEmail);

        // Assert
        assertEquals(3, result.activeStreakDays());
    }

    @Test
    @DisplayName("Debe calcular correctamente la racha activa de 5 días consecutivos")
    void getHomeKpisToday_ShouldCalculateActiveStreak_FiveConsecutiveDays() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        List<RouteExecution> routeExecutions = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            routeExecutions.add(createRouteExecution(
                    now.minusDays(i).minusHours(2),
                    1800L,
                    5.0,
                    250.0,
                    RouteExecution.RouteExecutionStatus.FINISHED
            ));
        }

        //testProfile.setRouteExecutions(routeExecutions);

        when(userProfileRepository.findByUser_Email(testEmail))
                .thenReturn(Optional.of(testProfile));
        when(userProfileService.isProfileComplete(testProfile))
                .thenReturn(true);

        // Act
        HomeKpisTodayResponseDTO result = homeService.getHomeKpisToday(testEmail);

        // Assert
        assertEquals(5, result.activeStreakDays());
    }

    @Test
    @DisplayName("La racha debe ser 0 cuando no hay actividad hoy")
    void getHomeKpisToday_WhenNoActivityToday_ShouldHaveZeroStreak() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        List<RouteExecution> routeExecutions = new ArrayList<>();

        routeExecutions.add(createRouteExecution(now.minusDays(1).minusHours(2), 2400L, 7.0, 350.0, RouteExecution.RouteExecutionStatus.FINISHED));
        routeExecutions.add(createRouteExecution(now.minusDays(2).minusHours(2), 2400L, 7.0, 350.0, RouteExecution.RouteExecutionStatus.FINISHED));

        //testProfile.setRouteExecutions(routeExecutions);

        when(userProfileRepository.findByUser_Email(testEmail))
                .thenReturn(Optional.of(testProfile));
        when(userProfileService.isProfileComplete(testProfile))
                .thenReturn(true);

        // Act
        HomeKpisTodayResponseDTO result = homeService.getHomeKpisToday(testEmail);

        // Assert
        assertEquals(0, result.activeStreakDays());
        assertEquals(0, result.routesCompletedToday());
    }

    @Test
    @DisplayName("La racha debe romperse si hay un día sin actividad")
    void getHomeKpisToday_StreakShouldBreak_WhenDayWithoutActivity() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        List<RouteExecution> routeExecutions = new ArrayList<>();

        routeExecutions.add(createRouteExecution(now.minusHours(1), 1800L, 5.0, 250.0, RouteExecution.RouteExecutionStatus.FINISHED));
        routeExecutions.add(createRouteExecution(now.minusDays(1).minusHours(2), 2400L, 7.0, 350.0, RouteExecution.RouteExecutionStatus.FINISHED));
        // Día 2 sin actividad
        routeExecutions.add(createRouteExecution(now.minusDays(3).minusHours(1), 1200L, 4.0, 200.0, RouteExecution.RouteExecutionStatus.FINISHED));

        //testProfile.setRouteExecutions(routeExecutions);

        when(userProfileRepository.findByUser_Email(testEmail))
                .thenReturn(Optional.of(testProfile));
        when(userProfileService.isProfileComplete(testProfile))
                .thenReturn(true);

        // Act
        HomeKpisTodayResponseDTO result = homeService.getHomeKpisToday(testEmail);

        // Assert
        assertEquals(2, result.activeStreakDays()); // Solo hoy y ayer
    }

    @Test
    @DisplayName("Debe contar múltiples rutas en el mismo día como un solo día para la racha")
    void getHomeKpisToday_MultipleRoutesInSameDay_ShouldCountAsOneDayInStreak() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        List<RouteExecution> routeExecutions = new ArrayList<>();

        // 3 rutas hoy
        routeExecutions.add(createRouteExecution(now.minusHours(6), 1800L, 5.0, 250.0, RouteExecution.RouteExecutionStatus.FINISHED));
        routeExecutions.add(createRouteExecution(now.minusHours(4), 1200L, 3.5, 180.0, RouteExecution.RouteExecutionStatus.FINISHED));
        routeExecutions.add(createRouteExecution(now.minusHours(1), 900L, 2.5, 120.0, RouteExecution.RouteExecutionStatus.FINISHED));

        // 2 rutas ayer
        routeExecutions.add(createRouteExecution(now.minusDays(1).minusHours(3), 1800L, 5.0, 250.0, RouteExecution.RouteExecutionStatus.FINISHED));
        routeExecutions.add(createRouteExecution(now.minusDays(1).minusHours(1), 1200L, 3.5, 180.0, RouteExecution.RouteExecutionStatus.FINISHED));

        //testProfile.setRouteExecutions(routeExecutions);

        when(userProfileRepository.findByUser_Email(testEmail))
                .thenReturn(Optional.of(testProfile));
        when(userProfileService.isProfileComplete(testProfile))
                .thenReturn(true);

        // Act
        HomeKpisTodayResponseDTO result = homeService.getHomeKpisToday(testEmail);

        // Assert
        assertEquals(3, result.routesCompletedToday()); // 3 rutas hoy
        assertEquals(2, result.activeStreakDays()); // 2 días consecutivos
        assertEquals(3900L, result.totalDurationSecToday());
        assertEquals(11.0, result.totalDistanceKmToday(), 0.01);
        assertEquals(550.0, result.caloriesKcalToday(), 0.01);
    }

    @Test
    @DisplayName("hasCreatedRoutes debe ser true cuando hay rutas aunque no sean de hoy")
    void getHomeKpisToday_HasCreatedRoutes_ShouldBeTrueWhenRoutesExist() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        List<RouteExecution> routeExecutions = new ArrayList<>();

        routeExecutions.add(createRouteExecution(now.minusDays(5), 1800L, 5.0, 250.0, RouteExecution.RouteExecutionStatus.FINISHED));

        //testProfile.setRouteExecutions(routeExecutions);

        when(userProfileRepository.findByUser_Email(testEmail))
                .thenReturn(Optional.of(testProfile));
        when(userProfileService.isProfileComplete(testProfile))
                .thenReturn(true);

        // Act
        HomeKpisTodayResponseDTO result = homeService.getHomeKpisToday(testEmail);

        // Assert
        assertTrue(result.hasCreatedRoutes());
        assertEquals(0, result.routesCompletedToday());
        assertEquals(0, result.activeStreakDays());
    }

    // ========================================
    // Tests para calculateKpisForToday
    // ========================================

    @Nested
    @DisplayName("Tests de calculateKpisForToday")
    class CalculateKpisForTodayTests {

        @Test
        @DisplayName("Debe retornar KPIs en cero cuando la lista está vacía")
        void calculateKpisForToday_EmptyList_ShouldReturnZeros() {
            // Arrange
            List<RouteExecution> routes = new ArrayList<>();

            // Act
            HomeKpisTodayResponseDTO result = homeService.calculateKpisForToday(routes);

            // Assert
            assertNotNull(result);
            assertEquals(0, result.routesCompletedToday());
            assertEquals(0L, result.totalDurationSecToday());
            assertEquals(0.0, result.totalDistanceKmToday());
            assertEquals(0.0, result.caloriesKcalToday());
            assertEquals(0, result.activeStreakDays());
            assertFalse(result.hasCreatedRoutes());
        }

        @Test
        @DisplayName("Debe calcular correctamente con una sola ruta completada hoy")
        void calculateKpisForToday_SingleRouteToday_ShouldCalculateCorrectly() {
            // Arrange
            LocalDateTime now = LocalDateTime.now();
            List<RouteExecution> routes = List.of(
                    createRouteExecution(now, 1800L, 5.0, 250.0, RouteExecution.RouteExecutionStatus.FINISHED)
            );

            // Act
            HomeKpisTodayResponseDTO result = homeService.calculateKpisForToday(routes);

            // Assert
            assertEquals(1, result.routesCompletedToday());
            assertEquals(1800L, result.totalDurationSecToday());
            assertEquals(5.0, result.totalDistanceKmToday(), 0.01);
            assertEquals(250.0, result.caloriesKcalToday(), 0.01);
            assertEquals(1, result.activeStreakDays());
            assertTrue(result.hasCreatedRoutes());
        }

        @Test
        @DisplayName("Debe sumar correctamente múltiples rutas completadas hoy")
        void calculateKpisForToday_MultipleRoutesToday_ShouldSumCorrectly() {
            // Arrange
            LocalDateTime now = LocalDateTime.now();
            List<RouteExecution> routes = List.of(
                    createRouteExecution(now.minusSeconds(30), 1800L, 5.0, 250.0, RouteExecution.RouteExecutionStatus.FINISHED),
                    createRouteExecution(now.minusSeconds(20), 1200L, 3.5, 180.0, RouteExecution.RouteExecutionStatus.FINISHED),
                    createRouteExecution(now.minusSeconds(10), 900L, 2.5, 120.0, RouteExecution.RouteExecutionStatus.FINISHED)
            );

            // Act
            HomeKpisTodayResponseDTO result = homeService.calculateKpisForToday(routes);

            // Assert
            assertEquals(3, result.routesCompletedToday());
            assertEquals(3900L, result.totalDurationSecToday()); // 65 min
            assertEquals(11.0, result.totalDistanceKmToday(), 0.01);
            assertEquals(550.0, result.caloriesKcalToday(), 0.01);
            assertTrue(result.hasCreatedRoutes());
        }

        @Test
        @DisplayName("No debe contar rutas que no estén en estado FINISHED")
        void calculateKpisForToday_ShouldIgnoreNonFinishedRoutes() {
            // Arrange
            LocalDateTime now = LocalDateTime.now();
            List<RouteExecution> routes = List.of(
                    createRouteExecution(now.minusSeconds(30), 1800L, 5.0, 250.0, RouteExecution.RouteExecutionStatus.FINISHED),
                    createRouteExecution(now.minusSeconds(20), 1200L, 3.5, 180.0, RouteExecution.RouteExecutionStatus.IN_PROGRESS),
                    createRouteExecution(now.minusSeconds(10), 900L, 2.5, 120.0, RouteExecution.RouteExecutionStatus.PAUSED)
            );

            // Act
            HomeKpisTodayResponseDTO result = homeService.calculateKpisForToday(routes);

            // Assert
            assertEquals(1, result.routesCompletedToday());
            assertEquals(1800L, result.totalDurationSecToday());
            assertEquals(5.0, result.totalDistanceKmToday(), 0.01);
            assertEquals(250.0, result.caloriesKcalToday(), 0.01);
        }

        @Test
        @DisplayName("No debe contar rutas de días anteriores")
        void calculateKpisForToday_ShouldIgnorePreviousDaysRoutes() {
            // Arrange
            LocalDateTime now = LocalDateTime.now();
            List<RouteExecution> routes = List.of(
                    createRouteExecution(now.minusSeconds(20), 1800L, 5.0, 250.0, RouteExecution.RouteExecutionStatus.FINISHED),
                    createRouteExecution(now.minusDays(1).minusHours(2), 2400L, 7.0, 350.0, RouteExecution.RouteExecutionStatus.FINISHED),
                    createRouteExecution(now.minusDays(2).minusHours(3), 1200L, 4.0, 200.0, RouteExecution.RouteExecutionStatus.FINISHED)
            );

            // Act
            HomeKpisTodayResponseDTO result = homeService.calculateKpisForToday(routes);

            // Assert
            assertEquals(1, result.routesCompletedToday());
            assertEquals(1800L, result.totalDurationSecToday());
            assertEquals(5.0, result.totalDistanceKmToday(), 0.01);
            assertEquals(250.0, result.caloriesKcalToday(), 0.01);
            assertTrue(result.hasCreatedRoutes()); // Tiene rutas aunque no sean de hoy
        }

        @Test
        @DisplayName("hasCreatedRoutes debe ser true si hay rutas antiguas pero no de hoy")
        void calculateKpisForToday_HasCreatedRoutes_TrueWithOldRoutes() {
            // Arrange
            LocalDateTime now = LocalDateTime.now();
            List<RouteExecution> routes = List.of(
                    createRouteExecution(now.minusDays(5), 1800L, 5.0, 250.0, RouteExecution.RouteExecutionStatus.FINISHED),
                    createRouteExecution(now.minusDays(10), 2400L, 7.0, 350.0, RouteExecution.RouteExecutionStatus.FINISHED)
            );

            // Act
            HomeKpisTodayResponseDTO result = homeService.calculateKpisForToday(routes);

            // Assert
            assertEquals(0, result.routesCompletedToday());
            assertEquals(0L, result.totalDurationSecToday());
            assertTrue(result.hasCreatedRoutes());
        }
    }

    // ========================================
    // Tests para calculateActiveStreak
    // ========================================

    @Nested
    @DisplayName("Tests de calculateActiveStreak")
    class CalculateActiveStreakTests {

        @Test
        @DisplayName("Debe retornar 0 cuando no hay rutas")
        void calculateActiveStreak_EmptyList_ShouldReturnZero() {
            // Arrange
            List<RouteExecution> routes = new ArrayList<>();

            // Act
            int streak = homeService.calculateActiveStreak(routes);

            // Assert
            assertEquals(0, streak);
        }

        @Test
        @DisplayName("Debe retornar 1 con una ruta completada hoy")
        void calculateActiveStreak_OneRouteToday_ShouldReturnOne() {
            // Arrange
            LocalDateTime now = LocalDateTime.now();
            List<RouteExecution> routes = List.of(
                    createRouteExecution(now.minusHours(2), 1800L, 5.0, 250.0, RouteExecution.RouteExecutionStatus.FINISHED)
            );

            // Act
            int streak = homeService.calculateActiveStreak(routes);

            // Assert
            assertEquals(1, streak);
        }

        @Test
        @DisplayName("Debe calcular correctamente racha de 3 días consecutivos")
        void calculateActiveStreak_ThreeConsecutiveDays_ShouldReturnThree() {
            // Arrange
            LocalDateTime now = LocalDateTime.now();
            List<RouteExecution> routes = List.of(
                    createRouteExecution(now.minusHours(1), 1800L, 5.0, 250.0, RouteExecution.RouteExecutionStatus.FINISHED),
                    createRouteExecution(now.minusDays(1).minusHours(2), 2400L, 7.0, 350.0, RouteExecution.RouteExecutionStatus.FINISHED),
                    createRouteExecution(now.minusDays(2).minusHours(3), 1200L, 4.0, 200.0, RouteExecution.RouteExecutionStatus.FINISHED)
            );

            // Act
            int streak = homeService.calculateActiveStreak(routes);

            // Assert
            assertEquals(3, streak);
        }

        @Test
        @DisplayName("Debe calcular correctamente racha de 7 días consecutivos")
        void calculateActiveStreak_SevenConsecutiveDays_ShouldReturnSeven() {
            // Arrange
            LocalDateTime now = LocalDateTime.now();
            List<RouteExecution> routes = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                routes.add(createRouteExecution(
                        now.minusDays(i).minusHours(2),
                        1800L,
                        5.0,
                        250.0,
                        RouteExecution.RouteExecutionStatus.FINISHED
                ));
            }

            // Act
            int streak = homeService.calculateActiveStreak(routes);

            // Assert
            assertEquals(7, streak);
        }

        @Test
        @DisplayName("La racha debe romperse si falta un día")
        void calculateActiveStreak_ShouldBreak_WhenMissingDay() {
            // Arrange
            LocalDateTime now = LocalDateTime.now();
            List<RouteExecution> routes = List.of(
                    createRouteExecution(now.minusHours(1), 1800L, 5.0, 250.0, RouteExecution.RouteExecutionStatus.FINISHED),
                    createRouteExecution(now.minusDays(1).minusHours(2), 2400L, 7.0, 350.0, RouteExecution.RouteExecutionStatus.FINISHED),
                    // Falta el día 2
                    createRouteExecution(now.minusDays(3).minusHours(1), 1200L, 4.0, 200.0, RouteExecution.RouteExecutionStatus.FINISHED)
            );

            // Act
            int streak = homeService.calculateActiveStreak(routes);

            // Assert
            assertEquals(2, streak); // Solo cuenta hoy y ayer
        }

        @Test
        @DisplayName("Debe retornar 0 si no hay actividad hoy")
        void calculateActiveStreak_NoActivityToday_ShouldReturnZero() {
            // Arrange
            LocalDateTime now = LocalDateTime.now();
            List<RouteExecution> routes = List.of(
                    createRouteExecution(now.minusDays(1).minusHours(2), 2400L, 7.0, 350.0, RouteExecution.RouteExecutionStatus.FINISHED),
                    createRouteExecution(now.minusDays(2).minusHours(2), 2400L, 7.0, 350.0, RouteExecution.RouteExecutionStatus.FINISHED),
                    createRouteExecution(now.minusDays(3).minusHours(2), 2400L, 7.0, 350.0, RouteExecution.RouteExecutionStatus.FINISHED)
            );

            // Act
            int streak = homeService.calculateActiveStreak(routes);

            // Assert
            assertEquals(0, streak);
        }

        @Test
        @DisplayName("Debe contar múltiples rutas en el mismo día como un solo día")
        void calculateActiveStreak_MultipleRoutesPerDay_ShouldCountAsOneDay() {
            // Arrange
            LocalDateTime now = LocalDateTime.now();
            List<RouteExecution> routes = List.of(
                    // 3 rutas hoy
                    createRouteExecution(now.minusHours(6), 1800L, 5.0, 250.0, RouteExecution.RouteExecutionStatus.FINISHED));
            routes.add(createRouteExecution(now.minusHours(4), 1200L, 3.5, 180.0, RouteExecution.RouteExecutionStatus.FINISHED));
            routes.add(createRouteExecution(now.minusHours(1), 900L, 2.5, 120.0, RouteExecution.RouteExecutionStatus.FINISHED));
            // 2 rutas ayer
            routes.add(createRouteExecution(now.minusDays(1).minusHours(3), 1800L, 5.0, 250.0, RouteExecution.RouteExecutionStatus.FINISHED));
            routes.add(createRouteExecution(now.minusDays(1).minusHours(1), 1200L, 3.5, 180.0, RouteExecution.RouteExecutionStatus.FINISHED));

            // Act
            int streak = homeService.calculateActiveStreak(routes);

            // Assert
            assertEquals(2, streak); // 2 días, no 5 rutas
        }

        @Test
        @DisplayName("No debe contar rutas que no estén FINISHED")
        void calculateActiveStreak_ShouldIgnoreNonFinishedRoutes() {
            // Arrange
            LocalDateTime now = LocalDateTime.now();
            List<RouteExecution> routes = List.of(
                    createRouteExecution(now.minusHours(3), 1800L, 5.0, 250.0, RouteExecution.RouteExecutionStatus.FINISHED),
                    createRouteExecution(now.minusHours(2), 1200L, 3.5, 180.0, RouteExecution.RouteExecutionStatus.IN_PROGRESS),
                    createRouteExecution(now.minusDays(1).minusHours(2), 2400L, 7.0, 350.0, RouteExecution.RouteExecutionStatus.FINISHED),
                    createRouteExecution(now.minusDays(2).minusHours(2), 1200L, 4.0, 200.0, RouteExecution.RouteExecutionStatus.PAUSED)
            );

            // Act
            int streak = homeService.calculateActiveStreak(routes);

            // Assert
            assertEquals(2, streak); // Solo hoy y ayer (las FINISHED)
        }

        @Test
        @DisplayName("Debe manejar correctamente fechas desordenadas")
        void calculateActiveStreak_WithUnorderedDates_ShouldCalculateCorrectly() {
            // Arrange
            LocalDateTime now = LocalDateTime.now();
            List<RouteExecution> routes = List.of(
                    createRouteExecution(now.minusDays(2).minusHours(2), 1200L, 4.0, 200.0, RouteExecution.RouteExecutionStatus.FINISHED),
                    createRouteExecution(now.minusHours(1), 1800L, 5.0, 250.0, RouteExecution.RouteExecutionStatus.FINISHED),
                    createRouteExecution(now.minusDays(1).minusHours(2), 2400L, 7.0, 350.0, RouteExecution.RouteExecutionStatus.FINISHED)
            );

            // Act
            int streak = homeService.calculateActiveStreak(routes);

            // Assert
            assertEquals(3, streak);
        }
    }

    // Método auxiliar para crear RouteExecution
    private RouteExecution createRouteExecution(
            LocalDateTime endTime,
            Long durationSec,
            Double distanceKm,
            Double calories,
            RouteExecution.RouteExecutionStatus status) {

        Route route = new Route();
        route.setDistanceKm(BigDecimal.valueOf(distanceKm));

        RouteExecution execution = new RouteExecution();
        execution.setRoute(route);
        execution.setUser(testUser);
        execution.setEndTime(endTime);
        execution.setDurationSec(durationSec);
        execution.setCalories(BigDecimal.valueOf(calories));
        execution.setStatus(status);

        return execution;
    }
}
