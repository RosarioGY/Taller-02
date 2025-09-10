package com.nttdata.loanvalidation.controller;

import com.nttdata.loanvalidation.model.LoanValidationRequest;
import com.nttdata.loanvalidation.model.LoanValidationResponse;
import com.nttdata.loanvalidation.service.EligibilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/** Exposes /loan-validations as per OpenAPI. */
@RestController
@RequestMapping(path = "/loan-validations", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
@RequiredArgsConstructor
public class LoanEligibilityController {
    private final EligibilityService eligibilityService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<LoanValidationResponse>> validate(
            @Valid @RequestBody Mono<LoanValidationRequest> requestMono) {
        return requestMono
            .flatMap(eligibilityService::evaluate)
            .map(ResponseEntity::ok);
    }
}
