package com.fitnessapp.fitapp_api.auth.service.implementation;

import com.fitnessapp.fitapp_api.auth.dto.UserAuthDTO;
import com.fitnessapp.fitapp_api.auth.repository.UserAuthRepository;
import com.fitnessapp.fitapp_api.auth.service.IUserAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserAuthServiceImpl implements IUserAuthService {

    @Autowired
    private UserAuthRepository userAuthRepository;

    @Override
    public UserAuthDTO register(UserAuthDTO userAuthDTO) {
        return null;
    }
}
