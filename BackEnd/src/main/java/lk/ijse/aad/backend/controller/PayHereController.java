package lk.ijse.aad.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lk.ijse.aad.backend.dto.PayHereInitDTO;
import lk.ijse.aad.backend.dto.PayHereNotifyDTO;
import lk.ijse.aad.backend.service.custom.PayHereService;
import lk.ijse.aad.backend.utill.APIResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController   // ✅ FIXED — was missing, that's why 500 error came
@RequestMapping("/api/v1/payments/payhere")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "PayHere Gateway", description = "PayHere payment gateway integration")
public class PayHereController {

    private final PayHereService payHereService;

    /**
     * POST /api/v1/payments/payhere/initiate?bookingId=5
     * USER calls this → creates PENDING payment → returns hash + fields for PayHere form
     */
    @Operation(summary = "Initiate PayHere payment",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/initiate")
    public ResponseEntity<APIResponse<PayHereInitDTO>> initiate(
            @RequestParam Long bookingId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            log.info("PayHere initiate: bookingId={}, user={}", bookingId, userDetails.getUsername());
            PayHereInitDTO dto = payHereService.initiatePayment(bookingId, userDetails.getUsername());
            return ResponseEntity.ok(new APIResponse<>(200, "Payment initiated", dto));
        } catch (Exception e) {
            log.error("PayHere initiate FAILED: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new APIResponse<>(500, e.getMessage(), null));
        }
    }

    /**
     * POST /api/v1/payments/payhere/notify
     * PayHere servers call this after payment — PUBLIC endpoint (no JWT)
     * Security = MD5 hash verification inside service
     */
    @Operation(summary = "PayHere notify webhook — called by PayHere servers")
    @PostMapping("/notify")
    public ResponseEntity<String> notify(@ModelAttribute PayHereNotifyDTO notify) {
        log.info("=== PayHere Notify === orderId={}, status={}", notify.getOrder_id(), notify.getStatus_code());
        try {
            payHereService.handleNotify(notify);
            return ResponseEntity.ok("OK");
        } catch (SecurityException e) {
            log.error("PayHere hash FAILED: {}", e.getMessage());
            return ResponseEntity.status(403).body("HASH_FAILED");
        } catch (Exception e) {
            log.error("PayHere notify error: {}", e.getMessage(), e);
            return ResponseEntity.ok("ERROR_LOGGED"); // return 200 so PayHere doesn't retry
        }
    }

    /**
     * GET /api/v1/payments/payhere/status?orderId=RNT-XXXX
     * Frontend polls this after PayHere return redirect
     */

    @GetMapping("/status")
    public ResponseEntity<APIResponse<String>> checkStatus(@RequestParam String orderId) {
        return ResponseEntity.ok(new APIResponse<>(200, "Query /api/v1/payments/booking/{id} for full status", orderId));
    }
}