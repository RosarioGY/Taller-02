package com.techgirls.loanvalidation.service;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;

import com.techgirls.loanvalidation.exception.InputValidationException;
import com.techgirls.loanvalidation.model.LoanValidationRequest;

class InputValidationServiceTest {

    private final InputValidationService inputValidationService = new InputValidationService();

    @Test
    void shouldThrowExceptionWhenRequestIsNull() {
        InputValidationException exception = assertThrows(InputValidationException.class, 
            () -> inputValidationService.validateRequest(null));
        assertEquals("Request cannot be null", exception.getMessage());
    }

    @Test
    void shouldValidateSuccessfullyWithValidRequest() {
        LoanValidationRequest request = createValidRequest();
        assertDoesNotThrow(() -> inputValidationService.validateRequest(request));
    }

    @Test
    void shouldThrowExceptionWhenMonthlySalaryIsNull() {
        LoanValidationRequest request = createValidRequest();
        request.setMonthlySalary(null);

        InputValidationException exception = assertThrows(InputValidationException.class, 
            () -> inputValidationService.validateRequest(request));
        assertEquals("Monthly salary is required", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenMonthlySalaryIsZero() {
        LoanValidationRequest request = createValidRequest();
        request.setMonthlySalary(0.0);

        InputValidationException exception = assertThrows(InputValidationException.class, 
            () -> inputValidationService.validateRequest(request));
        assertEquals("Monthly salary must be positive", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenMonthlySalaryIsNegative() {
        LoanValidationRequest request = createValidRequest();
        request.setMonthlySalary(-1000.0);

        InputValidationException exception = assertThrows(InputValidationException.class, 
            () -> inputValidationService.validateRequest(request));
        assertEquals("Monthly salary must be positive", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenMonthlySalaryIsTooLow() {
        LoanValidationRequest request = createValidRequest();
        request.setMonthlySalary(50.0); // Below minimum of 100.0

        InputValidationException exception = assertThrows(InputValidationException.class, 
            () -> inputValidationService.validateRequest(request));
        assertEquals("Monthly salary too low: minimum is 100.00", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenMonthlySalaryIsTooHigh() {
        LoanValidationRequest request = createValidRequest();
        request.setMonthlySalary(2_000_000.0); // Above maximum of 1,000,000.0

        InputValidationException exception = assertThrows(InputValidationException.class, 
            () -> inputValidationService.validateRequest(request));
        assertEquals("Monthly salary too high: maximum is 1000000.00", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenRequestedAmountIsNull() {
        LoanValidationRequest request = createValidRequest();
        request.setRequestedAmount(null);

        InputValidationException exception = assertThrows(InputValidationException.class, 
            () -> inputValidationService.validateRequest(request));
        assertEquals("Requested amount is required", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenRequestedAmountIsZero() {
        LoanValidationRequest request = createValidRequest();
        request.setRequestedAmount(0.0);

        InputValidationException exception = assertThrows(InputValidationException.class, 
            () -> inputValidationService.validateRequest(request));
        assertEquals("Requested amount must be positive", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenRequestedAmountIsTooLow() {
        LoanValidationRequest request = createValidRequest();
        request.setRequestedAmount(50.0);

        InputValidationException exception = assertThrows(InputValidationException.class, 
            () -> inputValidationService.validateRequest(request));
        assertEquals("Requested amount too low: minimum is 100.00", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenRequestedAmountIsTooHigh() {
        LoanValidationRequest request = createValidRequest();
        request.setRequestedAmount(15_000_000.0);

        InputValidationException exception = assertThrows(InputValidationException.class, 
            () -> inputValidationService.validateRequest(request));
        assertEquals("Requested amount too high: maximum is 10000000.00", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenTermMonthsIsNull() {
        LoanValidationRequest request = createValidRequest();
        request.setTermMonths(null);

        InputValidationException exception = assertThrows(InputValidationException.class, 
            () -> inputValidationService.validateRequest(request));
        assertEquals("Term in months is required", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenTermMonthsIsTooLow() {
        LoanValidationRequest request = createValidRequest();
        request.setTermMonths(0);

        InputValidationException exception = assertThrows(InputValidationException.class, 
            () -> inputValidationService.validateRequest(request));
        assertEquals("Term must be between 1 and 36 months", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenTermMonthsIsTooHigh() {
        LoanValidationRequest request = createValidRequest();
        request.setTermMonths(50);

        InputValidationException exception = assertThrows(InputValidationException.class, 
            () -> inputValidationService.validateRequest(request));
        assertEquals("Term must be between 1 and 36 months", exception.getMessage());
    }

    @Test
    void shouldAcceptNullLastLoanDate() {
        LoanValidationRequest request = createValidRequest();
        request.setLastLoanDate(null);

        assertDoesNotThrow(() -> inputValidationService.validateRequest(request));
    }

    @Test
    void shouldThrowExceptionWhenLastLoanDateIsInTheFuture() {
        LoanValidationRequest request = createValidRequest();
        LocalDate futureDate = LocalDate.now().plusDays(1);
        request.setLastLoanDate(JsonNullable.of(futureDate));

        InputValidationException exception = assertThrows(InputValidationException.class, 
            () -> inputValidationService.validateRequest(request));
        assertEquals("Last loan date cannot be in the future", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenLoanToIncomeRatioIsTooHigh() {
        LoanValidationRequest request = createValidRequest();
        request.setMonthlySalary(1000.0); // Annual = 12,000
        request.setRequestedAmount(300000.0); // Ratio = 25, exceeds max of 20

        InputValidationException exception = assertThrows(InputValidationException.class, 
            () -> inputValidationService.validateRequest(request));
        assertTrue(exception.getMessage().contains("Loan amount too high relative to income"));
    }

    @Test
    void shouldThrowExceptionWhenDebtServiceRatioExceeds80Percent() {
        LoanValidationRequest request = createValidRequest();
        request.setMonthlySalary(1000.0);
        request.setRequestedAmount(24000.0); // Monthly payment = 24000/12 = 2000 (200% of salary)
        request.setTermMonths(12);

        InputValidationException exception = assertThrows(InputValidationException.class, 
            () -> inputValidationService.validateRequest(request));
        assertTrue(exception.getMessage().contains("Monthly payment"));
    }

    private LoanValidationRequest createValidRequest() {
        LoanValidationRequest request = new LoanValidationRequest();
        request.setMonthlySalary(5000.0);
        request.setRequestedAmount(50000.0);
        request.setTermMonths(12);
        request.setLastLoanDate(JsonNullable.of(LocalDate.now().minusYears(1)));
        return request;
    }
}