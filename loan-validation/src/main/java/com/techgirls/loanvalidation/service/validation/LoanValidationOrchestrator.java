package com.techgirls.loanvalidation.service.validation;

import com.techgirls.loanvalidation.exception.ExternalServiceException;
import com.techgirls.loanvalidation.exception.LoanValidationException;
import com.techgirls.loanvalidation.model.LoanValidationRequest;
import com.techgirls.loanvalidation.model.LoanValidationResult;
import com.techgirls.loanvalidation.service.applicant.ApplicantIdentificationService;
import com.techgirls.loanvalidation.service.calculation.PaymentCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Orchestrates loan validation using a chain of validation rules.
 * 
 * This class implements the Orchestrator pattern and follows SOLID principles:
 * - Single Responsibility: Coordinates validation rules
 * - Open/Closed: New rules can be added without modifying this class
 * - Dependency Inversion: Depends on abstractions (LoanValidationRule interface)
 * 
 * Uses Lombok for:
 * - @RequiredArgsConstructor: Constructor injection
 * - @Slf4j: Logging
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoanValidationOrchestrator {
    
    private final List<LoanValidationRule> validationRules;
    private final PaymentCalculationService paymentCalculationService;
    private final ApplicantIdentificationService applicantIdentificationService;
    private final Clock clock;

    /**
     * Evaluates loan eligibility using all configured validation rules.
     * 
     * @param request the loan validation request
     * @return Mono containing the validation result
     */
    public Mono<LoanValidationResult> evaluate(LoanValidationRequest request) {
        log.debug("Starting orchestrated loan validation for request: monthlySalary={}, requestedAmount={}, termMonths={}", 
                 request.getMonthlySalary(), request.getRequestedAmount(), request.getTermMonths());
        
        try {
            // Build validation context with all necessary data
            ValidationContext context = buildValidationContext(request);
            
            // Execute validation rules in priority order and collect results
            return executeValidationRules(request, context)
                .collectList()
                .map(ruleResults -> buildFinalResult(ruleResults, context))
                .onErrorMap(this::mapValidationError);
                
        } catch (Exception ex) {
            log.error("Unexpected error during orchestrated loan evaluation", ex);
            return Mono.error(new LoanValidationException(
                "Unexpected error during loan eligibility evaluation: " + ex.getMessage(), ex));
        }
    }

    /**
     * Builds the validation context containing all shared data.
     */
    private ValidationContext buildValidationContext(LoanValidationRequest request) {
        LocalDate today = LocalDate.now(clock);
        LocalDate threeMonthsAgo = today.minusMonths(3);
        
        Double monthlyPayment = paymentCalculationService.calculateMonthlyPayment(
                request.getRequestedAmount(), 
                request.getTermMonths()
        );
        
        String applicantId = applicantIdentificationService.generateApplicantId(request);
        
        return ValidationContext.builder()
                .currentDate(today)
                .recentLoanThreshold(threeMonthsAgo)
                .monthlyPayment(monthlyPayment)
                .applicantId(applicantId)
                .externalDataAvailable(true)
                .build();
    }

    /**
     * Executes all validation rules in priority order.
     */
    private Flux<List<LoanValidationResult.ReasonsEnum>> executeValidationRules(
            LoanValidationRequest request, 
            ValidationContext context) {
        
        // Sort rules by priority (lower number = higher priority)
        List<LoanValidationRule> sortedRules = validationRules.stream()
                .sorted(Comparator.comparingInt(LoanValidationRule::getPriority))
                .collect(Collectors.toList());
        
        log.debug("Executing {} validation rules in priority order", sortedRules.size());
        
        return Flux.fromIterable(sortedRules)
                .flatMap(rule -> {
                    log.debug("Executing rule: {}", rule.getRuleName());
                    return rule.validate(request, context)
                            .doOnNext(reasons -> {
                                if (!reasons.isEmpty()) {
                                    log.debug("Rule {} found violations: {}", rule.getRuleName(), reasons);
                                }
                            });
                });
    }

    /**
     * Builds the final validation result from all rule results.
     */
    private LoanValidationResult buildFinalResult(
            List<List<LoanValidationResult.ReasonsEnum>> ruleResults, 
            ValidationContext context) {
        
        // Flatten all validation results
        List<LoanValidationResult.ReasonsEnum> allReasons = ruleResults.stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
        
        boolean eligible = allReasons.isEmpty();
        double monthlyPayment = context.getMonthlyPayment() != null ? context.getMonthlyPayment() : 0.0;
        
        log.info("Orchestrated loan evaluation completed: eligible={}, reasons={}, monthlyPayment={}", 
                eligible, allReasons, monthlyPayment);
        
        return new LoanValidationResult(eligible, allReasons, monthlyPayment);
    }

    /**
     * Maps validation errors to appropriate exception types.
     */
    private Throwable mapValidationError(Throwable throwable) {
        if (throwable instanceof ExternalServiceException) {
            return throwable;
        }
        
        log.error("Error during validation orchestration", throwable);
        return new ExternalServiceException("ValidationOrchestrator", 
            "Failed during validation process: " + throwable.getMessage(), throwable);
    }
}