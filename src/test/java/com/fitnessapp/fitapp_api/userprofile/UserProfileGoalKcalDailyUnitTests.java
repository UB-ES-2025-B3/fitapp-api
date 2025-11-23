package com.fitnessapp.fitapp_api.userprofile;

import com.fitnessapp.fitapp_api.auth.model.UserAuth;
import com.fitnessapp.fitapp_api.auth.repository.UserAuthRepository;
import com.fitnessapp.fitapp_api.core.exception.UserProfileNotFoundException;
import com.fitnessapp.fitapp_api.profile.dto.UserProfileResponseDTO;
import com.fitnessapp.fitapp_api.profile.mapper.UserProfileMapper;
import com.fitnessapp.fitapp_api.profile.model.UserProfile;
import com.fitnessapp.fitapp_api.profile.repository.UserProfileRepository;
import com.fitnessapp.fitapp_api.profile.service.implementation.UserProfileServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProfileGoalKcalDailyUnitTests {

    @Mock
    private UserProfileRepository profileRepo;

    @Mock
    private UserAuthRepository authRepo;

    @Spy
    private UserProfileMapper mapper = Mappers.getMapper(UserProfileMapper.class);

    @InjectMocks
    private UserProfileServiceImpl service;

    private UserAuth userAuth;

    @BeforeEach
    void setup() {
        userAuth = new UserAuth();
        userAuth.setId(1L);
        userAuth.setEmail("test@example.com");
    }

    private UserProfile mockProfile(Integer goalKcalDaily) {
        UserProfile p = new UserProfile();
        p.setId(10L);
        p.setUser(userAuth);
        p.setGoalKcalDaily(goalKcalDaily);
        return p;
    }

    private UserProfile mockProfileNoGoalKcalDaily() {
        UserProfile p = new UserProfile();
        p.setId(10L);
        p.setUser(userAuth);
        return p;
    }

    // ---------------------------------------------------------------------
    // TESTS
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("getMyProfile devuelve correctamente el goalKcalDaily del usuario")
    void getMyProfile_ReturnsGoalKcalDailyCorrectly() {
        UserProfile profile = mockProfile(1800);

        when(profileRepo.findByUser_Email("test@example.com"))
                .thenReturn(Optional.of(profile));

        UserProfileResponseDTO result = service.getMyProfile("test@example.com");

        assertNotNull(result);
        assertEquals(1800, result.goalKcalDaily());
    }

    @Test
    @DisplayName("Si el usuario tiene goalKcalDaily = 0, se devuelve 0")
    void getMyProfile_ReturnsZeroGoalKcalDaily() {
        UserProfile profile = mockProfile(0);

        when(profileRepo.findByUser_Email("test@example.com"))
                .thenReturn(Optional.of(profile));

        UserProfileResponseDTO result = service.getMyProfile("test@example.com");

        assertEquals(0, result.goalKcalDaily());
    }

    @Test
    @DisplayName("Si el usuario not tiene goalKcalDaily, se devuelve null")
    void getMyProfile_ReturnsNullGoalKcalDaily() {
        UserProfile profile = mockProfileNoGoalKcalDaily();

        when(profileRepo.findByUser_Email("test@example.com"))
                .thenReturn(Optional.of(profile));

        UserProfileResponseDTO result = service.getMyProfile("test@example.com");

        assertNull(result.goalKcalDaily());
    }

    @Test
    @DisplayName("getMyProfile lanza excepción si no existe el perfil")
    void getMyProfile_ProfileNotFound() {
        when(profileRepo.findByUser_Email("test@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(UserProfileNotFoundException.class,
                () -> service.getMyProfile("test@example.com"));
    }

    @Test
    @DisplayName("Mapper: UserProfile → Response incluye goalKcalDaily")
    void mapper_IncludesGoalKcalDaily() {
        UserProfile profile = mockProfile(2300);

        UserProfileResponseDTO dto = mapper.toResponseDto(profile, "test@example.com");

        assertEquals(2300, dto.goalKcalDaily());
    }
}
