package com.fitnessapp.fitapp_api.route.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
public record CheckpointRequestDTO(
    @Schema(
            description = "Nombre del checkpoint",
            example = "Fuente del Cedro"
    )
    @NotBlank(message = "Checkpoint name is required")
    String name,

    @Schema(
            description = "Coordenadas del checkpoint en formato 'lat,lon'",
            example = "41.00001,-8.50002"
    )
    @NotBlank(message = "Checkpoint point is required")
    String point
){
}
