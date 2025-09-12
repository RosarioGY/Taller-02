package com.techgirls.loanvalidation.exception;

/**
 * Base exception for all business-related errors in the loan validation service.
 * This provides a common parent for all business exceptions and enables 
 * consistent error handling patterns.
 */
public class BusinessException extends RuntimeException {
    
    private final String errorCode;
    private final String userMessage;
    
    public BusinessException(String message) {
        super(message);
        this.errorCode = "BUSINESS_ERROR";
        this.userMessage = message;
    }
    
    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.userMessage = message;
    }
    
    public BusinessException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.userMessage = message;
    }
    
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "BUSINESS_ERROR";
        this.userMessage = message;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public String getUserMessage() {
        return userMessage;
    }
}