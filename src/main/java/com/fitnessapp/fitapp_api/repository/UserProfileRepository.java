package com.fitnessapp.fitapp_api.repository;

import com.fitnessapp.fitapp_api.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Integer> {
}
