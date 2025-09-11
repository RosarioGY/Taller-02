package com.techgirls.loanvalidation.model;

import lombok.Builder;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/** Request model aligned with OpenAPI contract. */
@Data
@Builder
public class LoanValidationRequest {

    @NotNull
    private BigDecimal monthlySalary;

    @NotNull
    private BigDecimal requestedAmount;

    @NotNull
    private Integer termMonths;

    /** Optional; format: YYYY-MM-DD */
    private String lastLoanDate;
}
