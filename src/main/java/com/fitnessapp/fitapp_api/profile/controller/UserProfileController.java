package com.fitnessapp.fitapp_api.profile.controller;

import com.fitnessapp.fitapp_api.profile.dto.UserProfileRequestDTO;
import com.fitnessapp.fitapp_api.profile.dto.UserProfileResponseDTO;
import com.fitnessapp.fitapp_api.profile.service.IUserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/profile")
public class UserProfileController {

    private final IUserProfileService service;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponseDTO> getMyProfile(Principal principal) {
        return ResponseEntity.ok(service.getMyProfile(principal));
    }

    @PostMapping("/me")
    public ResponseEntity<UserProfileResponseDTO> createMyProfile(Principal principal, @Valid @RequestBody UserProfileRequestDTO body) {
        UserProfileResponseDTO created = service.createMyProfile(principal, body);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .build()
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileResponseDTO> updateMyProfile(Principal principal, @Valid @RequestBody UserProfileRequestDTO body) {
        UserProfileResponseDTO updated = service.updateMyProfile(principal, body);
        return ResponseEntity.ok(updated);
    }
}
