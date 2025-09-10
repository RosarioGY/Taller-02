package com.nttdata.loanvalidation.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

/** Request model matching the OpenAPI contract. */
@Data
@Builder
public class LoanValidationRequest {
    @NotBlank
    private String applicantId; // UUID recommended

    @NotNull
    private BigDecimal monthlySalary;

    @NotNull
    private BigDecimal requestedAmount;

    @NotNull
    private Integer termMonths;
}
