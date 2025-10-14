package com.fitnessapp.fitapp_api.auth.service;

import com.fitnessapp.fitapp_api.auth.dto.RegisterUserRequestDTO;
import com.fitnessapp.fitapp_api.auth.dto.UserAuthResponseDTO;

public interface IUserAuthService {
    // Metodo para registrar un nuevo usuario
    UserAuthResponseDTO register(RegisterUserRequestDTO registerUserRequestDTO);
}
