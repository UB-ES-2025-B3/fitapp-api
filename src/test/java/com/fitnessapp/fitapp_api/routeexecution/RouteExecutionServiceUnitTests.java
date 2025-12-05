package com.fitnessapp.fitapp_api.routeexecution;

import com.fitnessapp.fitapp_api.auth.model.UserAuth;
import com.fitnessapp.fitapp_api.calories.service.CalorieCalculationService;
import com.fitnessapp.fitapp_api.calories.dto.CCActivityRequest;
import com.fitnessapp.fitapp_api.core.exception.RouteNotFoundException;
import com.fitnessapp.fitapp_api.core.exception.RouteExecutionNotFoundException;
import com.fitnessapp.fitapp_api.core.exception.UserAuthNotFoundException;
import com.fitnessapp.fitapp_api.profile.model.UserProfile;
import com.fitnessapp.fitapp_api.profile.repository.UserProfileRepository;
import com.fitnessapp.fitapp_api.route.model.Route;
import com.fitnessapp.fitapp_api.route.repository.RouteRepository;
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

        when(executionRepository.findByIdAndUserEmail(400L, user.getEmail())).thenReturn(Optional.of(exec));
        when(userProfileRepository.findByUser_Email(user.getEmail())).thenReturn(Optional.of(new UserProfile()));
        when(calorieCalculationService.calculateCalories(any(UserProfile.class), any(CCActivityRequest.class))).thenReturn(123.45);
        when(executionRepository.save(any(RouteExecution.class))).thenAnswer(inv -> inv.getArgument(0));

        RouteExecutionRequestDTO req = new RouteExecutionRequestDTO(RouteExecution.ActivityType.RUNNING_MODERATE, "buenas");
        var dto = service.finishExecution(user.getEmail(), 400L, req);

        assertEquals("FINISHED", dto.status());
        assertNotNull(dto.endTime());
        assertNotNull(dto.durationSec());
        assertTrue(dto.durationSec() >= 0);
        assertNotNull(dto.calories());
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
}
