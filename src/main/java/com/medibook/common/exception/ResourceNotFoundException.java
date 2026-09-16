package com.medibook.common.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends MedibookException {

    public ResourceNotFoundException(String resource, Object identifier) {
        super(resource + " not found with identifier: " + identifier, HttpStatus.NOT_FOUND);
    }

    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}