package com.fitnessapp.fitapp_api.auth.service;

import com.fitnessapp.fitapp_api.auth.dto.UserAuthDTO;

public interface IUserAuthService {
    UserAuthDTO register(UserAuthDTO userAuthDTO);
}
