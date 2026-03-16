package lk.ijse.aad.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lk.ijse.aad.backend.enums.BookingStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Schema(description = "Booking data transfer object")
public class BookingDTO {

    @Schema(description = "Booking ID (auto-generated)", example = "101")
    private Long id;

    @NotNull(message = "User ID is required")
    @Positive(message = "User ID must be positive")
    @Schema(description = "ID of the user making the booking", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;

    @NotNull(message = "Vehicle ID is required")
    @Positive(message = "Vehicle ID must be positive")
    @Schema(description = "ID of the vehicle to book", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long vehicleId;

    @Schema(description = "Vehicle model (returned by server)", example = "Toyota Prius")
    private String vehicleModel;

    @Schema(description = "Vehicle plate (returned by server)", example = "CAB-1234")
    private String vehiclePlate;

    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date cannot be in the past")
    @Schema(description = "Booking start date", example = "2026-04-01", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    @Schema(description = "Booking end date", example = "2026-04-04", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate endDate;

    @Schema(description = "Total price (calculated by server)", example = "15000.0")
    private double totalPrice;

    @NotBlank(message = "Pickup location is required")
    @Size(max = 200, message = "Pickup location too long")
    @Schema(description = "Pickup location", example = "Colombo Fort", requiredMode = Schema.RequiredMode.REQUIRED)
    private String pickupLocation;

    @NotBlank(message = "Drop location is required")
    @Size(max = 200, message = "Drop location too long")
    @Schema(description = "Drop-off location", example = "Kandy City Centre", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dropLocation;

    @Schema(description = "Booking status", allowableValues = {"PENDING","CONFIRMED","COMPLETED","CANCELLED"})
    private BookingStatus status;

    @Schema(description = "Booking creation timestamp")
    private LocalDateTime createdAt;
}
