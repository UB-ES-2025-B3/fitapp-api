package com.fitnessapp.fitapp_api.profile.repository;

import com.fitnessapp.fitapp_api.profile.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    Boolean existsByUser_Email(String email);

    Optional<UserProfile> findByUser_Email(String email);
}