package com.techgirls.loanvalidation.service;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.techgirls.loanvalidation.model.LoanValidationRequest;
import com.techgirls.loanvalidation.model.LoanValidationResult;
import com.techgirls.loanvalidation.service.validation.LoanValidationOrchestrator;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Unit tests for LoanValidationService.
 * Tests the facade service that delegates to the orchestrator.
 */
@ExtendWith(MockitoExtension.class)
class LoanValidationServiceTest {

    @Mock
    private LoanValidationOrchestrator validationOrchestrator;

    @InjectMocks
    private LoanValidationService loanValidationService;

    private LoanValidationRequest request;

    @BeforeEach
    void setUp() {
        request = new LoanValidationRequest();
        request.setRequestedAmount(50000.0);
        request.setTermMonths(24);
        request.setMonthlySalary(8000.0);
    }

    @Test
    void shouldDelegateToOrchestratorSuccessfully() {
        // Given
        LoanValidationResult expectedResult = new LoanValidationResult(true, Collections.emptyList(), 2500.0);
        when(validationOrchestrator.evaluate(any(LoanValidationRequest.class)))
            .thenReturn(Mono.just(expectedResult));

        // When
        Mono<LoanValidationResult> result = loanValidationService.evaluate(request);

        // Then
        StepVerifier.create(result)
            .expectNext(expectedResult)
            .verifyComplete();

        verify(validationOrchestrator).evaluate(request);
    }

    @Test
    void shouldHandleOrchestratorError() {
        // Given
        RuntimeException expectedError = new RuntimeException("Orchestrator error");
        when(validationOrchestrator.evaluate(any(LoanValidationRequest.class)))
            .thenReturn(Mono.error(expectedError));

        // When
        Mono<LoanValidationResult> result = loanValidationService.evaluate(request);

        // Then
        StepVerifier.create(result)
            .expectError(RuntimeException.class)
            .verify();

        verify(validationOrchestrator).evaluate(request);
    }

    @Test
    void shouldPassRequestToOrchestrator() {
        // Given
        LoanValidationResult expectedResult = new LoanValidationResult(false, 
            Collections.singletonList(LoanValidationResult.ReasonsEnum.CAPACIDAD_INSUFICIENTE), 2500.0);
        when(validationOrchestrator.evaluate(request))
            .thenReturn(Mono.just(expectedResult));

        // When
        Mono<LoanValidationResult> result = loanValidationService.evaluate(request);

        // Then
        StepVerifier.create(result)
            .expectNext(expectedResult)
            .verifyComplete();

        verify(validationOrchestrator, times(1)).evaluate(request);
    }

    @Test
    void shouldHandleNullRequest() {
        // Given
        when(validationOrchestrator.evaluate(null))
            .thenReturn(Mono.error(new IllegalArgumentException("Request cannot be null")));

        // When
        Mono<LoanValidationResult> result = loanValidationService.evaluate(null);

        // Then
        StepVerifier.create(result)
            .expectError(IllegalArgumentException.class)
            .verify();
    }

    @Test
    void shouldLogSuccessfulValidation() {
        // Given
        LoanValidationResult expectedResult = new LoanValidationResult(true, Collections.emptyList(), 2500.0);
        when(validationOrchestrator.evaluate(any(LoanValidationRequest.class)))
            .thenReturn(Mono.just(expectedResult));

        // When
        Mono<LoanValidationResult> result = loanValidationService.evaluate(request);

        // Then
        StepVerifier.create(result)
            .expectNext(expectedResult)
            .verifyComplete();
        
        // Verify that orchestrator was called
        verify(validationOrchestrator).evaluate(request);
    }

    @Test
    void shouldLogFailedValidation() {
        // Given
        Exception error = new RuntimeException("Validation failed");
        when(validationOrchestrator.evaluate(any(LoanValidationRequest.class)))
            .thenReturn(Mono.error(error));

        // When
        Mono<LoanValidationResult> result = loanValidationService.evaluate(request);

        // Then
        StepVerifier.create(result)
            .expectError(RuntimeException.class)
            .verify();
        
        verify(validationOrchestrator).evaluate(request);
    }
}