package lk.ijse.aad.backend.service.custom;

import lk.ijse.aad.backend.dto.VehicleDTO;
import lk.ijse.aad.backend.enums.VehicleType;

import java.util.List;

public interface VehicleService {
    // Get all vehicles
    List<VehicleDTO> getAllVehicles();

    // Get vehicles by type (CAB, VAN, BUS, WHEEL)
    List<VehicleDTO> getVehiclesByType(VehicleType type);

    // Get only AVAILABLE vehicles
    List<VehicleDTO> getAvailableVehicles();

    // Get available vehicles by type
    List<VehicleDTO> getAvailableVehiclesByType(VehicleType type);

    // Get single vehicle
    VehicleDTO getVehicleById(Long id);

    // Admin: Add vehicle
    VehicleDTO addVehicle(VehicleDTO dto);

    // Admin: Update vehicle
    VehicleDTO updateVehicle(Long id, VehicleDTO dto);

    // Admin: Delete vehicle
    void deleteVehicle(Long id);
}
