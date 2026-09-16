package com.medibook.common.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends MedibookException {

    public ForbiddenException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}