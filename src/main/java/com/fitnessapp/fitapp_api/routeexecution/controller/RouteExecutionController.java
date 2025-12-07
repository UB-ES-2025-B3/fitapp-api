package com.fitnessapp.fitapp_api.routeexecution.controller;

import com.fitnessapp.fitapp_api.routeexecution.dto.RouteExecutionHistoryResponseDTO;
import com.fitnessapp.fitapp_api.routeexecution.dto.RouteExecutionRequestDTO;
import com.fitnessapp.fitapp_api.routeexecution.dto.RouteExecutionResponseDTO;
import com.fitnessapp.fitapp_api.routeexecution.service.RouteExecutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/executions")
@Tag(name = "Route Executions", description = "Endpoints para gestionar las ejecuciones de rutas del usuario autenticado.")
public class RouteExecutionController {

    private final RouteExecutionService service;

    // ---------------------------------------
    // POST /api/v1/executions/me/start/{routeId}
    // ---------------------------------------
    @Operation(
            summary = "Iniciar ejecución de una ruta",
            description = "Inicia una ejecución de la ruta indicada para el usuario autenticado.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Datos de la ejecución (tipo de actividad, notas opcionales)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RouteExecutionRequestDTO.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Ejecución iniciada exitosamente",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = RouteExecutionResponseDTO.class)
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Ruta no encontrada", content = @Content),
                    @ApiResponse(responseCode = "401", description = "No autorizado", content = @Content)
            }
    )
    @PostMapping("/me/start/{routeId}")
    public ResponseEntity<RouteExecutionResponseDTO> startExecution(
            Principal principal,
            @PathVariable Long routeId,
            @Valid @RequestBody RouteExecutionRequestDTO body
    ) {
        RouteExecutionResponseDTO started = service.startExecution(principal.getName(), routeId, body);
        return ResponseEntity.status(201).body(started);
    }

    // ---------------------------------------
    // POST /api/v1/executions/me/pause/{executionId}
    // ---------------------------------------
    @Operation(
            summary = "Pausar ejecución",
            description = "Pausa la ejecución en curso indicada para el usuario autenticado.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Ejecución pausada exitosamente",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = RouteExecutionResponseDTO.class)
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Ejecución no encontrada", content = @Content),
                    @ApiResponse(responseCode = "401", description = "No autorizado", content = @Content)
            }
    )
    @PostMapping("/me/pause/{executionId}")
    public ResponseEntity<RouteExecutionResponseDTO> pauseExecution(
            Principal principal,
            @PathVariable Long executionId
    ) {
        return ResponseEntity.ok(service.pauseExecution(principal.getName(), executionId));
    }

    // ---------------------------------------
    // POST /api/v1/executions/me/resume/{executionId}
    // ---------------------------------------
    @Operation(
            summary = "Reanudar ejecución",
            description = "Reanuda una ejecución pausada para el usuario autenticado.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Ejecución reanudada exitosamente",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = RouteExecutionResponseDTO.class)
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Ejecución no encontrada", content = @Content),
                    @ApiResponse(responseCode = "401", description = "No autorizado", content = @Content)
            }
    )
    @PostMapping("/me/resume/{executionId}")
    public ResponseEntity<RouteExecutionResponseDTO> resumeExecution(
            Principal principal,
            @PathVariable Long executionId
    ) {
        return ResponseEntity.ok(service.resumeExecution(principal.getName(), executionId));
    }

    // ---------------------------------------
    // POST /api/v1/executions/me/finish/{executionId}
    // ---------------------------------------
    @Operation(
            summary = "Finalizar ejecución",
            description = "Finaliza una ejecución en curso para el usuario autenticado y calcula las calorías consumidas.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Datos de la ejecución al finalizar (tipo de actividad, notas opcionales)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RouteExecutionRequestDTO.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Ejecución finalizada exitosamente",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = RouteExecutionResponseDTO.class)
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Ejecución no encontrada", content = @Content),
                    @ApiResponse(responseCode = "401", description = "No autorizado", content = @Content)
            }
    )
    @PostMapping("/me/finish/{executionId}")
    public ResponseEntity<RouteExecutionResponseDTO> finishExecution(
            Principal principal,
            @PathVariable Long executionId,
            @Valid @RequestBody RouteExecutionRequestDTO body
    ) {
        return ResponseEntity.ok(service.finishExecution(principal.getName(), executionId, body));
    }

    // ---------------------------------------
    // GET /api/v1/executions/me
    // ---------------------------------------
    @Operation(
            summary = "Obtener mis ejecuciones",
            description = "Devuelve todas las ejecuciones realizadas por el usuario autenticado.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Listado de ejecuciones obtenido correctamente",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = RouteExecutionResponseDTO.class)
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "No autorizado", content = @Content)
            }
    )
    @GetMapping("me")
    public ResponseEntity<List<RouteExecutionResponseDTO>> getMyExecutions(Principal principal) {
        List<RouteExecutionResponseDTO> executions = service.getMyExecutions(principal.getName());
        return ResponseEntity.ok(executions);
    }

    // ---------------------------------------
    // GET /api/v1/executions/me/history
    // ---------------------------------------
    @Operation(
            summary = "Obtener historial de ejecuciones finalizadas",
            description = "Devuelve el historial de ejecuciones finalizadas por el usuario autenticado.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Historial de ejecuciones obtenido correctamente",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = RouteExecutionHistoryResponseDTO.class)
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "No autorizado", content = @Content)
            }
    )
    @GetMapping("me/history")
    public ResponseEntity<List<RouteExecutionHistoryResponseDTO>> getMyCompletedExecutionsHistory(Principal principal) {
        List<RouteExecutionHistoryResponseDTO> history = service.getMyCompletedExecutionsHistory(principal.getName());
        return ResponseEntity.ok(history);
    }
}
