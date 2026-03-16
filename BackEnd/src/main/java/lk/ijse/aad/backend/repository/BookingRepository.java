package lk.ijse.aad.backend.repository;

import lk.ijse.aad.backend.entity.Booking;
import lk.ijse.aad.backend.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // user.userId (not user.id)
    List<Booking> findByUserUserId(Long userId);

    // vehicle.id is fine since Booking's vehicle PK field is "id"
    List<Booking> findByVehicleId(Long vehicleId);

    List<Booking> findByStatus(BookingStatus status);

    // user.userId (not user.id)
    List<Booking> findByUserUserIdAndStatus(Long userId, BookingStatus status);

    @Query("""
        SELECT COUNT(b) > 0 FROM Booking b
        WHERE b.vehicle.id = :vehicleId
          AND b.status NOT IN (:cancelled, :completed)
          AND b.startDate < :endDate
          AND b.endDate > :startDate
    """)
    boolean existsOverlappingBooking(
            @Param("vehicleId")  Long vehicleId,
            @Param("startDate")  LocalDate startDate,
            @Param("endDate")    LocalDate endDate,
            @Param("cancelled")  BookingStatus cancelled,
            @Param("completed")  BookingStatus completed
    );
}

