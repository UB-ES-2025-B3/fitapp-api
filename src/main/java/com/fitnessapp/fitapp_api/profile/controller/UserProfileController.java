// java
package com.fitnessapp.fitapp_api.profile.controller;

import com.fitnessapp.fitapp_api.profile.dto.UserProfileDto;
import com.fitnessapp.fitapp_api.profile.service.IUserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final IUserProfileService profileService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> getMe(Principal principal) {
        return ResponseEntity.ok(profileService.getMyProfile(principal.getName()));
    }

    @PostMapping("/me")
    public ResponseEntity<UserProfileDto> createMe(Principal principal, @RequestBody UserProfileDto dto) {
        UserProfileDto created = profileService.createMyProfile(principal.getName(), dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileDto> updateMe(Principal principal, @RequestBody UserProfileDto dto) {
        return ResponseEntity.ok(profileService.updateMyProfile(principal.getName(), dto));
    }
}
