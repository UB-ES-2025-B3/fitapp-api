
package com.fitnessapp.fitapp_api.profile.service;

import com.fitnessapp.fitapp_api.profile.dto.UserProfileDto;
import com.fitnessapp.fitapp_api.profile.model.UserProfile;

import java.security.Principal;

public interface UserProfileService {

    UserProfile getMyProfile(Principal principal);

    UserProfile createMyProfile(Principal principal, UserProfile toCreate);

    UserProfile updateMyProfile(Principal principal, UserProfile toUpdate);
}
