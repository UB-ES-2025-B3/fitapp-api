package com.fitnessapp.fitapp_api.stats.service.implementation;

import com.fitnessapp.fitapp_api.core.exception.UserProfileNotCompletedException;
import com.fitnessapp.fitapp_api.core.exception.UserProfileNotFoundException;
import com.fitnessapp.fitapp_api.profile.model.UserProfile;
import com.fitnessapp.fitapp_api.profile.repository.UserProfileRepository;
import com.fitnessapp.fitapp_api.profile.service.UserProfileService;
import com.fitnessapp.fitapp_api.routeexecution.model.RouteExecution;
import com.fitnessapp.fitapp_api.routeexecution.repository.RouteExecutionRepository;
import com.fitnessapp.fitapp_api.stats.dto.DailyKcalResponseDTO;
import com.fitnessapp.fitapp_api.stats.dto.EvolutionKcalResponseDTO;
import com.fitnessapp.fitapp_api.stats.service.EvolutionKcalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EvolutionKcalServiceImpl implements EvolutionKcalService {

    private final UserProfileRepository userProfileRepository;
    private final UserProfileService userProfileService;
    private final RouteExecutionRepository routeExecutionRepository;

    @Override
    public EvolutionKcalResponseDTO getEvolutionKcal(String email, int days) {

        // --- 1. Validar perfil ---
        UserProfile profile = userProfileRepository.findByUser_Email(email)
                .orElseThrow(() -> new UserProfileNotFoundException("User profile not found for email: " + email));

        if (!userProfileService.isProfileComplete(profile)) {
            throw new UserProfileNotCompletedException("User profile is not complete for email: " + email);
        }

        // --- 2. Obtener TODAS las ejecuciones del usuario ---
        List<RouteExecution> allExecutions = routeExecutionRepository.findAllByUserEmail(email);

        // --- 3. Filtrar solo las finalizadas y dentro del rango ---
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(days - 1);

        List<RouteExecution> executionsInRange = allExecutions.stream()
                .filter(re -> re.getStatus() == RouteExecution.RouteExecutionStatus.FINISHED)
                .filter(re -> re.getEndTime() != null)
                .filter(re -> {
                    LocalDate execDate = re.getEndTime().toLocalDate();
                    return !execDate.isBefore(startDate) && !execDate.isAfter(today);
                })
                .toList();

        // --- 4. Agrupamos por día y sumamos calorías ---
        Map<LocalDate, Double> kcalByDay = executionsInRange.stream()
                .filter(re -> re.getCalories() != null)
                .collect(Collectors.groupingBy(
                        re -> re.getEndTime().toLocalDate(),
                        Collectors.summingDouble(re -> re.getCalories().doubleValue())
                ));

        // --- 5. Generamos 30 puntos consecutivos ---
        List<DailyKcalResponseDTO> checkpoints = new ArrayList<>();

        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            double kcal = kcalByDay.getOrDefault(date, 0.0);

            checkpoints.add(new DailyKcalResponseDTO(
                    date.toString(),
                    kcal
            ));
        }

        return new EvolutionKcalResponseDTO(checkpoints);
    }
}
