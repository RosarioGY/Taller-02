package com.techgirls.loanvalidation.exception;

/**
 * Exception thrown when loan validation business rules fail.
 * This exception indicates that while the input is valid, the loan request
 * doesn't meet the business eligibility criteria.
 */
public class LoanValidationException extends BusinessException {
    
    public LoanValidationException(String message) {
        super("LOAN_VALIDATION_ERROR", message);
    }
    
    public LoanValidationException(String message, Throwable cause) {
        super("LOAN_VALIDATION_ERROR", message, cause);
    }
}