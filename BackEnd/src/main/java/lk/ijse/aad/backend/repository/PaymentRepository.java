package lk.ijse.aad.backend.repository;

import lk.ijse.aad.backend.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Booking ID ekata payment thiyanawada check karanawa
    Optional<Payment> findByBooking_Id(Long bookingId);

    // User ekage payments list
    List<Payment> findByUser_UserId(Long userId);

    // Transaction ID ekata payment gannawa
    Optional<Payment> findByTransactionId(String transactionId);
}