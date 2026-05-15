package velmora.composer.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.security.SecureRandom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Відправляє email-листи з кодом підтвердження реєстрації.
 * При відсутності конфігурації SMTP логує код в консоль (dev mode).
 * Генерує 6-значні коди через SecureRandom.
 */
@Service
public class EmailService {

  @Autowired(required = false)
  private JavaMailSender mailSender;

  private static final SecureRandom RNG = new SecureRandom();

  public String generateCode() {
    return String.format("%06d", RNG.nextInt(1000000));
  }

  public void sendVerificationEmail(String to, String code) {
    String html = buildVerificationHtml(code);

    if (mailSender == null) {
      System.out.println("[EMAIL] MailSender not configured");
      System.out.println("[EMAIL] Verification code for " + to + ": " + code);
      return;
    }

    try {
      MimeMessage msg = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
      helper.setTo(to);
      helper.setSubject("Verify your Velmora account");
      helper.setText(html, true);
      mailSender.send(msg);
      System.out.println("[EMAIL] Sent verification to " + to);
    } catch (MessagingException e) {
      System.out.println("[EMAIL] Failed to send to " + to + ": " + e.getMessage());
      System.out.println("[EMAIL] Verification code for " + to + ": " + code);
    }
  }

  private String buildVerificationHtml(String code) {
    return """
        <!DOCTYPE html>
        <html>
        <head><meta charset="UTF-8"/></head>
        <body style="margin:0;padding:0;background:#F6F3EE;font-family:'Inter','Helvetica',sans-serif;">
          <table width="100%%" cellpadding="0" cellspacing="0" style="background:#F6F3EE;padding:40px 20px;">
            <tr><td align="center">
              <table width="480" cellpadding="0" cellspacing="0" style="background:#FFFFFF;border-radius:16px;box-shadow:0 4px 24px rgba(0,0,0,0.04);">
                <tr><td style="padding:48px 40px 32px;" align="center">
                  <h1 style="font-family:'Times New Roman',serif;font-size:42px;color:#1A1A1A;letter-spacing:8px;margin:0 0 4px;">VELMORA</h1>
                  <p style="color:#78A0A0;font-size:12px;letter-spacing:4px;margin:0 0 24px;">OLFACTORY LABORATORY</p>
                  <hr style="border:none;border-top:1px solid #E8E4DE;margin:0 0 24px;"/>
                  <p style="color:#1A1A1A;font-size:14px;line-height:1.6;margin:0 0 4px;">Welcome to</p>
                  <p style="color:#1A1A1A;font-size:16px;font-weight:600;margin:0 0 20px;">Velmora Olfactory Lab</p>
                  <p style="color:#888;font-size:13px;line-height:1.5;margin:0 0 24px;">
                    Your journey into the art of fragrance composition begins here.<br/>
                    Use the code below to verify your account.
                  </p>
                  <div style="background:#F6F3EE;border:1px solid #E2A998;border-radius:12px;padding:20px 32px;margin:0 0 24px;display:inline-block;">
                    <span style="font-family:'Courier New',monospace;font-size:32px;font-weight:bold;color:#1A1A1A;letter-spacing:8px;">%s</span>
                  </div>
                  <p style="color:#A4A0C5;font-size:11px;line-height:1.5;margin:0 0 4px;">
                    This code expires in 15 minutes.
                  </p>
                  <p style="color:#B0ADA8;font-size:10px;line-height:1.5;margin:0;">
                    If you didn't create an account, ignore this email.
                  </p>
                  <hr style="border:none;border-top:1px solid #E8E4DE;margin:24px 0 16px;"/>
                  <p style="color:#B0ADA8;font-size:10px;margin:0;">
                    Velmora &middot; Digital Olfactory Laboratory
                  </p>
                </td></tr>
              </table>
            </td></tr>
          </table>
        </body>
        </html>
        """.formatted(code);
  }
}
