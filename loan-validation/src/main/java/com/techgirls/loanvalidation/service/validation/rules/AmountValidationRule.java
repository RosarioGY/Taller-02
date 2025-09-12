package com.techgirls.loanvalidation.service.validation.rules;

import com.techgirls.loanvalidation.model.LoanValidationRequest;
import com.techgirls.loanvalidation.model.LoanValidationResult;
import com.techgirls.loanvalidation.service.validation.LoanValidationRule;
import com.techgirls.loanvalidation.service.validation.ValidationContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates that monetary amounts are positive and valid.
 * This rule implements Single Responsibility Principle by handling only amount validation.
 */
@Component
@Slf4j
public class AmountValidationRule implements LoanValidationRule {

    @Override
    public Mono<List<LoanValidationResult.ReasonsEnum>> validate(
            LoanValidationRequest request, 
            ValidationContext context) {
        
        log.debug("Validating amounts for request");
        
        List<LoanValidationResult.ReasonsEnum> reasons = new ArrayList<>();
        
        if (isInvalidAmount(request.getMonthlySalary()) || isInvalidAmount(request.getRequestedAmount())) {
            reasons.add(LoanValidationResult.ReasonsEnum.DATOS_INVALIDOS);
            log.warn("Invalid amounts detected: monthlySalary={}, requestedAmount={}", 
                    request.getMonthlySalary(), request.getRequestedAmount());
        }
        
        return Mono.just(reasons);
    }

    @Override
    public int getPriority() {
        return 10; // High priority - validate basic data first
    }

    @Override
    public String getRuleName() {
        return "Amount Validation Rule";
    }
    
    private boolean isInvalidAmount(Double amount) {
        return amount == null || amount <= 0.0;
    }
}