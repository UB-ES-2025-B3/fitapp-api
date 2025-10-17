java
        package com.fitnessapp.fitapp_api.profile.controller;

import com.fitnessapp.fitapp_api.profile.model.UserProfile;
import com.fitnessapp.fitapp_api.profile.service.UserProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/profile")
public class UserProfileController {

    private final UserProfileService service;

    public UserProfileController(UserProfileService service) {
        this.service = service;
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfile> getMyProfile(Principal principal) {
        return ResponseEntity.ok(service.getMyProfile(principal));
    }

    @PostMapping("/me")
    public ResponseEntity<UserProfile> createMyProfile(Principal principal, @RequestBody UserProfile body) {
        UserProfile created = service.createMyProfile(principal, body);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfile> updateMyProfile(Principal principal, @RequestBody UserProfile body) {
        UserProfile updated = service.updateMyProfile(principal, body);
        return ResponseEntity.ok(updated);
    }
}
