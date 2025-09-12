package com.techgirls.loanvalidation.controller;

import com.techgirls.loanvalidation.exception.ExternalServiceException;
import com.techgirls.loanvalidation.exception.InputValidationException;
import com.techgirls.loanvalidation.exception.LoanValidationException;
import com.techgirls.loanvalidation.model.LoanValidationRequest;
import com.techgirls.loanvalidation.model.LoanValidationResult;
import com.techgirls.loanvalidation.service.InputValidationService;
import com.techgirls.loanvalidation.service.LoanValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoanValidationController Tests")
class LoanValidationControllerTest {

    @Mock
    private LoanValidationService loanValidationService;

    @Mock
    private InputValidationService inputValidationService;

    @Mock
    private ServerWebExchange exchange;

    @Mock
    private ServerHttpRequest request;

    private LoanValidationController controller;

    @BeforeEach
    void setUp() {
        controller = new LoanValidationController(loanValidationService, inputValidationService);
        
        // Mock basic exchange behavior
        when(exchange.getRequest()).thenReturn(request);
        when(request.getId()).thenReturn("test-request-id");
    }

    @Nested
    @DisplayName("Successful Loan Validation Tests")
    class SuccessfulValidationTests {

        @Test
        @DisplayName("Should successfully validate eligible loan request")
        void shouldSuccessfullyValidateEligibleLoanRequest() {
            // Given
            LoanValidationRequest request = createValidLoanRequest(3000.0, 5000.0, 24, null);
            LoanValidationResult expectedResult = createEligibleResult(200.0);
            
            doNothing().when(inputValidationService).validateRequest(any(LoanValidationRequest.class));
            when(loanValidationService.evaluate(any(LoanValidationRequest.class)))
                    .thenReturn(Mono.just(expectedResult));

            // When
            Mono<ResponseEntity<LoanValidationResult>> result = controller.validateLoan(
                    Mono.just(request), exchange);

            // Then
            StepVerifier.create(result)
                    .assertNext(response -> {
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                        assertThat(response.getBody()).isNotNull();
                        assertThat(response.getBody().getEligible()).isTrue();
                        assertThat(response.getBody().getReasons()).isEmpty();
                        assertThat(response.getBody().getMonthlyPayment()).isEqualTo(200.0);
                    })
                    .verifyComplete();

            verify(inputValidationService).validateRequest(eq(request));
            verify(loanValidationService).evaluate(eq(request));
        }

        @Test
        @DisplayName("Should successfully validate ineligible loan request with reasons")
        void shouldSuccessfullyValidateIneligibleLoanRequestWithReasons() {
            // Given
            LoanValidationRequest request = createValidLoanRequest(1000.0, 10000.0, 48, LocalDate.now().minusMonths(1));
            List<LoanValidationResult.ReasonsEnum> reasons = Arrays.asList(
                    LoanValidationResult.ReasonsEnum.CAPACIDAD_INSUFICIENTE,
                    LoanValidationResult.ReasonsEnum.PLAZO_MAXIMO_SUPERADO,
                    LoanValidationResult.ReasonsEnum.HAS_RECENT_LOANS
            );
            LoanValidationResult expectedResult = createIneligibleResult(reasons, 400.0);
            
            doNothing().when(inputValidationService).validateRequest(any(LoanValidationRequest.class));
            when(loanValidationService.evaluate(any(LoanValidationRequest.class)))
                    .thenReturn(Mono.just(expectedResult));

            // When
            Mono<ResponseEntity<LoanValidationResult>> result = controller.validateLoan(
                    Mono.just(request), exchange);

            // Then
            StepVerifier.create(result)
                    .assertNext(response -> {
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                        assertThat(response.getBody()).isNotNull();
                        assertThat(response.getBody().getEligible()).isFalse();
                        assertThat(response.getBody().getReasons()).hasSize(3);
                        assertThat(response.getBody().getReasons()).containsExactlyInAnyOrder(
                                LoanValidationResult.ReasonsEnum.CAPACIDAD_INSUFICIENTE,
                                LoanValidationResult.ReasonsEnum.PLAZO_MAXIMO_SUPERADO,
                                LoanValidationResult.ReasonsEnum.HAS_RECENT_LOANS
                        );
                        assertThat(response.getBody().getMonthlyPayment()).isEqualTo(400.0);
                    })
                    .verifyComplete();

            verify(inputValidationService).validateRequest(eq(request));
            verify(loanValidationService).evaluate(eq(request));
        }

        @Test
        @DisplayName("Should handle loan request without lastLoanDate")
        void shouldHandleLoanRequestWithoutLastLoanDate() {
            // Given
            LoanValidationRequest request = createValidLoanRequest(2500.0, 4000.0, 18, null);
            LoanValidationResult expectedResult = createEligibleResult(250.5);
            
            doNothing().when(inputValidationService).validateRequest(any(LoanValidationRequest.class));
            when(loanValidationService.evaluate(any(LoanValidationRequest.class)))
                    .thenReturn(Mono.just(expectedResult));

            // When
            Mono<ResponseEntity<LoanValidationResult>> result = controller.validateLoan(
                    Mono.just(request), exchange);

            // Then
            StepVerifier.create(result)
                    .assertNext(response -> {
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                        assertThat(response.getBody()).isNotNull();
                        assertThat(response.getBody().getEligible()).isTrue();
                        assertThat(response.getBody().getMonthlyPayment()).isEqualTo(250.5);
                    })
                    .verifyComplete();

            verify(inputValidationService).validateRequest(eq(request));
            verify(loanValidationService).evaluate(eq(request));
        }
    }

