package lk.ijse.aad.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DriverDTO {
    private Long id;
    private String name;
    private String phone;
    private String licenceNo;
    private LocalDate licenceExpiry;
    private String vehicleType;   // String for easy JSON binding
    private String status;
    private String profilePic;   // Base64
}
