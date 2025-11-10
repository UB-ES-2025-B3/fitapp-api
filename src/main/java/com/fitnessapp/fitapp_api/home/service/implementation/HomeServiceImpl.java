package com.fitnessapp.fitapp_api.home.service.implementation;

import com.fitnessapp.fitapp_api.core.exception.UserProfileNotCompletedException;
import com.fitnessapp.fitapp_api.core.exception.UserProfileNotFoundException;
import com.fitnessapp.fitapp_api.home.dto.HomeKpisTodayResponseDTO;
import com.fitnessapp.fitapp_api.home.service.HomeService;
import com.fitnessapp.fitapp_api.profile.model.UserProfile;
import com.fitnessapp.fitapp_api.profile.repository.UserProfileRepository;
import com.fitnessapp.fitapp_api.profile.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

        int mockRoutesCompleted = 2;
        long mockTotalDuration = 2712L; // 45 min, 12 seg
        double mockTotalDistance = 6.3;
        int mockCaloriesToday = 340;
        int mockActiveStreak = 3;
        boolean mockHasCreatedRoutes = true; // Para el estado vacío de "Crear tu primera ruta"

        // 3. Devolver el DTO con los datos (mock por ahora)
        return new HomeKpisTodayResponseDTO(
                mockRoutesCompleted,
                mockTotalDuration,
                mockTotalDistance,
                mockCaloriesToday,
                mockActiveStreak,
                mockHasCreatedRoutes
        );
    }

    private int calculateRoutesCompletedToday(UserProfile profile) {
        // Lógica para calcular las rutas completadas hoy basadas en el perfil del usuario
        return 0; // Mock
    }

    private int calculateActiveStreak(UserProfile profile) {
        // Lógica para calcular la racha activa basada en el perfil del usuario
        return 0; // Mock
    }
}
