package lk.ijse.aad.backend.repository;

import lk.ijse.aad.backend.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /** Find payment by our internal order ID (transactionId = PayHere order_id) */
    Optional<Payment> findByTransactionId(String transactionId);

    /** Find payment for a booking */
    Optional<Payment> findByBooking_Id(Long bookingId);

    /** All payments for a user */
    List<Payment> findByUser_UserId(Long userId);
}