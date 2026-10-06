package com.medibook.common.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends MedibookException {

    public ConflictException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}