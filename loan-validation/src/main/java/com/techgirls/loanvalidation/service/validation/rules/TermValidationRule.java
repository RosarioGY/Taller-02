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
 * Validates loan term is within acceptable range (1-36 months).
 * This rule implements Single Responsibility Principle by handling only term validation.
 */
@Component
@Slf4j
public class TermValidationRule implements LoanValidationRule {

    private static final int MIN_TERM_MONTHS = 1;
    private static final int MAX_TERM_MONTHS = 36;

    @Override
    public Mono<List<LoanValidationResult.ReasonsEnum>> validate(
            LoanValidationRequest request, 
            ValidationContext context) {
        
        log.debug("Validating loan term for request");
        
        List<LoanValidationResult.ReasonsEnum> reasons = new ArrayList<>();
        Integer term = request.getTermMonths();
        
        if (term == null || term < MIN_TERM_MONTHS || term > MAX_TERM_MONTHS) {
            reasons.add(LoanValidationResult.ReasonsEnum.PLAZO_MAXIMO_SUPERADO);
            log.warn("Invalid term detected: termMonths={}, valid range: {}-{}", 
                    term, MIN_TERM_MONTHS, MAX_TERM_MONTHS);
        }
        
        return Mono.just(reasons);
    }

    @Override
    public int getPriority() {
        return 20; // Medium priority
    }

    @Override
    public String getRuleName() {
        return "Term Validation Rule";
    }
}