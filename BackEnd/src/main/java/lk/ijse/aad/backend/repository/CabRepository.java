package lk.ijse.aad.backend.repository;

import lk.ijse.aad.backend.entity.Cab;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CabRepository extends JpaRepository<Cab,Integer> {
    Optional<Cab> findByCabPlate(String cabPlate);

    boolean existsByCabPlate(String cabPlate);

    // Only fetch metadata columns (no BLOB-Binary Large Object) for list views — keeps queries fast
    @Query("SELECT new lk.ijse.aad.backend.entity.Cab(" +
            "c.cabId, c.cabName, c.cabModel, c.cabPlate, c.cabType, " +
            "c.pricePerKm, c.seatCount, c.cabDescription, c.available, null, c.imageType) " +
            "FROM Cab c")
    List<Cab> findAllWithoutImage();

    List<Cab> findByAvailableTrue();

    List<Cab> findByCabType(String cabType);

}
