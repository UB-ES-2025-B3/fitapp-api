package com.fitnessapp.fitapp_api.home.service.implementation;

import com.fitnessapp.fitapp_api.core.exception.UserProfileNotCompletedException;
import com.fitnessapp.fitapp_api.core.exception.UserProfileNotFoundException;
import com.fitnessapp.fitapp_api.home.dto.HomeKpisTodayResponseDTO;
import com.fitnessapp.fitapp_api.home.service.HomeService;
import com.fitnessapp.fitapp_api.profile.model.UserProfile;
import com.fitnessapp.fitapp_api.profile.repository.UserProfileRepository;
import com.fitnessapp.fitapp_api.profile.service.UserProfileService;
import com.fitnessapp.fitapp_api.route.model.Route;
import com.fitnessapp.fitapp_api.route.repository.RouteRepository;
import com.fitnessapp.fitapp_api.routeexecution.model.RouteExecution;
import com.fitnessapp.fitapp_api.routeexecution.repository.RouteExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class HomeServiceImpl implements HomeService {
    private final UserProfileService userProfileService;
    private final UserProfileRepository userProfileRepository;
    private final RouteExecutionRepository routeExecutionRepository;
    private final RouteRepository routeRepository;

    @Override
    public HomeKpisTodayResponseDTO getHomeKpisToday(String email) {
        UserProfile profile = userProfileRepository.findByUser_Email(email)
                .orElseThrow(() -> new UserProfileNotFoundException("User profile not found for email: " + email));

        if (!userProfileService.isProfileComplete(profile)) {
            throw new UserProfileNotCompletedException("User profile is not complete for email: " + email);
        }

        List<RouteExecution> userRoutesExecutions = routeExecutionRepository.findAllByUserEmail(email);
        return calculateKpisForToday(userRoutesExecutions,email);
    }

    private HomeKpisTodayResponseDTO calculateKpisForToday(List<RouteExecution> routesExecutions,String email) {
        LocalDate today = LocalDate.now();

        List<RouteExecution> todaysCompletedRoutes = routesExecutions.stream()
                // 1. Filtrar por estado FINALIZADO primero
                .filter(routeExecution -> routeExecution.getStatus() == RouteExecution.RouteExecutionStatus.FINISHED)
                .filter(routeExecution -> routeExecution.getEndTime() != null)
                // 2. Ahora es seguro llamar a getEndTime(), porque las rutas finalizadas lo tienen
                .filter(routeExecution -> routeExecution.getEndTime().toLocalDate().equals(today))
                .toList();
        int routesCompleted = todaysCompletedRoutes.size();
        long totalDurationSec = todaysCompletedRoutes.stream()
                .mapToLong(routeExecution -> routeExecution.getDurationSec() != null ? routeExecution.getDurationSec() : 0L)
                .sum();
        double totalDistanceKm = todaysCompletedRoutes.stream()
                .filter(routeExecution -> routeExecution.getRoute() != null) // <--- Proteger contra rutas borradas
                .mapToDouble(routeExecution -> routeExecution.getRoute().getDistanceKm().doubleValue())
                .sum();

        double totalCalories = todaysCompletedRoutes.stream()
                .filter(routeExecution -> routeExecution.getCalories() != null)
                .mapToDouble(routeExecution -> routeExecution.getCalories().doubleValue())
                .sum();

        int activeStreak = calculateActiveStreak(routesExecutions);

        // Consultamos a la BD directamente en lugar de traer toda la lista
        boolean hasCreatedRoutes = routeRepository.existsByUser_EmailAndCreatedAtBetween(
                email,
                today.atStartOfDay(),
                today.atTime(LocalTime.MAX)
        );

        return new HomeKpisTodayResponseDTO(
                routesCompleted,
                totalDurationSec,
                totalDistanceKm,
                totalCalories,
                activeStreak,
                hasCreatedRoutes
        );
    }

    private int calculateActiveStreak(List<RouteExecution> completedRoutes) {
        List<LocalDate> completionDates = completedRoutes.stream()
                .filter(routeExecution -> routeExecution.getStatus() == RouteExecution.RouteExecutionStatus.FINISHED)
                .map(RouteExecution::getEndTime)
                .filter(Objects::nonNull)
                .map(java.time.LocalDateTime::toLocalDate)
                .distinct()
                .sorted(Comparator.reverseOrder())
                .toList();

        LocalDate today = LocalDate.now();
        int streak = 0;
        for (LocalDate date : completionDates) {
            if (date.equals(today.minusDays(streak))) {
                streak++;
            } else {
                break;
            }
        }
        return streak;
    }
}