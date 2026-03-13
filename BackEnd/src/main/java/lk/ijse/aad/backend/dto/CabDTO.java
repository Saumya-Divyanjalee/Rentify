package lk.ijse.aad.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class CabDTO {

    private Integer cabId;

    @NotBlank(message = "Cab name is required")
    private String cabName;

    @NotBlank(message = "Cab model is required")
    private String cabModel;

    @NotBlank(message = "Number plate is required")
    private String cabPlate;

    @NotBlank(message = "Cab type is required")
    private String cabType;

    @Positive(message = "Price per km must be positive")
    private double pricePerKm;

    @Min(value = 1, message = "Seat count must be at least 1")
    private int seatCount;

    private String cabDescription;

    private boolean available;

    // Base64 image string — frontend sends: "data:image/jpeg;base64,..."
    private String cabImageBase64;

    private String imageType;
}