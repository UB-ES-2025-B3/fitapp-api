package com.fitnessapp.fitapp_api.auth.mapper;

import com.fitnessapp.fitapp_api.auth.dto.RegisterUserRequestDTO;
import com.fitnessapp.fitapp_api.auth.model.UserAuth;
import org.mapstruct.Mapper;

// Mapper para convertir entre UserAuth y RegisterUserRequestDTO
@Mapper(componentModel = "spring")
public interface UserAuthMapper {
    UserAuth toEntity(RegisterUserRequestDTO dto);
}
