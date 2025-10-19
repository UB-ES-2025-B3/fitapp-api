package com.fitnessapp.fitapp_api.auth.service;

import com.fitnessapp.fitapp_api.auth.dto.LoginUserRequestDTO;
import com.fitnessapp.fitapp_api.auth.dto.LoginUserResponseDTO;
import com.fitnessapp.fitapp_api.auth.dto.RegisterUserRequestDTO;
import com.fitnessapp.fitapp_api.auth.dto.UserAuthResponseDTO;

public interface UserAuthService {
    // Metodo para registrar un nuevo usuario
    UserAuthResponseDTO register(RegisterUserRequestDTO registerUserRequestDTO);

    // Metodo para loguear un usuario
    LoginUserResponseDTO login(LoginUserRequestDTO loginUserRequestDTO);
}