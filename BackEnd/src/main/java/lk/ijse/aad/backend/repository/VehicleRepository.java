package lk.ijse.aad.backend.repository;

import lk.ijse.aad.backend.entity.Vehicle;
import lk.ijse.aad.backend.enums.VehicleStatus;
import lk.ijse.aad.backend.enums.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle,Long> {
    // Filter by type (CAB, VAN, BUS, WHEEL)
    List<Vehicle> findByType(VehicleType type);

    // Filter by status (AVAILABLE, BOOKED, MAINTENANCE)
    List<Vehicle> findByStatus(VehicleStatus status);

    // Filter by type AND status (e.g., only AVAILABLE CABs)
    List<Vehicle> findByTypeAndStatus(VehicleType type, VehicleStatus status);
}
