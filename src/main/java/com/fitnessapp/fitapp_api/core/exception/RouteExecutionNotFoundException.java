package com.fitnessapp.fitapp_api.core.exception;

public class RouteExecutionNotFoundException extends RuntimeException {
    public RouteExecutionNotFoundException(String message) {
        super(message);
    }
}
