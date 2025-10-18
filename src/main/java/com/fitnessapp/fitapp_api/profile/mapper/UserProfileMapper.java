package com.fitnessapp.fitapp_api.profile.mapper;

import com.fitnessapp.fitapp_api.auth.model.UserAuth;
import com.fitnessapp.fitapp_api.profile.dto.UserProfileRequestDTO;
import com.fitnessapp.fitapp_api.profile.dto.UserProfileResponseDTO;
import com.fitnessapp.fitapp_api.profile.model.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {
    @Mapping(target = "email", source = "email")
    UserProfileResponseDTO toResponseDto(UserProfile userProfile, String email);

    @Mapping(target = "user", source = "userAuth")
    UserProfile toEntity(UserProfileRequestDTO dto, UserAuth userAuth);
}
