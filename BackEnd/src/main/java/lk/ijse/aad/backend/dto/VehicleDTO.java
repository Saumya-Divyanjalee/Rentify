package lk.ijse.aad.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lk.ijse.aad.backend.entity.Driver;
import lk.ijse.aad.backend.enums.VehicleStatus;
import lk.ijse.aad.backend.enums.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Vehicle data transfer object")
public class VehicleDTO {

    @Schema(description = "Vehicle ID (auto-generated)", example = "1")
    private Long id;

    @NotBlank(message = "Model name is required")
    @Size(min = 2, max = 100, message = "Model must be 2–100 characters")
    @Schema(description = "Vehicle model name", example = "Toyota Prius", requiredMode = Schema.RequiredMode.REQUIRED)
    private String model;

    @NotBlank(message = "Plate number is required")
    @Pattern(regexp = "^[A-Z]{2,3}-\\d{4}$", message = "Plate format: CAB-1234")
    @Schema(description = "Vehicle plate number", example = "CAB-1234", requiredMode = Schema.RequiredMode.REQUIRED)
    private String plateNumber;

    @Min(value = 1, message = "Capacity must be at least 1")
    @Max(value = 60, message = "Capacity cannot exceed 60")
    @Schema(description = "Passenger capacity", example = "4")
    private int capacity;

    @Min(value = 1990, message = "Year must be 1990 or later")
    @Max(value = 2030, message = "Year cannot be in far future")
    @Schema(description = "Manufacturing year", example = "2022")
    private int year;

    @NotNull(message = "Vehicle type is required")
    @Schema(description = "Vehicle type", example = "CAB",
            allowableValues = {"CAB", "VAN", "BUS", "WHEEL"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private VehicleType type;

    @Schema(description = "Vehicle status (auto-set to AVAILABLE if not provided)",
            allowableValues = {"AVAILABLE", "BOOKED", "MAINTENANCE"})
    private VehicleStatus status;

    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @DecimalMax(value = "1000000.0", message = "Price seems too high")
    @Schema(description = "Daily rental price in LKR", example = "5000.0", requiredMode = Schema.RequiredMode.REQUIRED)
    private double pricePerDay;

    @Schema(description = "Whether vehicle insurance is currently active", example = "true")
    private boolean insuranceActive = true;

    @Schema(description = "Insurance expiry date (YYYY-MM-DD)", example = "2026-12-31")
    private LocalDate insuranceExpiryDate;

    @Schema(description = "Vehicle image (base64 or URL)")
    private String image;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Schema(description = "Optional vehicle description", example = "Comfortable air-conditioned cab")
    private String description;
}
