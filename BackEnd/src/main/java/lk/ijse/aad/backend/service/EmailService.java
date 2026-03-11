package lk.ijse.aad.backend.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // ──────────────────────────────────────────────────
    // 1. WELCOME EMAIL — triggered on signup
    // ──────────────────────────────────────────────────
    @Async
    public void sendWelcomeEmail(String toEmail, String fullName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("🏠 Welcome to Rentify — Your Account is Ready!");
            helper.setText(buildWelcomeHtml(fullName), true);
            mailSender.send(message);
            log.info("✅ Welcome email sent → {}", toEmail);
        } catch (Exception e) {
            log.error("❌ Welcome email failed for {}: {}", toEmail, e.getMessage());
        }
    }

    // ──────────────────────────────────────────────────
    // 2. LOGIN NOTIFICATION EMAIL — triggered on signin
    // ──────────────────────────────────────────────────
    @Async
    public void sendLoginNotificationEmail(String toEmail, String username) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("🔔 Rentify — New Login to Your Account");
            helper.setText(buildLoginHtml(username), true);
            mailSender.send(message);
            log.info("✅ Login notification sent → {}", toEmail);
        } catch (Exception e) {
            log.error("❌ Login email failed for {}: {}", toEmail, e.getMessage());
        }
    }

    // ──────────────────────────────────────────────────
    // HTML BUILDERS
    // ──────────────────────────────────────────────────
    private String buildWelcomeHtml(String fullName) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
              <meta charset="UTF-8">
              <style>
                body{font-family:'Segoe UI',Arial,sans-serif;background:#f0fdf4;margin:0;padding:0}
                .wrap{max-width:560px;margin:40px auto;background:#fff;border-radius:16px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,0.08)}
                .head{background:linear-gradient(135deg,#14532d 0%%,#16a34a 100%%);padding:40px 32px;text-align:center}
                .logo{font-size:36px;font-weight:800;color:#fff;letter-spacing:-1px}
                .logo span{color:#facc15}
                .tag{color:rgba(255,255,255,0.8);font-size:14px;margin-top:6px}
                .body{padding:36px 32px}
                .hi{font-size:22px;font-weight:700;color:#14532d;margin-bottom:12px}
                .txt{font-size:15px;color:#374151;line-height:1.7;margin-bottom:16px}
                .box{background:#f0fdf4;border-left:4px solid #16a34a;border-radius:8px;padding:16px 20px;margin:20px 0}
                .box p{margin:0;font-size:14px;color:#14532d;font-weight:500}
                .btn{display:inline-block;background:#16a34a;color:#fff;text-decoration:none;padding:14px 32px;border-radius:10px;font-weight:700;font-size:15px}
                .foot{background:#f9fefb;padding:20px 32px;text-align:center;font-size:12px;color:#9ca3af;border-top:1px solid #d1fae5}
              </style>
            </head>
            <body>
              <div class="wrap">
                <div class="head">
                  <div class="logo">Rent<span>ify</span></div>
                  <div class="tag">One click. One key. Unlimited freedom.</div>
                </div>
                <div class="body">
                  <div class="hi">Hi %s, welcome aboard! 🎉</div>
                  <p class="txt">Thank you for joining <strong>Rentify</strong>. Your account is active and ready to use.</p>
                  <div class="box">
                    <p>✅ Account created successfully<br>🔑 Log in anytime to browse listings<br>🏠 Start booking your first rental</p>
                  </div>
                  <p style="text-align:center;margin-top:28px">
                    <a href="http://localhost/pages/sign-in.html" class="btn">Go to Dashboard →</a>
                  </p>
                </div>
                <div class="foot">© 2026 Rentify Inc. · If you didn't create this account, ignore this email.</div>
              </div>
            </body>
            </html>
            """.formatted(fullName == null ? "there" : fullName);
    }

    private String buildLoginHtml(String username) {
        String time = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));
        return """
            <!DOCTYPE html>
            <html>
            <head>
              <meta charset="UTF-8">
              <style>
                body{font-family:'Segoe UI',Arial,sans-serif;background:#f0fdf4;margin:0;padding:0}
                .wrap{max-width:560px;margin:40px auto;background:#fff;border-radius:16px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,0.08)}
                .head{background:linear-gradient(135deg,#1e3a5f 0%%,#1d4ed8 100%%);padding:36px 32px;text-align:center}
                .logo{font-size:32px;font-weight:800;color:#fff;letter-spacing:-1px}
                .logo span{color:#facc15}
                .tag{color:rgba(255,255,255,0.7);font-size:13px;margin-top:4px}
                .body{padding:32px 32px 24px}
                .title{font-size:20px;font-weight:700;color:#1e3a5f;margin-bottom:10px}
                .txt{font-size:14px;color:#374151;line-height:1.7;margin-bottom:12px}
                .info{background:#eff6ff;border:1px solid #bfdbfe;border-radius:10px;padding:16px 20px;margin:18px 0}
                .row{display:flex;justify-content:space-between;font-size:13px;padding:5px 0;border-bottom:1px solid #dbeafe}
                .row:last-child{border-bottom:none}
                .lbl{color:#64748b;font-weight:600}
                .val{color:#1e3a5f;font-weight:700}
                .warn{background:#fff7ed;border:1px solid #fed7aa;border-radius:10px;padding:14px 18px;font-size:13px;color:#92400e;margin-top:16px}
                .foot{background:#f8fafc;padding:18px 32px;text-align:center;font-size:12px;color:#94a3b8;border-top:1px solid #e2e8f0}
              </style>
            </head>
            <body>
              <div class="wrap">
                <div class="head">
                  <div class="logo">Rent<span>ify</span></div>
                  <div class="tag">Security Notification</div>
                </div>
                <div class="body">
                  <div class="title">🔔 New Login Detected</div>
                  <p class="txt">A sign-in was just recorded on your <strong>Rentify</strong> account.</p>
                  <div class="info">
                    <div class="row"><span class="lbl">Username</span><span class="val">%s</span></div>
                    <div class="row"><span class="lbl">Time</span><span class="val">%s</span></div>
                    <div class="row"><span class="lbl">Status</span><span class="val" style="color:#16a34a">✅ Successful</span></div>
                  </div>
                  <div class="warn">⚠️ If this wasn't you, <strong>change your password immediately</strong> and contact support.</div>
                </div>
                <div class="foot">© 2026 Rentify · Automated security notification.</div>
              </div>
            </body>
            </html>
            """.formatted(username, time);
    }
}