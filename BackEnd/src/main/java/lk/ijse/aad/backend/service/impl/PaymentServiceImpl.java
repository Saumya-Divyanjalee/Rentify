package lk.ijse.aad.backend.service.impl;

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
import lk.ijse.aad.backend.service.EmailService;
import lk.ijse.aad.backend.service.custom.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository    userRepository;
    private final VehicleRepository vehicleRepository;
    private final EmailService      emailService;

    @Override
    @Transactional
    public PaymentResponseDTO processPayment(PaymentDTO dto, String username) {

        // Load user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException(
                        "User not found: " + username));

        // Load booking
        Booking booking = bookingRepository.findById(dto.getBookingId())
                .orElseThrow(() -> new RuntimeException(
                        "Booking not found: " + dto.getBookingId()));

        // Verify ownership
        if (!booking.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException(
                    "This booking does not belong to you.");
        }

        // Check already paid
        paymentRepository.findByBooking_Id(booking.getId())
                .ifPresent(p -> {
                    if (p.getStatus() == PaymentStatus.COMPLETED) {
                        throw new RuntimeException(
                                "This booking is already paid. Transaction: "
                                        + p.getTransactionId());
                    }
                });

        // Build payment
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setUser(user);
        payment.setAmount(dto.getAmount());
        payment.setCurrency(dto.getCurrency() != null
                ? dto.getCurrency() : "LKR");
        payment.setTransactionId("TXN-" + UUID.randomUUID()
                .toString().replace("-", "")
                .substring(0, 12).toUpperCase());

        PaymentMethod method = PaymentMethod.valueOf(
                dto.getPaymentMethod().toUpperCase());
        payment.setPaymentMethod(method);

        switch (method) {
            case CARD -> {
                validateCardDetails(dto);
                payment.setCardName(dto.getCardName());
                payment.setCardNumber(
                        maskCardNumber(dto.getCardNumber()));
                payment.setExpiryDate(dto.getExpiryDate());
                payment.setCvv("***");
                payment.setStatus(PaymentStatus.COMPLETED);
            }
            case CASH -> payment.setStatus(PaymentStatus.PENDING);
            case ONLINE -> payment.setStatus(PaymentStatus.PENDING);
        }

        Payment saved = paymentRepository.save(payment);

        // Confirm booking and vehicle if payment completed
        if (saved.getStatus() == PaymentStatus.COMPLETED) {
            booking.setStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);

            if (booking.getVehicle() != null) {
                booking.getVehicle().setStatus(VehicleStatus.BOOKED);
                vehicleRepository.save(booking.getVehicle());
            }
        }

        // ─────────────────────────────────────────────────
        // FIX: Pre-load all data as Strings BEFORE
        // calling sendPaymentSuccessEmail.
        //
        // EmailService now accepts plain Strings.
        // Do NOT pass Payment entity directly because
        // it has LAZY relationships that may fail
        // after session commits.
        // ─────────────────────────────────────────────────
        if (saved.getStatus() == PaymentStatus.COMPLETED) {

            // Pre-load all needed data as Strings
            String vehicleModel = "—";
            String bookingDates = "—";
            String bookingIdStr = "—";
            String amountStr    = "—";
            String currencyStr  = "LKR";
            String methodStr    = method.name();
            String cardNum      = saved.getCardNumber();
            String dateTimeStr  = "—";

            try {
                if (booking.getVehicle() != null) {
                    vehicleModel = booking.getVehicle().getModel();
                }
                if (booking.getStartDate() != null &&
                        booking.getEndDate() != null) {
                    bookingDates = booking.getStartDate()
                            + " → " + booking.getEndDate();
                }
                bookingIdStr = String.valueOf(booking.getId());

                if (saved.getAmount() != null) {
                    amountStr = saved.getAmount().toPlainString();
                }
                if (saved.getCurrency() != null) {
                    currencyStr = saved.getCurrency();
                }
                if (saved.getCreatedAt() != null) {
                    dateTimeStr = saved.getCreatedAt().format(
                            DateTimeFormatter.ofPattern(
                                    "dd MMM yyyy, hh:mm a"));
                }
            } catch (Exception e) {
                log.warn("Could not pre-load email data: {}",
                        e.getMessage());
            }

            // Send email with pre-loaded String values
            emailService.sendPaymentSuccessEmail(
                    user.getEmail(),
                    user.getFullName(),
                    saved.getTransactionId(),
                    amountStr,
                    currencyStr,
                    methodStr,
                    vehicleModel,
                    bookingDates,
                    bookingIdStr,
                    cardNum,
                    dateTimeStr
            );
        }

        return mapToResponse(saved);
    }

    @Override
    public PaymentResponseDTO getPaymentByBookingId(Long bookingId) {
        Payment payment = paymentRepository
                .findByBooking_Id(bookingId)
                .orElseThrow(() -> new RuntimeException(
                        "No payment found for booking: " + bookingId));
        return mapToResponse(payment);
    }

    @Override
    public List<PaymentResponseDTO> getPaymentsByUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException(
                        "User not found"));
        return paymentRepository
                .findByUser_UserId(user.getUserId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private void validateCardDetails(PaymentDTO dto) {
        if (dto.getCardName() == null ||
                dto.getCardName().isBlank())
            throw new RuntimeException("Card name is required");
        if (dto.getCardNumber() == null ||
                dto.getCardNumber().isBlank())
            throw new RuntimeException("Card number is required");
        if (dto.getExpiryDate() == null ||
                dto.getExpiryDate().isBlank())
            throw new RuntimeException("Expiry date is required");
        if (dto.getCvv() == null ||
                dto.getCvv().isBlank())
            throw new RuntimeException("CVV is required");
    }

    private String maskCardNumber(String raw) {
        if (raw == null || raw.length() < 4) return "****";
        String digits = raw.replaceAll("\\s", "");
        return "**** **** **** " +
                digits.substring(digits.length() - 4);
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