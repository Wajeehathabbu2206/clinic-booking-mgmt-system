package com.medibook.common.exception;

import org.springframework.http.HttpStatus;

public class HolidayConflictException extends MedibookException {
    public HolidayConflictException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
