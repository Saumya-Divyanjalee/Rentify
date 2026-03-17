package lk.ijse.aad.backend.entity;

import jakarta.persistence.*;
import lk.ijse.aad.backend.enums.InsuranceStatus;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "insurances")
@Data
public class Insurance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The vehicle this insurance policy belongs to
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    private String policyNumber;       // e.g. "INS-2024-00123"
    private String providerName;       // e.g. "AIA Insurance"
    private String coverageType;       // e.g. "Comprehensive", "Third Party"

    private double premiumAmount;      // Annual premium in LKR
    private double coverageAmount;     // Max claim amount in LKR

    private LocalDate startDate;
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    private InsuranceStatus status;    // ACTIVE, EXPIRED, CANCELLED

    private String notes;              // Optional extra info
}

