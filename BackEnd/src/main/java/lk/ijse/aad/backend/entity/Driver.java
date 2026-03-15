package lk.ijse.aad.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "drivers")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(name = "licence_no", nullable = false, unique = true, length = 20)
    private String licenceNo;

    @Column(name = "licence_expiry")
    private LocalDate licenceExpiry;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false)
    private VehicleType vehicleType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DriverStatus status;

    @Lob
    @Column(name = "profile_pic", columnDefinition = "LONGTEXT")
    private String profilePic;

    // Unified Enums to match Frontend options
    public enum VehicleType {
        Sedan, SUV, Mini, Van, Luxury, CAR, THREE_WHEEL, CAB, BUS
    }

    public enum DriverStatus {
        Active, Inactive, On_Trip
    }
}