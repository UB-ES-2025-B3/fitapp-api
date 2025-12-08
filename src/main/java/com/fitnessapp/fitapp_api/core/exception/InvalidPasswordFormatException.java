package com.fitnessapp.fitapp_api.core.exception;

public class InvalidPasswordFormatException extends RuntimeException {
    public InvalidPasswordFormatException(String message) {
        super(message);
    }
}
