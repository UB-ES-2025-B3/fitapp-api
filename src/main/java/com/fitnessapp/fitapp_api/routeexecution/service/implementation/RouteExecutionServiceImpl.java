package com.fitnessapp.fitapp_api.routeexecution.service.implementation;

import com.fitnessapp.fitapp_api.calories.service.CalorieCalculationService;
import com.fitnessapp.fitapp_api.calories.service.dto.CCActivityRequest;
import com.fitnessapp.fitapp_api.core.exception.RouteNotFoundException;
import com.fitnessapp.fitapp_api.core.exception.UserAuthNotFoundException;
import com.fitnessapp.fitapp_api.routeexecution.dto.RouteExecutionRequestDTO;
import com.fitnessapp.fitapp_api.routeexecution.dto.RouteExecutionResponseDTO;
import com.fitnessapp.fitapp_api.routeexecution.mapper.RouteExecutionMapper;
import com.fitnessapp.fitapp_api.route.model.Route;
import com.fitnessapp.fitapp_api.routeexecution.model.RouteExecution;
import com.fitnessapp.fitapp_api.routeexecution.model.RouteExecution.RouteExecutionStatus;
import com.fitnessapp.fitapp_api.routeexecution.repository.RouteExecutionRepository;
import com.fitnessapp.fitapp_api.route.repository.RouteRepository;
import com.fitnessapp.fitapp_api.profile.model.UserProfile;
import com.fitnessapp.fitapp_api.profile.repository.UserProfileRepository;
import com.fitnessapp.fitapp_api.auth.model.UserAuth;
import com.fitnessapp.fitapp_api.auth.repository.UserAuthRepository;
import com.fitnessapp.fitapp_api.routeexecution.service.RouteExecutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

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
        if (pauseTime == null) {
            throw new IllegalStateException("pauseTime is missing");
        }

        long pausedSec = Duration.between(pauseTime, now).toSeconds();
        Long prevTotalPaused = exec.getTotalPausedTimeSec() != null ? exec.getTotalPausedTimeSec() : 0L;
        exec.setTotalPausedTimeSec(prevTotalPaused + pausedSec);

        // clear pauseTime and set status/start continue
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
            throw new IllegalStateException("Execution already finished");
        }

        // si estaba pausada, acumular tiempo desde pause hasta now
        if (exec.getStatus() == RouteExecutionStatus.PAUSED && exec.getPauseTime() != null) {
            long pausedSec = Duration.between(exec.getPauseTime(), LocalDateTime.now()).toSeconds();
            Long prevTotal = exec.getTotalPausedTimeSec() != null ? exec.getTotalPausedTimeSec() : 0L;
            exec.setTotalPausedTimeSec(prevTotal + pausedSec);
            exec.setPauseTime(null);
        }

        exec.setEndTime(LocalDateTime.now());
        exec.setStatus(RouteExecutionStatus.FINISHED);

        // duration = end - start - totalPaused
        if (exec.getStartTime() != null && exec.getEndTime() != null) {
            long totalSec = Duration.between(exec.getStartTime(), exec.getEndTime()).toSeconds();
            long paused = exec.getTotalPausedTimeSec() != null ? exec.getTotalPausedTimeSec() : 0L;
            long durationSec = Math.max(0L, totalSec - paused);
            exec.setDurationSec(durationSec);
        }

        // activityType could come from finish request (override) or existing execution
        if (request.activityType() != null) {
            exec.setActivityType(request.activityType());
        }
        exec.setNotes(request.notes());

        // calcular calorías si hay perfil completo y duration > 0
        if (exec.getDurationSec() != null && exec.getDurationSec() > 0) {
            // obtener perfil del usuario
            UserProfile profile = userProfileRepository.findByUser_Email(email)
                    .orElse(null);

            if (profile != null) {
                CCActivityRequest ccRequest = new CCActivityRequest(exec.getActivityType().toString(), exec.getDurationSec());
                double calories = calorieCalculationService.calculateCalories(profile, ccRequest);
                exec.setCalories(BigDecimal.valueOf(calories));
            } else {
                // perfil no encontrado: dejar calories a null o 0 según convención
                exec.setCalories(BigDecimal.ZERO);
            }
        } else {
            exec.setCalories(BigDecimal.ZERO);
        }

        RouteExecution saved = executionRepository.save(exec);
        return mapper.toResponseDto(saved);
    }

    /**
     * Listar ejecuciones del usuario
     */
    @Transactional(readOnly = true)
    public List<RouteExecutionResponseDTO> getMyExecutions(String email) {
        return executionRepository.findAllByUserEmail(email)
                .stream()
                .map(mapper::toResponseDto)
                .toList();
    }
}
