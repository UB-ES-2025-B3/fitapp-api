package com.fitnessapp.fitapp_api.core.exception;

public class UserAuthNotFoundException extends RuntimeException {
    public UserAuthNotFoundException(String message) {
        super(message);
    }
}
