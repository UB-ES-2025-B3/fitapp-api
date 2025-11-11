package com.fitnessapp.fitapp_api.home.service.implementation;

import com.fitnessapp.fitapp_api.core.exception.UserProfileNotCompletedException;
import com.fitnessapp.fitapp_api.core.exception.UserProfileNotFoundException;
import com.fitnessapp.fitapp_api.home.dto.HomeKpisTodayResponseDTO;
import com.fitnessapp.fitapp_api.home.service.HomeService;
import com.fitnessapp.fitapp_api.profile.model.UserProfile;
import com.fitnessapp.fitapp_api.profile.repository.UserProfileRepository;
import com.fitnessapp.fitapp_api.profile.service.UserProfileService;
import com.fitnessapp.fitapp_api.routeexecution.model.RouteExecution;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeServiceImpl implements HomeService {
    private final UserProfileService userProfileService;
    private final UserProfileRepository userProfileRepository;

    @Override
    public HomeKpisTodayResponseDTO getHomeKpisToday(String email) {
        UserProfile profile = userProfileRepository.findByUser_Email(email)
                .orElseThrow(() -> new UserProfileNotFoundException("User profile not found for email: " + email));

        if (!userProfileService.isProfileComplete(profile)) {
            throw new UserProfileNotCompletedException("User profile is not complete for email: " + email);
        }

        List<RouteExecution> userRoutes = new ArrayList<>(); // Placeholder para luego colocar el verdadero get
        // List<RouteExecution> userRoutes = routeExecutionRepository.findAllByUserEmail(email);
        return calculateKpisForToday(userRoutes);
    }

    public HomeKpisTodayResponseDTO calculateKpisForToday(List<RouteExecution> routes) {
        LocalDate today = LocalDate.now();

        List<RouteExecution> todaysCompletedRoutes = routes.stream()
                .filter(routeExecution -> routeExecution.getEndTime().toLocalDate().equals(today))
                .filter(routeExecution -> routeExecution.getStatus() == RouteExecution.RouteExecutionStatus.FINISHED)
                .toList();
        int routesCompleted = todaysCompletedRoutes.size();
        long totalDurationSec = todaysCompletedRoutes.stream()
                .mapToLong(RouteExecution::getDurationSec)
                .sum();
        double totalDistanceKm = todaysCompletedRoutes.stream()
                .mapToDouble(routeExecution -> routeExecution.getRoute().getDistanceKm().doubleValue())
                .sum();
        double totalCalories = todaysCompletedRoutes.stream()
                .mapToDouble(routeExecution -> routeExecution.getCalories().doubleValue())
                .sum();

        int activeStreak = calculateActiveStreak(routes);

        return new HomeKpisTodayResponseDTO(
                routesCompleted,
                totalDurationSec,
                totalDistanceKm,
                totalCalories,
                activeStreak,
                !routes.isEmpty()
        );
    }

    public int calculateActiveStreak(List<RouteExecution> completedRoutes) {
        List<LocalDate> completionDates = completedRoutes.stream()
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
