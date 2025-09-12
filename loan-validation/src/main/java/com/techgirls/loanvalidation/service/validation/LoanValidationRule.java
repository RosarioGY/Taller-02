package com.techgirls.loanvalidation.service.validation;

import com.techgirls.loanvalidation.model.LoanValidationRequest;
import com.techgirls.loanvalidation.model.LoanValidationResult;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Interface for loan validation rules following Open/Closed Principle.
 * Each rule is responsible for a single validation concern.
 */
public interface LoanValidationRule {
    
    /**
     * Validates a specific aspect of the loan request.
     * 
     * @param request the loan validation request
     * @param context validation context containing additional data
     * @return Mono containing list of rejection reasons, empty if validation passes
     */
    Mono<List<LoanValidationResult.ReasonsEnum>> validate(
            LoanValidationRequest request, 
            ValidationContext context
    );
    
    /**
     * Returns the priority of this rule (lower number = higher priority).
     * Rules with higher priority are executed first.
     */
    default int getPriority() {
        return 100;
    }
    
    /**
     * Returns the name of this validation rule for logging purposes.
     */
    String getRuleName();
}