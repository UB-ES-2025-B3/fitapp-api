package com.fitnessapp.fitapp_api.routeexecution.service.implementation;

import com.fitnessapp.fitapp_api.auth.model.UserAuth;
import com.fitnessapp.fitapp_api.auth.repository.UserAuthRepository;
import com.fitnessapp.fitapp_api.calories.dto.CCActivityRequest;
import com.fitnessapp.fitapp_api.calories.service.CalorieCalculationService;
import com.fitnessapp.fitapp_api.core.exception.RouteExecutionNotFoundException;
import com.fitnessapp.fitapp_api.core.exception.RouteNotFoundException;
import com.fitnessapp.fitapp_api.core.exception.UserAuthNotFoundException;
import com.fitnessapp.fitapp_api.core.exception.UserProfileNotCompletedException;
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
import com.fitnessapp.fitapp_api.routeexecution.service.RouteExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RouteExecutionServiceImpl implements RouteExecutionService {

    private final RouteExecutionRepository executionRepository;
    private final RouteRepository routeRepository;
    private final UserAuthRepository userAuthRepository;
    private final UserProfileRepository userProfileRepository;
    private final RouteExecutionMapper mapper;
    private final CalorieCalculationService calorieCalculationService;
    private final PointsCalculationService pointsCalculationService;

    /**
     * Inicia una ejecución: crea entidad con status IN_PROGRESS y startTime = now.
     */
    public RouteExecutionResponseDTO startExecution(String email, Long routeId, RouteExecutionRequestDTO request) {
        // Verificar ruta y usuario
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RouteNotFoundException("Route not found for id: " + routeId));

        UserAuth user = userAuthRepository.findByEmail(email)
                .orElseThrow(() -> new UserAuthNotFoundException("User not found for email: " + email));

        // Crear ejecución inicial
        RouteExecution exec = new RouteExecution();
        exec.setRoute(route);
        exec.setUser(user);
        exec.setStatus(RouteExecutionStatus.IN_PROGRESS);
        exec.setStartTime(LocalDateTime.now());
        exec.setTotalPausedTimeSec(0L);
        exec.setDurationSec(0L);
        exec.setActivityType(request.activityType());
        exec.setNotes(request.notes());

        RouteExecution saved = executionRepository.save(exec);
        return mapper.toResponseDto(saved);
    }

    /**
     * Pausar: marca pauseTime ahora y cambia status a PAUSED.
     */
    public RouteExecutionResponseDTO pauseExecution(String email, Long executionId) {
        RouteExecution exec = executionRepository.findByIdAndUserEmail(executionId, email)
                .orElseThrow(() -> new com.fitnessapp.fitapp_api.core.exception.RouteExecutionNotFoundException(
                        "Execution not found for id: " + executionId));

        if (exec.getStatus() != RouteExecutionStatus.IN_PROGRESS) {
            // Si no está en curso no podemos pausar; lanzar IllegalState o similar
            if (exec.getStatus() == RouteExecutionStatus.PAUSED) return mapper.toResponseDto(exec);
            throw new IllegalStateException("Execution is not in progress and cannot be paused");
        }

        exec.setPauseTime(LocalDateTime.now());
        exec.setStatus(RouteExecutionStatus.PAUSED);

        RouteExecution saved = executionRepository.save(exec);
        return mapper.toResponseDto(saved);
    }

    /**
     * Reanudar: acumula tiempo pausado y vuelve a IN_PROGRESS.
     * Se asume que pauseTime está presente.
     */
    public RouteExecutionResponseDTO resumeExecution(String email, Long executionId) {
        RouteExecution exec = executionRepository.findByIdAndUserEmail(executionId, email)
                .orElseThrow(() -> new com.fitnessapp.fitapp_api.core.exception.RouteExecutionNotFoundException(
                        "Execution not found for id: " + executionId));

        if (exec.getStatus() != RouteExecutionStatus.PAUSED) {
            throw new IllegalStateException("Execution is not paused and cannot be resumed");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime pauseTime = exec.getPauseTime();
        // Protección contra NullPointer si los datos vinieron corruptos
        if (pauseTime != null) {
            long pausedSec = Duration.between(pauseTime, now).toSeconds();
            // [MODIFICADO] Evitar tiempos negativos si el reloj del servidor se desajusta
            pausedSec = Math.max(0, pausedSec);

            Long prevTotalPaused = exec.getTotalPausedTimeSec() != null ? exec.getTotalPausedTimeSec() : 0L;
            exec.setTotalPausedTimeSec(prevTotalPaused + pausedSec);
        }

        exec.setPauseTime(null);
        exec.setStatus(RouteExecutionStatus.IN_PROGRESS);

        RouteExecution saved = executionRepository.save(exec);
        return mapper.toResponseDto(saved);
    }

    /**
     * Finalizar: fija endTime, calcula duración efectiva y calorías, status FINISHED.
     */
    public RouteExecutionResponseDTO finishExecution(String email, Long executionId, RouteExecutionRequestDTO request) {
        RouteExecution exec = executionRepository.findByIdAndUserEmail(executionId, email)
                .orElseThrow(() -> new com.fitnessapp.fitapp_api.core.exception.RouteExecutionNotFoundException(
                        "Execution not found for id : " + executionId));

        if (exec.getStatus() == RouteExecutionStatus.FINISHED) {
            // Si ya estaba finalizada, devolver ok en lugar de error
            return mapper.toResponseDto(exec);
        }

        // si estaba pausada, acumular tiempo desde pause hasta now
        if (exec.getStatus() == RouteExecutionStatus.PAUSED && exec.getPauseTime() != null) {
            long pausedSec = Duration.between(exec.getPauseTime(), LocalDateTime.now()).toSeconds();
            Long prevTotal = exec.getTotalPausedTimeSec() != null ? exec.getTotalPausedTimeSec() : 0L;
            // Protección contra negativos
            exec.setTotalPausedTimeSec(prevTotal + Math.max(0, pausedSec));
            exec.setPauseTime(null);
        }

        exec.setEndTime(LocalDateTime.now());
        exec.setStatus(RouteExecutionStatus.FINISHED);

        // duration = end - start - totalPaused
        if (exec.getStartTime() != null && exec.getEndTime() != null) {
            long totalSec = Duration.between(exec.getStartTime(), exec.getEndTime()).toSeconds();
            long paused = exec.getTotalPausedTimeSec() != null ? exec.getTotalPausedTimeSec() : 0L;
            // Math.max para asegurar duración >= 0
            long durationSec = Math.max(0L, totalSec - paused);
            exec.setDurationSec(durationSec);
        } else {
            exec.setDurationSec(0L);
        }

        // activityType could come from finish request (override) or existing execution
        if (request.activityType() != null) {
            exec.setActivityType(request.activityType());
        }
        exec.setNotes(request.notes());

        // Método seguro para calcular calorías sin romper la transacción
        calculateAndSetCaloriesSafe(email, exec);

        // Método seguro para calcular puntos sin romper la transacción
        calculateAndSetPointsSafe(exec);

        RouteExecution saved = executionRepository.save(exec);
        return mapper.toResponseDto(saved);
    }

    /**
     * Nuevo método helper para blindar el cálculo de calorías
     */
    private void calculateAndSetCaloriesSafe(String email, RouteExecution exec) {
        if (exec.getDurationSec() == null || exec.getDurationSec() <= 0) {
            exec.setCalories(BigDecimal.ZERO);
            return;
        }

        try {
            UserProfile profile = userProfileRepository.findByUser_Email(email).orElse(null);

            if (profile != null) {
                // Fallback a WALKING_MODERATE si no hay actividad definida
                String activityStr = exec.getActivityType() != null ? exec.getActivityType().toString() : "WALKING_MODERATE";

                CCActivityRequest ccRequest = new CCActivityRequest(activityStr, exec.getDurationSec());

                double calories = calorieCalculationService.calculateCalories(profile, ccRequest);
                exec.setCalories(BigDecimal.valueOf(calories));
            } else {
                log.warn("No calorie calculation: User profile not found for email {}", email);
                exec.setCalories(BigDecimal.ZERO);
            }
        } catch (UserProfileNotCompletedException | IllegalArgumentException e) {
            // Atrapamos excepciones de negocio esperadas (perfil incompleto)
            log.warn("Cannot calculate calories for execution {}: {}", exec.getId(), e.getMessage());
            exec.setCalories(BigDecimal.ZERO);
        } catch (Exception e) {
            // Atrapamos cualquier otro error inesperado
            log.error("Unexpected error calculating calories for execution {}", exec.getId(), e);
            exec.setCalories(BigDecimal.ZERO);
        }
    }

    /**
     * Nuevo método helper para blindar el cálculo de puntos
     */
    private void calculateAndSetPointsSafe(RouteExecution exec) {
        if (exec.getDurationSec() == null || exec.getDurationSec() <= 0) {
            exec.setPoints(0L);
            return;
        }
        try {
            UserProfile profile = userProfileRepository.findByUser_Email(
                    exec.getUser().getEmail()).orElse(null);

            if (profile != null) {
                if (exec.getActivityType() == null) {
                    throw new IllegalArgumentException("Activity type is required for points calculation");
                }
                PCActivityRequestDTO pcRequest = new PCActivityRequestDTO(
                        exec.getRoute().getDistanceKm().doubleValue(),
                        exec.getDurationSec(),
                        exec.getActivityType().toString(),
                        calorieCalculationService.hasReachedDailyGoal(profile)
                );
                long points = pointsCalculationService.calculatePoints(pcRequest);
                exec.setPoints(points);

                long currentPoints = profile.getPoints() != null ? profile.getPoints() : 0L;
                profile.setPoints(currentPoints + points);
                userProfileRepository.save(profile);
            } else {
                log.warn("No points calculation: User profile not found for email {}", exec.getUser().getEmail());
                exec.setPoints(0L);
            }
        } catch (IllegalArgumentException e) {
            log.warn("Cannot calculate points for execution {}: {}", exec.getId(), e.getMessage());
            exec.setPoints(0L);
        } catch (Exception e) {
            log.error("Unexpected error calculating points for execution {}", exec.getId(), e);
            exec.setPoints(0L);
        }
    }

    /**
     * Listar ejecuciones totales del usuario
     */
    @Transactional(readOnly = true)
    public List<RouteExecutionResponseDTO> getMyExecutions(String email) {
        return executionRepository.findAllByUserEmail(email)
                .stream()
                .map(mapper::toResponseDto)
                .toList();
    }

    /**
     * Obtener historial de ejecuciones completadas del usuario
     */
    @Transactional(readOnly = true)
    public List<RouteExecutionHistoryResponseDTO> getMyCompletedExecutionsHistory(String email) {
        return executionRepository.findAllByUserEmailAndStatusOrderByEndTimeDesc(email, RouteExecutionStatus.FINISHED)
                .stream()
                .map(mapper::toHistoryResponseDto)
                .toList();
    }

    // Método helper para evitar duplicar el .findById...orElseThrow
    private RouteExecution getExecutionOrThrow(Long id, String email) {
        return executionRepository.findByIdAndUserEmail(id, email)
                .orElseThrow(() -> new RouteExecutionNotFoundException(
                        "Execution not found for id: " + id));
    }
}
