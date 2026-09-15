package com.smarttrader.exception;

public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String entity, String id) {
        super(entity + " not found: " + id);
    }
}
