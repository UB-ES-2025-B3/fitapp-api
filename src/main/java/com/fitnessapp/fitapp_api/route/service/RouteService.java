package com.fitnessapp.fitapp_api.route.service;

import com.fitnessapp.fitapp_api.route.dto.RouteRequestDTO;
import com.fitnessapp.fitapp_api.route.dto.RouteResponseDTO;

import java.util.List;

public interface RouteService {

    // para obtener todas las rutas activas (no eliminadas) del usuario autenticado
    List<RouteResponseDTO> getMyRoutes(String email);

    // para crear una nueva ruta
    RouteResponseDTO createRoute(String email, RouteRequestDTO toCreate);

    // para actualizar una ruta existente
    RouteResponseDTO updateRoute(String email, Long id, RouteRequestDTO toUpdate);

    // para eliminar una ruta (soft o hard delete)
    void deleteRoute(String email, Long id);
}
