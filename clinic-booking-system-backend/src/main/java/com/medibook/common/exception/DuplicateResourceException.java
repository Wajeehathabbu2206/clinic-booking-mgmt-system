package com.medibook.common.exception;

import org.springframework.http.HttpStatus;

public class DuplicateResourceException extends MedibookException {
    public DuplicateResourceException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
