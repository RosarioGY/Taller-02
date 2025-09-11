package com.techgirls.loanvalidation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanValidationResponse {
  private boolean eligible;
  private double monthlyPayment;
  private List<String> reasons;
}
