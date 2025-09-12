package org.openapitools.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * LoanValidationResult
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-09-11T02:17:50.625965500-05:00[America/Lima]", comments = "Generator version: 7.7.0")
public class LoanValidationResult {

  private Boolean eligible;

  /**
   * Gets or Sets reasons
   */
  public enum ReasonsEnum {
    HAS_RECENT_LOANS("HAS_RECENT_LOANS"),
    
    PLAZO_MAXIMO_SUPERADO("PLAZO_MAXIMO_SUPERADO"),
    
    CAPACIDAD_INSUFICIENTE("CAPACIDAD_INSUFICIENTE"),
    
    DATOS_INVALIDOS("DATOS_INVALIDOS");

    private String value;

    ReasonsEnum(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static ReasonsEnum fromValue(String value) {
      for (ReasonsEnum b : ReasonsEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  @Valid
  private List<ReasonsEnum> reasons = new ArrayList<>();

  private Double monthlyPayment;

  public LoanValidationResult() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public LoanValidationResult(Boolean eligible, List<ReasonsEnum> reasons, Double monthlyPayment) {
    this.eligible = eligible;
    this.reasons = reasons;
    this.monthlyPayment = monthlyPayment;
  }

  public LoanValidationResult eligible(Boolean eligible) {
    this.eligible = eligible;
    return this;
  }

  /**
   * Get eligible
   * @return eligible
   */
  @NotNull 
  @Schema(name = "eligible", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("eligible")
  public Boolean getEligible() {
    return eligible;
  }

  public void setEligible(Boolean eligible) {
    this.eligible = eligible;
  }

  public LoanValidationResult reasons(List<ReasonsEnum> reasons) {
    this.reasons = reasons;
    return this;
  }

  public LoanValidationResult addReasonsItem(ReasonsEnum reasonsItem) {
    if (this.reasons == null) {
      this.reasons = new ArrayList<>();
    }
    this.reasons.add(reasonsItem);
    return this;
  }

  /**
   * Get reasons
   * @return reasons
   */
  @NotNull 
  @Schema(name = "reasons", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("reasons")
  public List<ReasonsEnum> getReasons() {
    return reasons;
  }

  public void setReasons(List<ReasonsEnum> reasons) {
    this.reasons = reasons;
  }

  public LoanValidationResult monthlyPayment(Double monthlyPayment) {
    this.monthlyPayment = monthlyPayment;
    return this;
  }

  /**
   * Get monthlyPayment
   * @return monthlyPayment
   */
  @NotNull 
  @Schema(name = "monthlyPayment", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("monthlyPayment")
  public Double getMonthlyPayment() {
    return monthlyPayment;
  }

  public void setMonthlyPayment(Double monthlyPayment) {
    this.monthlyPayment = monthlyPayment;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LoanValidationResult loanValidationResult = (LoanValidationResult) o;
    return Objects.equals(this.eligible, loanValidationResult.eligible) &&
        Objects.equals(this.reasons, loanValidationResult.reasons) &&
        Objects.equals(this.monthlyPayment, loanValidationResult.monthlyPayment);
  }

  @Override
  public int hashCode() {
    return Objects.hash(eligible, reasons, monthlyPayment);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LoanValidationResult {\n");
    sb.append("    eligible: ").append(toIndentedString(eligible)).append("\n");
    sb.append("    reasons: ").append(toIndentedString(reasons)).append("\n");
    sb.append("    monthlyPayment: ").append(toIndentedString(monthlyPayment)).append("\n");
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

