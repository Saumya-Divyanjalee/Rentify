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

    private String policyNumber;
    private String providerName;
    private String coverageType;

    private double premiumAmount;
    private double coverageAmount;

    private LocalDate startDate;
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    private InsuranceStatus status;

    private String notes;
}

