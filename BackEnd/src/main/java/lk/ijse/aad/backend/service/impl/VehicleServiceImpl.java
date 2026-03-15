package lk.ijse.aad.backend.service.impl;

import lk.ijse.aad.backend.dto.VehicleDTO;
import lk.ijse.aad.backend.entity.Vehicle;
import lk.ijse.aad.backend.enums.VehicleStatus;
import lk.ijse.aad.backend.enums.VehicleType;
import lk.ijse.aad.backend.repository.VehicleRepository;
import lk.ijse.aad.backend.service.custom.VehicleService;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private static final Logger log = (Logger) LoggerFactory.getLogger(VehicleServiceImpl.class);
    private final VehicleRepository vehicleRepository;

    //helper method
    private VehicleDTO toDTO(Vehicle v){
        VehicleDTO dto = new VehicleDTO();
        dto.setId(v.getId());
        dto.setModel(v.getModel());
        dto.setPlateNumber(v.getPlateNumber());
        dto.setCapacity(v.getCapacity());
        dto.setYear(v.getYear());
        dto.setType(v.getType());
        dto.setStatus(v.getStatus());
        dto.setPricePerDay(v.getPricePerDay());
        dto.setInsuranceActive(v.isInsuranceActive());
        dto.setInsuranceExpiryDate(v.getInsuranceExpiryDate());
        dto.setImage(v.getImage());
        dto.setDescription(v.getDescription());
        return dto;
    }

    private Vehicle toEntity(VehicleDTO dto) {
        Vehicle v = new Vehicle();
        v.setModel(dto.getModel());
        v.setPlateNumber(dto.getPlateNumber());
        v.setCapacity(dto.getCapacity());
        v.setYear(dto.getYear());
        v.setType(dto.getType());
        v.setStatus(dto.getStatus() != null ? dto.getStatus() : VehicleStatus.AVAILABLE);
        v.setPricePerDay(dto.getPricePerDay());
        v.setInsuranceActive(dto.isInsuranceActive());
        v.setInsuranceExpiryDate(dto.getInsuranceExpiryDate());
        v.setImage(dto.getImage());
        v.setDescription(dto.getDescription());
        return v;
    }


    @Override
    public List<VehicleDTO> getAllVehicles() {
        return List.of();
    }

    @Override
    public List<VehicleDTO> getVehiclesByType(VehicleType type) {
        return List.of();
    }

    @Override
    public List<VehicleDTO> getAvailableVehicles() {
        return List.of();
    }

    @Override
    public List<VehicleDTO> getAvailableVehiclesByType(VehicleType type) {
        return List.of();
    }

    @Override
    public VehicleDTO getVehicleById(Long id) {
        return null;
    }

    @Override
    public VehicleDTO addVehicle(VehicleDTO dto) {
        return null;
    }

    @Override
    public VehicleDTO updateVehicle(Long id, VehicleDTO dto) {
        return null;
    }

    @Override
    public void deleteVehicle(Long id) {

    }
}
