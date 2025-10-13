package com.fitnessapp.fitapp_api.auth.controller;

import com.fitnessapp.fitapp_api.auth.dto.UserAuthDTO;
import com.fitnessapp.fitapp_api.auth.service.IUserAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class UserAuthController {

    @Autowired
    private IUserAuthService userAuthService;

    // Endpoint for user registration
    @PostMapping("/register")
    public ResponseEntity<UserAuthDTO> register(@RequestBody UserAuthDTO userAuthDTO) {
        return new ResponseEntity<>(this.userAuthService.register(userAuthDTO), HttpStatus.CREATED);
    }
}
