package com.techgirls.loanvalidation.web;

import com.techgirls.loanvalidation.exception.BusinessException;
import com.techgirls.loanvalidation.exception.ExternalServiceException;
import com.techgirls.loanvalidation.exception.InputValidationException;
import com.techgirls.loanvalidation.exception.LoanValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

/**
 * Global exception handler for the loan validation service.
 * Implements RFC 7807 Problem Details for HTTP APIs.
 * Provides consistent error responses and structured logging.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(InputValidationException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleInputValidation(
            InputValidationException ex, ServerWebExchange exchange) {
        
        String traceId = generateTraceId();
        logError(traceId, "Input validation error", ex, exchange);
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, ex.getUserMessage());
        problemDetail.setType(URI.create("https://api.loanvalidation.com/problems/input-validation"));
        problemDetail.setTitle("Input Validation Error");
        problemDetail.setProperty("errorCode", ex.getErrorCode());
        problemDetail.setProperty("traceId", traceId);
        problemDetail.setProperty("timestamp", Instant.now());
        
        return Mono.just(ResponseEntity.badRequest().body(problemDetail));
    }

    @ExceptionHandler(LoanValidationException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleLoanValidation(
            LoanValidationException ex, ServerWebExchange exchange) {
        
        String traceId = generateTraceId();
        logError(traceId, "Loan validation error", ex, exchange);
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.UNPROCESSABLE_ENTITY, ex.getUserMessage());
        problemDetail.setType(URI.create("https://api.loanvalidation.com/problems/loan-validation"));
        problemDetail.setTitle("Loan Validation Error");
        problemDetail.setProperty("errorCode", ex.getErrorCode());
        problemDetail.setProperty("traceId", traceId);
        problemDetail.setProperty("timestamp", Instant.now());
        
        return Mono.just(ResponseEntity.unprocessableEntity().body(problemDetail));
    }

    @ExceptionHandler(ExternalServiceException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleExternalService(
            ExternalServiceException ex, ServerWebExchange exchange) {
        
        String traceId = generateTraceId();
        logError(traceId, "External service error", ex, exchange);
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.SERVICE_UNAVAILABLE, "External service temporarily unavailable");
        problemDetail.setType(URI.create("https://api.loanvalidation.com/problems/external-service"));
        problemDetail.setTitle("External Service Error");
        problemDetail.setProperty("errorCode", ex.getErrorCode());
        problemDetail.setProperty("serviceName", ex.getServiceName());
        problemDetail.setProperty("traceId", traceId);
        problemDetail.setProperty("timestamp", Instant.now());
        
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(problemDetail));
    }

    @ExceptionHandler(BusinessException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleBusiness(
            BusinessException ex, ServerWebExchange exchange) {
        
        String traceId = generateTraceId();
        logError(traceId, "Business error", ex, exchange);
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, ex.getUserMessage());
        problemDetail.setType(URI.create("https://api.loanvalidation.com/problems/business"));
        problemDetail.setTitle("Business Error");
        problemDetail.setProperty("errorCode", ex.getErrorCode());
        problemDetail.setProperty("traceId", traceId);
        problemDetail.setProperty("timestamp", Instant.now());
        
        return Mono.just(ResponseEntity.badRequest().body(problemDetail));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleBindException(
            WebExchangeBindException ex, ServerWebExchange exchange) {
        
        String traceId = generateTraceId();
        logError(traceId, "Request binding error", ex, exchange);
        
        StringBuilder errors = new StringBuilder();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.append(error.getField()).append(": ").append(error.getDefaultMessage()).append("; "));
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, "Invalid request format: " + errors.toString());
        problemDetail.setType(URI.create("https://api.loanvalidation.com/problems/request-binding"));
        problemDetail.setTitle("Request Binding Error");
        problemDetail.setProperty("traceId", traceId);
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("fieldErrors", ex.getBindingResult().getFieldErrors());
        
        return Mono.just(ResponseEntity.badRequest().body(problemDetail));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ProblemDetail>> handleGeneral(
            Exception ex, ServerWebExchange exchange) {
        
        String traceId = generateTraceId();
        logError(traceId, "Unexpected error", ex, exchange);
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        problemDetail.setType(URI.create("https://api.loanvalidation.com/problems/internal"));
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setProperty("traceId", traceId);
        problemDetail.setProperty("timestamp", Instant.now());
        
        return Mono.just(ResponseEntity.internalServerError().body(problemDetail));
    }

    private String generateTraceId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private void logError(String traceId, String errorType, Exception ex, ServerWebExchange exchange) {
        String requestPath = exchange.getRequest().getPath().toString();
        String method = exchange.getRequest().getMethod().toString();
        
        log.error("Error [{}] - {} {} - {}: {}", 
                traceId, method, requestPath, errorType, ex.getMessage(), ex);
    }
}