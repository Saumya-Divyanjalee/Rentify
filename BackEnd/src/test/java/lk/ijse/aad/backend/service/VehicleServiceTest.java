package lk.ijse.aad.backend.service;

import lk.ijse.aad.backend.dto.VehicleDTO;
import lk.ijse.aad.backend.entity.Vehicle;
import lk.ijse.aad.backend.enums.VehicleStatus;
import lk.ijse.aad.backend.enums.VehicleType;
import lk.ijse.aad.backend.repository.VehicleRepository;
import lk.ijse.aad.backend.service.impl.VehicleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("VehicleService Unit Tests")
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private VehicleServiceImpl vehicleService;

    private Vehicle sampleVehicle;
    private VehicleDTO sampleDTO;

    @BeforeEach
    void setUp() {
        // Sample Vehicle entity
        sampleVehicle = new Vehicle();
        sampleVehicle.setId(1L);
        sampleVehicle.setModel("Toyota Prius");
        sampleVehicle.setPlateNumber("CAB-1234");
        sampleVehicle.setCapacity(4);
        sampleVehicle.setYear(2022);
        sampleVehicle.setType(VehicleType.CAB);
        sampleVehicle.setStatus(VehicleStatus.AVAILABLE);
        sampleVehicle.setPricePerDay(5000.0);
        sampleVehicle.setDescription("Comfortable cab");

        // Sample DTO
        sampleDTO = new VehicleDTO();
        sampleDTO.setModel("Toyota Prius");
        sampleDTO.setPlateNumber("CAB-1234");
        sampleDTO.setCapacity(4);
        sampleDTO.setYear(2022);
        sampleDTO.setType(VehicleType.CAB);
        sampleDTO.setStatus(VehicleStatus.AVAILABLE);
        sampleDTO.setPricePerDay(5000.0);
        sampleDTO.setDescription("Comfortable cab");
    }

    // ==================== GET ALL ====================

    @Test
    @DisplayName("✅ getAllVehicles - should return all vehicles")
    void getAllVehicles_ShouldReturnAllVehicles() {
        when(vehicleRepository.findAll()).thenReturn(List.of(sampleVehicle));

        List<VehicleDTO> result = vehicleService.getAllVehicles();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getModel()).isEqualTo("Toyota Prius");
        verify(vehicleRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("✅ getAllVehicles - empty list when no vehicles")
    void getAllVehicles_WhenEmpty_ShouldReturnEmptyList() {
        when(vehicleRepository.findAll()).thenReturn(List.of());

        List<VehicleDTO> result = vehicleService.getAllVehicles();

        assertThat(result).isEmpty();
    }

    // ==================== GET BY TYPE ====================

    @Test
    @DisplayName("✅ getVehiclesByType - should return only CAB vehicles")
    void getVehiclesByType_ShouldReturnCorrectType() {
        when(vehicleRepository.findByType(VehicleType.CAB)).thenReturn(List.of(sampleVehicle));

        List<VehicleDTO> result = vehicleService.getVehiclesByType(VehicleType.CAB);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo(VehicleType.CAB);
    }

    // ==================== GET AVAILABLE ====================

    @Test
    @DisplayName("✅ getAvailableVehicles - should return AVAILABLE vehicles only")
    void getAvailableVehicles_ShouldReturnAvailableOnly() {
        when(vehicleRepository.findByStatus(VehicleStatus.AVAILABLE))
                .thenReturn(List.of(sampleVehicle));

        List<VehicleDTO> result = vehicleService.getAvailableVehicles();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
    }

    // ==================== GET BY ID ====================

    @Test
    @DisplayName("✅ getVehicleById - should return correct vehicle")
    void getVehicleById_ShouldReturnVehicle() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(sampleVehicle));

        VehicleDTO result = vehicleService.getVehicleById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getModel()).isEqualTo("Toyota Prius");
    }

    @Test
    @DisplayName("❌ getVehicleById - should throw exception when not found")
    void getVehicleById_WhenNotFound_ShouldThrowException() {
        when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.getVehicleById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Vehicle not found");
    }

    // ==================== ADD VEHICLE ====================

    @Test
    @DisplayName("✅ addVehicle - should save and return vehicle")
    void addVehicle_ShouldSaveAndReturnVehicle() {
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(sampleVehicle);

        VehicleDTO result = vehicleService.addVehicle(sampleDTO);

        assertThat(result).isNotNull();
        assertThat(result.getModel()).isEqualTo("Toyota Prius");
        verify(vehicleRepository, times(1)).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("✅ addVehicle - default status should be AVAILABLE")
    void addVehicle_WithNullStatus_ShouldDefaultToAvailable() {
        sampleDTO.setStatus(null); // No status provided
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(sampleVehicle);

        VehicleDTO result = vehicleService.addVehicle(sampleDTO);

        assertThat(result).isNotNull();
        verify(vehicleRepository).save(argThat(v -> v.getStatus() == VehicleStatus.AVAILABLE));
    }

    // ==================== UPDATE VEHICLE ====================

    @Test
    @DisplayName("✅ updateVehicle - should update fields correctly")
    void updateVehicle_ShouldUpdateFields() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(sampleVehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(sampleVehicle);

        sampleDTO.setModel("Honda Fit");
        sampleDTO.setPricePerDay(4500.0);

        VehicleDTO result = vehicleService.updateVehicle(1L, sampleDTO);

        assertThat(result).isNotNull();
        verify(vehicleRepository).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("❌ updateVehicle - should throw when vehicle not found")
    void updateVehicle_WhenNotFound_ShouldThrow() {
        when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.updateVehicle(99L, sampleDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Vehicle not found");
    }

    // ==================== DELETE VEHICLE ====================

    @Test
    @DisplayName("✅ deleteVehicle - should delete existing vehicle")
    void deleteVehicle_ShouldDeleteSuccessfully() {
        when(vehicleRepository.existsById(1L)).thenReturn(true);
        doNothing().when(vehicleRepository).deleteById(1L);

        vehicleService.deleteVehicle(1L);

        verify(vehicleRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("❌ deleteVehicle - should throw when not found")
    void deleteVehicle_WhenNotFound_ShouldThrow() {
        when(vehicleRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> vehicleService.deleteVehicle(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Vehicle not found");

        verify(vehicleRepository, never()).deleteById(any());
    }
}

