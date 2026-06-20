package com.alden.logmonitoringapp.exception;

// thrown when a resource is missing or not owned by the caller; RuntimeException makes it unchecked (no throws needed)
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
