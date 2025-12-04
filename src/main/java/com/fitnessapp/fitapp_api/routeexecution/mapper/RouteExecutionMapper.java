package com.fitnessapp.fitapp_api.routeexecution.mapper;

import com.fitnessapp.fitapp_api.auth.model.UserAuth;
import com.fitnessapp.fitapp_api.routeexecution.dto.RouteExecutionHistoryResponseDTO;
import com.fitnessapp.fitapp_api.routeexecution.dto.RouteExecutionRequestDTO;
import com.fitnessapp.fitapp_api.routeexecution.dto.RouteExecutionResponseDTO;
import com.fitnessapp.fitapp_api.routeexecution.model.RouteExecution;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.context.annotation.Primary;

@Mapper(componentModel = "spring")
@Primary
public interface RouteExecutionMapper {

    // --- CREAR ---
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", source = "userAuth")
    @Mapping(target = "status", ignore = true) // se inicializa en IN_PROGRESS en el servicio
    @Mapping(target = "startTime", ignore = true) // set en el servicio al iniciar
    @Mapping(target = "pauseTime", ignore = true)
    @Mapping(target = "endTime", ignore = true)
    @Mapping(target = "totalPausedTimeSec", ignore = true)
    @Mapping(target = "durationSec", ignore = true)
    @Mapping(target = "calories", ignore = true)
    RouteExecution toEntity(RouteExecutionRequestDTO dto, UserAuth userAuth);

    // --- ACTUALIZAR ---
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    void updateEntityFromDto(RouteExecutionRequestDTO dto, @MappingTarget RouteExecution routeExecution);

    // --- RESPUESTA ---
    @Mapping(target = "userEmail", source = "user.email")
    @Mapping(target = "routeId", expression = "java(execution.getRoute() != null ? execution.getRoute().getId() : null)")
    @Mapping(target = "routeName", expression = "java(execution.getRoute() != null ? execution.getRoute().getName() : \"Ruta Eliminada\")")
    RouteExecutionResponseDTO toResponseDto(RouteExecution execution);

    // --- HISTORIAL ---
    @Mapping(target = "routeName", expression = "java(execution.getRoute() != null ? execution.getRoute().getName() : \"Ruta Eliminada\")")
    @Mapping(target = "distanceKm", source = "java(execution.getRoute() != null ? execution.getRoute().getDistanceKm()")
    RouteExecutionHistoryResponseDTO toHistoryResponseDto(RouteExecution execution);

}
