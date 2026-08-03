package com.example.backend.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import sibApi.TransactionalEmailsApi;
import sibModel.CreateSmtpEmail;
import sibModel.SendSmtpEmail;
import sibModel.SendSmtpEmailSender;
import sibModel.SendSmtpEmailTo;

import java.util.List;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${brevo.api.key:}")
    private String brevoApiKey;

    private static final String BRAND_COLOR  = "#4c8cff";
    private static final String DARK_BG      = "#0f172a";
    private static final String CARD_BG      = "#1e293b";
    private static final String TEXT_PRIMARY = "#f1f5f9";
    private static final String TEXT_MUTED   = "#94a3b8";
    private static final String BORDER       = "#334155";

    // ── Public API ────────────────────────────────────────────────────────────

    /** Send a plain notification email (task / project events). */
    public void sendTaskUpdateEmail(String to, String subject, String body) {
        sendHtml(to, subject, body);
    }

    /** Send the OTP email with a large, prominent code block. */
    public void sendOtpEmail(String to, String otp) {
        String subject = "Your TaskMan Password Reset Code";
        String html = buildHtml(
            "Password Reset Request",
            "We received a request to reset the password for your TaskMan account.",
            buildOtpBlock(otp),
            "This code expires in <strong style='color:" + TEXT_PRIMARY + "'>10 minutes</strong>. "
            + "Do not share it with anyone.<br><br>"
            + "If you did not request a password reset, you can safely ignore this email — "
            + "your account remains secure."
        );
        sendHtml(to, subject, html);
    }

    // ── HTML builder ──────────────────────────────────────────────────────────

    /**
     * Wraps a pre-built HTML body snippet in the full branded email shell.
     * @param headline  Bold heading inside the card
     * @param intro     Introductory paragraph
     * @param content   Middle content block (HTML)
     * @param footer    Closing paragraph before the disclaimer
     */
    public String buildHtml(String headline, String intro, String content, String footer) {
        return "<!DOCTYPE html>" +
            "<html lang='en'><head><meta charset='UTF-8'>" +
            "<meta name='viewport' content='width=device-width,initial-scale=1'>" +
            "<title>TaskMan</title></head>" +
            "<body style='margin:0;padding:0;background:" + DARK_BG + ";font-family:-apple-system,BlinkMacSystemFont," +
            "\"Segoe UI\",Roboto,Helvetica,Arial,sans-serif;'>" +

            // Outer wrapper
            "<table width='100%' cellpadding='0' cellspacing='0' border='0' style='background:" + DARK_BG + ";padding:40px 16px;'>" +
            "<tr><td align='center'>" +

            // Card
            "<table width='100%' cellpadding='0' cellspacing='0' border='0' style='max-width:560px;background:" + CARD_BG + ";" +
            "border-radius:16px;border:1px solid " + BORDER + ";overflow:hidden;'>" +

            // Header bar
            "<tr><td style='background:linear-gradient(135deg," + BRAND_COLOR + " 0%,#7c66ff 100%);padding:28px 32px;'>" +
            "<table width='100%' cellpadding='0' cellspacing='0' border='0'><tr>" +
            "<td><span style='display:inline-block;background:rgba(255,255,255,0.15);border-radius:10px;" +
            "padding:6px 10px;font-size:20px;'>&#9632;</span></td>" +
            "<td style='padding-left:12px;'>" +
            "<span style='font-size:20px;font-weight:700;color:#ffffff;letter-spacing:-0.5px;'>TaskMan</span><br>" +
            "<span style='font-size:12px;color:rgba(255,255,255,0.7);'>Role-Based Task Management</span>" +
            "</td></tr></table>" +
            "</td></tr>" +

            // Body
            "<tr><td style='padding:32px;'>" +
            "<h2 style='margin:0 0 12px;font-size:20px;font-weight:700;color:" + TEXT_PRIMARY + ";letter-spacing:-0.3px;'>" + headline + "</h2>" +
            "<p style='margin:0 0 24px;font-size:15px;line-height:1.6;color:" + TEXT_MUTED + ";'>" + intro + "</p>" +
            content +
            "<p style='margin:24px 0 0;font-size:14px;line-height:1.7;color:" + TEXT_MUTED + ";'>" + footer + "</p>" +
            "</td></tr>" +

            // Divider
            "<tr><td style='padding:0 32px;'>" +
            "<hr style='border:none;border-top:1px solid " + BORDER + ";margin:0;'>" +
            "</td></tr>" +

            // Footer / disclaimer
            "<tr><td style='padding:20px 32px 28px;'>" +
            "<p style='margin:0 0 6px;font-size:12px;color:" + TEXT_MUTED + ";line-height:1.6;'>" +
            "<strong style='color:#64748b;'>&#9888; Auto-generated email</strong> — " +
            "This message was sent automatically by the TaskMan notification system. " +
            "Please do not reply to this email as this mailbox is not monitored." +
            "</p>" +
            "<p style='margin:0;font-size:12px;color:#475569;'>" +
            "&copy; " + java.time.Year.now().getValue() + " TaskMan &nbsp;&bull;&nbsp; " +
            "Role-Based Task Management Platform &nbsp;&bull;&nbsp; All rights reserved." +
            "</p>" +
            "</td></tr>" +

            "</table>" + // end card
            "</td></tr></table>" + // end outer
            "</body></html>";
    }

    /** Builds a highlighted info box (key-value pairs). */
    public String buildInfoBox(String... keyValues) {
        StringBuilder sb = new StringBuilder();
        sb.append("<table width='100%' cellpadding='0' cellspacing='0' border='0' style='background:#0f172a;" +
                  "border-radius:12px;border:1px solid " + BORDER + ";margin:0 0 8px;overflow:hidden;'>");
        for (int i = 0; i < keyValues.length - 1; i += 2) {
            String borderStyle = (i + 2 < keyValues.length - 1)
                ? "border-bottom:1px solid " + BORDER + ";" : "";
            sb.append("<tr>")
              .append("<td style='padding:12px 16px;font-size:12px;font-weight:600;text-transform:uppercase;" +
                      "letter-spacing:0.06em;color:#64748b;white-space:nowrap;width:1%;").append(borderStyle).append("'>")
              .append(keyValues[i]).append("</td>")
              .append("<td style='padding:12px 16px;font-size:14px;color:").append(TEXT_PRIMARY).append(";").append(borderStyle).append("'>")
              .append(keyValues[i + 1]).append("</td>")
              .append("</tr>");
        }
        sb.append("</table>");
        return sb.toString();
    }

    /** Builds a large OTP code block. */
    private String buildOtpBlock(String otp) {
        return "<div style='text-align:center;margin:8px 0 8px;'>" +
               "<div style='display:inline-block;background:#0f172a;border:2px solid " + BRAND_COLOR + ";" +
               "border-radius:14px;padding:20px 40px;'>" +
               "<p style='margin:0 0 4px;font-size:11px;font-weight:600;text-transform:uppercase;" +
               "letter-spacing:0.1em;color:" + TEXT_MUTED + ";'>Your verification code</p>" +
               "<p style='margin:0;font-size:40px;font-weight:800;letter-spacing:12px;color:" + BRAND_COLOR + ";" +
               "font-family:\"Courier New\",monospace;'>" + otp + "</p>" +
               "</div></div>";
    }

    /** Builds a status badge pill. */
    public String buildBadge(String text, String color) {
        return "<span style='display:inline-block;background:" + color + "22;color:" + color + ";" +
               "border:1px solid " + color + "55;border-radius:20px;padding:3px 12px;" +
               "font-size:12px;font-weight:600;'>" + text + "</span>";
    }

    // ── Internal send ─────────────────────────────────────────────────────────

    private void sendHtml(String to, String subject, String html) {
        log.info("Sending email: to={}, subject={}", to, subject);
        // Use Brevo HTTP API if key is configured (required on Render - SMTP is blocked)
        if (brevoApiKey != null && !brevoApiKey.isBlank()) {
            sendViaBrevoApi(to, subject, html);
        } else {
            sendViaSMTP(to, subject, html);
        }
    }

    private void sendViaBrevoApi(String to, String subject, String html) {
        try {
            sibApi.ApiClient client = sibApi.Configuration.getDefaultApiClient();
            client.setApiKey(brevoApiKey);

            TransactionalEmailsApi apiInstance = new TransactionalEmailsApi();

            SendSmtpEmailSender sender = new SendSmtpEmailSender();
            sender.setEmail(fromEmail);
            sender.setName("TaskMan Notifications");

            SendSmtpEmailTo recipient = new SendSmtpEmailTo();
            recipient.setEmail(to);

            SendSmtpEmail email = new SendSmtpEmail();
            email.setSender(sender);
            email.setTo(List.of(recipient));
            email.setSubject(subject);
            email.setHtmlContent(html);
            email.setTextContent(html.replaceAll("<[^>]+>", "").replaceAll("\\s{2,}", " ").trim());

            CreateSmtpEmail result = apiInstance.sendTransacEmail(email);
            log.info("Email sent via Brevo API: to={}, messageId={}", to, result.getMessageId());
        } catch (Exception e) {
            log.error("Brevo API email failed: to={}, subject={}, error={}", to, subject, e.getMessage(), e);
        }
    }

    private void sendViaSMTP(String to, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("TaskMan Notifications <" + fromEmail + ">");
            helper.setReplyTo(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            String plainText = html.replaceAll("<[^>]+>", "").replaceAll("\\s{2,}", " ").trim();
            helper.setText(plainText, html);
            message.addHeader("X-Mailer", "TaskMan-Notification-Service/1.0");
            message.addHeader("Precedence", "bulk");
            message.addHeader("Auto-Submitted", "auto-generated");
            mailSender.send(message);
            log.info("Email sent via SMTP: to={}, subject={}", to, subject);
        } catch (Exception e) {
            log.error("SMTP email failed: to={}, subject={}, error={}", to, subject, e.getMessage(), e);
        }
    }
}
