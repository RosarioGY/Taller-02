package com.nttdata.loanvalidation.controller;

import com.nttdata.loanvalidation.model.LoanValidationRequest;
import com.nttdata.loanvalidation.model.LoanValidationResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/loan-eligibility")
public class LoanEligibilityController {
    @PostMapping
    public LoanValidationResponse validateLoan(@RequestBody LoanValidationRequest request) {
        // Lógica de validación simulada
        return new LoanValidationResponse(true, null);
    }
}

