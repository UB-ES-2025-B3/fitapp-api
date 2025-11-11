package com.fitnessapp.fitapp_api.routeexecution.dto;

import com.fitnessapp.fitapp_api.routeexecution.model.RouteExecution;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record RouteExecutionRequestDTO (
        @Schema(
                description = "Tipo de actividad realizada durante la ruta",
                example = "RUNNING_SLOW",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Activity type is required")
        RouteExecution.ActivityType activityType,

        @Schema(
                description = "Notas opcionales sobre la ejecución",
                example = "Ruta bastante dura, mucho desnivel",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        String notes

){
}
