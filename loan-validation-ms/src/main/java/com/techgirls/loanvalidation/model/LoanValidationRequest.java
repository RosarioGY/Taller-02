package com.techgirls.loanvalidation.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanValidationRequest {
  @NotNull @Min(1)
  private Double monthlySalary;

  @NotNull @Min(1)
  private Double requestedAmount;

  @NotNull @Min(1)
  private Integer termMonths;

  /** Regla R1: antigüedad laboral en meses. Debe ser >= 3. */
  @NotNull @Min(0)
  private Integer employmentMonths;
}
