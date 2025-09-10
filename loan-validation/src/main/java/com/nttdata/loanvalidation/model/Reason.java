package com.nttdata.loanvalidation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Business rule failure reason. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reason {
    private String code; // R1, R2, R3, R4
    private String message; // Spanish human-readable message
}
