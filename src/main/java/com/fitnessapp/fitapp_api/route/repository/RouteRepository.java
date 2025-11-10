package com.fitnessapp.fitapp_api.route.repository;

import com.fitnessapp.fitapp_api.route.model.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {

    List<Route> findAllByUserEmail(String email);

    Optional<Route> findByIdAndUserEmail(Long id, String email);

    // Hard delete
    @Modifying
    @Transactional
    @Query("DELETE FROM Route r WHERE r.id = :id")
    void hardDelete(Long id);
}