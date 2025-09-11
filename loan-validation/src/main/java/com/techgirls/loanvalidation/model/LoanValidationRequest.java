package com.techgirls.loanvalidation.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanValidationRequest {

    @NotNull
    private BigDecimal monthlySalary;

    @NotNull
    private BigDecimal requestedAmount;

    @NotNull
    private Integer termMonths;

    /** Opcional; si lo envías, usa formato ISO yyyy-MM-dd */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate lastLoanDate;
}
