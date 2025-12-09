package com.fitnessapp.fitapp_api.routeexecution;

import com.fitnessapp.fitapp_api.auth.model.UserAuth;
import com.fitnessapp.fitapp_api.calories.service.CalorieCalculationService;
import com.fitnessapp.fitapp_api.calories.dto.CCActivityRequest;
import com.fitnessapp.fitapp_api.core.exception.RouteNotFoundException;
import com.fitnessapp.fitapp_api.core.exception.RouteExecutionNotFoundException;
import com.fitnessapp.fitapp_api.core.exception.UserAuthNotFoundException;
import com.fitnessapp.fitapp_api.gamification.dto.PCActivityRequestDTO;
import com.fitnessapp.fitapp_api.gamification.service.PointsCalculationService;
import com.fitnessapp.fitapp_api.profile.model.UserProfile;
import com.fitnessapp.fitapp_api.profile.repository.UserProfileRepository;
import com.fitnessapp.fitapp_api.route.model.Route;
import com.fitnessapp.fitapp_api.route.repository.RouteRepository;
import com.fitnessapp.fitapp_api.routeexecution.dto.RouteExecutionHistoryResponseDTO;
import com.fitnessapp.fitapp_api.routeexecution.dto.RouteExecutionRequestDTO;
import com.fitnessapp.fitapp_api.routeexecution.dto.RouteExecutionResponseDTO;
import com.fitnessapp.fitapp_api.routeexecution.mapper.RouteExecutionMapper;
import com.fitnessapp.fitapp_api.routeexecution.model.RouteExecution;
import com.fitnessapp.fitapp_api.routeexecution.model.RouteExecution.RouteExecutionStatus;
import com.fitnessapp.fitapp_api.routeexecution.repository.RouteExecutionRepository;
import com.fitnessapp.fitapp_api.routeexecution.service.implementation.RouteExecutionServiceImpl;
import com.fitnessapp.fitapp_api.auth.repository.UserAuthRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RouteExecutionServiceUnitTests {

    @Mock
    private RouteExecutionRepository executionRepository;

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private UserAuthRepository userAuthRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Spy
    private RouteExecutionMapper mapper = Mappers.getMapper(RouteExecutionMapper.class);

    @Mock
    private CalorieCalculationService calorieCalculationService;
    @Mock
    private PointsCalculationService pointsCalculationService;

    @InjectMocks
    private RouteExecutionServiceImpl service;

    private UserAuth user;
    private Route route;

    @BeforeEach
    void setUp() {
        user = new UserAuth();
        user.setId(1L);
        user.setEmail("tester@example.com");

        route = new Route();
        route.setId(10L);
        route.setName("Ruta Test");
        route.setDistanceKm(BigDecimal.valueOf(3.0));
        route.setUser(user);
    }

    // Helper to create a RouteExecution entity
    private RouteExecution createExecution(Long id, RouteExecutionStatus status, LocalDateTime start, LocalDateTime pauseTime, Long totalPaused, Long durationSec) {
        RouteExecution ex = new RouteExecution();
        ex.setId(id);
        ex.setRoute(route);
        ex.setUser(user);
        ex.setStatus(status);
        ex.setStartTime(start);
        ex.setPauseTime(pauseTime);
        ex.setTotalPausedTimeSec(totalPaused != null ? totalPaused : 0L);
        ex.setDurationSec(durationSec);
        ex.setNotes(null);
        return ex;
    }

    // ============================================
    // startExecution
    // ============================================
    @Test
    @DisplayName("startExecution — ruta y usuario existen → crea ejecución IN_PROGRESS")
    void startExecution_ShouldCreateExecution() {
        RouteExecutionRequestDTO req = new RouteExecutionRequestDTO(null, "Notas");

        when(routeRepository.findById(10L)).thenReturn(Optional.of(route));
        when(userAuthRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        // capture saved entity and return it with id
        when(executionRepository.save(any(RouteExecution.class))).thenAnswer(inv -> {
            RouteExecution r = inv.getArgument(0);
            r.setId(100L);
            return r;
        });

        RouteExecutionResponseDTO result = service.startExecution(user.getEmail(), 10L, req);

        assertNotNull(result);
        assertEquals(100L, result.id());
        assertEquals("IN_PROGRESS", result.status());
        verify(executionRepository).save(any(RouteExecution.class));
    }

    @Test
    @DisplayName("startExecution — ruta no existe → lanza RouteNotFoundException")
    void startExecution_RouteNotFound_ShouldThrow() {
        RouteExecutionRequestDTO req = new RouteExecutionRequestDTO(null, "Notas");

        when(routeRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(RouteNotFoundException.class, () -> service.startExecution(user.getEmail(), 10L, req));
    }

    @Test
    @DisplayName("startExecution — usuario no existe → lanza UserAuthNotFoundException")
    void startExecution_UserNotFound_ShouldThrow() {
        RouteExecutionRequestDTO req = new RouteExecutionRequestDTO(null, "Notas");

        when(routeRepository.findById(10L)).thenReturn(Optional.of(route));
        when(userAuthRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());

        assertThrows(UserAuthNotFoundException.class, () -> service.startExecution(user.getEmail(), 10L, req));
    }

    // ============================================
    // pauseExecution
    // ============================================
    @Test
    @DisplayName("pauseExecution — ejecución en IN_PROGRESS → marca pauseTime y pasa a PAUSED")
    void pauseExecution_InProgress_ShouldPause() {
        LocalDateTime now = LocalDateTime.now();
        RouteExecution exec = createExecution(200L, RouteExecutionStatus.IN_PROGRESS, now.minusSeconds(30), null, 0L, 0L);

        when(executionRepository.findByIdAndUserEmail(200L, user.getEmail())).thenReturn(Optional.of(exec));
        when(executionRepository.save(any(RouteExecution.class))).thenAnswer(inv -> inv.getArgument(0));
        // mapper will convert to DTO; we spy mapper so it works

        var dto = service.pauseExecution(user.getEmail(), 200L);

        assertEquals("PAUSED", dto.status());
        assertNotNull(dto.pauseTime());
        verify(executionRepository).save(exec);
    }

    @Test
    @DisplayName("pauseExecution — si no está IN_PROGRESS lanza IllegalStateException")
    void pauseExecution_NotInProgress_ShouldThrow() {
        LocalDateTime now = LocalDateTime.now();
        RouteExecution exec = createExecution(201L, RouteExecutionStatus.FINISHED, now.minusSeconds(100), null, 0L, 100L);

        when(executionRepository.findByIdAndUserEmail(201L, user.getEmail())).thenReturn(Optional.of(exec));

        assertThrows(IllegalStateException.class, () -> service.pauseExecution(user.getEmail(), 201L));
    }

    // ============================================
    // resumeExecution
    // ============================================
    @Test
    @DisplayName("resumeExecution — ejecución pausada → acumula tiempo pausado y vuelve a IN_PROGRESS")
    void resumeExecution_Paused_ShouldResumeAndAccumulate() {
        LocalDateTime start = LocalDateTime.now().minusMinutes(5);
        LocalDateTime pauseAt = LocalDateTime.now().minusSeconds(2); // paused 2 seconds ago
        RouteExecution exec = createExecution(300L, RouteExecutionStatus.PAUSED, start, pauseAt, 5L, 0L);

        when(executionRepository.findByIdAndUserEmail(300L, user.getEmail())).thenReturn(Optional.of(exec));
        when(executionRepository.save(any(RouteExecution.class))).thenAnswer(inv -> inv.getArgument(0));

        var dto = service.resumeExecution(user.getEmail(), 300L);

        assertEquals("IN_PROGRESS", dto.status());
        // totalPausedTimeSec should be >= previous total (>=5)
        assertTrue(exec.getTotalPausedTimeSec() >= 5L);
        assertNull(exec.getPauseTime());
        verify(executionRepository).save(exec);
    }

    @Test
    @DisplayName("resumeExecution — si no está PAUSED lanza IllegalStateException")
    void resumeExecution_NotPaused_ShouldThrow() {
        LocalDateTime start = LocalDateTime.now().minusMinutes(10);
        RouteExecution exec = createExecution(301L, RouteExecutionStatus.IN_PROGRESS, start, null, 0L, 0L);

        when(executionRepository.findByIdAndUserEmail(301L, user.getEmail())).thenReturn(Optional.of(exec));

        assertThrows(IllegalStateException.class, () -> service.resumeExecution(user.getEmail(), 301L));
    }

    // ============================================
    // finishExecution
    // ============================================
    @Test
    @DisplayName("finishExecution — desde IN_PROGRESS calcula duración y calorías y marca FINISHED")
    void finishExecution_FromInProgress_ShouldFinishAndCalculateCalories() {
        LocalDateTime start = LocalDateTime.now().minusMinutes(10);
        RouteExecution exec = createExecution(400L, RouteExecutionStatus.IN_PROGRESS, start, null, 0L, null);

        UserProfile profile = new UserProfile();
        profile.setUser(user);
        profile.setPoints(80L);

        when(executionRepository.findByIdAndUserEmail(400L, user.getEmail())).thenReturn(Optional.of(exec));
        when(userProfileRepository.findByUser_Email(user.getEmail())).thenReturn(Optional.of(profile));
        when(calorieCalculationService.calculateCalories(any(UserProfile.class), any(CCActivityRequest.class))).thenReturn(123.45);
        when(calorieCalculationService.hasReachedDailyGoal(profile)).thenReturn(false);
        when(pointsCalculationService.calculatePoints(any())).thenReturn(40L);
        when(executionRepository.save(any(RouteExecution.class))).thenAnswer(inv -> inv.getArgument(0));

        RouteExecutionRequestDTO req = new RouteExecutionRequestDTO(RouteExecution.ActivityType.RUNNING_MODERATE, "buenas");
        var dto = service.finishExecution(user.getEmail(), 400L, req);

        assertEquals("FINISHED", dto.status());
        assertNotNull(dto.endTime());
        assertNotNull(dto.durationSec());
        assertTrue(dto.durationSec() >= 0);
        assertEquals(123.45, dto.calories(), 0.01);
        assertEquals(40L, dto.points());
        assertEquals(120L, profile.getPoints());
        verify(pointsCalculationService).calculatePoints(any());
        verify(userProfileRepository).save(profile);
        verify(executionRepository).save(exec);
    }

    @Test
    @DisplayName("finishExecution — si ya estaba FINISHED devuelve el DTO sin recalcular")
    void finishExecution_AlreadyFinished_ShouldReturnExisting() {
        LocalDateTime start = LocalDateTime.now().minusMinutes(10);
        LocalDateTime end = LocalDateTime.now().minusMinutes(1);
        RouteExecution exec = createExecution(401L, RouteExecutionStatus.FINISHED, start, null, 0L, 540L);
        exec.setEndTime(end);
        exec.setCalories(BigDecimal.valueOf(50.0));

        when(executionRepository.findByIdAndUserEmail(401L, user.getEmail())).thenReturn(Optional.of(exec));

        var dto = service.finishExecution(user.getEmail(), 401L, new RouteExecutionRequestDTO(null, null));

        assertEquals("FINISHED", dto.status());
        // ensure mapper translated calories to DTO value (may be null depending on mapper)
        assertEquals(50.0, dto.calories(), 0.01);
    }

    @Test
    @DisplayName("finishExecution — ejecución no encontrada → lanza RouteExecutionNotFoundException")
    void finishExecution_NotFound_ShouldThrow() {
        when(executionRepository.findByIdAndUserEmail(999L, user.getEmail())).thenReturn(Optional.empty());

        assertThrows(RouteExecutionNotFoundException.class, () -> service.finishExecution(user.getEmail(), 999L, new RouteExecutionRequestDTO(null, null)));
    }

    // ============================================
    // getMyExecutions
    // ============================================
    @Test
    @DisplayName("getMyExecutions — devuelve lista mapeada correctamente")
    void getMyExecutions_ShouldReturnList() {
        LocalDateTime now = LocalDateTime.now();
        RouteExecution e1 = createExecution(500L, RouteExecutionStatus.FINISHED, now.minusDays(1), null, 0L, 3600L);
        RouteExecution e2 = createExecution(501L, RouteExecutionStatus.IN_PROGRESS, now.minusMinutes(10), null, 0L, 0L);

        when(executionRepository.findAllByUserEmail(user.getEmail())).thenReturn(List.of(e1, e2));

        var result = service.getMyExecutions(user.getEmail());

        assertEquals(2, result.size());
        verify(executionRepository).findAllByUserEmail(user.getEmail());
    }

    // ============================================
    // getMyCompletedExecutionsHistory
    // ============================================
    // TODO: Aca se necesita un test de integracion para verificar que solo se retornan las ejecuciones FINISHED
    @Test
    @DisplayName("getMyCompletedExecutionsHistory — devuelve historial ordenado y mapeado")
    void getMyCompletedExecutionsHistory_ShouldReturnHistory() {
        LocalDateTime now = LocalDateTime.now();
        RouteExecution older = createExecution(600L, RouteExecutionStatus.FINISHED, now.minusHours(2), null, 0L, 3600L);
        older.setEndTime(now.minusHours(2));
        RouteExecution recent = createExecution(601L, RouteExecutionStatus.FINISHED, now.minusMinutes(30), null, 0L, 1800L);
        recent.setEndTime(now.minusMinutes(30));

        when(executionRepository.findAllByUserEmailAndStatusOrderByEndTimeDesc(
                user.getEmail(), RouteExecutionStatus.FINISHED))
                .thenReturn(List.of(recent, older));

        List<RouteExecutionHistoryResponseDTO> history = service.getMyCompletedExecutionsHistory(user.getEmail());

        assertEquals(2, history.size());
        assertEquals(recent.getRoute().getName(), history.get(0).routeName());
        assertEquals(older.getDurationSec(), history.get(1).durationSec());
        verify(executionRepository).findAllByUserEmailAndStatusOrderByEndTimeDesc(user.getEmail(), RouteExecutionStatus.FINISHED);
    }

    @Test
    @DisplayName("getMyCompletedExecutionsHistory — retorna lista vacía cuando no hay ejecuciones")
    void getMyCompletedExecutionsHistory_ShouldReturnEmptyList() {
        when(executionRepository.findAllByUserEmailAndStatusOrderByEndTimeDesc(user.getEmail(), RouteExecutionStatus.FINISHED))
                .thenReturn(List.of());

        List<RouteExecutionHistoryResponseDTO> history = service.getMyCompletedExecutionsHistory(user.getEmail());

        assertTrue(history.isEmpty());
        verify(executionRepository).findAllByUserEmailAndStatusOrderByEndTimeDesc(user.getEmail(), RouteExecutionStatus.FINISHED);
    }

    // ============================================
    // Puntos en Ejecución finalizada y perfil
    // ============================================
    @Test
    @DisplayName("finishExecution — suma puntos y actualiza el perfil")
    void finishExecution_ShouldAccumulatePointsAndUpdateProfile() {
        LocalDateTime start = LocalDateTime.now().minusMinutes(15);
        RouteExecution exec = createExecution(600L, RouteExecutionStatus.IN_PROGRESS, start, null, 0L, null);
        exec.setActivityType(RouteExecution.ActivityType.RUNNING_INTENSE);

        UserProfile profile = new UserProfile();
        profile.setUser(user);
        profile.setPoints(200L);

        when(executionRepository.findByIdAndUserEmail(600L, user.getEmail())).thenReturn(Optional.of(exec));
        when(userProfileRepository.findByUser_Email(user.getEmail())).thenReturn(Optional.of(profile));
        when(calorieCalculationService.calculateCalories(any(UserProfile.class), any(CCActivityRequest.class))).thenReturn(250.0);
        when(calorieCalculationService.hasReachedDailyGoal(profile)).thenReturn(true);
        when(pointsCalculationService.calculatePoints(any())).thenReturn(60L);
        when(executionRepository.save(any(RouteExecution.class))).thenAnswer(inv -> inv.getArgument(0));

        var dto = service.finishExecution(user.getEmail(), 600L,
                new RouteExecutionRequestDTO(RouteExecution.ActivityType.RUNNING_INTENSE, "puntos"));

        assertEquals(60L, dto.points());
        assertEquals(260L, profile.getPoints());
        verify(pointsCalculationService).calculatePoints(any());
        verify(userProfileRepository).save(profile);
    }

    @Test
    @DisplayName("finishExecution — sin perfil no actualiza puntos")
    void finishExecution_NoProfile_ShouldSkipPointsCalculation() {
        LocalDateTime start = LocalDateTime.now().minusMinutes(15);
        RouteExecution exec = createExecution(601L, RouteExecutionStatus.IN_PROGRESS, start, null, 0L, null);
        exec.setActivityType(RouteExecution.ActivityType.WALKING_MODERATE);

        when(executionRepository.findByIdAndUserEmail(601L, user.getEmail())).thenReturn(Optional.of(exec));
        when(userProfileRepository.findByUser_Email(user.getEmail())).thenReturn(Optional.empty());
        when(executionRepository.save(any(RouteExecution.class))).thenAnswer(inv -> inv.getArgument(0));

        var dto = service.finishExecution(user.getEmail(), 601L,
                new RouteExecutionRequestDTO(RouteExecution.ActivityType.WALKING_MODERATE, null));

        assertEquals(0L, dto.points());
        verify(pointsCalculationService, never()).calculatePoints(any());
        verify(userProfileRepository, never()).save(any(UserProfile.class));
    }

    @Test
    @DisplayName("finishExecution — múltiples rutas acumulan puntos sobre el perfil")
    void finishExecution_MultipleRoutes_ShouldKeepAccumulatingPoints() {
        LocalDateTime start = LocalDateTime.now().minusMinutes(20);
        RouteExecution firstExec = createExecution(700L, RouteExecutionStatus.IN_PROGRESS, start, null, 0L, null);
        firstExec.setActivityType(RouteExecution.ActivityType.RUNNING_INTENSE);

        Route secondRoute = new Route();
        secondRoute.setId(11L);
        secondRoute.setName("Ruta Extra");
        secondRoute.setDistanceKm(BigDecimal.valueOf(5.5));
        secondRoute.setUser(user);

        RouteExecution secondExec = createExecution(701L, RouteExecutionStatus.IN_PROGRESS, start.plusMinutes(5), null, 0L, null);
        secondExec.setRoute(secondRoute);
        secondExec.setActivityType(RouteExecution.ActivityType.CYCLING_MODERATE);

        UserProfile profile = new UserProfile();
        profile.setUser(user);
        profile.setPoints(120L);

        when(executionRepository.findByIdAndUserEmail(700L, user.getEmail())).thenReturn(Optional.of(firstExec));
        when(executionRepository.findByIdAndUserEmail(701L, user.getEmail())).thenReturn(Optional.of(secondExec));
        when(userProfileRepository.findByUser_Email(user.getEmail())).thenReturn(Optional.of(profile));
        when(calorieCalculationService.calculateCalories(any(UserProfile.class), any(CCActivityRequest.class)))
                .thenReturn(300.0, 180.0);
        when(calorieCalculationService.hasReachedDailyGoal(profile)).thenReturn(true);
        when(pointsCalculationService.calculatePoints(any())).thenReturn(80L, 35L);
        when(executionRepository.save(any(RouteExecution.class))).thenAnswer(inv -> inv.getArgument(0));

        var firstDto = service.finishExecution(user.getEmail(), 700L,
                new RouteExecutionRequestDTO(RouteExecution.ActivityType.RUNNING_INTENSE, "ruta 1"));
        var secondDto = service.finishExecution(user.getEmail(), 701L,
                new RouteExecutionRequestDTO(RouteExecution.ActivityType.CYCLING_MODERATE, "ruta 2"));

        assertEquals(80L, firstDto.points());
        assertEquals(35L, secondDto.points());
        assertEquals(120L + 80L + 35L, profile.getPoints());
        verify(pointsCalculationService, times(2)).calculatePoints(any());
        verify(userProfileRepository, times(2)).save(profile);
    }

    @Test
    @DisplayName("finishExecution — perfil con goal diario completado envía flag y recibe puntos extra")
    void finishExecution_DailyGoalCompleted_ShouldSendBonusFlag() {
        LocalDateTime start = LocalDateTime.now().minusMinutes(25);
        RouteExecution exec = createExecution(720L, RouteExecutionStatus.IN_PROGRESS, start, null, 0L, null);
        exec.setActivityType(RouteExecution.ActivityType.RUNNING_MODERATE);

        UserProfile profile = new UserProfile();
        profile.setUser(user);
        profile.setPoints(90L);

        when(executionRepository.findByIdAndUserEmail(720L, user.getEmail())).thenReturn(Optional.of(exec));
        when(userProfileRepository.findByUser_Email(user.getEmail())).thenReturn(Optional.of(profile));
        when(calorieCalculationService.calculateCalories(any(UserProfile.class), any(CCActivityRequest.class))).thenReturn(220.0);
        when(calorieCalculationService.hasReachedDailyGoal(profile)).thenReturn(true);
        when(pointsCalculationService.calculatePoints(any())).thenReturn(70L);
        when(executionRepository.save(any(RouteExecution.class))).thenAnswer(inv -> inv.getArgument(0));

        var dto = service.finishExecution(user.getEmail(), 720L,
                new RouteExecutionRequestDTO(RouteExecution.ActivityType.RUNNING_MODERATE, "goal cumplido"));

        ArgumentCaptor<PCActivityRequestDTO> captor = ArgumentCaptor.forClass(PCActivityRequestDTO.class);
        verify(pointsCalculationService).calculatePoints(captor.capture());

        assertEquals(70L, dto.points());
        assertEquals(160L, profile.getPoints());
        assertTrue(captor.getValue().dailyGoalCompleted());
        verify(userProfileRepository).save(profile);
    }

    @Test
    @DisplayName("finishExecution — perfil sin goal diario cumplido no marca el flag ni aplica bonus")
    void finishExecution_DailyGoalNotCompleted_ShouldSendFalseFlag() {
        LocalDateTime start = LocalDateTime.now().minusMinutes(30);
        RouteExecution exec = createExecution(721L, RouteExecutionStatus.IN_PROGRESS, start, null, 0L, null);
        exec.setActivityType(RouteExecution.ActivityType.CYCLING_MODERATE);

        UserProfile profile = new UserProfile();
        profile.setUser(user);
        profile.setPoints(50L);

        when(executionRepository.findByIdAndUserEmail(721L, user.getEmail())).thenReturn(Optional.of(exec));
        when(userProfileRepository.findByUser_Email(user.getEmail())).thenReturn(Optional.of(profile));
        when(calorieCalculationService.calculateCalories(any(UserProfile.class), any(CCActivityRequest.class))).thenReturn(140.0);
        when(calorieCalculationService.hasReachedDailyGoal(profile)).thenReturn(false);
        when(pointsCalculationService.calculatePoints(any())).thenReturn(25L);
        when(executionRepository.save(any(RouteExecution.class))).thenAnswer(inv -> inv.getArgument(0));

        var dto = service.finishExecution(user.getEmail(), 721L,
                new RouteExecutionRequestDTO(RouteExecution.ActivityType.CYCLING_MODERATE, "sin bonus"));

        ArgumentCaptor<PCActivityRequestDTO> captor = ArgumentCaptor.forClass(PCActivityRequestDTO.class);
        verify(pointsCalculationService).calculatePoints(captor.capture());

        assertEquals(25L, dto.points());
        assertEquals(75L, profile.getPoints());
        assertFalse(captor.getValue().dailyGoalCompleted());
        verify(userProfileRepository).save(profile);
    }
}
