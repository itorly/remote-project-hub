package com.itorly.rph.common.exception;

public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
//(We’ll reuse jakarta.persistence.EntityNotFoundException for 404,
// so no custom NotFound for now.