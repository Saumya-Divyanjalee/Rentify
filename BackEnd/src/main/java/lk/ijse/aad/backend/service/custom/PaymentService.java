package lk.ijse.aad.backend.service.custom;

import lk.ijse.aad.backend.dto.PaymentDTO;
import lk.ijse.aad.backend.dto.PaymentResponseDTO;

import java.util.List;

public interface PaymentService {
    /**
     * Main payment process method.
     * @param dto      Frontend ekin aawa payment data
     * @param username JWT ekin gatta logged-in user's username
     * @return         PaymentResponseDTO with transaction details
     */
    PaymentResponseDTO processPayment(PaymentDTO dto, String username);

    /**
     * Booking ID ekata gatta payment details.
     */
    PaymentResponseDTO getPaymentByBookingId(Long bookingId);

    /**
     * User eke hela payments listi.
     */
    List<PaymentResponseDTO> getPaymentsByUser(String username);
}


