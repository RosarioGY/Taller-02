package com.techgirls.loanvalidation.service.calculation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class PaymentCalculationServiceTest {

    private final PaymentCalculationService paymentCalculationService = new PaymentCalculationService();

    @Test
    void shouldCalculateMonthlyPaymentWithValidInputs() {
        Double result = paymentCalculationService.calculateMonthlyPayment(12000.0, 12);
        
        assertEquals(1000.0, result);
    }

    @Test
    void shouldReturnNullWhenRequestedAmountIsNull() {
        Double result = paymentCalculationService.calculateMonthlyPayment(null, 12);
        
        assertNull(result);
    }

    @Test
    void shouldReturnNullWhenTermMonthsIsNull() {
        Double result = paymentCalculationService.calculateMonthlyPayment(12000.0, null);
        
        assertNull(result);
    }

    @Test
    void shouldReturnNullWhenTermMonthsIsZero() {
        Double result = paymentCalculationService.calculateMonthlyPayment(12000.0, 0);
        
        assertNull(result);
    }

    @Test
    void shouldReturnNullWhenTermMonthsIsNegative() {
        Double result = paymentCalculationService.calculateMonthlyPayment(12000.0, -5);
        
        assertNull(result);
    }

    @Test
    void shouldCalculateCorrectPaymentForDifferentAmounts() {
        assertEquals(500.0, paymentCalculationService.calculateMonthlyPayment(6000.0, 12));
        assertEquals(2500.0, paymentCalculationService.calculateMonthlyPayment(60000.0, 24));
        assertEquals(1000.0, paymentCalculationService.calculateMonthlyPayment(36000.0, 36));
    }

    @Test
    void shouldCalculateMonthlyPaymentWithInterestValidInputs() {
        Double result = paymentCalculationService.calculateMonthlyPaymentWithInterest(12000.0, 12, 0.05);
        
        assertNotNull(result);
        assertTrue(result > 1000.0); // Should be more than simple division due to interest
    }

    @Test
    void shouldReturnNullWhenInterestCalculationParametersAreNull() {
        assertNull(paymentCalculationService.calculateMonthlyPaymentWithInterest(null, 12, 0.05));
        assertNull(paymentCalculationService.calculateMonthlyPaymentWithInterest(12000.0, null, 0.05));
        assertNull(paymentCalculationService.calculateMonthlyPaymentWithInterest(12000.0, 12, null));
    }

    @Test
    void shouldReturnNullWhenTermMonthsIsInvalidForInterestCalculation() {
        assertNull(paymentCalculationService.calculateMonthlyPaymentWithInterest(12000.0, 0, 0.05));
        assertNull(paymentCalculationService.calculateMonthlyPaymentWithInterest(12000.0, -5, 0.05));
    }

    @Test
    void shouldUseSimpleCalculationWhenInterestRateIsZero() {
        Double withoutInterest = paymentCalculationService.calculateMonthlyPayment(12000.0, 12);
        Double withZeroInterest = paymentCalculationService.calculateMonthlyPaymentWithInterest(12000.0, 12, 0.0);
        
        assertEquals(withoutInterest, withZeroInterest);
    }

    @Test
    void shouldCalculateCorrectInterestPaymentForKnownValues() {
        // Test case: $10,000 loan, 12 months, 12% annual rate (1% monthly)
        Double result = paymentCalculationService.calculateMonthlyPaymentWithInterest(10000.0, 12, 0.12);
        
        assertNotNull(result);
        // Expected payment should be approximately $888.49
        assertEquals(888.49, result, 0.01);
    }

    @Test
    void shouldCalculateHigherPaymentWithHigherInterestRate() {
        Double lowInterest = paymentCalculationService.calculateMonthlyPaymentWithInterest(10000.0, 12, 0.05);
        Double highInterest = paymentCalculationService.calculateMonthlyPaymentWithInterest(10000.0, 12, 0.15);
        
        assertNotNull(lowInterest);
        assertNotNull(highInterest);
        assertTrue(highInterest > lowInterest);
    }

    @Test
    void shouldCalculateLowerPaymentWithLongerTerm() {
        Double shortTerm = paymentCalculationService.calculateMonthlyPaymentWithInterest(10000.0, 12, 0.10);
        Double longTerm = paymentCalculationService.calculateMonthlyPaymentWithInterest(10000.0, 24, 0.10);
        
        assertNotNull(shortTerm);
        assertNotNull(longTerm);
        assertTrue(longTerm < shortTerm);
    }
}