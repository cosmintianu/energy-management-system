package org.example.user_management_microservice.handlers.exceptions.model;

import org.springframework.http.HttpStatus;

import java.util.List;

public class CustomException extends RuntimeException {
    private final String resource;
    private final HttpStatus status;
    private final List<String> validationErrors;


    public CustomException(String message, String resource, HttpStatus status, List<String> validationErrors) {
        super(message);
        this.resource = resource;
        this.status = status;
        this.validationErrors = validationErrors;
    }

    public String getResource() {
        return resource;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public List<String> getValidationErrors() {
        return validationErrors;
    }
}
