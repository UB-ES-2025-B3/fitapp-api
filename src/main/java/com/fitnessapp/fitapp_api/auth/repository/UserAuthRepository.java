package com.fitnessapp.fitapp_api.auth.repository;

import com.fitnessapp.fitapp_api.auth.model.UserAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAuthRepository extends JpaRepository<UserAuth, Long> {

}
