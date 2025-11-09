package com.fitnessapp.fitapp_api.home.service.implementation;

import com.fitnessapp.fitapp_api.core.exception.UserProfileNotCompletedException;
import com.fitnessapp.fitapp_api.home.dto.HomeKpisTodayResponseDTO;
import com.fitnessapp.fitapp_api.home.service.HomeService;
import com.fitnessapp.fitapp_api.profile.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HomeServiceImpl implements HomeService {
    private final UserProfileService userProfileService;

    @Override
    public HomeKpisTodayResponseDTO getHomeKpisToday(String email) {
        if (!userProfileService.isProfileComplete(email)) {
            throw new UserProfileNotCompletedException("User profile is not complete for email: " + email);
        }
        int mockRoutesCompleted = 2;
        long mockTotalDuration = 2712L; // 45 min, 12 seg
        double mockTotalDistance = 6.3;
        int mockCaloriesToday = 340;
        int mockActiveStreak = 3;
        boolean mockHasCreatedRoutes = true; // Para el estado vacío de "Crear tu primera ruta"

        // 3. Devolver el DTO con los datos "mock"
        return new HomeKpisTodayResponseDTO(
                mockRoutesCompleted,
                mockTotalDuration,
                mockTotalDistance,
                mockCaloriesToday,
                mockActiveStreak,
                mockHasCreatedRoutes
        );

    }
}
