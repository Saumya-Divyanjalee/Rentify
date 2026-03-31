package lk.ijse.aad.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponseDTO {
    private Long paymentId;
    private String transactionId;
    private String status;
    private String paymentMethod;
    private BigDecimal amount;
    private String currency;
    private LocalDateTime createdAt;


    private Long bookingId;


    private String userName;
    private String userEmail;
}
