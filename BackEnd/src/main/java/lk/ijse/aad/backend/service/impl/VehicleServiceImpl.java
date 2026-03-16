package lk.ijse.aad.backend.service.impl;

import lk.ijse.aad.backend.dto.VehicleDTO;
import lk.ijse.aad.backend.entity.Vehicle;
import lk.ijse.aad.backend.enums.VehicleStatus;
import lk.ijse.aad.backend.enums.VehicleType;
import lk.ijse.aad.backend.exception.ResourceNotFoundException;
import lk.ijse.aad.backend.repository.VehicleRepository;
import lk.ijse.aad.backend.service.custom.VehicleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

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
        log.debug("Fetching all vehicles");
        List<VehicleDTO> list = vehicleRepository.findAll()
                .stream().map(this::toDTO).collect(Collectors.toList());
        log.info("getAllVehicles → {} results",list.size());
        return list;
    }

    @Override
    public List<VehicleDTO> getVehiclesByType(VehicleType type) {
        log.debug("Fetching vehicles by type: {}",type);
        return vehicleRepository.findByType(type)
                .stream().map(this :: toDTO).collect(Collectors.toList());


    }

    @Override
    public List<VehicleDTO> getAvailableVehicles() {

        return vehicleRepository.findByStatus(VehicleStatus.AVAILABLE)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<VehicleDTO> getAvailableVehiclesByType(VehicleType type) {
        return vehicleRepository.findByTypeAndStatus(type,VehicleStatus.AVAILABLE)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public VehicleDTO getVehicleById(Long id) {
        Vehicle v = vehicleRepository.findById(id)
                .orElseThrow(() ->{
                    log.error("Vehicle not found: {}",id);
                    return new ResourceNotFoundException("Vehicle not found: "+id);
                });
        return toDTO(v);
    }

    @Override
    public VehicleDTO addVehicle(VehicleDTO dto) {
        log.info("Adding vehicle: model={}, type={}",dto.getModel(),dto.getType());
        Vehicle saved = vehicleRepository.save(toEntity(dto));
        log.info("Vehicle saved id={}",saved.getId());
        return toDTO(saved);
    }

    @Override
    public VehicleDTO updateVehicle(Long id, VehicleDTO dto) {
        log.info("Updating vehicle id={}",id);
        Vehicle existing = vehicleRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Vehicle not found: " + id));

        existing.setModel(dto.getModel());
        existing.setPlateNumber(dto.getPlateNumber());
        existing.setCapacity(dto.getCapacity());
        existing.setYear(dto.getYear());
        existing.setType(dto.getType());
        existing.setStatus(dto.getStatus());
        existing.setPricePerDay(dto.getPricePerDay());
        existing.setInsuranceActive(dto.isInsuranceActive());
        existing.setInsuranceExpiryDate(dto.getInsuranceExpiryDate());
        existing.setImage(dto.getImage());
        existing.setDescription(dto.getDescription());

        log.info("Vehicle updated id={}",id);
        return toDTO(vehicleRepository.save(existing));
    }

    @Override
    public void deleteVehicle(Long id) {
        log.warn("Deleting vehicle id={}",id);
        if (!vehicleRepository.existsById(id)){
            log.error("Vehicle not found by delete, id={}",id);
            throw new ResourceNotFoundException("Vehicle not found: "+id);

        }
        vehicleRepository.deleteById(id);
        log.info("Vehicle deleted id={}",id);
    }
}
