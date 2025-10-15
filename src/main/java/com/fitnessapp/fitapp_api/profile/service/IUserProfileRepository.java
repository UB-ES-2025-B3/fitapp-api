package com.fitnessapp.fitapp_api.profile.repository;

import com.fitnessapp.fitapp_api.profile.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IUserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByUserEmail(String email);
}
