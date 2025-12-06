package com.fitnessapp.fitapp_api.routeexecution.repository;

import com.fitnessapp.fitapp_api.routeexecution.model.RouteExecution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RouteExecutionRepository extends JpaRepository<RouteExecution, Long> {

    Optional<RouteExecution> findByIdAndUserEmail(Long id, String email);

    List<RouteExecution> findAllByUserEmail(String email);

    boolean existsByRouteId(Long routeId);

    List<RouteExecution> findAllByUserEmailAndEndTimeBetweenAndStatus(String userEmail, LocalDateTime startTime, LocalDateTime endTime, RouteExecution.RouteExecutionStatus status);

    List<RouteExecution> findAllByUserEmailAndStatusOrderByEndTimeDesc(String email, RouteExecution.RouteExecutionStatus status);
}
