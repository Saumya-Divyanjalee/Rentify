package lk.ijse.aad.backend.utill;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Slf4j
public class PayHereHashUtil {

    private PayHereHashUtil() {}

     public static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();

        } catch (Exception e) {
            throw new RuntimeException("MD5 hashing failed", e);
        }
    }

     public static String formatAmount(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }


    public static String generateCheckoutHash(
            String merchantId,
            String orderId,
            BigDecimal amount,
            String currency,
            String merchantSecret
    ) {
        String formattedAmount = formatAmount(amount);

        String secretHash = md5(merchantSecret).toUpperCase();

        String raw = merchantId
                + orderId
                + formattedAmount
                + currency
                + secretHash;

        String finalHash = md5(raw).toUpperCase();

        log.info("=== PAYHERE CHECKOUT HASH DEBUG ===");
        log.info("merchantId     : {}", merchantId);
        log.info("orderId        : {}", orderId);
        log.info("amount         : {}", formattedAmount);
        log.info("currency       : {}", currency);
        log.info("secretHash     : {}", secretHash);
        log.info("raw string     : {}", raw);
        log.info("final hash     : {}", finalHash);

        return finalHash;
    }


    public static boolean verifyNotifyHash(
            String merchantId,
            String orderId,
            String payhereAmount,
            String payhereCurrency,
            String statusCode,
            String merchantSecret,
            String receivedMd5sig
    ) {
        String secretHash = md5(merchantSecret).toUpperCase();

        String raw = merchantId
                + orderId
                + payhereAmount
                + payhereCurrency
                + statusCode
                + secretHash;

        String expected = md5(raw).toUpperCase();

        boolean match = expected.equalsIgnoreCase(receivedMd5sig);

        log.info("=== PAYHERE NOTIFY HASH DEBUG ===");
        log.info("expected hash  : {}", expected);
        log.info("received hash  : {}", receivedMd5sig);

        if (!match) {
            log.warn("HASH MISMATCH DETECTED");
        }

        return match;
    }
}