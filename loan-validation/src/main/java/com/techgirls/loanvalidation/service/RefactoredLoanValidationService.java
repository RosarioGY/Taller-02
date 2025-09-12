package com.techgirls.loanvalidation.service;

import com.techgirls.loanvalidation.exception.ExternalServiceException;
import com.techgirls.loanvalidation.exception.LoanValidationException;
import com.techgirls.loanvalidation.model.LoanValidationRequest;
import com.techgirls.loanvalidation.model.LoanValidationResult;
import com.techgirls.loanvalidation.service.applicant.ApplicantIdentificationService;
import com.techgirls.loanvalidation.service.calculation.PaymentCalculationService;
import com.techgirls.loanvalidation.service.validation.LoanValidationRule;
import com.techgirls.loanvalidation.service.validation.ValidationContext;
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
 * Refactored loan validation service following SOLID principles.
 * 
 * SOLID Principles Applied:
 * - Single Responsibility: Orchestrates validation rules, doesn't implement them
 * - Open/Closed: New validation rules can be added without modifying this class
 * - Liskov Substitution: All validation rules implement the same interface
 * - Interface Segregation: Uses focused interfaces for different concerns
 * - Dependency Inversion: Depends on abstractions (interfaces) not concrete classes
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RefactoredLoanValidationService {
    
    private final List<LoanValidationRule> validationRules;
    private final PaymentCalculationService paymentCalculationService;
    private final ApplicantIdentificationService applicantIdentificationService;
    private final Clock clock;

    /**
     * Evaluates loan eligibility using a chain of validation rules.
     * This method orchestrates the validation process without implementing specific rules.
     */
    public Mono<LoanValidationResult> evaluate(LoanValidationRequest request) {
        log.debug("Starting loan eligibility evaluation for request: monthlySalary={}, requestedAmount={}, termMonths={}", 
                 request.getMonthlySalary(), request.getRequestedAmount(), request.getTermMonths());
        
        try {
            // Prepare validation context
            ValidationContext context = buildValidationContext(request);
            
            // Execute validation rules in priority order
            return executeValidationRules(request, context)
                .collectList()
                .map(ruleResults -> {
                    // Flatten all validation results
                    List<LoanValidationResult.ReasonsEnum> allReasons = ruleResults.stream()
                            .flatMap(List::stream)
                            .collect(Collectors.toList());
                    
                    boolean eligible = allReasons.isEmpty();
                    double monthlyPayment = context.getMonthlyPayment() != null ? context.getMonthlyPayment() : 0.0;
                    
                    log.info("Loan evaluation completed: eligible={}, reasons={}", eligible, allReasons);
                    
                    return new LoanValidationResult(eligible, allReasons, monthlyPayment);
                })
                .onErrorMap(throwable -> {
                    if (throwable instanceof ExternalServiceException) {
                        return throwable;
                    }
                    log.error("Error communicating with external services", throwable);
                    return new ExternalServiceException("ValidationService", 
                        "Failed during validation process: " + throwable.getMessage(), throwable);
                });
                
        } catch (Exception ex) {
            log.error("Unexpected error during loan evaluation", ex);
            return Mono.error(new LoanValidationException(
                "Unexpected error during loan eligibility evaluation: " + ex.getMessage(), ex));
        }
    }

    /**
     * Builds the validation context with all necessary data for rules.
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
}