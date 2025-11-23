package com.fitnessapp.fitapp_api.route;

import com.fitnessapp.fitapp_api.auth.model.UserAuth;
import com.fitnessapp.fitapp_api.core.exception.RouteNotFoundException;
import com.fitnessapp.fitapp_api.core.exception.UserAuthNotFoundException;
import com.fitnessapp.fitapp_api.route.dto.CheckpointRequestDTO;
import com.fitnessapp.fitapp_api.route.dto.RouteRequestDTO;
import com.fitnessapp.fitapp_api.route.dto.RouteResponseDTO;
import com.fitnessapp.fitapp_api.route.mapper.RouteMapper;
import com.fitnessapp.fitapp_api.route.model.Checkpoint;
import com.fitnessapp.fitapp_api.route.model.Route;
import com.fitnessapp.fitapp_api.route.repository.RouteRepository;
import com.fitnessapp.fitapp_api.route.service.implementation.RouteServiceImpl;
import com.fitnessapp.fitapp_api.routeexecution.repository.RouteExecutionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RouteCheckpointsUnitTests {

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private RouteExecutionRepository routeExecutionRepository;

    @Mock
    private com.fitnessapp.fitapp_api.auth.repository.UserAuthRepository userAuthRepository;

    @Spy
    private RouteMapper mapper = Mappers.getMapper(RouteMapper.class);

    @InjectMocks
    private RouteServiceImpl routeService;

    private UserAuth user;

    @BeforeEach
    void setUp() {
        user = new UserAuth();
        user.setId(1L);
        user.setEmail("test@example.com");
    }

    // Helpers
    private RouteRequestDTO buildRequest(List<CheckpointRequestDTO> checkpoints) {
        return new RouteRequestDTO(
                "Ruta X",
                "41.1,-8.6",
                "41.3,-8.8",
                BigDecimal.valueOf(3.0),
                checkpoints
        );
    }

    private Route mockRoute(Long id, List<Checkpoint> cps) {
        Route r = new Route();
        r.setId(id);
        r.setName("Ruta X");
        r.setStartPoint("41.1,-8.6");
        r.setEndPoint("41.3,-8.8");
        r.setDistanceKm(BigDecimal.ONE);
        r.setUser(user);
        r.setCheckpoints(cps);
        return r;
    }

    private Checkpoint checkpoint(String name, String point) {
        Checkpoint c = new Checkpoint();
        c.setName(name);
        c.setPoint(point);
        return c;
    }

    @Test
    @DisplayName("Crear ruta con lista completa de checkpoints")
    void createRoute_WithCheckpoints_ShouldCreateSuccessfully() {
        List<CheckpointRequestDTO> checkpoints = List.of(
                new CheckpointRequestDTO("P1", "41.10,-8.60"),
                new CheckpointRequestDTO("P2", "41.15,-8.62")
        );
        RouteRequestDTO dto = buildRequest(checkpoints);

        when(userAuthRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));
        when(routeRepository.save(any(Route.class)))
                .thenAnswer(i -> i.getArgument(0));

        RouteResponseDTO response = routeService.createRoute(user.getEmail(), dto);

        assertNotNull(response);
        assertEquals(2, response.checkpoints().size());
        verify(routeRepository).save(any(Route.class));
    }

    @Test
    @DisplayName("Crear ruta — checkpoints se asignan correctamente en Route antes del save")
    void createRoute_CheckpointsMappedToEntityCorrectly() {
        List<CheckpointRequestDTO> checkpoints = List.of(
                new CheckpointRequestDTO("C1", "10,10"),
                new CheckpointRequestDTO("C2", "20,20")
        );
        RouteRequestDTO dto = buildRequest(checkpoints);

        when(userAuthRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));
        when(routeRepository.save(any(Route.class)))
                .thenAnswer(invocation -> {
                    Route saved = invocation.getArgument(0);
                    // Assert before the service returns DTO
                    assertEquals(2, saved.getCheckpoints().size());
                    assertEquals("C1", saved.getCheckpoints().get(0).getName());
                    assertEquals("10,10", saved.getCheckpoints().get(0).getPoint());
                    return saved;
                });

        routeService.createRoute(user.getEmail(), dto);
    }

    @Test
    @DisplayName("Crear ruta con lista vacía de checkpoints")
    void createRoute_EmptyCheckpointList_ShouldCreateSuccessfully() {
        RouteRequestDTO dto = buildRequest(new ArrayList<>());

        when(userAuthRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));
        when(routeRepository.save(any(Route.class)))
                .thenAnswer(i -> i.getArgument(0));

        RouteResponseDTO response = routeService.createRoute(user.getEmail(), dto);

        assertNotNull(response);
        assertEquals(0, response.checkpoints().size());
    }

    @Test
    @DisplayName("Crear ruta con checkpoints = null")
    void createRoute_NullCheckpoints_ShouldCreateSuccessfully() {
        RouteRequestDTO dto = buildRequest(null);

        when(userAuthRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));
        when(routeRepository.save(any(Route.class)))
                .thenAnswer(i -> i.getArgument(0));

        RouteResponseDTO response = routeService.createRoute(user.getEmail(), dto);

        assertNotNull(response);
        assertNull(response.checkpoints());
    }

    @Test
    @DisplayName("Crear ruta — usuario no existe → excepción")
    void createRoute_UserNotFound_ShouldThrow() {
        when(userAuthRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.empty());

        RouteRequestDTO dto = buildRequest(List.of());

        assertThrows(UserAuthNotFoundException.class,
                () -> routeService.createRoute(user.getEmail(), dto));
    }

    @Test
    @DisplayName("Actualizar ruta reemplaza lista de checkpoints")
    void updateRoute_ReplacesCheckpointList() {
        Route route = mockRoute(1L, new ArrayList<>(List.of(
                checkpoint("Old1", "11,22")
        )));

        when(routeRepository.findByIdAndUserEmail(1L, user.getEmail()))
                .thenReturn(Optional.of(route));
        when(routeRepository.save(any(Route.class)))
                .thenAnswer(i -> i.getArgument(0));

        RouteRequestDTO dto = buildRequest(List.of(
                new CheckpointRequestDTO("New1", "41.1,-8.6"),
                new CheckpointRequestDTO("New2", "41.2,-8.7")
        ));

        RouteResponseDTO response = routeService.updateRoute(user.getEmail(), 1L, dto);

        assertNotNull(response);
        assertEquals(2, response.checkpoints().size());
    }

    @Test
    @DisplayName("Actualizar ruta — lista vacía → elimina checkpoints")
    void updateRoute_EmptyList_RemovesAll() {
        Route route = mockRoute(1L, new ArrayList<>(List.of(
                checkpoint("A", "1,1"),
                checkpoint("B", "2,2")
        )));

        when(routeRepository.findByIdAndUserEmail(1L, user.getEmail()))
                .thenReturn(Optional.of(route));
        when(routeRepository.save(any(Route.class)))
                .thenAnswer(i -> i.getArgument(0));

        RouteRequestDTO dto = buildRequest(new ArrayList<>());

        RouteResponseDTO result = routeService.updateRoute(user.getEmail(), 1L, dto);

        assertNotNull(result);
        assertEquals(0, result.checkpoints().size());
    }

    @Test
    @DisplayName("Actualizar ruta — checkpoints null")
    void updateRoute_NullList_AllowsNull() {
        Route route = mockRoute(1L, List.of(checkpoint("X", "1,1")));

        when(routeRepository.findByIdAndUserEmail(1L, user.getEmail()))
                .thenReturn(Optional.of(route));
        when(routeRepository.save(any(Route.class)))
                .thenAnswer(i -> i.getArgument(0));

        RouteRequestDTO dto = buildRequest(null);

        RouteResponseDTO result = routeService.updateRoute(user.getEmail(), 1L, dto);

        assertNull(result.checkpoints());
    }

    @Test
    @DisplayName("Actualizar ruta — no encontrada")
    void updateRoute_RouteNotFound_ShouldThrow() {
        when(routeRepository.findByIdAndUserEmail(99L, user.getEmail()))
                .thenReturn(Optional.empty());

        RouteRequestDTO dto = buildRequest(List.of());

        assertThrows(RouteNotFoundException.class,
                () -> routeService.updateRoute(user.getEmail(), 99L, dto));
    }

    @Test
    @DisplayName("Obtener ruta devuelve checkpoints")
    void getRouteById_ReturnsCheckpoints() {
        Route route = mockRoute(1L, List.of(
                checkpoint("A", "11,11"),
                checkpoint("B", "22,22")
        ));

        when(routeRepository.findByIdAndUserEmail(1L, user.getEmail()))
                .thenReturn(Optional.of(route));

        RouteResponseDTO result = routeService.getRouteById(1L, user.getEmail());

        assertEquals(2, result.checkpoints().size());
    }

    @Test
    @DisplayName("Obtener ruta — no existe")
    void getRouteById_NotFound() {
        when(routeRepository.findByIdAndUserEmail(1L, user.getEmail()))
                .thenReturn(Optional.empty());

        assertThrows(RouteNotFoundException.class,
                () -> routeService.getRouteById(1L, user.getEmail()));
    }
}
