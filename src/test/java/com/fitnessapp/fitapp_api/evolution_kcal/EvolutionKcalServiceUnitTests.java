package com.fitnessapp.fitapp_api.evolution_kcal;

import com.fitnessapp.fitapp_api.auth.model.UserAuth;
import com.fitnessapp.fitapp_api.core.exception.UserProfileNotCompletedException;
import com.fitnessapp.fitapp_api.core.exception.UserProfileNotFoundException;
import com.fitnessapp.fitapp_api.profile.model.UserProfile;
import com.fitnessapp.fitapp_api.profile.repository.UserProfileRepository;
import com.fitnessapp.fitapp_api.profile.service.UserProfileService;
import com.fitnessapp.fitapp_api.route.model.Route;
import com.fitnessapp.fitapp_api.routeexecution.model.RouteExecution;
import com.fitnessapp.fitapp_api.routeexecution.repository.RouteExecutionRepository;
import com.fitnessapp.fitapp_api.stats.dto.DailyKcalResponseDTO;
import com.fitnessapp.fitapp_api.stats.dto.EvolutionKcalResponseDTO;
import com.fitnessapp.fitapp_api.stats.service.implementation.EvolutionKcalServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EvolutionKcalServiceUnitTests {

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private UserProfileService userProfileService;

    @Mock
    private RouteExecutionRepository routeExecutionRepository;

    @InjectMocks
    private EvolutionKcalServiceImpl evolutionKcalService;

    private UserAuth user;
    private UserProfile profile;
    private String email;

    @BeforeEach
    void setUp() {
        email = "test@example.com";

        user = new UserAuth();
        user.setId(1L);
        user.setEmail(email);

        profile = new UserProfile();
        profile.setId(1L);
        profile.setUser(user);
    }

    // Helper para crear ejecuciones de ruta
    private RouteExecution exec(LocalDateTime end, double kcal, RouteExecution.RouteExecutionStatus st) {
        RouteExecution r = new RouteExecution();
        r.setUser(user);
        r.setEndTime(end);
        r.setStatus(st);
        r.setCalories(BigDecimal.valueOf(kcal));

        Route route = new Route();
        r.setRoute(route);

        return r;
    }

    // ============================================================
    // VALIDACIONES DE PERFIL
    // ============================================================

    @Test
    @DisplayName("Debe lanzar UserProfileNotFoundException si el perfil no existe")
    void getEvolutionKcal_ProfileNotFound_ShouldThrow() {
        when(userProfileRepository.findByUser_Email(email))
                .thenReturn(Optional.empty());

        assertThrows(UserProfileNotFoundException.class,
                () -> evolutionKcalService.getEvolutionKcal(email, 30));

        verify(userProfileRepository).findByUser_Email(email);
    }

    @Test
    @DisplayName("Debe lanzar UserProfileNotCompletedException si el perfil está incompleto")
    void getEvolutionKcal_ProfileNotComplete_ShouldThrow() {
        when(userProfileRepository.findByUser_Email(email))
                .thenReturn(Optional.of(profile));
        when(userProfileService.isProfileComplete(profile))
                .thenReturn(false);

        assertThrows(UserProfileNotCompletedException.class,
                () -> evolutionKcalService.getEvolutionKcal(email, 30));
    }

    // ============================================================
    // CÁLCULO DE EVOLUCIÓN DE KCAL
    // ============================================================

    @Test
    @DisplayName("Debe retornar 30 días con todo a 0 cuando no hay rutas")
    void getEvolutionKcal_NoRoutes_ShouldReturnZeroes() {
        when(userProfileRepository.findByUser_Email(email))
                .thenReturn(Optional.of(profile));
        when(userProfileService.isProfileComplete(profile))
                .thenReturn(true);

        when(routeExecutionRepository.findAllByUserEmail(email))
                .thenReturn(new ArrayList<>());

        EvolutionKcalResponseDTO result = evolutionKcalService.getEvolutionKcal(email, 30);

        assertEquals(30, result.points().size());

        // Todos los caloriesKcal deben ser 0.0
        assertTrue(result.points().stream()
                .allMatch(d -> d.caloriesKcal() == 0.0));
    }

    @Test
    @DisplayName("Debe sumar correctamente las kcal de un día con múltiples rutas FINISHED")
    void getEvolutionKcal_MultipleRoutesSameDay_ShouldSum() {
        LocalDateTime today = LocalDateTime.now();

        List<RouteExecution> execs = List.of(
                exec(today.minusHours(1), 100, RouteExecution.RouteExecutionStatus.FINISHED),
                exec(today.minusHours(3), 250, RouteExecution.RouteExecutionStatus.FINISHED)
        );

        when(userProfileRepository.findByUser_Email(email))
                .thenReturn(Optional.of(profile));
        when(userProfileService.isProfileComplete(profile))
                .thenReturn(true);

        when(routeExecutionRepository.findAllByUserEmail(email))
                .thenReturn(execs);

        EvolutionKcalResponseDTO result = evolutionKcalService.getEvolutionKcal(email, 30);

        // El último índice corresponde a "hoy"
        DailyKcalResponseDTO todayDto = result.points().get(29);
        assertEquals(350.0, todayDto.caloriesKcal(), 0.01);
    }

    @Test
    @DisplayName("Debe ignorar rutas que no están FINISHED")
    void getEvolutionKcal_ShouldIgnoreNonFinishedRoutes() {
        LocalDateTime today = LocalDateTime.now();

        List<RouteExecution> execs = List.of(
                exec(today.minusHours(1), 200, RouteExecution.RouteExecutionStatus.FINISHED),
                exec(today.minusHours(2), 999, RouteExecution.RouteExecutionStatus.IN_PROGRESS),
                exec(today.minusHours(3), 999, RouteExecution.RouteExecutionStatus.PAUSED)
        );

        when(userProfileRepository.findByUser_Email(email))
                .thenReturn(Optional.of(profile));
        when(userProfileService.isProfileComplete(profile))
                .thenReturn(true);

        when(routeExecutionRepository.findAllByUserEmail(email))
                .thenReturn(execs);

        EvolutionKcalResponseDTO result = evolutionKcalService.getEvolutionKcal(email, 30);

        DailyKcalResponseDTO todayDto = result.points().get(29);
        assertEquals(200.0, todayDto.caloriesKcal(), 0.01);
    }

    @Test
    @DisplayName("Debe dejar días sin actividad en 0")
    void getEvolutionKcal_DaysWithoutActivity_ShouldBeZero() {
        LocalDateTime today = LocalDateTime.now();

        List<RouteExecution> execs = List.of(
                exec(today.minusDays(2), 100, RouteExecution.RouteExecutionStatus.FINISHED)
        );

        when(userProfileRepository.findByUser_Email(email))
                .thenReturn(Optional.of(profile));
        when(userProfileService.isProfileComplete(profile))
                .thenReturn(true);

        when(routeExecutionRepository.findAllByUserEmail(email))
                .thenReturn(execs);

        EvolutionKcalResponseDTO result = evolutionKcalService.getEvolutionKcal(email, 30);

        // índices: 29 = hoy, 28 = ayer, 27 = anteayer
        assertEquals(0.0, result.points().get(29).caloriesKcal(), 0.01); // hoy
        assertEquals(0.0, result.points().get(28).caloriesKcal(), 0.01); // ayer
        assertEquals(100.0, result.points().get(27).caloriesKcal(), 0.01); // anteayer
    }

    @Test
    @DisplayName("Debe filtrar rutas fuera del periodo solicitado")
    void getEvolutionKcal_ShouldFilterOutOlderData() {
        LocalDateTime today = LocalDateTime.now();

        List<RouteExecution> execs = List.of(
                exec(today.minusDays(31), 999, RouteExecution.RouteExecutionStatus.FINISHED),
                exec(today.minusDays(5), 300, RouteExecution.RouteExecutionStatus.FINISHED)
        );

        when(userProfileRepository.findByUser_Email(email))
                .thenReturn(Optional.of(profile));
        when(userProfileService.isProfileComplete(profile))
                .thenReturn(true);

        when(routeExecutionRepository.findAllByUserEmail(email))
                .thenReturn(execs);

        EvolutionKcalResponseDTO result = evolutionKcalService.getEvolutionKcal(email, 30);

        // hoy = índice 29
        assertEquals(0.0, result.points().get(29).caloriesKcal(), 0.01);
        // hace 5 días => índice 29 - 5 = 24
        assertEquals(300.0, result.points().get(24).caloriesKcal(), 0.01);
    }
}
