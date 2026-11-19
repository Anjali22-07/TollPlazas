package com.tollplazas.backend.exceptions;

import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice 
public class BadRequestException extends RuntimeException {
    
    public BadRequestException(String message) {
        super(message);
    }
}
