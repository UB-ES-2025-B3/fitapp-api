package com.fitnessapp.fitapp_api.core.exception;

public class UserProfileNotCompletedException extends RuntimeException {
    public UserProfileNotCompletedException(String message) {
        super(message);
    }
}
