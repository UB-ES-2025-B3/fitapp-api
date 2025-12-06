package com.fitnessapp.fitapp_api.routeexecution.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(name = "RouteExecutionHistoryResponseDTO", description = "DTO para el historial de ejecuciones de rutas finalizadas por el usuario.")
public record RouteExecutionHistoryResponseDTO(
        @Schema(
                description = "Nombre de la ruta ejecutada.",
                example = "Parque Central"
        )
        String routeName,

        @Schema(
                description = "Fecha y hora de finalización de la ejecución.",
                example = "2025-12-04T18:30:00"
        )
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime endTime,

        @Schema(
                description = "Distancia recorrida en kilómetros.",
                example = "5.25"
        )
        BigDecimal distanceKm,

        @Schema(
                description = "Tipo de actividad realizada.",
                example = "RUNNING_MODERATE"
        )
        String activityType,

        @Schema(
                description = "Duración total de la ejecución en segundos.",
                example = "1800"
        )
        Long durationSec,

        @Schema(
                description = "Calorías estimadas consumidas durante la ejecución.",
                example = "350.50"
        )
        BigDecimal calories,

        @Schema(
                description = "Puntos obtenidos por la ejecución.",
                example = "150"
        )
        long points
) {
}
