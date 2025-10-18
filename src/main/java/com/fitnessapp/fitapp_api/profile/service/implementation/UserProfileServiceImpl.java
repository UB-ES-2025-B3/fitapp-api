package com.fitnessapp.fitapp_api.profile.service.implementation;

import com.fitnessapp.fitapp_api.auth.repository.UserAuthRepository;
import com.fitnessapp.fitapp_api.core.exception.UserAuthNotFoundException;
import com.fitnessapp.fitapp_api.core.exception.UserProfileAlreadyExistsException;
import com.fitnessapp.fitapp_api.core.exception.UserProfileNotFoundException;
import com.fitnessapp.fitapp_api.profile.dto.UserProfileRequestDTO;
import com.fitnessapp.fitapp_api.profile.dto.UserProfileResponseDTO;
import com.fitnessapp.fitapp_api.profile.model.UserProfile;
import com.fitnessapp.fitapp_api.profile.repository.UserProfileRepository;
import com.fitnessapp.fitapp_api.profile.service.IUserProfileService;
import com.fitnessapp.fitapp_api.profile.mapper.UserProfileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@Service
@Transactional
@RequiredArgsConstructor
public class UserProfileServiceImpl implements IUserProfileService {

    private final UserProfileRepository repository;
    private final UserAuthRepository userAuthRepository;
    private final UserProfileMapper mapper;

    /**
    private String normalizeName(String name) {
        if (name == null) return null;
        return name.trim().toLowerCase();
    }

    private Optional<UserProfile> findByEmailName(String name) {
        String email = normalizeName(name);
        if (email == null) return Optional.empty();
        return repository.findByUser_Email(email);
    }


    @Override
    public UserProfile getMyProfile(Principal principal) {
        String name = normalizeName(principal.getName());
        return findByEmailName(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Perfil no encontrado para: " + name));
    }

    @Override
    public UserProfile createMyProfile(Principal principal, UserProfile toCreate) {
        String name = normalizeName(principal.getName());
        if (name == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Principal inválido");
        }

        if (repository.existsByUser_Email(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Perfil ya existe para el email: " + name);
        }

        if (toCreate.getUser() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El perfil a crear debe incluir la referencia al usuario");
        }

        // asegurarse que la entidad User asociada tenga el email normalizado ¿¿es necesario??
        if (toCreate.getUser().getEmail() != null) {
            toCreate.getUser().setEmail(normalizeName(toCreate.getUser().getEmail()));
        }

        return repository.save(toCreate);
    }

    @Override
    public UserProfile updateMyProfile(Principal principal, UserProfile toUpdate) {
        String name = normalizeName(principal.getName());
        if (name == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Principal inválido");
        }

        UserProfile existing = findByEmailName(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Perfil no encontrado para: " + name));

        // aplicar cambios solo si vienen no nulos (preserva datos existentes)
        if (toUpdate.getFirstName() != null) existing.setFirstName(toUpdate.getFirstName());
        if (toUpdate.getLastName() != null) existing.setLastName(toUpdate.getLastName());
        if (toUpdate.getBirthdate() != null) existing.setBirthdate(toUpdate.getBirthdate());
        if (toUpdate.getHeightCm() != null) existing.setHeightCm(toUpdate.getHeightCm());
        if (toUpdate.getWeightKg() != null) existing.setWeightKg(toUpdate.getWeightKg());

        return repository.save(existing);
    }
    */
    @Override
    public UserProfileResponseDTO getMyProfile(Principal principal){
        if (principal == null || principal.getName() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid authentication");
        }
        var profile = repository.findByUser_Email(principal.getName())
                .orElseThrow(() -> new UserProfileNotFoundException(
                        "Profile not found for: " + principal.getName()
                ));

        return mapper.toResponseDto(profile, principal.getName());
    }

    @Override
    public UserProfileResponseDTO createMyProfile(Principal principal, UserProfileRequestDTO toCreate) {
        if (principal == null || principal.getName() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid authentication");
        }
        if (repository.existsByUser_Email(principal.getName())) {
            throw new UserProfileAlreadyExistsException(
                    "Profile already exists for email: " + principal.getName()
            );
        } else {
            var userAuth = userAuthRepository.findByEmail(principal.getName())
                    .orElseThrow(() -> new UserAuthNotFoundException(
                            "User not found for email: " + principal.getName()
                    ));
            UserProfile existingProfile = mapper.toEntity(toCreate, userAuth);
            UserProfile savedProfile = repository.save(existingProfile);
            return mapper.toResponseDto(savedProfile, principal.getName());
        }
    }

    @Override
    public UserProfileResponseDTO updateMyProfile(Principal principal, UserProfileRequestDTO toUpdate) {
        if (principal == null || principal.getName() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid authentication");
        }
        var existingProfile = repository.findByUser_Email(principal.getName())
                .orElseThrow(() -> new UserProfileNotFoundException(
                        "Profile not found for: " + principal.getName()
                ));

        // Update fields if they are provided in the request
        if (toUpdate.firstName() != null) existingProfile.setFirstName(toUpdate.firstName());
        if (toUpdate.lastName() != null) existingProfile.setLastName(toUpdate.lastName());
        if (toUpdate.birthDate() != null) existingProfile.setBirthDate(toUpdate.birthDate());
        if (toUpdate.heightCm() != null) existingProfile.setHeightCm(toUpdate.heightCm());
        if (toUpdate.weightKg() != null) existingProfile.setWeightKg(toUpdate.weightKg());

        UserProfile updatedProfile = repository.save(existingProfile);
        return mapper.toResponseDto(updatedProfile, principal.getName());
    }
}
