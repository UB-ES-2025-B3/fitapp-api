package com.fitnessapp.fitapp_api.route.controller;

import com.fitnessapp.fitapp_api.route.dto.RouteRequestDTO;
import com.fitnessapp.fitapp_api.route.dto.RouteResponseDTO;
import com.fitnessapp.fitapp_api.route.service.RouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/routes")
@Tag(name = "Routes", description = "Endpoints para gestionar las rutas del usuario autenticado.")
public class RouteController {

    private final RouteService service;

    // ---------------------------------------
    // GET /api/v1/routes
    // ---------------------------------------
    @Operation(
            summary = "Listar mis rutas activas",
            description = "Devuelve todas las rutas activas (no eliminadas) asociadas al usuario autenticado.",
            parameters = {
                    @Parameter(name = "principal", hidden = true, description = "Usuario autenticado")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Listado de rutas obtenido correctamente",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = RouteResponseDTO.class)
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "No autorizado", content = @Content)
            }
    )
    @GetMapping
    public ResponseEntity<List<RouteResponseDTO>> getMyRoutes(Principal principal) {
        List<RouteResponseDTO> routes = service.getMyRoutes(principal.getName());
        return ResponseEntity.ok(routes);
    }

    // ---------------------------------------
    // GET /api/v1/routes/{id}
    // ---------------------------------------
    @Operation(
            summary = "Mostrar una ruta por su id",
            description = "Devuelve la ruta activas (no eliminadas) asociadas al usuario autenticado según la id de la ruta.",
            parameters = {
                    @Parameter(name="id",hidden = true,description = "Id de la ruta"),
                    @Parameter(name = "principal", hidden = true, description = "Usuario autenticado")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Ruta obtenida correctamente",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = RouteResponseDTO.class)
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "No autorizado", content = @Content)
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<RouteResponseDTO> getRouteById(@PathVariable Long id
                                                                ,Principal principal) {
        RouteResponseDTO route = service.getRouteById(id,principal.getName());
        return ResponseEntity.ok(route);
    }

    // ---------------------------------------
    // POST /api/v1/routes
    // ---------------------------------------
    @Operation(
            summary = "Crear nueva ruta",
            description = "Crea una nueva ruta asociada al usuario autenticado.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Datos de la nueva ruta",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RouteRequestDTO.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Ruta creada exitosamente",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = RouteResponseDTO.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Datos inválidos o incompletos",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "No autorizado / No autenticado",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    @PostMapping
    public ResponseEntity<RouteResponseDTO> createRoute(Principal principal, @Valid @RequestBody RouteRequestDTO body) {
        RouteResponseDTO created = service.createRoute(principal.getName(), body);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    // ---------------------------------------
    // PUT /api/v1/routes/{id}
    // ---------------------------------------
    @Operation(
            summary = "Actualizar una ruta existente",
            description = "Actualiza los datos (nombre, inicio, fin, distancia) de una ruta existente del usuario autenticado.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Datos actualizados de la ruta",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RouteRequestDTO.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Ruta actualizada exitosamente",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = RouteResponseDTO.class)
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Ruta no encontrada", content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "401", description = "No autorizado", content = @Content(mediaType = "application/json"))
            }
    )
    @PutMapping("/{id}")
    public ResponseEntity<RouteResponseDTO> updateRoute(
            Principal principal,
            @PathVariable Long id,
            @Valid @RequestBody RouteRequestDTO body
    ) {
        RouteResponseDTO updated = service.updateRoute(principal.getName(), id, body);
        return ResponseEntity.ok(updated);
    }

    // ---------------------------------------
    // DELETE /api/v1/routes/{id}
    // ---------------------------------------
    @Operation(
            summary = "Eliminar una ruta",
            description = "Elimina una ruta del usuario autenticado. Si la ruta tiene ejecuciones asociadas, realiza soft delete; de lo contrario, hard delete.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Ruta eliminada exitosamente (soft o hard)", content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "404", description = "Ruta no encontrada", content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "401", description = "No autorizado", content = @Content(mediaType = "application/json"))
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoute(Principal principal, @PathVariable Long id) {
        service.deleteRoute(principal.getName(), id);
        return ResponseEntity.noContent().build();
    }
}