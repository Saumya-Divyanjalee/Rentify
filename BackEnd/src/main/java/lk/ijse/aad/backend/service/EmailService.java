package lk.ijse.aad.backend.service.impl;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * General-purpose email sender used by controllers directly.
     * Fixed: was missing — controllers called emailService.sendEmail() but the
     * method did not exist publicly. Previously only private send() existed.
     */
    @Async
    public void sendEmail(String to, String subject, String body) {
        sendHtml(to, subject, wrap(
                "<p style='color:#555;line-height:1.8;white-space:pre-line'>" + body + "</p>"
        ));
    }

    /** Welcome email sent after signup. */
    @Async
    public void sendWelcomeEmail(String to, String fullName) {
        sendHtml(to, "Welcome to Rentify 🚗", buildWelcomeHtml(fullName));
    }

    /** Login notification email. */

    @Async
    public void sendLoginNotificationEmail(String to, String username) {
        String subject = "New Sign-In Detected 🔐";
        String htmlBody = buildLoginHtml(username);
        sendHtml(to, subject, htmlBody);
    }

    // ── Internal send helper ──────────────────────────────────────────────────

    private void sendHtml(String to, String subject, String html) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(msg);
        } catch (Exception e) {
            System.err.println("[EmailService] Failed to send to " + to + " : " + e.getMessage());
        }
    }

    // ── HTML templates ────────────────────────────────────────────────────────

    private String wrap(String body) {
        return "<!DOCTYPE html><html><body style='margin:0;padding:0;background:#f4f4f0;font-family:monospace'>"
                + "<div style='max-width:580px;margin:40px auto;background:#fff;border-radius:16px;"
                + "overflow:hidden;border:2px solid #FFC107'>"
                + "<div style='background:#0a0a0a;padding:28px 36px;display:flex;align-items:center;gap:14px'>"
                + "<div style='width:42px;height:42px;background:#FFC107;border-radius:10px;"
                + "display:flex;align-items:center;justify-content:center;font-weight:700;"
                + "font-size:18px;color:#0a0a0a'>R</div>"
                + "<span style='font-size:26px;font-weight:700;color:#fff'>Rent"
                + "<span style=\"color:#FFC107\">ify</span></span></div>"
                + "<div style='padding:36px'>" + body + "</div>"
                + "<div style='background:#f4f4f0;padding:16px 36px;text-align:center;"
                + "font-size:11px;color:#999'>© 2026 Rentify Inc. · Sri Lanka 🇱🇰</div>"
                + "</div></body></html>";
    }

    private String buildWelcomeHtml(String fullName) {
        return wrap(
                "<h2 style='color:#0a0a0a;margin:0 0 14px;font-size:22px'>Welcome, " + fullName + "! 🎉</h2>"
                        + "<p style='color:#555;line-height:1.8;margin:0 0 20px'>Your Rentify account is ready. "
                        + "Explore Sri Lanka's #1 cab rental platform with verified drivers, transparent pricing "
                        + "and instant booking.</p>"
                        + "<div style='margin:28px 0;text-align:center'>"
                        + "<a href='http://localhost/pages/sign-in.html' style='background:#16a34a;color:#fff;"
                        + "padding:14px 32px;border-radius:8px;text-decoration:none;font-weight:700;"
                        + "font-size:14px'>Get Started →</a></div>"
                        + "<p style='color:#aaa;font-size:12px'>If you didn't create this account, "
                        + "please ignore this email.</p>"
        );
    }

    private String buildLoginHtml(String username) {
        return wrap(
                "<h2 style='color:#0a0a0a;margin:0 0 14px;font-size:22px'>New Sign-In Detected 🔐</h2>"
                        + "<p style='color:#555;line-height:1.8;margin:0 0 16px'>A login was recorded for account: "
                        + "<strong style='color:#0a0a0a'>" + username + "</strong></p>"
                        + "<div style='background:#fefce8;border:1.5px solid #fde68a;padding:14px 18px;"
                        + "border-radius:10px;margin-bottom:20px'>"
                        + "<p style='margin:0;color:#92400e;font-size:13px'>⚠️ If this wasn't you, "
                        + "change your password immediately to secure your account.</p></div>"
                        + "<p style='color:#aaa;font-size:12px'>This is an automated security notification "
                        + "from Rentify.</p>"
        );
    }
}