package lk.ijse.aad.backend.service.impl;

import lk.ijse.aad.backend.dto.PaymentDTO;
import lk.ijse.aad.backend.dto.PaymentResponseDTO;
import lk.ijse.aad.backend.service.EmailService;
import lk.ijse.aad.backend.service.custom.PaymentService;

import java.util.List;


import lk.ijse.aad.backend.dto.PaymentDTO;
import lk.ijse.aad.backend.dto.PaymentResponseDTO;
import lk.ijse.aad.backend.entity.Booking;
import lk.ijse.aad.backend.entity.Payment;
import lk.ijse.aad.backend.entity.User;
import lk.ijse.aad.backend.enums.BookingStatus;
import lk.ijse.aad.backend.enums.PaymentMethod;
import lk.ijse.aad.backend.enums.PaymentStatus;
import lk.ijse.aad.backend.enums.VehicleStatus;
import lk.ijse.aad.backend.repository.BookingRepository;
import lk.ijse.aad.backend.repository.PaymentRepository;
import lk.ijse.aad.backend.repository.UserRepository;
import lk.ijse.aad.backend.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository  bookingRepository;
    private final UserRepository     userRepository;
    private final VehicleRepository  vehicleRepository;
    private final EmailService emailService;

    // ── 1. MAIN PROCESS ────────────────────────────────────────────────────────

    @Override
    @Transactional
    public PaymentResponseDTO processPayment(PaymentDTO dto, String username) {

        // Step 1 ── User gannawa (JWT token ekin aawa username use karanawa)
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        // Step 2 ── Booking gannawa
        Booking booking = bookingRepository.findById(dto.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found: " + dto.getBookingId()));

        // Step 3 ── Booking user-ta belong wenawada check karanawa
        if (!booking.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("This booking does not belong to you.");
        }

        // Step 4 ── Already paid check
        paymentRepository.findByBooking_Id(booking.getId()).ifPresent(p -> {
            if (p.getStatus() == PaymentStatus.COMPLETED) {
                throw new RuntimeException("This booking is already paid. Transaction: " + p.getTransactionId());
            }
        });

        // Step 5 ── Payment entity hada gannawa
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setUser(user);
        payment.setAmount(dto.getAmount());
        payment.setCurrency(dto.getCurrency() != null ? dto.getCurrency() : "LKR");
        payment.setTransactionId("TXN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());

        // Step 6 ── Payment method anuwata handle karanawa
        PaymentMethod method = PaymentMethod.valueOf(dto.getPaymentMethod().toUpperCase());
        payment.setPaymentMethod(method);

        switch (method) {
            case CARD -> {
                validateCardDetails(dto);
                payment.setCardName(dto.getCardName());
                payment.setCardNumber(maskCardNumber(dto.getCardNumber()));
                payment.setExpiryDate(dto.getExpiryDate());
                payment.setCvv("***"); // CVV never store karanna epa
                payment.setStatus(PaymentStatus.COMPLETED);
            }
            case CASH -> {
                // Cash ekedi driver thamai genawa — PENDING thenawa
                payment.setStatus(PaymentStatus.PENDING);
            }
            case ONLINE -> {
                // Real gateway nattam PENDING, gateway use karat COMPLETED
                payment.setStatus(PaymentStatus.PENDING);
            }
        }

        // Step 7 ── Save
        Payment saved = paymentRepository.save(payment);

        // Step 8 ── Payment COMPLETED nattam booking CONFIRMED + Vehicle BOOKED karanawa
        if (saved.getStatus() == PaymentStatus.COMPLETED) {
            booking.setStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);

            // Vehicle status BOOKED karanawa — available naha kiyala dekhennawa
            if (booking.getVehicle() != null) {
                booking.getVehicle().setStatus(VehicleStatus.BOOKED);
                vehicleRepository.save(booking.getVehicle());
            }
        }

        // Step 9 ── Success email send karanawa
        if (saved.getStatus() == PaymentStatus.COMPLETED) {
            emailService.sendPaymentSuccessEmail(
                    user.getEmail(),
                    user.getFullName(),
                    saved
            );
        }

        return mapToResponse(saved);
    }

    // ── 2. GET BY BOOKING ──────────────────────────────────────────────────────

    @Override
    public PaymentResponseDTO getPaymentByBookingId(Long bookingId) {
        Payment payment = paymentRepository.findByBooking_Id(bookingId)
                .orElseThrow(() -> new RuntimeException("No payment found for booking: " + bookingId));
        return mapToResponse(payment);
    }

    // ── 3. GET BY USER ─────────────────────────────────────────────────────────

    @Override
    public List<PaymentResponseDTO> getPaymentsByUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return paymentRepository.findByUser_UserId(user.getUserId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ── HELPERS ────────────────────────────────────────────────────────────────

    private void validateCardDetails(PaymentDTO dto) {
        if (dto.getCardName()   == null || dto.getCardName().isBlank())   throw new RuntimeException("Card name is required");
        if (dto.getCardNumber() == null || dto.getCardNumber().isBlank()) throw new RuntimeException("Card number is required");
        if (dto.getExpiryDate() == null || dto.getExpiryDate().isBlank()) throw new RuntimeException("Expiry date is required");
        if (dto.getCvv()        == null || dto.getCvv().isBlank())        throw new RuntimeException("CVV is required");
    }

    /** Card number masking — show only last 4 digits. e.g. **** **** **** 1234 */
    private String maskCardNumber(String raw) {
        if (raw == null || raw.length() < 4) return "****";
        String digits = raw.replaceAll("\\s", "");
        return "**** **** **** " + digits.substring(digits.length() - 4);
    }

    private PaymentResponseDTO mapToResponse(Payment p) {
        PaymentResponseDTO res = new PaymentResponseDTO();
        res.setPaymentId(p.getId());
        res.setTransactionId(p.getTransactionId());
        res.setStatus(p.getStatus().name());
        res.setPaymentMethod(p.getPaymentMethod().name());
        res.setAmount(p.getAmount());
        res.setCurrency(p.getCurrency());
        res.setCreatedAt(p.getCreatedAt());
        res.setBookingId(p.getBooking().getId());
        res.setUserName(p.getUser().getFullName());
        res.setUserEmail(p.getUser().getEmail());
        return res;
    }
}

