package com.techgirls.loanvalidation.service.calculation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service responsible for loan payment calculations.
 * This follows Single Responsibility Principle by handling only calculation logic.
 */
@Service
@Slf4j
public class PaymentCalculationService {

    /**
     * Calculates the monthly payment for a loan.
     * Currently implements simple division (requestedAmount / termMonths).
     * In a real system, this would include interest rate calculations.
     * 
     * @param requestedAmount the total amount requested
     * @param termMonths the loan term in months
     * @return the calculated monthly payment, or null if inputs are invalid
     */
    public Double calculateMonthlyPayment(Double requestedAmount, Integer termMonths) {
        log.debug("Calculating monthly payment: amount={}, term={}", requestedAmount, termMonths);
        
        if (requestedAmount == null || termMonths == null || termMonths <= 0) {
            log.warn("Invalid parameters for payment calculation: amount={}, term={}", 
                    requestedAmount, termMonths);
            return null;
        }
        
        Double monthlyPayment = requestedAmount / termMonths;
        log.debug("Calculated monthly payment: {}", monthlyPayment);
        
        return monthlyPayment;
    }
    
    /**
     * Calculates the monthly payment with interest rate.
     * This method can be extended for more complex interest calculations.
     * 
     * @param requestedAmount the total amount requested
     * @param termMonths the loan term in months
     * @param annualInterestRate the annual interest rate as a decimal (e.g., 0.05 for 5%)
     * @return the calculated monthly payment including interest
     */
    public Double calculateMonthlyPaymentWithInterest(
            Double requestedAmount, 
            Integer termMonths, 
            Double annualInterestRate) {
        
        log.debug("Calculating monthly payment with interest: amount={}, term={}, rate={}", 
                requestedAmount, termMonths, annualInterestRate);
        
        if (requestedAmount == null || termMonths == null || termMonths <= 0 || annualInterestRate == null) {
            log.warn("Invalid parameters for interest payment calculation");
            return null;
        }
        
        if (annualInterestRate == 0.0) {
            return calculateMonthlyPayment(requestedAmount, termMonths);
        }
        
        double monthlyRate = annualInterestRate / 12;
        double factor = Math.pow(1 + monthlyRate, termMonths);
        double monthlyPayment = requestedAmount * (monthlyRate * factor) / (factor - 1);
        
        log.debug("Calculated monthly payment with interest: {}", monthlyPayment);
        
        return monthlyPayment;
    }
}