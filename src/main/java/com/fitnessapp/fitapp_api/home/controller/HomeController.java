package com.fitnessapp.fitapp_api.home.controller;

import com.fitnessapp.fitapp_api.home.dto.HomeKpisTodayResponseDTO;
import com.fitnessapp.fitapp_api.home.service.HomeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/home")
@Tag(name = "Home", description = "Endpoints para la página de inicio / dashboard.")
public class HomeController {
    private final HomeService homeService;

    /**
     * GET /kpis/today
     */
    // TODO: Añadir documentación Swagger
    @GetMapping("/kpis/today")
    public ResponseEntity<HomeKpisTodayResponseDTO> getHomeKpisToday(Principal principal) {
        return ResponseEntity.ok(homeService.getHomeKpisToday(principal.getName()));
    }
}
