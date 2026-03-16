package lk.ijse.aad.backend.controller;

import jakarta.validation.Valid;
import lk.ijse.aad.backend.dto.PaymentDTO;
import lk.ijse.aad.backend.dto.PaymentResponseDTO;
import lk.ijse.aad.backend.service.custom.PaymentService;
import lk.ijse.aad.backend.utill.APIResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Payment process karanawa.
     * JWT token ekin logged-in user automatically gannawa — RequestParam epa.
     */
    @PostMapping("/process")
    public ResponseEntity<APIResponse<PaymentResponseDTO>> processPayment(
            @Valid @RequestBody PaymentDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        // @AuthenticationPrincipal → JWTAuthFilter set karat username directly gannawa
        String username = userDetails.getUsername();
        PaymentResponseDTO response = paymentService.processPayment(dto, username);

        return ResponseEntity.ok(new APIResponse<>(200, "Payment processed successfully", response));
    }

    /**
     * Booking ID ekata gatta payment details.
     */
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<APIResponse<PaymentResponseDTO>> getByBooking(
            @PathVariable Long bookingId) {

        PaymentResponseDTO response = paymentService.getPaymentByBookingId(bookingId);
        return ResponseEntity.ok(new APIResponse<>(200, "Payment found", response));
    }

    /**
     * Logged-in user eke hela payments.
     */
    @GetMapping("/my")
    public ResponseEntity<APIResponse<List<PaymentResponseDTO>>> getMyPayments(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<PaymentResponseDTO> payments = paymentService.getPaymentsByUser(userDetails.getUsername());
        return ResponseEntity.ok(new APIResponse<>(200, "Payments retrieved", payments));
    }
}
