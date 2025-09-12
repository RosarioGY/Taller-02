package com.techgirls.loanvalidation.exception;

/**
 * Exception thrown when input validation fails.
 * This exception indicates that the client provided invalid input data
 * that doesn't meet the business requirements or basic validation rules.
 */
public class InputValidationException extends BusinessException {
    
    public InputValidationException(String message) {
        super("INPUT_VALIDATION_ERROR", message);
    }
    
    public InputValidationException(String message, Throwable cause) {
        super("INPUT_VALIDATION_ERROR", message, cause);
    }
}