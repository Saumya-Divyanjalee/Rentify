package lk.ijse.aad.backend.service;


import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Sends a welcome email to the new user.
     * Runs asynchronously so it doesn't block the signup response.
     */
    @Async
    public void sendWelcomeEmail(String toEmail, String fullName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("🏠 Welcome to Rentify — Your Account is Ready!");

            String html = buildWelcomeHtml(fullName);
            helper.setText(html, true);  // true = HTML content

            mailSender.send(message);
            log.info("Welcome email sent to: {}", toEmail);

        } catch (Exception e) {
            // Log the error but don't fail signup
            log.error("Failed to send welcome email to {}: {}", toEmail, e.getMessage());
        }
    }

    private String buildWelcomeHtml(String fullName) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
              <meta charset="UTF-8">
              <style>
                body { font-family: 'Segoe UI', Arial, sans-serif; background: #f0fdf4; margin: 0; padding: 0; }
                .container { max-width: 560px; margin: 40px auto; background: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 24px rgba(0,0,0,0.08); }
                .header { background: linear-gradient(135deg, #14532d 0%, #16a34a 100%); padding: 40px 32px; text-align: center; }
                .logo { font-size: 36px; font-weight: 800; color: #ffffff; letter-spacing: -1px; }
                .logo span { color: #facc15; }
                .tagline { color: rgba(255,255,255,0.8); font-size: 14px; margin-top: 6px; }
                .body { padding: 36px 32px; }
                .greeting { font-size: 22px; font-weight: 700; color: #14532d; margin-bottom: 12px; }
                .text { font-size: 15px; color: #374151; line-height: 1.7; margin-bottom: 16px; }
                .highlight-box { background: #f0fdf4; border-left: 4px solid #16a34a; border-radius: 8px; padding: 16px 20px; margin: 20px 0; }
                .highlight-box p { margin: 0; font-size: 14px; color: #14532d; font-weight: 500; }
                .btn { display: inline-block; background: #16a34a; color: #ffffff; text-decoration: none; padding: 14px 32px; border-radius: 10px; font-weight: 700; font-size: 15px; margin: 8px 0; }
                .footer { background: #f9fefb; padding: 20px 32px; text-align: center; font-size: 12px; color: #9ca3af; border-top: 1px solid #d1fae5; }
              </style>
            </head>
            <body>
              <div class="container">
                <div class="header">
                  <div class="logo">Rent<span>ify</span></div>
                  <div class="tagline">One click. One key. Unlimited freedom.</div>
                </div>
                <div class="body">
                  <div class="greeting">Hi %s, welcome aboard! 🎉</div>
                  <p class="text">
                    Thank you for joining <strong>Rentify</strong> — the smarter way to find and manage rentals.
                    Your account has been successfully created.
                  </p>
                  <div class="highlight-box">
                    <p>✅ Your account is active and ready to use.<br>
                       🔑 Log in anytime to browse listings, manage bookings, and more.</p>
                  </div>
                  <p class="text">
                    Explore thousands of verified listings across 50+ cities and experience
                    instant booking with our trusted platform.
                  </p>
                  <p style="text-align:center; margin-top:28px;">
                    <a href="http://localhost/pages/sign-in.html" class="btn">Go to My Dashboard →</a>
                  </p>
                </div>
                <div class="footer">
                  © 2026 Rentify Inc. · You received this because you signed up at rentify.com<br>
                  If you did not create this account, please ignore this email.
                </div>
              </div>
            </body>
            </html>
            """.formatted(fullName);
    }
}
