package com.fitnessapp.fitapp_api.repository;

import com.fitnessapp.fitapp_api.entity.UserAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAuthRepository extends JpaRepository<UserAuth, Integer> {

}
