package com.fitnessapp.fitapp_api.stats.Controller;

import com.fitnessapp.fitapp_api.stats.dto.EvolutionKcalResponseDTO;
import com.fitnessapp.fitapp_api.stats.service.EvolutionKcalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
@Tag(name = "Stats", description = "Endpoints para estadísticas históricas del usuario")
public class StatsController {

    private final EvolutionKcalService evolutionKcalService;

    /**
     * GET /evolution
     */
    @Operation(
            summary = "Obtener evolución histórica de Kcal",
            description = "Devuelve las Kcal quemadas en los últimos N días (por defecto 30), una por cada día, para graficar en el frontend.",
            parameters = {
                    @Parameter(
                            name = "metric",
                            description = "Métrica solicitada. Actualmente solo 'kcal'.",
                            example = "kcal"
                    ),
                    @Parameter(
                            name = "period",
                            description = "Periodo histórico a consultar. Formato '30d'. Actualmente solo se soporta número + 'd'.",
                            example = "30d"
                    ),
                    @Parameter(
                            name = "principal",
                            hidden = true,
                            description = "Inyectado por Spring Security, representa el usuario autenticado."
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "400",
                            description = "Parámetros inválidos",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "No autenticado - Token JWT inválido o faltante",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Perfil de usuario no encontrado",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "422",
                            description = "Perfil de usuario incompleto",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    @GetMapping("/evolution")
    public ResponseEntity<EvolutionKcalResponseDTO> getKcalEvolution(
            Principal principal,
            @RequestParam(defaultValue = "kcal") String metric,
            @RequestParam(defaultValue = "30d") String period
    ) {

        // --- Validar métrica ---
        if (!metric.equalsIgnoreCase("kcal")) {
            return ResponseEntity.badRequest().build();
        }

        // --- Validar 30 dias ---
        if (!"30d".equalsIgnoreCase(period)) {
            return ResponseEntity.badRequest().build();
        }

        // --- Parsear periodo: ej "30d" → 30 ---
        int days;
        try {
            days = Integer.parseInt(period.replace("d", ""));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }

        EvolutionKcalResponseDTO evolution =
                evolutionKcalService.getEvolutionKcal(principal.getName(), days);

        return ResponseEntity.ok(evolution);
    }
}