    @Nested
    @DisplayName("Input Validation Error Tests")
    class InputValidationErrorTests {

        @Test
        @DisplayName("Should propagate InputValidationException")
        void shouldPropagateInputValidationException() {
            // Given
            LoanValidationRequest request = createInvalidLoanRequest(-1000.0, 0.0, 50);
            InputValidationException expectedException = new InputValidationException("Invalid monthly salary");
            
            doThrow(expectedException).when(inputValidationService).validateRequest(any(LoanValidationRequest.class));

            // When
            Mono<ResponseEntity<LoanValidationResult>> result = controller.validateLoan(
                    Mono.just(request), exchange);

            // Then
            StepVerifier.create(result)
                    .expectError(InputValidationException.class)
                    .verify();

            verify(inputValidationService).validateRequest(eq(request));
            verify(loanValidationService, never()).evaluate(any());
        }

        @Test
        @DisplayName("Should handle empty request stream")
        void shouldHandleEmptyRequestStream() {
            // When
            Mono<ResponseEntity<LoanValidationResult>> result = controller.validateLoan(
                    Mono.empty(), exchange);

            // Then
            StepVerifier.create(result)
                    .verifyComplete();

            verify(inputValidationService, never()).validateRequest(any());
            verify(loanValidationService, never()).evaluate(any());
        }
    }

    @Nested
    @DisplayName("Service Error Tests")
    class ServiceErrorTests {

        @Test
        @DisplayName("Should propagate LoanValidationException from service")
        void shouldPropagateLoanValidationExceptionFromService() {
            // Given
            LoanValidationRequest request = createValidLoanRequest(3000.0, 5000.0, 24, null);
            LoanValidationException expectedException = new LoanValidationException("Business rule validation failed");
            
            doNothing().when(inputValidationService).validateRequest(any(LoanValidationRequest.class));
            when(loanValidationService.evaluate(any(LoanValidationRequest.class)))
                    .thenReturn(Mono.error(expectedException));

            // When
            Mono<ResponseEntity<LoanValidationResult>> result = controller.validateLoan(
                    Mono.just(request), exchange);

            // Then
            StepVerifier.create(result)
                    .expectError(LoanValidationException.class)
                    .verify();

            verify(inputValidationService).validateRequest(eq(request));
            verify(loanValidationService).evaluate(eq(request));
        }

        @Test
        @DisplayName("Should propagate ExternalServiceException from service")
        void shouldPropagateExternalServiceExceptionFromService() {
            // Given
            LoanValidationRequest request = createValidLoanRequest(3000.0, 5000.0, 24, null);
            ExternalServiceException expectedException = new ExternalServiceException(
                    "LoanHistoryService", "External service unavailable");
            
            doNothing().when(inputValidationService).validateRequest(any(LoanValidationRequest.class));
            when(loanValidationService.evaluate(any(LoanValidationRequest.class)))
                    .thenReturn(Mono.error(expectedException));

            // When
            Mono<ResponseEntity<LoanValidationResult>> result = controller.validateLoan(
                    Mono.just(request), exchange);

            // Then
            StepVerifier.create(result)
                    .expectError(ExternalServiceException.class)
                    .verify();

            verify(inputValidationService).validateRequest(eq(request));
            verify(loanValidationService).evaluate(eq(request));
        }

