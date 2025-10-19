package com.fitnessapp.fitapp_api.profile.service;

import com.fitnessapp.fitapp_api.profile.dto.UserProfileRequestDTO;
import com.fitnessapp.fitapp_api.profile.dto.UserProfileResponseDTO;

public interface UserProfileService {

    UserProfileResponseDTO getMyProfile(String email);

    UserProfileResponseDTO createMyProfile(String email, UserProfileRequestDTO toCreate);

    UserProfileResponseDTO updateMyProfile(String email, UserProfileRequestDTO toUpdate);
}