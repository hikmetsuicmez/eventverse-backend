package com.hikmetsuicmez.eventverse.exception;

public class EventCapacityFullException extends RuntimeException {
    public EventCapacityFullException(String message) {
        super(message);
    }
} 