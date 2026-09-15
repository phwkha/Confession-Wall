package com.example.confessionwall.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class UnauthorizedOriginException extends RuntimeException {

    public UnauthorizedOriginException(String message) {
        super(message);
    }

    public UnauthorizedOriginException(String message, Throwable cause) {
        super(message, cause);
    }
}
