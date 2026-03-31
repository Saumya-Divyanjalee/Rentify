package lk.ijse.aad.backend.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

//generate getters for all fields
@Getter
//register this class in spring container as a configuration class
@Configuration
public class PayHereConfig {

    //merchant id from payhere dashboard(used to identify merchant account)
    @Value("${payhere.merchant-id}")
    private String merchantId;

    //secret key used for security(hash generation and verification)
    @Value("${payhere.merchant-secret}")
    private String merchantSecret;

    // Environment flag → true = sandbox (testing), false = live (real payments)
    @Value("${payhere.sandbox:true}")
    private boolean sandbox;

    //payhere will send payment status to this url (backend endpoint)
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