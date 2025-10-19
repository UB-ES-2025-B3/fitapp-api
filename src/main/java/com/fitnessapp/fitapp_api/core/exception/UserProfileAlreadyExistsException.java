package com.fitnessapp.fitapp_api.core.exception;

public class UserProfileAlreadyExistsException extends RuntimeException {
    public UserProfileAlreadyExistsException(String message) {
        super(message);
    }
}
