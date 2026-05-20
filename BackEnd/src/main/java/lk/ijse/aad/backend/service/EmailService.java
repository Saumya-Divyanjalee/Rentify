package lk.ijse.aad.backend.service;

import jakarta.mail.internet.MimeMessage;
import lk.ijse.aad.backend.entity.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Async
    public void sendEmail(String to, String subject, String body) {
        sendHtml(to, subject, wrap(
                "<p style='color:#555;line-height:1.8;white-space:pre-line'>" + body + "</p>"
        ));
    }

    @Async
    public void sendWelcomeEmail(String to, String fullName) {
        sendHtml(to, "Welcome to Rentify 🚗", buildWelcomeHtml(fullName));
    }

    @Async
    public void sendLoginNotificationEmail(String to, String username) {
        sendHtml(to, "New Sign-In Detected 🔐", buildLoginHtml(username));
    }

    // ═══════════════════════════════════════════════════
    // FIX: Accept pre-loaded strings instead of Payment object
    // Reason: Payment entity has LAZY relationships.
    // Passing the Payment object and calling
    // payment.getBooking().getVehicle().getModel()
    // inside buildPaymentHtml() causes
    // LazyInitializationException because the
    // Hibernate session is already closed.
    //
    // Solution: Pre-load all needed data in
    // PayHereServiceImpl while session is open,
    // then pass as simple String values here.
    // ═══════════════════════════════════════════════════
    public void sendPaymentSuccessEmail(
            String to,
            String fullName,
            String transactionId,
            String amount,
            String currency,
            String paymentMethod,
            String vehicleModel,
            String bookingDates,
            String bookingId,
            String cardNumber,
            String dateTime) {

        try {
            String html = buildPaymentHtml(
                    fullName,
                    transactionId,
                    amount,
                    currency,
                    paymentMethod,
                    vehicleModel,
                    bookingDates,
                    bookingId,
                    cardNumber,
                    dateTime
            );
            sendHtml(to, "✅ Payment Confirmed – Rentify", html);
            log.info("✅ Payment email sent to: {}", to);
        } catch (Exception e) {
            log.warn("Payment email failed: {}", e.getMessage());
            log.error("Payment email full error: ", e);
            // Do NOT rethrow — email failure is non-critical
            // Payment is already COMPLETED in database
        }
    }

    // ─────────────────────────────────────────────────────
    // sendHtml — internal helper
    // Does NOT rethrow exception
    // Logs error clearly instead
    // ─────────────────────────────────────────────────────
    private void sendHtml(String to, String subject, String html) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(msg);
            log.info("✅ Email sent → to={} subject={}", to, subject);
        } catch (Exception e) {
            log.error("❌ sendHtml FAILED → to={} subject={} error={}",
                    to, subject, e.getMessage(), e);
            // Do NOT rethrow — caller handles failure
        }
    }

    // ─────────────────────────────────────────────────────
    // HTML TEMPLATES
    // ─────────────────────────────────────────────────────

    private String wrap(String body) {
        return "<!DOCTYPE html><html><body style='margin:0;padding:0;background:#0d0d0d;" +
                "font-family:\"Roboto Mono\",monospace'>"
                + "<div style='max-width:600px;margin:40px auto;background:#1a1a1a;" +
                "border-radius:16px;overflow:hidden;border:1px solid #2a2a2a'>"
                + "<div style='background:#161616;padding:24px 36px;" +
                "border-bottom:2px solid #f5c518'>"
                + "<span style='font-size:24px;font-weight:700;color:#fff;" +
                "letter-spacing:-1px'>Rent"
                + "<span style=\"color:#f5c518\">ify</span></span>"
                + "<span style='margin-left:12px;font-size:10px;color:#666;" +
                "letter-spacing:2px;text-transform:uppercase'>Vehicle Rentals</span>"
                + "</div>"
                + "<div style='padding:36px'>" + body + "</div>"
                + "<div style='background:#111;padding:16px 36px;text-align:center;" +
                "font-size:11px;color:#444;border-top:1px solid #222'>" +
                "© 2026 Rentify · Sri Lanka 🇱🇰</div>"
                + "</div></body></html>";
    }

    private String buildWelcomeHtml(String fullName) {
        return wrap(
                "<h2 style='color:#fff;margin:0 0 14px;font-size:20px'>" +
                        "Welcome, " + fullName + "! 🎉</h2>"
                        + "<p style='color:#999;line-height:1.9;margin:0 0 20px;font-size:13px'>" +
                        "Your Rentify account is ready. "
                        + "Explore Sri Lanka's #1 vehicle rental platform.</p>"
                        + "<p style='color:#555;font-size:11px'>" +
                        "If you didn't create this account, ignore this email.</p>"
        );
    }

    private String buildLoginHtml(String username) {
        return wrap(
                "<h2 style='color:#fff;margin:0 0 14px;font-size:20px'>" +
                        "New Sign-In Detected 🔐</h2>"
                        + "<p style='color:#999;line-height:1.9;margin:0 0 16px;font-size:13px'>" +
                        "A login was recorded for: "
                        + "<strong style='color:#f5c518'>" + username + "</strong></p>"
                        + "<div style='background:#1f1a0a;border:1px solid #7a5c00;" +
                        "padding:14px 18px;border-radius:10px;margin-bottom:20px'>"
                        + "<p style='margin:0;color:#d4a800;font-size:12px'>⚠️ If this wasn't you, "
                        + "change your password immediately.</p></div>"
                        + "<p style='color:#555;font-size:11px'>" +
                        "Automated security notification from Rentify.</p>"
        );
    }

    // ─────────────────────────────────────────────────────
    // FIX: Accepts plain Strings — no LAZY loading!
    // All data pre-loaded in PayHereServiceImpl
    // ─────────────────────────────────────────────────────
    private String buildPaymentHtml(
            String fullName,
            String transactionId,
            String amount,
            String currency,
            String paymentMethod,
            String vehicleModel,
            String bookingDates,
            String bookingId,
            String cardNumber,
            String dateTime) {

        return wrap(
                "<div style='background:#0a1f0a;border:1px solid #166534;" +
                        "border-radius:12px;padding:20px;text-align:center;margin-bottom:28px'>"
                        + "<div style='font-size:36px;margin-bottom:8px'>✅</div>"
                        + "<h2 style='color:#22c55e;margin:0;font-size:18px;font-weight:700'>" +
                        "Payment Successful!</h2>"
                        + "<p style='color:#4ade80;margin:6px 0 0;font-size:12px'>" +
                        "Transaction confirmed</p>"
                        + "</div>"
                        + "<p style='color:#e8e8e8;font-size:14px;margin:0 0 20px'>Hi " +
                        "<strong>" + fullName + "</strong>,</p>"
                        + "<p style='color:#999;font-size:13px;line-height:1.8;margin:0 0 24px'>"
                        + "Your payment has been received and your booking is now "
                        + "<strong style='color:#22c55e'>CONFIRMED</strong>. "
                        + "Your driver will contact you before pickup.</p>"
                        + "<div style='background:#161616;border:1px solid #2a2a2a;" +
                        "border-radius:12px;overflow:hidden;margin-bottom:24px'>"
                        + "<div style='padding:14px 20px;border-bottom:1px solid #222;" +
                        "background:#1f1f1f'>"
                        + "<span style='font-size:10px;font-weight:700;letter-spacing:2px;" +
                        "text-transform:uppercase;color:#666'>Transaction Details</span>"
                        + "</div>"
                        + buildDetailRow("Transaction ID",  transactionId, "#f5c518")
                        + buildDetailRow("Amount",
                        "Rs " + amount + " " + currency, "#22c55e")
                        + buildDetailRow("Payment Method",  paymentMethod, null)
                        + buildDetailRow("Date & Time",     dateTime, null)
                        + buildDetailRow("Vehicle",         vehicleModel, null)
                        + buildDetailRow("Booking Period",  bookingDates, null)
                        + buildDetailRow("Booking ID",      "BK-" + bookingId, null)
                        + buildDetailRow("Status",          "✅ COMPLETED", "#22c55e")
                        + "</div>"
                        + (cardNumber != null && !cardNumber.isBlank()
                        ? "<div style='background:#0f0f1a;border:1px solid #1e1e3a;" +
                        "border-radius:10px;padding:14px 20px;margin-bottom:24px'>"
                        + "<p style='margin:0 0 4px;font-size:10px;color:#555;" +
                        "letter-spacing:1.5px;text-transform:uppercase'>Card Used</p>"
                        + "<p style='margin:0;color:#7c9cff;font-size:14px;" +
                        "letter-spacing:3px'>" + cardNumber + "</p>"
                        + "</div>"
                        : "")
                        + "<p style='color:#555;font-size:11px;line-height:1.7'>"
                        + "Please keep your Transaction ID safe for future reference. "
                        + "For any queries, contact our support team.</p>"
        );
    }

    private String buildDetailRow(
            String label, String value, String valueColor) {
        String color = valueColor != null ? valueColor : "#ccc";
        return "<div style='display:flex;justify-content:space-between;" +
                "align-items:center;padding:11px 20px;border-bottom:1px solid #1e1e1e'>"
                + "<span style='font-size:12px;color:#666'>" + label + "</span>"
                + "<span style='font-size:12px;font-weight:600;color:" +
                color + "'>" + value + "</span>"
                + "</div>";
    }
}