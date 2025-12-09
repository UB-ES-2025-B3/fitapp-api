package com.fitnessapp.fitapp_api.auth.service.implementation;

import com.fitnessapp.fitapp_api.auth.dto.*;
import com.fitnessapp.fitapp_api.auth.mapper.UserAuthMapper;
import com.fitnessapp.fitapp_api.auth.model.UserAuth;
import com.fitnessapp.fitapp_api.auth.repository.UserAuthRepository;
import com.fitnessapp.fitapp_api.auth.service.UserAuthService;
import com.fitnessapp.fitapp_api.core.exception.*;
import com.fitnessapp.fitapp_api.core.util.JwtUtils;
import com.fitnessapp.fitapp_api.profile.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserAuthServiceImpl implements UserAuthService {

    private final UserAuthRepository userAuthRepository;
    private final UserAuthMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    private final UserProfileRepository userProfileRepository;

    @Override
    public UserAuthResponseDTO register(RegisterUserRequestDTO registerUserRequestDTO) {
        // Verificar si el email ya está en uso. Se lanza excepción si es así.
        if (userAuthRepository.existsByEmail(registerUserRequestDTO.email())) {
            throw new UserAlreadyExistsException("Email is already in use");
        }

        // Mapear DTO a entidad, encriptar la contraseña y guardar el usuario
        UserAuth user = mapper.toEntity(registerUserRequestDTO);
        user.setPassword(passwordEncoder.encode(registerUserRequestDTO.password()));
        UserAuth savedUser = userAuthRepository.save(user);

        // Autenticar al usuario recién registrado y generar el token JWT
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        savedUser.getEmail(),
                        registerUserRequestDTO.password()
                )
        );
        // Se guarda el usuario autenticado en el contexto de seguridad.
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtUtils.createToken(authentication);

        // Retornar un DTO response con el token
        return new UserAuthResponseDTO(
                savedUser.getId(),
                token,
                false
        );
    }

    @Override
    public LoginUserResponseDTO login(LoginUserRequestDTO loginUserRequestDTO) {
        // Autenticar al usuario recién registrado y generar el token JWT
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginUserRequestDTO.email(),
                        loginUserRequestDTO.password()
                )
        );
        // Se guarda el usuario autenticado en el contexto de seguridad.
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtUtils.createToken(authentication);

        // Comprobamos si el usuario tiene un perfil asociado o no.
        Boolean profileExists = userProfileRepository.existsByUser_Email(loginUserRequestDTO.email());

        // Retornar un DTO response con el token
        return new LoginUserResponseDTO(
                token,
                profileExists
        );
    }

    @Override
    public UserAuthResponseDTO changePassword(String email, ChangePasswordRequestDTO dto) {
        // Recuperar el usuario por email
        UserAuth user = userAuthRepository.findByEmail(email)
                .orElseThrow(() -> new UserAuthNotFoundException("User not found"));

        // Verificar que la contraseña actual coincida
        if (!passwordEncoder.matches(dto.currentPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Current password is incorrect");
        }

        // Verificar que la nueva contraseña coincida con la confirmación
        if (!dto.newPassword().equals(dto.confirmPassword())) {
            throw new PasswordConfirmationException("New password and confirmation do not match");
        }

        // Verificar que la nueva contraseña cumple los requisitos mínimos
        if (!dto.newPassword().matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,}$")) {
            throw new InvalidPasswordFormatException("New password does not meet requirements");
        }

        // Codificar y guardar la nueva contraseña
        user.setPassword(passwordEncoder.encode(dto.newPassword()));
        userAuthRepository.save(user);

        // Generar un nuevo token con la contraseña actualizada
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, dto.newPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtUtils.createToken(authentication);

        // Retornar DTO con token actualizado
        return new UserAuthResponseDTO(user.getId(), token,
                userProfileRepository.existsByUser_Email(email));
    }
}