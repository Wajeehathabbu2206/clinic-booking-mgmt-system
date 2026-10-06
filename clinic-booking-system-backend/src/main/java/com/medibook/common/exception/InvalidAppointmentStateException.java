package com.medibook.common.exception;

import org.springframework.http.HttpStatus;

public class InvalidAppointmentStateException extends MedibookException {
    public InvalidAppointmentStateException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
