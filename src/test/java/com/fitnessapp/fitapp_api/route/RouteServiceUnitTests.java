package com.fitnessapp.fitapp_api.route;

import com.fitnessapp.fitapp_api.auth.model.UserAuth;
import com.fitnessapp.fitapp_api.auth.repository.UserAuthRepository;
import com.fitnessapp.fitapp_api.core.exception.RouteNotFoundException;
import com.fitnessapp.fitapp_api.core.exception.UserAuthNotFoundException;
import com.fitnessapp.fitapp_api.route.dto.RouteRequestDTO;
import com.fitnessapp.fitapp_api.route.dto.RouteResponseDTO;
import com.fitnessapp.fitapp_api.route.mapper.RouteMapper;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RouteServiceUnitTests {

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private UserAuthRepository userAuthRepository;

    @Mock
    private RouteExecutionRepository routeExecutionRepository;

    @Spy
    private RouteMapper mapper = Mappers.getMapper(RouteMapper.class);

    @InjectMocks
    private RouteServiceImpl service;

    private UserAuth user;

    @BeforeEach
    void setup() {
        user = new UserAuth();
        user.setId(1L);
        user.setEmail("john@doe.com");
    }

    // --------------------------------------------------------------------------------------------
    // Helpers
    // --------------------------------------------------------------------------------------------

    private RouteRequestDTO request() {
        return new RouteRequestDTO(
                "Ruta prueba",
                "41.1,-8.6",
                "41.2,-8.7",
                BigDecimal.valueOf(2.5),
                null
        );
    }

    private Route route(Long id) {
        Route r = new Route();
        r.setId(id);
        r.setName("Ruta prueba");
        r.setStartPoint("41,1");
        r.setEndPoint("41,2");
        r.setDistanceKm(BigDecimal.ONE);
        r.setUser(user);
        return r;
    }

    // =================================================================================================
    // GET /my-routes
    // =================================================================================================
    @Test
    @DisplayName("getMyRoutes devuelve lista mapeada correctamente")
    void getMyRoutes_ShouldReturnList() {
        when(routeRepository.findAllByUserEmail(user.getEmail()))
                .thenReturn(List.of(route(1L), route(2L)));

        List<RouteResponseDTO> result = service.getMyRoutes(user.getEmail());

        assertEquals(2, result.size());
        verify(routeRepository).findAllByUserEmail(user.getEmail());
    }

    // =================================================================================================
    // GET by ID
    // =================================================================================================
    @Test
    @DisplayName("getRouteById — ruta encontrada → devuelve DTO")
    void getRouteById_ShouldReturnDto() {
        when(routeRepository.findByIdAndUserEmail(1L, user.getEmail()))
                .thenReturn(Optional.of(route(1L)));

        RouteResponseDTO result = service.getRouteById(1L, user.getEmail());

        assertNotNull(result);
        assertEquals(1L, result.id());
    }

    @Test
    @DisplayName("getRouteById — ruta no existe → lanza excepción")
    void getRouteById_NotFound_ShouldThrow() {
        when(routeRepository.findByIdAndUserEmail(1L, user.getEmail()))
                .thenReturn(Optional.empty());

        assertThrows(RouteNotFoundException.class,
                () -> service.getRouteById(1L, user.getEmail()));
    }

    // =================================================================================================
    // CREATE
    // =================================================================================================
    @Test
    @DisplayName("createRoute — usuario existe → crea correctamente")
    void createRoute_ShouldCreate() {
        RouteRequestDTO dto = request();

        when(userAuthRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));
        when(routeRepository.save(any(Route.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        RouteResponseDTO result = service.createRoute(user.getEmail(), dto);

        assertNotNull(result);
        verify(routeRepository).save(any(Route.class));
    }

    @Test
    @DisplayName("createRoute — usuario no existe → lanza UserAuthNotFoundException")
    void createRoute_UserNotFound_ShouldThrow() {
        when(userAuthRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.empty());

        assertThrows(UserAuthNotFoundException.class,
                () -> service.createRoute(user.getEmail(), request()));
    }

    // =================================================================================================
    // UPDATE
    // =================================================================================================
    @Test
    @DisplayName("updateRoute — ruta existe → actualiza y devuelve DTO")
    void updateRoute_ShouldUpdate() {
        Route existing = route(1L);
        when(routeRepository.findByIdAndUserEmail(1L, user.getEmail()))
                .thenReturn(Optional.of(existing));
        when(routeRepository.save(any(Route.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        RouteResponseDTO result = service.updateRoute(user.getEmail(), 1L, request());

        assertNotNull(result);
        verify(routeRepository).save(existing);
    }

    @Test
    @DisplayName("updateRoute — ruta no existe → lanza excepción")
    void updateRoute_NotFound_ShouldThrow() {
        when(routeRepository.findByIdAndUserEmail(1L, user.getEmail()))
                .thenReturn(Optional.empty());

        assertThrows(RouteNotFoundException.class,
                () -> service.updateRoute(user.getEmail(), 1L, request()));
    }

    // =================================================================================================
    // DELETE
    // =================================================================================================
    @Test
    @DisplayName("deleteRoute — sin ejecuciones → hard delete")
    void deleteRoute_NoExecutions_ShouldHardDelete() {
        Route r = route(1L);
        when(routeRepository.findByIdAndUserEmail(1L, user.getEmail()))
                .thenReturn(Optional.of(r));

        when(routeExecutionRepository.existsByRouteId(1L))
                .thenReturn(false);

        service.deleteRoute(user.getEmail(), 1L);

        verify(routeRepository).hardDelete(1L);
    }

    @Test
    @DisplayName("deleteRoute — con ejecuciones → soft delete")
    void deleteRoute_WithExecutions_ShouldSoftDelete() {
        Route r = route(1L);

        when(routeRepository.findByIdAndUserEmail(1L, user.getEmail()))
                .thenReturn(Optional.of(r));

        when(routeExecutionRepository.existsByRouteId(1L))
                .thenReturn(true);

        service.deleteRoute(user.getEmail(), 1L);

        verify(routeRepository).delete(r);
    }

    @Test
    @DisplayName("deleteRoute — ruta no existe → lanza RouteNotFoundException")
    void deleteRoute_NotFound_ShouldThrow() {
        when(routeRepository.findByIdAndUserEmail(1L, user.getEmail()))
                .thenReturn(Optional.empty());

        assertThrows(RouteNotFoundException.class,
                () -> service.deleteRoute(user.getEmail(), 1L));
    }
}
