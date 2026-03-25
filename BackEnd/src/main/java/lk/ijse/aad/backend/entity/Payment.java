package lk.ijse.aad.backend.entity;

import jakarta.persistence.*;
import lk.ijse.aad.backend.enums.PaymentMethod;
import lk.ijse.aad.backend.enums.PaymentStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor   // Required by JPA: creates a no-argument constructor
@ToString
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Relationships ─────────────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ── Payment core ──────────────────────────────────────────────────────

    @Column(name = "transaction_id", unique = true, nullable = false)
    private String transactionId;      // our orderId / RNT-XXXXXXXXXX

    /**
     * PayHere's own payment_id (received in notify).
     * Null until PayHere notifies us.
     */
    @Column(name = "payhere_payment_id")
    private String payherePaymentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;  // CARD | CASH | ONLINE

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;         // PENDING | COMPLETED | FAILED | REFUNDED

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", length = 10)
    private String currency = "LKR";

    // ── Card details (manual card payments — masked) ───────────────────

    @Column(name = "card_name")
    private String cardName;

    @Column(name = "card_number")
    private String cardNumber;     // stored masked: **** **** **** 1234

    @Column(name = "expiry_date")
    private String expiryDate;

    @Column(name = "cvv")
    private String cvv;            // always stored as "***"

    // ── Metadata ──────────────────────────────────────────────────────────

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}