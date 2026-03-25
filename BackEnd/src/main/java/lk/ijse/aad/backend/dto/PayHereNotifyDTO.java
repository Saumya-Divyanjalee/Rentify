package lk.ijse.aad.backend.dto;


import lombok.Data;

@Data
public class PayHereNotifyDTO {
    private String merchant_id;
    private String order_id;        // our transactionId
    private String payment_id;      // PayHere's own payment ID
    private String payhere_amount;  // e.g. "1000.00"
    private String payhere_currency;
    private String status_code;     // 2=success, 0=pending, -1=canceled, -2=failed, -3=chargedback
    private String md5sig;          // signature to verify
    private String status_message;
    private String method;          // "VISA", "MASTER", etc.
    private String card_holder_name;
    private String card_no;         // masked
    private String card_expiry;
    private String custom_1;        // we store bookingId here
    private String custom_2;        // we store userId here
}
