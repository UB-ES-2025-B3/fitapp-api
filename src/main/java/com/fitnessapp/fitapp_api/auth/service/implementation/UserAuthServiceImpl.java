package com.fitnessapp.fitapp_api.auth.service.implementation;

import com.fitnessapp.fitapp_api.auth.dto.LoginUserRequestDTO;
import com.fitnessapp.fitapp_api.auth.dto.LoginUserResponseDTO;
import com.fitnessapp.fitapp_api.auth.dto.RegisterUserRequestDTO;
import com.fitnessapp.fitapp_api.auth.dto.UserAuthResponseDTO;
import com.fitnessapp.fitapp_api.auth.mapper.UserAuthMapper;
import com.fitnessapp.fitapp_api.auth.model.UserAuth;
import com.fitnessapp.fitapp_api.auth.repository.UserAuthRepository;
import com.fitnessapp.fitapp_api.auth.service.UserAuthService;
import com.fitnessapp.fitapp_api.core.exception.UserAlreadyExistsException;
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
}