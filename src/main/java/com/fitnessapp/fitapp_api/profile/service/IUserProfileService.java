
package com.fitnessapp.fitapp_api.profile.service;

import com.fitnessapp.fitapp_api.profile.dto.UserProfileDto;

public interface IUserProfileService {
    UserProfileDto getMyProfile(String username);
    UserProfileDto createMyProfile(String username, UserProfileDto dto);
    UserProfileDto updateMyProfile(String username, UserProfileDto dto);
}
