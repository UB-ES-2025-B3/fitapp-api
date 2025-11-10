package com.fitnessapp.fitapp_api.route.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;


public record RouteRequestDTO(

        @Schema(
                description = "Nombre de la ruta",
                example = "Ruta por la montaña",
                minLength = 1,
                maxLength = 100,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Route name is required")
        @Size(min = 1, max = 100, message = "Route name must be between 1 and 100 characters")
        String name,

        @Schema(
                description = "Punto de inicio de la ruta (coordenadas en formato 'lat,lon')",
                example = "41.0000000,-8.600000",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Start point is required")
        String startPoint,

        @Schema(
                description = "Punto final de la ruta (coordenadas en formato 'lat,lon')",
                example = "41.0000000,-8.600000",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "End point is required")
        String endPoint,

        @Schema(
                description = " Distancia estimada de la ruta en km",
                example = "0.05",
                minimum = "0.01",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Distance is required")
        @DecimalMin(value = "0.01", message = "Distance must be greater than 0")
        @Digits(integer = 4, fraction = 2, message = "Distance must be a valid number (max 4 digits and 2 decimals")
        BigDecimal distanceKm

) {
}
