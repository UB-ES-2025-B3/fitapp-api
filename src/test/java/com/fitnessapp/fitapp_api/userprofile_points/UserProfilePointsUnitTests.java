package com.fitnessapp.fitapp_api.userprofile_points;

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
class UserProfilePointsUnitTests {

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

    private UserProfile mockProfile(Long points) {
        UserProfile p = new UserProfile();
        p.setId(10L);
        p.setUser(userAuth);
        p.setPoints(points); // NUEVO CAMPO
        return p;
    }

    // ---------------------------------------------------------------------
    // TESTS
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("getMyProfile devuelve correctamente los puntos del usuario")
    void getMyProfile_ReturnsPointsCorrectly() {
        UserProfile profile = mockProfile(1500L);

        when(profileRepo.findByUser_Email("test@example.com"))
                .thenReturn(Optional.of(profile));

        UserProfileResponseDTO result = service.getMyProfile("test@example.com");

        assertNotNull(result);
        assertEquals(1500L, result.points());
    }

    @Test
    @DisplayName("Si el usuario tiene 0 puntos, se devuelve 0")
    void getMyProfile_ReturnsZeroPoints() {
        UserProfile profile = mockProfile(0L);

        when(profileRepo.findByUser_Email("test@example.com"))
                .thenReturn(Optional.of(profile));

        UserProfileResponseDTO result = service.getMyProfile("test@example.com");

        assertEquals(0L, result.points());
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
    @DisplayName("Mapper: UserProfile → Response incluye el campo points")
    void mapper_IncludesPoints() {
        UserProfile profile = mockProfile(333L);

        UserProfileResponseDTO dto = mapper.toResponseDto(profile, "test@example.com");

        assertEquals(333L, dto.points());
    }
}
