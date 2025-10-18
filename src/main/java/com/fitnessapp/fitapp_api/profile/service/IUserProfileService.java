
package com.fitnessapp.fitapp_api.profile.service;

import com.fitnessapp.fitapp_api.profile.dto.UserProfileRequestDTO;
import com.fitnessapp.fitapp_api.profile.dto.UserProfileResponseDTO;

import java.security.Principal;

public interface IUserProfileService {

    UserProfileResponseDTO getMyProfile(Principal principal);

    UserProfileResponseDTO createMyProfile(Principal principal, UserProfileRequestDTO toCreate);

    UserProfileResponseDTO updateMyProfile(Principal principal, UserProfileRequestDTO toUpdate);
}
