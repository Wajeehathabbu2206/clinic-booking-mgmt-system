package com.medibook.common.exception;

import org.springframework.http.HttpStatus;

public class MedicalRecordNotFoundException extends MedibookException {

    public MedicalRecordNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
