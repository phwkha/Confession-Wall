package com.example.confessionwall.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class InvalidCsrfTokenException extends RuntimeException {
    public InvalidCsrfTokenException(String message) {
        super(message);
    }
    public InvalidCsrfTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
