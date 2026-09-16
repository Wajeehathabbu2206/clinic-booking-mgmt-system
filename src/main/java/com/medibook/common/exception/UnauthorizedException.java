package com.medibook.common.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends MedibookException {

    public UnauthorizedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}