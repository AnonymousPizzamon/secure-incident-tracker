package com.alden.logmonitoringapp.exception;

import java.util.Map;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonInclude;

// json body returned for every handled error
@JsonInclude(JsonInclude.Include.NON_NULL) // omit fieldErrors when there are none
public class ApiError {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private Map<String, String> fieldErrors;

    public ApiError(int status, String error, String message) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
    }

    public ApiError(int status, String error, String message, Map<String, String> fieldErrors) {
        this(status, error, message);
        this.fieldErrors = fieldErrors;
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public int getStatus() { return status; }
    public String getError() { return error; }
    public String getMessage() { return message; }
    public Map<String, String> getFieldErrors() { return fieldErrors; }
}
