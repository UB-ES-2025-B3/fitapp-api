package com.fitnessapp.fitapp_api.routeexecution.service;

import com.fitnessapp.fitapp_api.routeexecution.dto.RouteExecutionRequestDTO;
import com.fitnessapp.fitapp_api.routeexecution.dto.RouteExecutionResponseDTO;

import java.util.List;

public interface RouteExecutionService {

    List<RouteExecutionResponseDTO> getMyExecutions(String email);
    RouteExecutionResponseDTO startExecution(String email, Long routeId, RouteExecutionRequestDTO request);
    RouteExecutionResponseDTO pauseExecution(String email, Long executionId);
    RouteExecutionResponseDTO resumeExecution(String email, Long executionId);
    RouteExecutionResponseDTO finishExecution(String email, Long executionId, RouteExecutionRequestDTO request);

}
