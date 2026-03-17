package lk.ijse.aad.backend.service.custom;

import lk.ijse.aad.backend.dto.InsuranceDTO;
import lk.ijse.aad.backend.enums.InsuranceStatus;

import java.util.List;

public interface InsuranceService {

    // Get all insurance policies
    List<InsuranceDTO> getAllPolicies();

    // Get a single policy by ID
    InsuranceDTO getPolicyById(Long id);

    // Get all policies for a specific vehicle
    List<InsuranceDTO> getPoliciesByVehicle(Long vehicleId);

    // Get policies filtered by status
    List<InsuranceDTO> getPoliciesByStatus(InsuranceStatus status);

    // Get active policy for a specific vehicle
    List<InsuranceDTO> getActivePoliciesByVehicle(Long vehicleId);

    // Get policies expiring within the next N days
    List<InsuranceDTO> getExpiringPolicies(int daysAhead);

    // Admin: Add a new policy
    InsuranceDTO addPolicy(InsuranceDTO dto);

    // Admin: Update a policy
    InsuranceDTO updatePolicy(Long id, InsuranceDTO dto);

    // Admin: Cancel a policy (soft status change)
    InsuranceDTO cancelPolicy(Long id);

    // Admin: Delete a policy permanently
    void deletePolicy(Long id);
}


