package lk.ijse.aad.backend.repository;

import lk.ijse.aad.backend.entity.Insurance;
import lk.ijse.aad.backend.enums.InsuranceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InsuranceRepository extends JpaRepository<Insurance, Long> {

    // All policies for a specific vehicle
    List<Insurance> findByVehicleId(Long vehicleId);

    // Policies by status (ACTIVE, EXPIRED, CANCELLED)
    List<Insurance> findByStatus(InsuranceStatus status);

    // Active policies for a specific vehicle
    List<Insurance> findByVehicleIdAndStatus(Long vehicleId, InsuranceStatus status);

    // Check duplicate policy number
    boolean existsByPolicyNumber(String policyNumber);

    // Find policy by policy number
    Optional<Insurance> findByPolicyNumber(String policyNumber);

    // Policies expiring on or before a given date (useful for expiry alerts)
    List<Insurance> findByExpiryDateBeforeAndStatus(LocalDate date, InsuranceStatus status);
}