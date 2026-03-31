package lk.ijse.aad.backend.dto;


import lombok.Data;

@Data
public class PayHereNotifyDTO {
    private String merchant_id;
    private String order_id;
    private String payment_id;
    private String payhere_amount;
    private String payhere_currency;
    private String status_code;
    private String md5sig;
    private String status_message;
    private String method;
    private String card_holder_name;
    private String card_no;
    private String card_expiry;
    private String custom_1;
    private String custom_2;
}
