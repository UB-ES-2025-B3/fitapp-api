package com.fitnessapp.fitapp_api.profile.service.implementation;

import com.fitnessapp.fitapp_api.profile.dto.UserProfileDto;
import com.fitnessapp.fitapp_api.profile.model.UserProfile;
import com.fitnessapp.fitapp_api.profile.repository.UserProfileRepository;
import com.fitnessapp.fitapp_api.auth.model.UserAuth;
import com.fitnessapp.fitapp_api.auth.repository.UserAuthRepository;
import com.fitnessapp.fitapp_api.profile.service.IUserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements IUserProfileService {

    private final UserProfileRepository profileRepo;
    private final UserAuthRepository userRepo;

    @Override
    public UserProfileDto getMyProfile(String email) {
        UserProfile p = profileRepo.findByUserEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Perfil no encontrado"));
        return toDto(p);
    }

    @Override
    public UserProfileDto createMyProfile(String email, UserProfileDto dto) {
        if (profileRepo.findByUserEmail(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Perfil ya existe");
        }
        UserAuth user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        UserProfile p = new UserProfile();
        p.setUser(user);
        p.setFirstName(dto.getFirstName());
        p.setLastName(dto.getLastName());
        p.setBirthDate(dto.getBirthDate());
        p.setHeightCm(dto.getHeightCm());
        p.setWeightKg(dto.getWeightKg());
        UserProfile saved = profileRepo.save(p);
        return toDto(saved);
    }

    @Override
    public UserProfileDto updateMyProfile(String email, UserProfileDto dto) {
        UserProfile p = profileRepo.findByUserEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Perfil no encontrado"));
        if (dto.getFirstName() != null) p.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) p.setLastName(dto.getLastName());
        if (dto.getBirthDate() != null) p.setBirthDate(dto.getBirthDate());
        if (dto.getHeightCm() != null) p.setHeightCm(dto.getHeightCm());
        if (dto.getWeightKg() != null) p.setWeightKg(dto.getWeightKg());
        UserProfile saved = profileRepo.save(p);
        return toDto(saved);
    }

    private UserProfileDto toDto(UserProfile p) {
        return UserProfileDto.builder()
                .email(p.getUser().getEmail())
                .firstName(p.getFirstName())
                .lastName(p.getLastName())
                .birthDate(p.getBirthDate())
                .heightCm(p.getHeightCm())
                .weightKg(p.getWeightKg())
                .build();
    }
}
