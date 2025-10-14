package com.fitnessapp.fitapp_api.auth.service.implementation;

import com.fitnessapp.fitapp_api.auth.dto.RegisterUserRequestDTO;
import com.fitnessapp.fitapp_api.auth.dto.UserAuthResponseDTO;
import com.fitnessapp.fitapp_api.auth.mapper.UserAuthMapper;
import com.fitnessapp.fitapp_api.auth.model.UserAuth;
import com.fitnessapp.fitapp_api.auth.repository.UserAuthRepository;
import com.fitnessapp.fitapp_api.auth.service.IUserAuthService;
import com.fitnessapp.fitapp_api.core.exception.UserAlreadyExistsException;
import com.fitnessapp.fitapp_api.core.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserAuthServiceImpl implements IUserAuthService {

    private final UserAuthRepository userAuthRepository;
    private final UserAuthMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

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
        String token = jwtUtils.createToken(authentication);

        // Retornar un DTO response con el token
        return new UserAuthResponseDTO(
                token,
                false
        );
    }
}
