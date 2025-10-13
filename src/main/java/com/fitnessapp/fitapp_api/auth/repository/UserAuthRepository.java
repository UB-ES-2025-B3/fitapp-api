package com.fitnessapp.fitapp_api.auth.repository;

import com.fitnessapp.fitapp_api.auth.model.UserAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAuthRepository extends JpaRepository<UserAuth, Long> {
    boolean existsByEmail(String email);
    Optional<UserAuth> findByEmail(String email);
}
