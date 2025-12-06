package com.fitnessapp.fitapp_api.auth.service;

import com.fitnessapp.fitapp_api.auth.dto.*;

public interface UserAuthService {
    // Metodo para registrar un nuevo usuario
    UserAuthResponseDTO register(RegisterUserRequestDTO registerUserRequestDTO);

    // Metodo para loguear un usuario
    LoginUserResponseDTO login(LoginUserRequestDTO loginUserRequestDTO);

    // Metodo para cambiar contraseña
    UserAuthResponseDTO changePassword(String email, ChangePasswordRequestDTO dto);

}