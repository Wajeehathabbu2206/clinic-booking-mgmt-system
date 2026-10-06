package com.medibook.common.exception;

import org.springframework.http.HttpStatus;

public class AppointmentNotFoundException extends MedibookException {
    public AppointmentNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
