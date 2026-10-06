package com.medibook.common.exception;

import org.springframework.http.HttpStatus;

public class NotificationNotFoundException extends MedibookException {
    public NotificationNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}