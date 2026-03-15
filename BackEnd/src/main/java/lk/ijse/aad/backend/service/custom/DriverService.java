package lk.ijse.aad.backend.service.custom;

import lk.ijse.aad.backend.dto.DriverDTO;

import java.util.List;

public interface DriverService {
    DriverDTO addDriver(DriverDTO dto);
    DriverDTO updateDriver(Long id,DriverDTO dto);
    void deleteDriver(Long id);
    DriverDTO getDriver(Long id);
    List<DriverDTO> getAllDrivers();
    List<DriverDTO> searchDrivers(String query,String vehicleType);

}
