package com.medibook.common.exception;

import org.springframework.http.HttpStatus;

public class ReviewNotFoundException extends MedibookException {

    public ReviewNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
