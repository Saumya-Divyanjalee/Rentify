package lk.ijse.aad.backend.service.impl;

import lk.ijse.aad.backend.config.PayHereConfig;
import lk.ijse.aad.backend.dto.PayHereInitDTO;
import lk.ijse.aad.backend.dto.PayHereNotifyDTO;
import lk.ijse.aad.backend.entity.Booking;
import lk.ijse.aad.backend.entity.Payment;
import lk.ijse.aad.backend.entity.User;
import lk.ijse.aad.backend.enums.BookingStatus;
import lk.ijse.aad.backend.enums.PaymentMethod;
import lk.ijse.aad.backend.enums.PaymentStatus;
import lk.ijse.aad.backend.enums.VehicleStatus;
import lk.ijse.aad.backend.exception.ResourceNotFoundException;
import lk.ijse.aad.backend.repository.BookingRepository;
import lk.ijse.aad.backend.repository.PaymentRepository;
import lk.ijse.aad.backend.repository.UserRepository;
import lk.ijse.aad.backend.repository.VehicleRepository;
import lk.ijse.aad.backend.service.EmailService;
import lk.ijse.aad.backend.service.custom.PayHereService;
import lk.ijse.aad.backend.utill.PayHereHashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayHereServiceImpl implements PayHereService {

    private final PayHereConfig payHereConfig;
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final EmailService emailService;

    private static final String STATUS_SUCCESS = "2";
    private static final String STATUS_PENDING = "0";
    private static final String STATUS_CANCELLED = "-1";
    private static final String STATUS_FAILED = "-2";
    private static final String STATUS_CHARGEDBACK = "-3";

    @Override
    @Transactional
    public PayHereInitDTO initiatePayment(Long bookingId, String username) {
        log.info("Initiating PayHere payment: bookingId={}, user={}", bookingId, username);

        // 1. Load user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        // 2. Load booking
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));

        // 3. Verify ownership
        if (!booking.getUser().getUserId().equals(user.getUserId())) {
            throw new SecurityException("This booking does not belong to you.");
        }

        // 4. Check already paid
        paymentRepository.findByBooking_Id(bookingId).ifPresent(p -> {
            if (p.getStatus() == PaymentStatus.COMPLETED) {
                throw new RuntimeException("Booking #" + bookingId + " is already paid. Transaction: " + p.getTransactionId());
            }
        });

        // 5. Generate unique order ID
        String orderId = "RNT-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();

        // 6. Amount
        BigDecimal amount = BigDecimal.valueOf(booking.getTotalPrice());
        String currency = "LKR";

        // 7. Create or reuse PENDING payment record
        Payment payment = paymentRepository.findByBooking_Id(bookingId).orElse(new Payment());
        payment.setBooking(booking);
        payment.setUser(user);
        payment.setAmount(amount);
        payment.setCurrency(currency);
        payment.setTransactionId(orderId);
        payment.setPaymentMethod(PaymentMethod.ONLINE);
        payment.setStatus(PaymentStatus.PENDING);
        Payment saved = paymentRepository.save(payment);
        log.info("PENDING payment saved: id={}, orderId={}", saved.getId(), orderId);

        // 8. Build hash
        String hash = PayHereHashUtil.generateCheckoutHash(
                payHereConfig.getMerchantId(),
                orderId,
                amount,
                currency,
                payHereConfig.getMerchantSecret()
        );

        // 9. Vehicle info
        String vehicleModel = "Vehicle Rental";
        long days = 1;
        try {
            if (booking.getVehicle() != null) vehicleModel = booking.getVehicle().getModel();
            if (booking.getStartDate() != null && booking.getEndDate() != null) {
                days = java.time.temporal.ChronoUnit.DAYS.between(booking.getStartDate(), booking.getEndDate());
                if (days < 1) days = 1;
            }
        } catch (Exception e) {
            log.warn("Could not load vehicle info: {}", e.getMessage());
        }
        String itemDesc = vehicleModel + " Rental - " + days + " day" + (days > 1 ? "s" : "");

        // 10. User info
        String[] nameParts = splitName(user.getFullName());
        String phone = (user.getPhone() != null && !user.getPhone().isBlank()) ? user.getPhone() : "0771234567";

        // 11.  Use clean URLs (strips IntelliJ ?_ijt= parameters)
        String returnUrl = payHereConfig.getCleanReturnUrl();
        String cancelUrl = payHereConfig.getCleanCancelUrl();
        log.info("Return URL: {}", returnUrl);
        log.info("Cancel URL: {}", cancelUrl);

        // 12. Build DTO
        PayHereInitDTO dto = new PayHereInitDTO();
        dto.setMerchantId(payHereConfig.getMerchantId());
        dto.setReturnUrl(returnUrl);
        dto.setCancelUrl(cancelUrl);
        dto.setNotifyUrl(payHereConfig.getNotifyUrl());
        dto.setOrderId(orderId);
        dto.setItems(itemDesc);
        dto.setCurrency(currency);
        dto.setAmount(amount);
        dto.setFirstName(nameParts[0]);
        dto.setLastName(nameParts[1]);
        dto.setEmail(user.getEmail() != null ? user.getEmail() : "customer@rentify.lk");
        dto.setPhone(phone);
        dto.setAddress("Sri Lanka");
        dto.setCity("Colombo");
        dto.setCountry("Sri Lanka");
        dto.setHash(hash);
        dto.setCheckoutUrl(payHereConfig.getCheckoutUrl());
        dto.setBookingId(bookingId);
        dto.setPaymentId(saved.getId());

        log.info("PayHere DTO ready: orderId={}, amount={}", orderId, amount);
        return dto;
    }

    @Override
    @Transactional
    public void handleNotify(PayHereNotifyDTO notify) {
        log.info("=== PayHere Notify: orderId={}, status={}", notify.getOrder_id(), notify.getStatus_code());

        // 1. Verify hash
        boolean valid = PayHereHashUtil.verifyNotifyHash(
                payHereConfig.getMerchantId(),
                notify.getOrder_id(),
                notify.getPayhere_amount(),
                notify.getPayhere_currency(),
                notify.getStatus_code(),
                payHereConfig.getMerchantSecret(),
                notify.getMd5sig()
        );
        if (!valid) {
            log.error("PayHere HASH MISMATCH: orderId={}", notify.getOrder_id());
            throw new SecurityException("PayHere hash verification failed.");
        }

        // 2. Find payment
        Payment payment = paymentRepository.findByTransactionId(notify.getOrder_id())
                .orElseThrow(() -> new ResourceNotFoundException("No payment for orderId: " + notify.getOrder_id()));

        // 3. Store PayHere details
        if (notify.getPayment_id() != null) payment.setPayherePaymentId(notify.getPayment_id());
        if (notify.getCard_no() != null && !notify.getCard_no().isBlank()) payment.setCardNumber(notify.getCard_no());
        if (notify.getCard_holder_name() != null) payment.setCardName(notify.getCard_holder_name());

        // 4. Update status
        switch (notify.getStatus_code()) {
            case STATUS_SUCCESS -> {
                payment.setStatus(PaymentStatus.COMPLETED);
                confirmBookingAndVehicle(payment.getBooking());
                log.info(" Payment COMPLETED: orderId={}", notify.getOrder_id());
                try {
                    emailService.sendPaymentSuccessEmail(
                            payment.getUser().getEmail(),
                            payment.getUser().getFullName(),
                            payment
                    );
                } catch (Exception e) {
                    log.warn("Email failed (non-critical): {}", e.getMessage());
                }
            }
            case STATUS_PENDING    -> { payment.setStatus(PaymentStatus.PENDING);  log.info("⏳ PENDING: {}", notify.getOrder_id()); }
            case STATUS_CANCELLED  -> { payment.setStatus(PaymentStatus.FAILED);   log.warn("❌ CANCELLED: {}", notify.getOrder_id()); }
            case STATUS_FAILED     -> { payment.setStatus(PaymentStatus.FAILED);   log.warn("❌ FAILED: {}", notify.getOrder_id()); }
            case STATUS_CHARGEDBACK-> { payment.setStatus(PaymentStatus.REFUNDED); log.warn("🔁 CHARGEDBACK: {}", notify.getOrder_id()); }
            default -> log.warn("Unknown status_code: {}", notify.getStatus_code());
        }

        paymentRepository.save(payment);
    }

    private void confirmBookingAndVehicle(Booking booking) {
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);
        if (booking.getVehicle() != null) {
            booking.getVehicle().setStatus(VehicleStatus.BOOKED);
            vehicleRepository.save(booking.getVehicle());
        }
    }

    private String[] splitName(String fullName) {
        if (fullName == null || fullName.isBlank()) return new String[]{"Customer", ""};
        String[] parts = fullName.trim().split("\\s+", 2);
        return parts.length == 2 ? new String[]{parts[0], parts[1]} : new String[]{parts[0], ""};
    }
}