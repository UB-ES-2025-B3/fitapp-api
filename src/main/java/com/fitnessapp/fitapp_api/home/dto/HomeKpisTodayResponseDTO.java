package com.fitnessapp.fitapp_api.home.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record HomeKpisTodayResponseDTO(
        @Schema(
                description = "Número de rutas completadas hoy por el usuario",
                example = "3") int routesCompletedToday,
        @Schema(
                description = "Duración total en segundos de las actividades de hoy",
                example = "5400") long totalDurationSecToday,
        @Schema(
                description = "Distancia total en kilómetros recorrida hoy",
                example = "12.0") double totalDistanceKmToday,
        @Schema(
                description = "Calorías quemadas hoy",
                example = "340.9") double caloriesKcalToday,
        @Schema(
                description = "Número de días consecutivos con actividad",
                example = "4") int activeStreakDays,
        @Schema(
                description = "Indica si el usuario ha creado alguna ruta",
                example = "true") boolean hasCreatedRoutes
) {}
