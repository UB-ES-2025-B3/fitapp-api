package com.fitnessapp.fitapp_api.profile.controller;

import com.fitnessapp.fitapp_api.profile.dto.UserProfileRequestDTO;
import com.fitnessapp.fitapp_api.profile.dto.UserProfileResponseDTO;
import com.fitnessapp.fitapp_api.profile.service.UserProfileService;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/profiles")
@Tag(name = "User Profiles", description = "Endpoints para gestionar el perfil del usuario autenticado.")
public class UserProfileController {

    private final UserProfileService service;

    /**
     * GET /me
     */
    @Operation(
            summary = "Obtener mi perfil",
            description = "Recupera la información del perfil del usuario actualmente autenticado.",
            parameters = {
                    @Parameter(
                            name = "principal",
                            hidden = true,
                            description = "Inyectado por Spring Security, representa el usuario autenticado."
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Perfil recuperado exitosamente",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = UserProfileResponseDTO.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Perfil no encontrado (el usuario está logueado pero no ha creado su perfil)",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "No autorizado / No autenticado",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponseDTO> getMyProfile(Principal principal) {
        return ResponseEntity.ok(service.getMyProfile(principal.getName()));
    }

    /**
     * POST /me
     */
    @Operation(
            summary = "Crear mi perfil",
            description = "Crea un nuevo perfil para el usuario autenticado. Solo se puede crear un perfil por usuario.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Datos completos del perfil a crear",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserProfileRequestDTO.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Perfil creado exitosamente",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = UserProfileResponseDTO.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "El perfil ya existe para este usuario",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Datos de entrada inválidos (falla Bean Validation)",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "No autorizado / No autenticado",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    @PostMapping("/me")
    public ResponseEntity<UserProfileResponseDTO> createMyProfile(Principal principal, @Valid @RequestBody UserProfileRequestDTO body) {
        UserProfileResponseDTO created = service.createMyProfile(principal.getName(), body);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .build()
                .toUri();
        return ResponseEntity.created(location).body(created);
    }
    /**
     * PUT /me
     */
    @Operation(
            summary = "Actualizar mi perfil (Completo)",
            description = "Actualiza completamente el perfil del usuario autenticado. Se requiere enviar todos los campos del DTO (PUT).",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Todos los datos completos del perfil para la actualización (PUT)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserProfileRequestDTO.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Perfil actualizado exitosamente",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = UserProfileResponseDTO.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Perfil no encontrado (no existe un perfil para actualizar)",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Datos de entrada inválidos (falla Bean Validation)",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "No autorizado / No autenticado",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    @PutMapping("/me")
    public ResponseEntity<UserProfileResponseDTO> updateMyProfile(Principal principal, @Valid @RequestBody UserProfileRequestDTO body) {
        UserProfileResponseDTO updated = service.updateMyProfile(principal.getName(), body);
        return ResponseEntity.ok(updated);
    }
}