package com.fitnessapp.fitapp_api.route.mapper;

import com.fitnessapp.fitapp_api.auth.model.UserAuth;
import com.fitnessapp.fitapp_api.route.dto.CheckpointRequestDTO;
import com.fitnessapp.fitapp_api.route.dto.CheckpointResponseDTO;
import com.fitnessapp.fitapp_api.route.dto.RouteRequestDTO;
import com.fitnessapp.fitapp_api.route.dto.RouteResponseDTO;
import com.fitnessapp.fitapp_api.route.model.Checkpoint;
import com.fitnessapp.fitapp_api.route.model.Route;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface RouteMapper {

    // --- CREAR ---
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", source = "userAuth")
    @Mapping(target = "checkpoints", source = "dto.checkpoints") // List<CheckpointRequestDTO> -> List<Checkpoint>
    Route toEntity(RouteRequestDTO dto, UserAuth userAuth);

    // --- ACTUALIZAR ---
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "checkpoints", source = "dto.checkpoints") // List<CheckpointRequestDTO> -> List<Checkpoint>
    void updateEntityFromDto(RouteRequestDTO dto, @MappingTarget Route route);

    // --- RESPUESTA ---
    @Mapping(target = "userEmail", source = "user.email")
    @Mapping(target = "checkpoints", source = "checkpoints") // List<Checkpoint> -> List<CheckpointResponseDTO>
    RouteResponseDTO toResponseDto(Route route);

    List<RouteResponseDTO> toResponseDtoList(List<Route> routes);

    // Métodos auxiliares para MapStruct
    default List<Checkpoint> mapFromRequestDtoList(List<CheckpointRequestDTO> dtos) {
        if (dtos == null) return null;
        return dtos.stream()
                .map(dto -> new Checkpoint(dto.name(), dto.point()))
                .collect(Collectors.toList());
    }

    default List<CheckpointResponseDTO> mapToResponseDtoList(List<Checkpoint> entities) {
        if (entities == null) return null;
        return entities.stream()
                .map(entity -> new CheckpointResponseDTO(entity.getName(), entity.getPoint()))
                .collect(Collectors.toList());
    }

}
