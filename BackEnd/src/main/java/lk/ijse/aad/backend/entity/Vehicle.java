package lk.ijse.aad.backend.entity;

import jakarta.persistence.*;
import lk.ijse.aad.backend.enums.VehicleStatus;
import lk.ijse.aad.backend.enums.VehicleType;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "vehicles")
@Data
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String model;
    private String plateNumber;
    private int capacity;
    private int year;

    @Enumerated(EnumType.STRING)
    private VehicleType type;           // CAB, VAN, BUS, WHEEL

    @Enumerated(EnumType.STRING)
    private VehicleStatus status = VehicleStatus.AVAILABLE;

    private double pricePerDay;

    // ── Insurance fields ──────────────────────────────────
    private boolean insuranceActive = true;
    private LocalDate insuranceExpiryDate;

    @Column(columnDefinition = "LONGTEXT")
    private String image;

    private String description;
}
