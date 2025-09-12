package com.techgirls.loanvalidation.service;

import com.techgirls.loanvalidation.exception.ExternalServiceException;
import com.techgirls.loanvalidation.exception.LoanValidationException;
import com.techgirls.loanvalidation.model.LoanValidationRequest;
import com.techgirls.loanvalidation.model.LoanValidationResult;
import com.techgirls.loanvalidation.service.validation.LoanValidationOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Enhanced loan validation service following SOLID principles and using Lombok.
 * 
 * This service acts as a facade that delegates to a more sophisticated validation orchestrator.
 * It maintains backward compatibility while providing a cleaner, more maintainable architecture.
 * 
 * SOLID Principles Applied:
 * - Single Responsibility: Only responsible for providing the public API
 * - Open/Closed: New validation logic can be added through the orchestrator
 * - Dependency Inversion: Depends on abstractions through the orchestrator
 * 
 * Lombok Features Used:
 * - @RequiredArgsConstructor: Generates constructor for final fields
 * - @Slf4j: Provides logging capability
 * 
 * Business Rules:
 * R1: No loans in the last 3 months (inclusive).
 * R2: 1 <= termMonths <= 36.
 * R3: monthlyPayment <= 0.40 * monthlySalary.
 * R4: monthlySalary > 0 and requestedAmount > 0.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoanValidationService {
    
    private final LoanValidationOrchestrator validationOrchestrator;

    /**
     * Evaluates loan eligibility using the orchestrated validation approach.
     * 
     * This method now delegates to the LoanValidationOrchestrator, which implements
     * a more sophisticated validation architecture following SOLID principles.
     * 
     * @param request the loan validation request
     * @return Mono containing the validation result
     */
    public Mono<LoanValidationResult> evaluate(LoanValidationRequest request) {
        log.debug("Delegating loan validation to orchestrator for request: monthlySalary={}, requestedAmount={}, termMonths={}", 
                 request.getMonthlySalary(), request.getRequestedAmount(), request.getTermMonths());
        
        return validationOrchestrator.evaluate(request)
                .doOnSuccess(result -> log.info("Validation completed successfully: eligible={}", result.getEligible()))
                .doOnError(error -> log.error("Validation failed with error: {}", error.getMessage()));
    }

}