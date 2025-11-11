package com.fitnessapp.fitapp_api.route.service.implementation;

import com.fitnessapp.fitapp_api.auth.repository.UserAuthRepository;
import com.fitnessapp.fitapp_api.core.exception.RouteNotFoundException;
import com.fitnessapp.fitapp_api.core.exception.UserAuthNotFoundException;
import com.fitnessapp.fitapp_api.route.dto.RouteRequestDTO;
import com.fitnessapp.fitapp_api.route.dto.RouteResponseDTO;
import com.fitnessapp.fitapp_api.route.mapper.RouteMapper;
import com.fitnessapp.fitapp_api.route.model.Route;
import com.fitnessapp.fitapp_api.route.repository.RouteRepository;
import com.fitnessapp.fitapp_api.route.service.RouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class RouteServiceImpl implements RouteService {

    private final RouteRepository routeRepository;
    private final UserAuthRepository userAuthRepository;
    @Qualifier("routeMapper")
    private final RouteMapper mapper;

    @Override
    public List<RouteResponseDTO> getMyRoutes(String email) {
        return routeRepository.findAllByUserEmail(email)
                .stream()
                .map(mapper::toResponseDto)
                .toList();
    }

    @Override
    public RouteResponseDTO getRouteById(Long id, String email){
        var route = routeRepository.findByIdAndUserEmail(id, email)
                .orElseThrow(() -> new RouteNotFoundException("Route not found for id: " + id));

        return mapper.toResponseDto(route);
    }

    @Override
    public RouteResponseDTO createRoute(String email, RouteRequestDTO toCreate) {
        var userAuth = userAuthRepository.findByEmail(email)
                .orElseThrow(() -> new UserAuthNotFoundException("User not found for email: " + email));

        Route newRoute = mapper.toEntity(toCreate, userAuth);
        Route savedRoute = routeRepository.save(newRoute);

        return mapper.toResponseDto(savedRoute);
    }

    @Override
    public RouteResponseDTO updateRoute(String email, Long id, RouteRequestDTO toUpdate) {
        var route = routeRepository.findByIdAndUserEmail(id, email)
                .orElseThrow(() -> new RouteNotFoundException("Route not found for id: " + id));

        mapper.updateEntityFromDto(toUpdate, route);

        Route updated = routeRepository.save(route);
        return mapper.toResponseDto(updated);
    }

    @Override
    public void deleteRoute(String email, Long id) {
        var route = routeRepository.findByIdAndUserEmail(id, email)
                .orElseThrow(() -> new RouteNotFoundException("Route not found for id: " + id));

        // pendiente de implementacion de RouteExecution
        // boolean hasExecutions = routeExecutionRepository.existsRouteExecutionByRouteId(route.getId());
        boolean hasExecutions = false;

        if (hasExecutions) {
            // Soft delete
            routeRepository.delete(route);
        } else {
            // Hard delete
            routeRepository.hardDelete(route.getId());
        }
    }
}