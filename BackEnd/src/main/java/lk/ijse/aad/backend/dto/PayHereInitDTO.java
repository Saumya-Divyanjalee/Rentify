package lk.ijse.aad.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayHereInitDTO {
     private String merchantId;
    private String returnUrl;
    private String cancelUrl;
    private String notifyUrl;

    private String orderId;
    private String items;
    private String currency;
    private BigDecimal amount;

    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String country;


    private String hash;

     private String checkoutUrl;

     private Long bookingId;
    private Long paymentId;

}
