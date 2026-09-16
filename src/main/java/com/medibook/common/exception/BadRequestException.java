package com.medibook.common.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends MedibookException {

    public BadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}