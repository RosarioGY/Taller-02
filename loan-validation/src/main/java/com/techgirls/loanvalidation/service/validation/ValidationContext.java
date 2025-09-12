package com.techgirls.loanvalidation.service.validation;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

/**
 * Context object that contains shared data for validation rules.
 * This follows the Context pattern and helps avoid passing multiple parameters.
 */
@Data
@Builder
public class ValidationContext {
    
    /**
     * Current date for validation calculations.
     */
    private final LocalDate currentDate;
    
    /**
     * Date threshold for recent loan validation (3 months ago).
     */
    private final LocalDate recentLoanThreshold;
    
    /**
     * Calculated monthly payment for the requested loan.
     */
    private final Double monthlyPayment;
    
    /**
     * Last loan date retrieved from external service (if available).
     */
    private final LocalDate lastLoanDate;
    
    /**
     * Flag indicating if external service data is available.
     */
    private final boolean externalDataAvailable;
    
    /**
     * Simulated applicant ID for testing purposes.
     */
    private final String applicantId;
}