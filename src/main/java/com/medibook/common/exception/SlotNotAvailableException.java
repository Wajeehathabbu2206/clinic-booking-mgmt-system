package com.medibook.common.exception;

import org.springframework.http.HttpStatus;

public class SlotNotAvailableException extends MedibookException {
    public SlotNotAvailableException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
