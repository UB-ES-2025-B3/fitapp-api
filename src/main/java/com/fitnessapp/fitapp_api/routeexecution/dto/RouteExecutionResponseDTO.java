package com.fitnessapp.fitapp_api.routeexecution.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record RouteExecutionResponseDTO(
        @Schema(
                description = "Id de la ejecución de la ruta",
                example = "1"
        )
        Long id,

        @Schema(
                description = "Id de la ruta asociada",
                example = "10"
        )
        Long routeId,

        @Schema(
                description = "Nombre de la ruta asociada.",
                example = "Paseo por el parque")
        String routeName,

        @Schema(
                description = "Email del usuario que realizó la ruta",
                example = "usuario@email.com"
        )
        String userEmail,

        @Schema(
                description = "Estado actual de la ejecución",
                example = "IN_PROGRESS"
        )
        String status,

        @Schema(
                description = "Fecha y hora de inicio de la ejecución",
                example = "2025-11-11T08:30:00"
        )
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") // [Modificado] Asegura formato String ISO
        LocalDateTime startTime,

        @Schema(
                description = "Fecha y hora de la última pausa (si está pausada)",
                example = "2025-11-11T08:45:00"
        )
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") // [Modificado] Asegura formato String ISO
        LocalDateTime pauseTime,

        @Schema(
                description = "Fecha y hora de finalización",
                example = "2025-11-11T09:30:00"
        )
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") // [Modificado] Asegura formato String ISO
        LocalDateTime endTime,

        @Schema(
                description = "Tiempo total en segundos que la ejecución estuvo pausada",
                example = "300"
        )
        Long totalPausedTimeSec,

        @Schema(
                description = "Duración total de la ruta en segundos (hora_fin - hora_inicio - tiempo_pausado_total)",
                example = "3600"
        )
        Long durationSec,

        @Schema(
                description = "Tipo de actividad realizada",
                example = "RUNNING_SLOW"
        )
        String activityType,

        @Schema(
                description = "Calorías calculadas para la ejecución",
                example = "450.5"
        )
        Double calories,

        @Schema(
                description = "Puntos obtenidos por la ejecución",
                example = "150"
        )
        Long points,

        @Schema(
                description = "Notas opcionales del usuario",
                example = "Ruta dura con viento"
        )
        String notes
) {
}
