package com.fitnessapp.fitapp_api.profile.service.implementation;

import com.fitnessapp.fitapp_api.profile.model.UserProfile;
import com.fitnessapp.fitapp_api.profile.repository.UserProfileRepository;
import com.fitnessapp.fitapp_api.profile.service.UserProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.Optional;

@Service
@Transactional
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository repository;

    public UserProfileServiceImpl(UserProfileRepository repository) {
        this.repository = repository;
    }

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
}
