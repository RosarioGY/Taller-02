package com.techgirls.loanvalidation.config;

import com.techgirls.loanvalidation.service.validation.LoanValidationRule;
import com.techgirls.loanvalidation.service.validation.rules.AmountValidationRule;
import com.techgirls.loanvalidation.service.validation.rules.PaymentCapacityRule;
import com.techgirls.loanvalidation.service.validation.rules.RecentLoanRule;
import com.techgirls.loanvalidation.service.validation.rules.TermValidationRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration class for loan validation rules.
 * 
 * This configuration demonstrates the Open/Closed Principle:
 * - Open for extension: New rules can be added by creating new implementations
 * - Closed for modification: Existing rules don't need to change
 * 
 * Uses Lombok @Slf4j for logging configuration setup.
 */
@Configuration
@Slf4j
public class ValidationRulesConfig {

    /**
     * Provides the list of all validation rules to be executed.
     * 
     * This method showcases the Dependency Inversion Principle by returning
     * a list of abstract interfaces rather than concrete implementations.
     * 
     * @param amountValidationRule injected amount validation rule
     * @param termValidationRule injected term validation rule  
     * @param paymentCapacityRule injected payment capacity rule
     * @param recentLoanRule injected recent loan rule
     * @return ordered list of validation rules
     */
    @Bean
    public List<LoanValidationRule> validationRules(
            AmountValidationRule amountValidationRule,
            TermValidationRule termValidationRule,
            PaymentCapacityRule paymentCapacityRule,
            RecentLoanRule recentLoanRule) {
        
        List<LoanValidationRule> rules = Arrays.asList(
                amountValidationRule,
                termValidationRule, 
                paymentCapacityRule,
                recentLoanRule
        );
        
        log.info("Configured {} validation rules", rules.size());
        rules.forEach(rule -> log.debug("Registered rule: {} with priority: {}", 
                rule.getRuleName(), rule.getPriority()));
        
        return rules;
    }
}