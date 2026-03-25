package lk.ijse.aad.backend.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class PayHereConfig {

    @Value("${payhere.merchant-id}")
    private String merchantId;

    @Value("${payhere.merchant-secret}")
    private String merchantSecret;

    @Value("${payhere.sandbox:true}")
    private boolean sandbox;

    @Value("${payhere.notify-url}")
    private String notifyUrl;

    @Value("${payhere.return-url}")
    private String returnUrl;

    @Value("${payhere.cancel-url}")
    private String cancelUrl;

    public String getCheckoutUrl() {
        return sandbox
                ? "https://sandbox.payhere.lk/pay/checkout"
                : "https://www.payhere.lk/pay/checkout";
    }

    /**
     * ✅ Strip any IntelliJ ?_ijt= or ?_ij_reload= parameters from URLs
     * IntelliJ automatically appends these when opening HTML files
     * PayHere rejects URLs with unknown query parameters
     */
    public String getCleanReturnUrl() {
        return stripIntelliJParams(returnUrl);
    }

    public String getCleanCancelUrl() {
        return stripIntelliJParams(cancelUrl);
    }

    private String stripIntelliJParams(String url) {
        if (url == null) return url;
        // Remove everything from ? onwards if it contains IntelliJ params
        if (url.contains("?_ijt=") || url.contains("&_ijt=") ||
                url.contains("?_ij_reload=") || url.contains("&_ij_reload=")) {
            int idx = url.indexOf('?');
            if (idx != -1) return url.substring(0, idx);
        }
        return url;
    }
}