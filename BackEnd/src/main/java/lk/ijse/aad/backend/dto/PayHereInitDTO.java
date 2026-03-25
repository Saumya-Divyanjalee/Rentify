package lk.ijse.aad.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayHereInitDTO {
    /* ── PayHere required fields ── */
    private String merchantId;
    private String returnUrl;
    private String cancelUrl;
    private String notifyUrl;

    private String orderId;         // maps to our Payment.transactionId
    private String items;           // e.g. "Toyota Prius Rental - 3 days"
    private String currency;        // "LKR"
    private BigDecimal amount;

    /* ── Buyer info ── */
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String country;

    /* ── Security hash (generated server-side) ── */
    private String hash;

    /* ── Checkout URL (sandbox / live) ── */
    private String checkoutUrl;

    /* ── Our internal booking/payment IDs for frontend reference ── */
    private Long bookingId;
    private Long paymentId;

}
