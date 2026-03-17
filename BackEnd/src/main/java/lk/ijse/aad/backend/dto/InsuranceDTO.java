package lk.ijse.aad.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lk.ijse.aad.backend.enums.InsuranceStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Insurance policy data transfer object")
public class InsuranceDTO {

    @Schema(description = "Insurance ID (auto-generated)", example = "1")
    private Long id;

    @NotNull(message = "Vehicle ID is required")
    @Schema(description = "ID of the vehicle this policy belongs to", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long vehicleId;

    @Schema(description = "Vehicle plate number (read-only, returned in responses)", example = "CAB-1234")
    private String vehiclePlateNumber;   // populated on read, ignored on write

    @NotBlank(message = "Policy number is required")
    @Size(max = 50, message = "Policy number cannot exceed 50 characters")
    @Schema(description = "Unique insurance policy number", example = "INS-2024-00123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String policyNumber;

    @NotBlank(message = "Provider name is required")
    @Size(max = 100, message = "Provider name cannot exceed 100 characters")
    @Schema(description = "Insurance provider / company name", example = "AIA Insurance", requiredMode = Schema.RequiredMode.REQUIRED)
    private String providerName;

    @NotBlank(message = "Coverage type is required")
    @Size(max = 100, message = "Coverage type cannot exceed 100 characters")
    @Schema(description = "Type of coverage", example = "Comprehensive", requiredMode = Schema.RequiredMode.REQUIRED)
    private String coverageType;

    @DecimalMin(value = "0.0", inclusive = false, message = "Premium amount must be greater than 0")
    @Schema(description = "Annual premium amount in LKR", example = "15000.0")
    private double premiumAmount;

    @DecimalMin(value = "0.0", inclusive = false, message = "Coverage amount must be greater than 0")
    @Schema(description = "Maximum claim coverage amount in LKR", example = "2000000.0")
    private double coverageAmount;

    @NotNull(message = "Start date is required")
    @Schema(description = "Policy start date (YYYY-MM-DD)", example = "2025-01-01", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate startDate;

    @NotNull(message = "Expiry date is required")
    @Schema(description = "Policy expiry date (YYYY-MM-DD)", example = "2026-01-01", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate expiryDate;

    @Schema(description = "Policy status", allowableValues = {"ACTIVE", "EXPIRED", "CANCELLED"})
    private InsuranceStatus status;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    @Schema(description = "Optional notes or remarks", example = "Includes roadside assistance")
    private String notes;
}

