package com.fitnessapp.fitapp_api.profile.service.implementation;

import com.fitnessapp.fitapp_api.auth.repository.UserAuthRepository;
import com.fitnessapp.fitapp_api.core.exception.UserAuthNotFoundException;
import com.fitnessapp.fitapp_api.core.exception.UserProfileAlreadyExistsException;
import com.fitnessapp.fitapp_api.core.exception.UserProfileNotFoundException;
import com.fitnessapp.fitapp_api.profile.dto.UserProfileRequestDTO;
import com.fitnessapp.fitapp_api.profile.dto.UserProfileResponseDTO;
import com.fitnessapp.fitapp_api.profile.mapper.UserProfileMapper;
import com.fitnessapp.fitapp_api.profile.model.UserProfile;
import com.fitnessapp.fitapp_api.profile.repository.UserProfileRepository;
import com.fitnessapp.fitapp_api.profile.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository repository;
    private final UserAuthRepository userAuthRepository;
    private final UserProfileMapper mapper;

    @Override
    public UserProfileResponseDTO getMyProfile(String email) {

        var profile = repository.findByUser_Email(email)
                .orElseThrow(() -> new UserProfileNotFoundException(
                        "Profile not found for: " + email
                ));

        return mapper.toResponseDto(profile, email);
    }

    @Override
    public UserProfileResponseDTO createMyProfile(String email, UserProfileRequestDTO toCreate) {

        if (repository.existsByUser_Email(email)) {
            throw new UserProfileAlreadyExistsException(
                    "Profile already exists for email: " + email
            );
        } else {
            var userAuth = userAuthRepository.findByEmail(email)
                    //Nunca debería entrar a esta excepción
                    .orElseThrow(() -> new UserAuthNotFoundException(
                            "User not found for email: " + email
                    ));
            UserProfile existingProfile = mapper.toEntity(toCreate, userAuth);
            UserProfile savedProfile = repository.save(existingProfile);
            return mapper.toResponseDto(savedProfile, email);
        }
    }

    @Override
    public UserProfileResponseDTO updateMyProfile(String email, UserProfileRequestDTO toUpdate) {

        var existingProfile = repository.findByUser_Email(email)
                .orElseThrow(() -> new UserProfileNotFoundException(
                        "Profile not found for: " + email
                ));

        // Update fields
        existingProfile.setFirstName(toUpdate.firstName());
        existingProfile.setLastName(toUpdate.lastName());
        existingProfile.setGender(toUpdate.gender());
        existingProfile.setBirthDate(toUpdate.birthDate());
        existingProfile.setHeightCm(toUpdate.heightCm());
        existingProfile.setWeightKg(toUpdate.weightKg());
        existingProfile.setTimeZone(toUpdate.timeZone());

        UserProfile updatedProfile = repository.save(existingProfile);
        return mapper.toResponseDto(updatedProfile, email);
    }

    @Override
    public boolean isProfileComplete(String email) {
        var profile = repository.findByUser_Email(email)
                .orElseThrow(() -> new UserProfileNotFoundException(
                        "Profile not found for: " + email
                ));

        return profile.getFirstName() != null &&
               profile.getLastName() != null &&
               profile.getBirthDate() != null &&
               profile.getHeightCm() != null &&
               profile.getWeightKg() != null;
    }

    @Override
    public boolean isProfileComplete(UserProfile profile) {
        return profile.getFirstName() != null &&
                profile.getLastName() != null &&
                profile.getBirthDate() != null &&
                profile.getHeightCm() != null &&
                profile.getWeightKg() != null;
    }
}