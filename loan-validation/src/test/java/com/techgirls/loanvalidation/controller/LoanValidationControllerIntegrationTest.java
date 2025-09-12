package com.techgirls.loanvalidation.controller;

import com.techgirls.loanvalidation.model.LoanValidationRequest;
import com.techgirls.loanvalidation.model.LoanValidationResult;
import com.techgirls.loanvalidation.service.InputValidationService;
import com.techgirls.loanvalidation.service.LoanValidationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@WebFluxTest(LoanValidationController.class)
@DisplayName("LoanValidationController Integration Tests")
class LoanValidationControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private LoanValidationService loanValidationService;

    @MockBean
    private InputValidationService inputValidationService;

    @Nested
    @DisplayName("POST /loan-validations")
    class ValidateLoanEndpointTests {

        @Test
        @DisplayName("Should return 200 OK with eligible result for valid request")
        void shouldReturn200OkWithEligibleResult() {
            // Given
            LoanValidationRequest request = createValidRequest();
            LoanValidationResult mockResult = new LoanValidationResult(true, Collections.emptyList(), 250.0);

            doNothing().when(inputValidationService).validateRequest(any(LoanValidationRequest.class));
            when(loanValidationService.evaluate(any(LoanValidationRequest.class)))
                    .thenReturn(Mono.just(mockResult));

            // When & Then
            webTestClient.post()
                    .uri("/loan-validations")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody(LoanValidationResult.class)
                    .value(result -> {
                        assert result.getEligible().equals(true);
                        assert result.getReasons().isEmpty();
                        assert result.getMonthlyPayment().equals(250.0);
                    });
        }

        @Test
        @DisplayName("Should return 200 OK with ineligible result and reasons")
        void shouldReturn200OkWithIneligibleResult() {
            // Given
            LoanValidationRequest request = createValidRequest();
            LoanValidationResult mockResult = new LoanValidationResult(
                    false,
                    Arrays.asList(
                            LoanValidationResult.ReasonsEnum.CAPACIDAD_INSUFICIENTE,
                            LoanValidationResult.ReasonsEnum.HAS_RECENT_LOANS
                    ),
                    400.0
            );

            doNothing().when(inputValidationService).validateRequest(any(LoanValidationRequest.class));
            when(loanValidationService.evaluate(any(LoanValidationRequest.class)))
                    .thenReturn(Mono.just(mockResult));

            // When & Then
            webTestClient.post()
                    .uri("/loan-validations")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody(LoanValidationResult.class)
                    .value(result -> {
                        assert result.getEligible().equals(false);
                        assert result.getReasons().size() == 2;
                        assert result.getReasons().contains(LoanValidationResult.ReasonsEnum.CAPACIDAD_INSUFICIENTE);
                        assert result.getReasons().contains(LoanValidationResult.ReasonsEnum.HAS_RECENT_LOANS);
                        assert result.getMonthlyPayment().equals(400.0);
                    });
        }

        @Test
        @DisplayName("Should return 400 Bad Request for invalid JSON")
        void shouldReturn400BadRequestForInvalidJson() {
            // Given
            String invalidJson = "{ invalid json }";

            // When & Then
            webTestClient.post()
                    .uri("/loan-validations")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(invalidJson)
                    .exchange()
                    .expectStatus().isBadRequest();
        }

        @Test
        @DisplayName("Should return 415 Unsupported Media Type for wrong content type")
        void shouldReturn415UnsupportedMediaType() {
            // Given
            LoanValidationRequest request = createValidRequest();

            // When & Then
            webTestClient.post()
                    .uri("/loan-validations")
                    .contentType(MediaType.TEXT_PLAIN)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isEqualTo(415);
        }

        @Test
        @DisplayName("Should handle request with minimum valid values")
        void shouldHandleMinimumValidValues() {
            // Given
            LoanValidationRequest request = new LoanValidationRequest();
            request.setMonthlySalary(0.01);
            request.setRequestedAmount(0.01);
            request.setTermMonths(1);
            request.setLastLoanDate(JsonNullable.undefined());

            LoanValidationResult mockResult = new LoanValidationResult(true, Collections.emptyList(), 0.01);

            doNothing().when(inputValidationService).validateRequest(any(LoanValidationRequest.class));
            when(loanValidationService.evaluate(any(LoanValidationRequest.class)))
                    .thenReturn(Mono.just(mockResult));

            // When & Then
            webTestClient.post()
                    .uri("/loan-validations")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(LoanValidationResult.class)
                    .value(result -> {
                        assert result.getEligible().equals(true);
                        assert result.getMonthlyPayment().equals(0.01);
                    });
        }

        @Test
        @DisplayName("Should handle request with maximum valid values")
        void shouldHandleMaximumValidValues() {
            // Given
            LoanValidationRequest request = new LoanValidationRequest();
            request.setMonthlySalary(999999.99);
            request.setRequestedAmount(999999.99);
            request.setTermMonths(36);
            request.setLastLoanDate(JsonNullable.undefined());

            LoanValidationResult mockResult = new LoanValidationResult(true, Collections.emptyList(), 50000.0);

            doNothing().when(inputValidationService).validateRequest(any(LoanValidationRequest.class));
            when(loanValidationService.evaluate(any(LoanValidationRequest.class)))
                    .thenReturn(Mono.just(mockResult));

            // When & Then
            webTestClient.post()
                    .uri("/loan-validations")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(LoanValidationResult.class)
                    .value(result -> {
                        assert result.getEligible().equals(true);
                        assert result.getMonthlyPayment().equals(50000.0);
                    });
        }

        @Test
        @DisplayName("Should handle request with lastLoanDate")
        void shouldHandleRequestWithLastLoanDate() {
            // Given
            LoanValidationRequest request = new LoanValidationRequest();
            request.setMonthlySalary(3000.0);
            request.setRequestedAmount(5000.0);
            request.setTermMonths(24);
            request.setLastLoanDate(JsonNullable.of(LocalDate.now().minusMonths(6)));

            LoanValidationResult mockResult = new LoanValidationResult(true, Collections.emptyList(), 250.0);

            doNothing().when(inputValidationService).validateRequest(any(LoanValidationRequest.class));
            when(loanValidationService.evaluate(any(LoanValidationRequest.class)))
                    .thenReturn(Mono.just(mockResult));

            // When & Then
            webTestClient.post()
                    .uri("/loan-validations")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(LoanValidationResult.class)
                    .value(result -> {
                        assert result.getEligible().equals(true);
                        assert result.getMonthlyPayment().equals(250.0);
                    });
        }

        @Test
        @DisplayName("Should return 404 Not Found for invalid endpoints")
        void shouldReturn404NotFoundForInvalidEndpoints() {
            // When & Then
            webTestClient.post()
                    .uri("/invalid-endpoint")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(createValidRequest())
                    .exchange()
                    .expectStatus().isNotFound();
        }

        @Test
        @DisplayName("Should return 405 Method Not Allowed for GET request")
        void shouldReturn405MethodNotAllowedForGetRequest() {
            // When & Then
            webTestClient.get()
                    .uri("/loan-validations")
                    .exchange()
                    .expectStatus().isEqualTo(405);
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle service errors gracefully")
        void shouldHandleServiceErrorsGracefully() {
            // Given
            LoanValidationRequest request = createValidRequest();

            doNothing().when(inputValidationService).validateRequest(any(LoanValidationRequest.class));
            when(loanValidationService.evaluate(any(LoanValidationRequest.class)))
                    .thenReturn(Mono.error(new RuntimeException("Service error")));

            // When & Then
            webTestClient.post()
                    .uri("/loan-validations")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().is5xxServerError();
        }

        @Test
        @DisplayName("Should handle validation errors gracefully")  
        void shouldHandleValidationErrorsGracefully() {
            // Given
            LoanValidationRequest request = createValidRequest();

            doThrow(new RuntimeException("Validation error"))
                    .when(inputValidationService).validateRequest(any(LoanValidationRequest.class));

            // When & Then
            webTestClient.post()
                    .uri("/loan-validations")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().is5xxServerError();
        }
    }

    // Helper methods
    private LoanValidationRequest createValidRequest() {
        LoanValidationRequest request = new LoanValidationRequest();
        request.setMonthlySalary(3000.0);
        request.setRequestedAmount(5000.0);
        request.setTermMonths(24);
        request.setLastLoanDate(JsonNullable.undefined());
        return request;
    }
}