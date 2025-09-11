package com.techgirls.loanvalidation.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LoanValidationResult {
    private Boolean eligible;
    private List<String> reasons;
    private Double monthlyPayment;
}
