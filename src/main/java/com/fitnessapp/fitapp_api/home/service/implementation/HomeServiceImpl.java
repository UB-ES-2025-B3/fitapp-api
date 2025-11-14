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
import java.util.Comparator;
import java.util.List;

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
                // 2. Ahora es seguro llamar a getEndTime(), porque las rutas finalizadas lo tienen
                .filter(routeExecution -> routeExecution.getEndTime().toLocalDate().equals(today))
                .toList();
        int routesCompleted = todaysCompletedRoutes.size();
        long totalDurationSec = todaysCompletedRoutes.stream()
                .mapToLong(RouteExecution::getDurationSec)
                .sum();
        double totalDistanceKm = todaysCompletedRoutes.stream()
                .mapToDouble(routeExecution -> routeExecution.getRoute().getDistanceKm().doubleValue())
                .sum();
        double totalCalories = todaysCompletedRoutes.stream()
                // 1. Asegurarse de que las calorías no sean nulas antes de sumar
                .filter(routeExecution -> routeExecution.getCalories() != null)
                // 2. Ahora es seguro llamar a .doubleValue()
                .mapToDouble(routeExecution -> routeExecution.getCalories().doubleValue())
                .sum();

        int activeStreak = calculateActiveStreak(routesExecutions);

        List<Route> routes = routeRepository.findAllByUserEmail(email).stream()
                .filter(route -> route.getCreatedAt().toLocalDate().equals(today))
                .toList();

        return new HomeKpisTodayResponseDTO(
                routesCompleted,
                totalDurationSec,
                totalDistanceKm,
                totalCalories,
                activeStreak,
                !routes.isEmpty()
        );
    }

    private int calculateActiveStreak(List<RouteExecution> completedRoutes) {
        List<LocalDate> completionDates = completedRoutes.stream()
                .filter(routeExecution -> routeExecution.getStatus() == RouteExecution.RouteExecutionStatus.FINISHED)
                .map(routeExecution -> routeExecution.getEndTime().toLocalDate())
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