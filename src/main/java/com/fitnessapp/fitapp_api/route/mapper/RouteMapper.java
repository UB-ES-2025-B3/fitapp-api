package com.fitnessapp.fitapp_api.route.mapper;

import com.fitnessapp.fitapp_api.auth.model.UserAuth;
import com.fitnessapp.fitapp_api.route.dto.RouteRequestDTO;
import com.fitnessapp.fitapp_api.route.dto.RouteResponseDTO;
import com.fitnessapp.fitapp_api.route.model.Route;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RouteMapper {

    // --- CREAR ---
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", source = "userAuth")
    Route toEntity(RouteRequestDTO dto, UserAuth userAuth);

    // --- ACTUALIZAR ---
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    void updateEntityFromDto(RouteRequestDTO dto, @MappingTarget Route route);

    // --- RESPUESTA ---
    @Mapping(target = "userEmail", source = "user.email")
    RouteResponseDTO toResponseDto(Route route);

    List<RouteResponseDTO> toResponseDtoList(List<Route> routes);

}
