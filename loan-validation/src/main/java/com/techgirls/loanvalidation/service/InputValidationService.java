package com.techgirls.loanvalidation.service;

import com.techgirls.loanvalidation.exception.InputValidationException;
import com.techgirls.loanvalidation.model.LoanValidationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Service responsible for comprehensive input validation of loan requests.
 * Performs business-level validation beyond basic Bean Validation.
 * 
 * Validation Rules:
 * - Salary and amount must be positive and within realistic ranges
 * - Term must be between 1-36 months
 * - Loan date must be valid if provided
 * - Cross-field validations (debt-to-income ratios)
 */
@Slf4j
@Service
public class InputValidationService {
    
    // Business constants for realistic validation
    private static final double MIN_MONTHLY_SALARY = 100.0;
    private static final double MAX_MONTHLY_SALARY = 1_000_000.0;
    private static final double MIN_REQUESTED_AMOUNT = 100.0;
    private static final double MAX_REQUESTED_AMOUNT = 10_000_000.0;
    private static final int MIN_TERM_MONTHS = 1;
    private static final int MAX_TERM_MONTHS = 36;
    private static final double MAX_LOAN_TO_INCOME_RATIO = 20.0; // Max 20x annual salary
    
    /**
     * Validates a loan request and throws InputValidationException if invalid.
     * 
     * @param request the loan request to validate
     * @throws InputValidationException if validation fails
     */
    public void validateRequest(LoanValidationRequest request) {
        log.debug("Starting input validation for loan request");
        
        if (request == null) {
            throw new InputValidationException("Request cannot be null");
        }
        
        validateSalary(request.getMonthlySalary());
        validateRequestedAmount(request.getRequestedAmount());
        validateTermMonths(request.getTermMonths());
        
        // Validate lastLoanDate if present (it's optional)
        if (request.getLastLoanDate() != null && request.getLastLoanDate().isPresent()) {
            validateLastLoanDate(request.getLastLoanDate().get());
        }
        
        validateCrossFieldRules(request);
        
        log.debug("Input validation completed successfully");
    }
    
    private void validateSalary(Double monthlySalary) {
        if (monthlySalary == null) {
            throw new InputValidationException("Monthly salary is required");
        }
        
        if (monthlySalary <= 0) {
            throw new InputValidationException("Monthly salary must be positive");
        }
        
        if (monthlySalary < MIN_MONTHLY_SALARY) {
            throw new InputValidationException(
                String.format("Monthly salary too low: minimum is %.2f", MIN_MONTHLY_SALARY));
        }
        
        if (monthlySalary > MAX_MONTHLY_SALARY) {
            throw new InputValidationException(
                String.format("Monthly salary too high: maximum is %.2f", MAX_MONTHLY_SALARY));
        }
    }
    
    private void validateRequestedAmount(Double requestedAmount) {
        if (requestedAmount == null) {
            throw new InputValidationException("Requested amount is required");
        }
        
        if (requestedAmount <= 0) {
            throw new InputValidationException("Requested amount must be positive");
        }
        
        if (requestedAmount < MIN_REQUESTED_AMOUNT) {
            throw new InputValidationException(
                String.format("Requested amount too low: minimum is %.2f", MIN_REQUESTED_AMOUNT));
        }
        
        if (requestedAmount > MAX_REQUESTED_AMOUNT) {
            throw new InputValidationException(
                String.format("Requested amount too high: maximum is %.2f", MAX_REQUESTED_AMOUNT));
        }
    }
    
    private void validateTermMonths(Integer termMonths) {
        if (termMonths == null) {
            throw new InputValidationException("Term in months is required");
        }
        
        if (termMonths < MIN_TERM_MONTHS || termMonths > MAX_TERM_MONTHS) {
            throw new InputValidationException(
                String.format("Term must be between %d and %d months", MIN_TERM_MONTHS, MAX_TERM_MONTHS));
        }
    }
    
    private void validateLastLoanDate(LocalDate lastLoanDate) {
        LocalDate today = LocalDate.now();
        
        // Last loan date cannot be in the future
        if (lastLoanDate.isAfter(today)) {
            throw new InputValidationException("Last loan date cannot be in the future");
        }
        
        // Last loan date cannot be too far in the past (more than 10 years)
        LocalDate tenYearsAgo = today.minusYears(10);
        if (lastLoanDate.isBefore(tenYearsAgo)) {
            throw new InputValidationException("Last loan date cannot be more than 10 years ago");
        }
    }
    
    private void validateCrossFieldRules(LoanValidationRequest request) {
        // Loan-to-income ratio validation
        if (request.getMonthlySalary() != null && request.getRequestedAmount() != null) {
            double annualSalary = request.getMonthlySalary() * 12;
            double loanToIncomeRatio = request.getRequestedAmount() / annualSalary;
            
            if (loanToIncomeRatio > MAX_LOAN_TO_INCOME_RATIO) {
                throw new InputValidationException(
                    String.format("Loan amount too high relative to income: ratio is %.2f, maximum allowed is %.2f", 
                                loanToIncomeRatio, MAX_LOAN_TO_INCOME_RATIO));
            }
        }
        
        // Basic debt service ratio check
        if (request.getMonthlySalary() != null && request.getRequestedAmount() != null && request.getTermMonths() != null) {
            double monthlyPayment = request.getRequestedAmount() / request.getTermMonths();
            double debtServiceRatio = monthlyPayment / request.getMonthlySalary();
            
            // This is a preliminary check - business rules will do the official validation
            if (debtServiceRatio > 0.8) { // 80% is clearly excessive
                throw new InputValidationException(
                    String.format("Monthly payment (%.2f) would exceed 80%% of monthly salary", monthlyPayment));
            }
        }
    }
}