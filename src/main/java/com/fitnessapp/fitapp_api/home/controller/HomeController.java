package com.fitnessapp.fitapp_api.home.controller;

import com.fitnessapp.fitapp_api.home.dto.HomeKpisTodayResponseDTO;
import com.fitnessapp.fitapp_api.home.service.HomeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;


@RestController
@RequestMapping("/api/v1/home")
@RequiredArgsConstructor
@Tag(name = "Home", description = "Endpoints para la pantalla principal y KPIs del usuario")
@SecurityRequirement(name = "bearer-jwt")
public class HomeController {

    private final HomeService homeService;

    /**
     * GET /kpis/today
     */
    @Operation(
            summary = "Obtener KPIs de hoy",
            description = "Recupera los KPIs del usuario para el día actual, incluyendo rutas completadas, duración total, distancia total, calorías quemadas y racha activa.",
            parameters = {
                    @Parameter(
                            name = "principal",
                            hidden = true,
                            description = "Inyectado por Spring Security, representa el usuario autenticado."
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "401",
                            description = "No autenticado - Token JWT inválido o no proporcionado",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Perfil de usuario no encontrado",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "422",
                            description = "Perfil de usuario incompleto - Faltan datos requeridos en el perfil",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    public ResponseEntity<HomeKpisTodayResponseDTO> getHomeKpisToday(Principal principal) {
        return ResponseEntity.ok(homeService.getHomeKpisToday(principal.getName()));
    }
}
