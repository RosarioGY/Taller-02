package com.techgirls.loanvalidation.web;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;

import com.techgirls.loanvalidation.exception.BusinessException;
import com.techgirls.loanvalidation.exception.ExternalServiceException;
import com.techgirls.loanvalidation.exception.InputValidationException;
import com.techgirls.loanvalidation.exception.LoanValidationException;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private static final String TRACE_ID_KEY = "traceId";
    private static final String TIMESTAMP_KEY = "timestamp";
    private static final String FIELD_ERRORS_KEY = "fieldErrors";

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private ServerWebExchange exchange;

    @BeforeEach
    void setUp() {
        when(exchange.getRequest()).thenReturn(MockServerRequest.builder().build().exchange().getRequest());
    }

    @Test
    void shouldHandleInputValidationException() {
        InputValidationException exception = new InputValidationException("Invalid input data");
        
        Mono<ResponseEntity<ProblemDetail>> result = globalExceptionHandler.handleInputValidation(exception, exchange);
        
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    ProblemDetail problemDetail = response.getBody();
                    assertThat(problemDetail).isNotNull();
                    assertThat(problemDetail.getStatus()).isEqualTo(400);
                    assertThat(problemDetail.getDetail()).contains("Invalid input data");
                    assertThat(problemDetail.getType()).isEqualTo(URI.create("https://api.loanvalidation.com/problems/input-validation"));
                    assertThat(problemDetail.getTitle()).isEqualTo("Input Validation Error");
                })
                .verifyComplete();
    }

    @Test
    void shouldHandleLoanValidationException() {
        LoanValidationException exception = new LoanValidationException("Loan validation failed");
        
        Mono<ResponseEntity<ProblemDetail>> result = globalExceptionHandler.handleLoanValidation(exception, exchange);
        
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
                    ProblemDetail problemDetail = response.getBody();
                    assertThat(problemDetail).isNotNull();
                    assertThat(problemDetail.getStatus()).isEqualTo(422);
                    assertThat(problemDetail.getDetail()).contains("Loan validation failed");
                    assertThat(problemDetail.getType()).isEqualTo(URI.create("https://api.loanvalidation.com/problems/loan-validation"));
                    assertThat(problemDetail.getTitle()).isEqualTo("Loan Validation Error");
                })
                .verifyComplete();
    }

    @Test
    void shouldHandleExternalServiceException() {
        ExternalServiceException exception = new ExternalServiceException("loanHistoryService", "Service is unavailable");
        
        Mono<ResponseEntity<ProblemDetail>> result = globalExceptionHandler.handleExternalService(exception, exchange);
        
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
                    ProblemDetail problemDetail = response.getBody();
                    assertThat(problemDetail).isNotNull();
                    assertThat(problemDetail.getStatus()).isEqualTo(502);
                    assertThat(problemDetail.getDetail()).contains("External service 'loanHistoryService' error: Service is unavailable");
                    assertThat(problemDetail.getType()).isEqualTo(URI.create("https://api.loanvalidation.com/problems/external-service"));
                    assertThat(problemDetail.getTitle()).isEqualTo("External Service Error");
                })
                .verifyComplete();
    }

    @Test
    void shouldHandleBusinessException() {
        BusinessException exception = new BusinessException("Business rule violation");
        
        Mono<ResponseEntity<ProblemDetail>> result = globalExceptionHandler.handleBusiness(exception, exchange);
        
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                    ProblemDetail problemDetail = response.getBody();
                    assertThat(problemDetail).isNotNull();
                    assertThat(problemDetail.getStatus()).isEqualTo(409);
                    assertThat(problemDetail.getDetail()).contains("Business rule violation");
                    assertThat(problemDetail.getType()).isEqualTo(URI.create("https://api.loanvalidation.com/problems/business-rule"));
                    assertThat(problemDetail.getTitle()).isEqualTo("Business Rule Violation");
                })
                .verifyComplete();
    }

    @Test
    void shouldHandleWebExchangeBindExceptionWithFieldErrors() {
        BindingResult bindingResult = mock(BindingResult.class);
        
        FieldError fieldError1 = new FieldError("loanRequest", "amount", "must be positive");
        FieldError fieldError2 = new FieldError("loanRequest", "applicantId", "must not be null");
        List<FieldError> fieldErrors = Arrays.asList(fieldError1, fieldError2);
        
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);
        
        WebExchangeBindException exception = new WebExchangeBindException(null, bindingResult);
        
        Mono<ResponseEntity<ProblemDetail>> result = globalExceptionHandler.handleBindException(exception, exchange);
        
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    ProblemDetail problemDetail = response.getBody();
                    assertThat(problemDetail).isNotNull();
                    assertThat(problemDetail.getStatus()).isEqualTo(400);
                    assertThat(problemDetail.getDetail()).contains("Invalid request format");
                    assertThat(problemDetail.getDetail()).contains("amount: must be positive");
                    assertThat(problemDetail.getDetail()).contains("applicantId: must not be null");
                    assertThat(problemDetail.getType()).isEqualTo(URI.create("https://api.loanvalidation.com/problems/request-binding"));
                    assertThat(problemDetail.getTitle()).isEqualTo("Request Binding Error");
                    
                    Map<String, Object> properties = problemDetail.getProperties();
                    if (properties != null) {
                        assertThat(properties.get(FIELD_ERRORS_KEY)).isEqualTo(fieldErrors);
                        assertThat(properties.get(TRACE_ID_KEY)).isNotNull();
                        assertThat(properties.get(TIMESTAMP_KEY)).isNotNull();
                    }
                })
                .verifyComplete();
    }

    @Test
    void shouldHandleWebExchangeBindExceptionWithMultipleFieldErrors() {
        BindingResult bindingResult = mock(BindingResult.class);
        
        FieldError fieldError1 = new FieldError("loanRequest", "amount", "must be between 1000 and 500000");
        FieldError fieldError2 = new FieldError("loanRequest", "term", "must be between 1 and 30");
        FieldError fieldError3 = new FieldError("loanRequest", "interestRate", "must be positive");
        List<FieldError> fieldErrors = Arrays.asList(fieldError1, fieldError2, fieldError3);
        
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);
        
        WebExchangeBindException exception = new WebExchangeBindException(null, bindingResult);
        
        Mono<ResponseEntity<ProblemDetail>> result = globalExceptionHandler.handleBindException(exception, exchange);
        
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    ProblemDetail problemDetail = response.getBody();
                    assertThat(problemDetail).isNotNull();
                    assertThat(problemDetail.getStatus()).isEqualTo(400);
                    assertThat(problemDetail.getDetail()).contains("Invalid request format");
                    assertThat(problemDetail.getDetail()).contains("amount: must be between 1000 and 500000");
                    assertThat(problemDetail.getDetail()).contains("term: must be between 1 and 30");
                    assertThat(problemDetail.getDetail()).contains("interestRate: must be positive");
                    assertThat(problemDetail.getType()).isEqualTo(URI.create("https://api.loanvalidation.com/problems/request-binding"));
                    assertThat(problemDetail.getTitle()).isEqualTo("Request Binding Error");
                })
                .verifyComplete();
    }

    @Test
    void shouldHandleWebExchangeBindExceptionWithEmptyFieldErrors() {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(Arrays.asList());
        
        WebExchangeBindException exception = new WebExchangeBindException(null, bindingResult);
        
        Mono<ResponseEntity<ProblemDetail>> result = globalExceptionHandler.handleBindException(exception, exchange);
        
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    ProblemDetail problemDetail = response.getBody();
                    assertThat(problemDetail).isNotNull();
                    assertThat(problemDetail.getStatus()).isEqualTo(400);
                    assertThat(problemDetail.getDetail()).contains("Invalid request format");
                    assertThat(problemDetail.getType()).isEqualTo(URI.create("https://api.loanvalidation.com/problems/request-binding"));
                    assertThat(problemDetail.getTitle()).isEqualTo("Request Binding Error");
                })
                .verifyComplete();
    }

    @Test
    void shouldHandleGeneralException() {
        Exception exception = new RuntimeException("Unexpected error occurred");
        
        Mono<ResponseEntity<ProblemDetail>> result = globalExceptionHandler.handleGeneral(exception, exchange);
        
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                    ProblemDetail problemDetail = response.getBody();
                    assertThat(problemDetail).isNotNull();
                    assertThat(problemDetail.getStatus()).isEqualTo(500);
                    assertThat(problemDetail.getDetail()).isEqualTo("An unexpected error occurred. Please try again later.");
                    assertThat(problemDetail.getType()).isEqualTo(URI.create("https://api.loanvalidation.com/problems/internal-server-error"));
                    assertThat(problemDetail.getTitle()).isEqualTo("Internal Server Error");
                })
                .verifyComplete();
    }
}