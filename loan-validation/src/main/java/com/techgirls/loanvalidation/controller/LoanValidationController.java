package com.techgirls.loanvalidation.controller;

import com.techgirls.loanvalidation.api.DefaultApi;
import com.techgirls.loanvalidation.model.LoanValidationRequest;
import com.techgirls.loanvalidation.model.LoanValidationResult;
import com.techgirls.loanvalidation.service.InputValidationService;
import com.techgirls.loanvalidation.service.LoanValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Unified controller implementing the generated OpenAPI interface.
 * Uses only OpenAPI generated models for complete contract compliance.
 * Enhanced with comprehensive input validation and error handling.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class LoanValidationController implements DefaultApi {
    
    private final LoanValidationService loanValidationService;
    private final InputValidationService inputValidationService;

    @Override
    public Mono<ResponseEntity<LoanValidationResult>> validateLoan(
            Mono<LoanValidationRequest> loanValidationRequest, 
            ServerWebExchange exchange) {
        
        String requestId = exchange.getRequest().getId();
        log.info("Processing loan validation request: {}", requestId);
        
        return loanValidationRequest
                .doOnNext(request -> {
                    log.debug("Validating request: monthlySalary={}, requestedAmount={}, termMonths={}", 
                             request.getMonthlySalary(), request.getRequestedAmount(), request.getTermMonths());
                    // Perform comprehensive input validation
                    inputValidationService.validateRequest(request);
                })
                .flatMap(loanValidationService::evaluate)
                .map(result -> {
                    log.info("Loan validation completed for request {}: eligible={}", requestId, result.getEligible());
                    return ResponseEntity.ok(result);
                })
                .doOnError(error -> log.error("Error processing loan validation request {}: {}", requestId, error.getMessage()));
    }
}