package lk.ijse.aad.backend.repository;

import lk.ijse.aad.backend.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {
    boolean existsByLicenceNo(String licenceNo);

    @Query("SELECT d FROM Driver d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR d.licenceNo LIKE CONCAT('%', :q, '%')")
    List<Driver> search(@Param("q") String q);

    List<Driver> findByVehicleType(Driver.VehicleType vehicleType);

    @Query("SELECT d FROM Driver d WHERE d.vehicleType = :vtype AND " +
            "(LOWER(d.name) LIKE LOWER(CONCAT('%', :q, '%')) OR d.licenceNo LIKE CONCAT('%', :q, '%'))")
    List<Driver> searchByVehicleType(@Param("q") String q, @Param("vtype") Driver.VehicleType vtype);
}