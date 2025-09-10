package com.nttdata.loanvalidation.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/** Response model matching the OpenAPI contract. */
@Data
@Builder
public class LoanValidationResponse {
    private boolean eligible;
    private List<Reason> reasons;
    private BigDecimal monthlyPayment;
    private LocalDate evaluatedAt;
}
