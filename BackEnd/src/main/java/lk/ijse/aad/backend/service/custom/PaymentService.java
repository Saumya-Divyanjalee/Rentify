package lk.ijse.aad.backend.service.custom;

import lk.ijse.aad.backend.dto.PaymentDTO;
import lk.ijse.aad.backend.dto.PaymentResponseDTO;

import java.util.List;

public interface PaymentService {

    PaymentResponseDTO processPayment(PaymentDTO dto, String username);


    PaymentResponseDTO getPaymentByBookingId(Long bookingId);


    List<PaymentResponseDTO> getPaymentsByUser(String username);
}


