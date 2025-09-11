package com.techgirls.loanvalidation.controller;

import com.techgirls.loanvalidation.model.LoanValidationRequest;
import com.techgirls.loanvalidation.model.LoanValidationResponse;
import com.techgirls.loanvalidation.service.LoanEligibilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/loan-eligibility")
@RequiredArgsConstructor
public class LoanEligibilityController {
  private final LoanEligibilityService service;

  @PostMapping
  public Mono<LoanValidationResponse> validate(@Valid @RequestBody LoanValidationRequest req) {
    return service.validate(req);
  }
}
