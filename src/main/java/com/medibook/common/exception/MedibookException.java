package com.medibook.common.exception;

import org.springframework.http.HttpStatus;

public class MedibookException extends RuntimeException {

    private final HttpStatus status;

    public MedibookException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}