        @Test
        @DisplayName("Should handle generic RuntimeException from service")
        void shouldHandleGenericRuntimeExceptionFromService() {
            // Given
            LoanValidationRequest request = createValidLoanRequest(3000.0, 5000.0, 24, null);
            RuntimeException expectedException = new RuntimeException("Unexpected error");
            
            doNothing().when(inputValidationService).validateRequest(any(LoanValidationRequest.class));
            when(loanValidationService.evaluate(any(LoanValidationRequest.class)))
                    .thenReturn(Mono.error(expectedException));

            // When
            Mono<ResponseEntity<LoanValidationResult>> result = controller.validateLoan(
                    Mono.just(request), exchange);

            // Then
            StepVerifier.create(result)
                    .expectError(RuntimeException.class)
                    .verify();

            verify(inputValidationService).validateRequest(eq(request));
            verify(loanValidationService).evaluate(eq(request));
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle minimum valid values")
        void shouldHandleMinimumValidValues() {
            // Given
            LoanValidationRequest request = createValidLoanRequest(0.01, 0.01, 1, null);
            LoanValidationResult expectedResult = createEligibleResult(0.01);
            
            doNothing().when(inputValidationService).validateRequest(any(LoanValidationRequest.class));
            when(loanValidationService.evaluate(any(LoanValidationRequest.class)))
                    .thenReturn(Mono.just(expectedResult));

            // When
            Mono<ResponseEntity<LoanValidationResult>> result = controller.validateLoan(
                    Mono.just(request), exchange);

            // Then
            StepVerifier.create(result)
                    .assertNext(response -> {
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                        assertThat(response.getBody()).isNotNull();
                        assertThat(response.getBody().getEligible()).isTrue();
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should handle maximum valid values")
        void shouldHandleMaximumValidValues() {
            // Given
            LoanValidationRequest request = createValidLoanRequest(999999.99, 999999.99, 36, null);
            LoanValidationResult expectedResult = createEligibleResult(50000.0);
            
            doNothing().when(inputValidationService).validateRequest(any(LoanValidationRequest.class));
            when(loanValidationService.evaluate(any(LoanValidationRequest.class)))
                    .thenReturn(Mono.just(expectedResult));

            // When
            Mono<ResponseEntity<LoanValidationResult>> result = controller.validateLoan(
                    Mono.just(request), exchange);

            // Then
            StepVerifier.create(result)
                    .assertNext(response -> {
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                        assertThat(response.getBody()).isNotNull();
                        assertThat(response.getBody().getEligible()).isTrue();
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should handle recent loan date")
        void shouldHandleRecentLoanDate() {
            // Given
            LocalDate recentDate = LocalDate.now().minusWeeks(2);
            LoanValidationRequest request = createValidLoanRequest(3000.0, 5000.0, 24, recentDate);
            List<LoanValidationResult.ReasonsEnum> reasons = Arrays.asList(
                    LoanValidationResult.ReasonsEnum.HAS_RECENT_LOANS
            );
            LoanValidationResult expectedResult = createIneligibleResult(reasons, 250.0);
            
            doNothing().when(inputValidationService).validateRequest(any(LoanValidationRequest.class));
            when(loanValidationService.evaluate(any(LoanValidationRequest.class)))
                    .thenReturn(Mono.just(expectedResult));

            // When
            Mono<ResponseEntity<LoanValidationResult>> result = controller.validateLoan(
                    Mono.just(request), exchange);

            // Then
            StepVerifier.create(result)
                    .assertNext(response -> {
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                        assertThat(response.getBody()).isNotNull();
                        assertThat(response.getBody().getEligible()).isFalse();
                        assertThat(response.getBody().getReasons()).contains(
                                LoanValidationResult.ReasonsEnum.HAS_RECENT_LOANS);
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should handle old loan date")
        void shouldHandleOldLoanDate() {
            // Given
            LocalDate oldDate = LocalDate.now().minusYears(1);
            LoanValidationRequest request = createValidLoanRequest(3000.0, 5000.0, 24, oldDate);
            LoanValidationResult expectedResult = createEligibleResult(250.0);
            
            doNothing().when(inputValidationService).validateRequest(any(LoanValidationRequest.class));
            when(loanValidationService.evaluate(any(LoanValidationRequest.class)))
                    .thenReturn(Mono.just(expectedResult));

            // When
            Mono<ResponseEntity<LoanValidationResult>> result = controller.validateLoan(
                    Mono.just(request), exchange);

            // Then
            StepVerifier.create(result)
                    .assertNext(response -> {
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                        assertThat(response.getBody()).isNotNull();
                        assertThat(response.getBody().getEligible()).isTrue();
                        assertThat(response.getBody().getReasons()).isEmpty();
                    })
                    .verifyComplete();
        }
    }

    // Helper methods for creating test data
    
    private LoanValidationRequest createValidLoanRequest(Double monthlySalary, Double requestedAmount, 
                                                        Integer termMonths, LocalDate lastLoanDate) {
        LoanValidationRequest request = new LoanValidationRequest();
        request.setMonthlySalary(monthlySalary);
        request.setRequestedAmount(requestedAmount);
        request.setTermMonths(termMonths);
        if (lastLoanDate != null) {
            request.setLastLoanDate(JsonNullable.of(lastLoanDate));
        } else {
            request.setLastLoanDate(JsonNullable.undefined());
        }
        return request;
    }
    
    private LoanValidationRequest createInvalidLoanRequest(Double monthlySalary, Double requestedAmount, 
                                                          Integer termMonths) {
        LoanValidationRequest request = new LoanValidationRequest();
        request.setMonthlySalary(monthlySalary);
        request.setRequestedAmount(requestedAmount);
        request.setTermMonths(termMonths);
        request.setLastLoanDate(JsonNullable.undefined());
        return request;
    }
    
    private LoanValidationResult createEligibleResult(Double monthlyPayment) {
        return new LoanValidationResult(true, new ArrayList<>(), monthlyPayment);
    }
    
    private LoanValidationResult createIneligibleResult(List<LoanValidationResult.ReasonsEnum> reasons, 
                                                       Double monthlyPayment) {
        return new LoanValidationResult(false, reasons, monthlyPayment);
    }
}