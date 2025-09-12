package com.techgirls.loanvalidation.exception;

/**
 * Exception thrown when external service calls fail.
 * This exception indicates that an external dependency (like loan history service)
 * is unavailable or returned an error response.
 */
public class ExternalServiceException extends BusinessException {
    
    private final String serviceName;
    
    public ExternalServiceException(String serviceName, String message) {
        super("EXTERNAL_SERVICE_ERROR", 
              String.format("External service '%s' error: %s", serviceName, message));
        this.serviceName = serviceName;
    }
    
    public ExternalServiceException(String serviceName, String message, Throwable cause) {
        super("EXTERNAL_SERVICE_ERROR", 
              String.format("External service '%s' error: %s", serviceName, message), 
              cause);
        this.serviceName = serviceName;
    }
    
    public String getServiceName() {
        return serviceName;
    }
}