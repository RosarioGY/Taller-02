package org.openapitools.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.time.LocalDate;
import java.util.Arrays;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.format.annotation.DateTimeFormat;
import java.util.NoSuchElementException;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * LoanValidationRequest
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-09-11T02:17:50.625965500-05:00[America/Lima]", comments = "Generator version: 7.7.0")
public class LoanValidationRequest {

  private Double monthlySalary;

  private Double requestedAmount;

  private Integer termMonths;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private JsonNullable<LocalDate> lastLoanDate = JsonNullable.<LocalDate>undefined();

  public LoanValidationRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public LoanValidationRequest(Double monthlySalary, Double requestedAmount, Integer termMonths) {
    this.monthlySalary = monthlySalary;
    this.requestedAmount = requestedAmount;
    this.termMonths = termMonths;
  }

  public LoanValidationRequest monthlySalary(Double monthlySalary) {
    this.monthlySalary = monthlySalary;
    return this;
  }

  /**
   * Get monthlySalary
   * minimum: 0.01
   * @return monthlySalary
   */
  @NotNull @DecimalMin("0.01") 
  @Schema(name = "monthlySalary", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("monthlySalary")
  public Double getMonthlySalary() {
    return monthlySalary;
  }

  public void setMonthlySalary(Double monthlySalary) {
    this.monthlySalary = monthlySalary;
  }

  public LoanValidationRequest requestedAmount(Double requestedAmount) {
    this.requestedAmount = requestedAmount;
    return this;
  }

  /**
   * Get requestedAmount
   * minimum: 0.01
   * @return requestedAmount
   */
  @NotNull @DecimalMin("0.01") 
  @Schema(name = "requestedAmount", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("requestedAmount")
  public Double getRequestedAmount() {
    return requestedAmount;
  }

  public void setRequestedAmount(Double requestedAmount) {
    this.requestedAmount = requestedAmount;
  }

  public LoanValidationRequest termMonths(Integer termMonths) {
    this.termMonths = termMonths;
    return this;
  }

  /**
   * Get termMonths
   * minimum: 1
   * maximum: 36
   * @return termMonths
   */
  @NotNull @Min(1) @Max(36) 
  @Schema(name = "termMonths", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("termMonths")
  public Integer getTermMonths() {
    return termMonths;
  }

  public void setTermMonths(Integer termMonths) {
    this.termMonths = termMonths;
  }

  public LoanValidationRequest lastLoanDate(LocalDate lastLoanDate) {
    this.lastLoanDate = JsonNullable.of(lastLoanDate);
    return this;
  }

  /**
   * Fecha del último préstamo del solicitante.
   * @return lastLoanDate
   */
  @Valid 
  @Schema(name = "lastLoanDate", description = "Fecha del último préstamo del solicitante.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("lastLoanDate")
  public JsonNullable<LocalDate> getLastLoanDate() {
    return lastLoanDate;
  }

  public void setLastLoanDate(JsonNullable<LocalDate> lastLoanDate) {
    this.lastLoanDate = lastLoanDate;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LoanValidationRequest loanValidationRequest = (LoanValidationRequest) o;
    return Objects.equals(this.monthlySalary, loanValidationRequest.monthlySalary) &&
        Objects.equals(this.requestedAmount, loanValidationRequest.requestedAmount) &&
        Objects.equals(this.termMonths, loanValidationRequest.termMonths) &&
        equalsNullable(this.lastLoanDate, loanValidationRequest.lastLoanDate);
  }

  private static <T> boolean equalsNullable(JsonNullable<T> a, JsonNullable<T> b) {
    return a == b || (a != null && b != null && a.isPresent() && b.isPresent() && Objects.deepEquals(a.get(), b.get()));
  }

  @Override
  public int hashCode() {
    return Objects.hash(monthlySalary, requestedAmount, termMonths, hashCodeNullable(lastLoanDate));
  }

  private static <T> int hashCodeNullable(JsonNullable<T> a) {
    if (a == null) {
      return 1;
    }
    return a.isPresent() ? Arrays.deepHashCode(new Object[]{a.get()}) : 31;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LoanValidationRequest {\n");
    sb.append("    monthlySalary: ").append(toIndentedString(monthlySalary)).append("\n");
    sb.append("    requestedAmount: ").append(toIndentedString(requestedAmount)).append("\n");
    sb.append("    termMonths: ").append(toIndentedString(termMonths)).append("\n");
    sb.append("    lastLoanDate: ").append(toIndentedString(lastLoanDate)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

