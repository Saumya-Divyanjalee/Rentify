package lk.ijse.aad.backend.service.impl;

import org.springframework.transaction.annotation.Transactional;
import lk.ijse.aad.backend.dto.DriverDTO;
import lk.ijse.aad.backend.entity.Driver;
import lk.ijse.aad.backend.repository.DriverRepository;
import lk.ijse.aad.backend.service.custom.DriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DriverServiceImpl implements DriverService {

    private final DriverRepository driverRepository;

    // FIX: Ensure all fields including licenceExpiry are mapped to Entity
    private Driver toEntity(DriverDTO dto) {
        return Driver.builder()
                .name(dto.getName())
                .phone(dto.getPhone())
                .licenceNo(dto.getLicenceNo())
                .licenceExpiry(dto.getLicenceExpiry())
                .vehicleType(Driver.VehicleType.valueOf(dto.getVehicleType()))
                // FIX: Replace spaces with underscores for Enum safety (e.g. "On Trip" -> "On_Trip")
                .status(Driver.DriverStatus.valueOf(dto.getStatus().replace(" ", "_")))
                .profilePic(dto.getProfilePic())
                .build();
    }

    private DriverDTO toDTO(Driver driver){
        return DriverDTO.builder()
                .id(driver.getId())
                .name(driver.getName())
                .phone(driver.getPhone())
                .licenceNo(driver.getLicenceNo())
                .licenceExpiry(driver.getLicenceExpiry())
                .vehicleType(driver.getVehicleType().name())
                .status(driver.getStatus().name().replace("_", " ")) // "On_Trip" → "On Trip"
                .profilePic(driver.getProfilePic())
                .build();
    }

    @Override
    public DriverDTO addDriver(DriverDTO dto) {
        if (driverRepository.existsByLicenceNo(dto.getLicenceNo())){
            throw new RuntimeException("Licence number already exists: "+dto.getLicenceNo());
        }
        Driver saved = driverRepository.save(toEntity(dto));
        return toDTO(saved);
    }

    @Override
    public DriverDTO updateDriver(Long id, DriverDTO dto) {
        Driver existing = driverRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Driver not found ID: " + id));

        // Validation for unique licence number if changed
        if (!existing.getLicenceNo().equals(dto.getLicenceNo()) &&
                driverRepository.existsByLicenceNo(dto.getLicenceNo())) {
            throw new RuntimeException("Licence number already in use");
        }

        existing.setName(dto.getName());
        existing.setPhone(dto.getPhone());
        existing.setLicenceNo(dto.getLicenceNo());
        existing.setLicenceExpiry(dto.getLicenceExpiry());
        existing.setVehicleType(Driver.VehicleType.valueOf(dto.getVehicleType()));
        existing.setStatus(Driver.DriverStatus.valueOf(dto.getStatus().replace(" ", "_")));

        if (dto.getProfilePic() != null) {
            existing.setProfilePic(dto.getProfilePic());
        }

        return toDTO(driverRepository.save(existing));
    }

    @Override
    public List<DriverDTO> searchDrivers(String query, String vehicleType) {
        // FIX: Handle blank strings from frontend search inputs
        String q = (query != null && !query.isBlank()) ? query : null;
        Driver.VehicleType vType = (vehicleType != null && !vehicleType.isBlank()) ?
                Driver.VehicleType.valueOf(vehicleType) : null;

        if (q != null && vType != null) {
            return driverRepository.searchByVehicleType(q, vType).stream().map(this::toDTO).toList();
        } else if (q != null) {
            return driverRepository.search(q).stream().map(this::toDTO).toList();
        } else if (vType != null) {
            return driverRepository.findByVehicleType(vType).stream().map(this::toDTO).toList();
        }
        return driverRepository.findAll().stream().map(this::toDTO).toList();
    }

    @Override
    public void deleteDriver(Long id) {
        if (!driverRepository.existsById(id)){
            throw new RuntimeException("Driver not found with ID: "+id);
        }
        driverRepository.deleteById(id);

    }



    @Override
    @Transactional(readOnly = true)
    public DriverDTO getDriver(Long id) {
        return driverRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Driver not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DriverDTO> getAllDrivers() {
        return driverRepository.findAll()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }


}
