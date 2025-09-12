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
 * Validates payment capacity - monthly payment should not exceed 40% of monthly salary.
 * This rule implements Single Responsibility Principle by handling only capacity validation.
 */
@Component
@Slf4j
public class PaymentCapacityRule implements LoanValidationRule {

    private static final double MAX_PAYMENT_RATIO = 0.40;

    @Override
    public Mono<List<LoanValidationResult.ReasonsEnum>> validate(
            LoanValidationRequest request, 
            ValidationContext context) {
        
        log.debug("Validating payment capacity for request");
        
        List<LoanValidationResult.ReasonsEnum> reasons = new ArrayList<>();
        
        Double monthlySalary = request.getMonthlySalary();
        Double monthlyPayment = context.getMonthlyPayment();
        
        if (monthlySalary != null && monthlyPayment != null) {
            double maxAllowedPayment = monthlySalary * MAX_PAYMENT_RATIO;
            
            if (monthlyPayment > maxAllowedPayment) {
                reasons.add(LoanValidationResult.ReasonsEnum.CAPACIDAD_INSUFICIENTE);
                log.warn("Insufficient payment capacity: monthlyPayment={}, maxAllowed={}, ratio={}", 
                        monthlyPayment, maxAllowedPayment, MAX_PAYMENT_RATIO);
            }
        }
        
        return Mono.just(reasons);
    }

    @Override
    public int getPriority() {
        return 30; // Medium priority
    }

    @Override
    public String getRuleName() {
        return "Payment Capacity Rule";
    }
}