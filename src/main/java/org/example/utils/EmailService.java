package org.example.utils;


import org.example.entities.Session;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.Random;


public class EmailService {

    private static final String SMTP_HOST       = "smtp.gmail.com";
    private static final int    SMTP_PORT       = 587;
    private static final String SENDER_EMAIL    = "eduversee2026@gmail.com";
    private static final String SENDER_PASSWORD = "fhgmptylxpiidasf";

    private static jakarta.mail.Session buildMailSession() {
        Properties props = new Properties();
        props.put("mail.smtp.auth",            "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host",            SMTP_HOST);
        props.put("mail.smtp.port",            String.valueOf(SMTP_PORT));
        props.put("mail.smtp.ssl.trust",       SMTP_HOST);
        return jakarta.mail.Session.getInstance(props, new Authenticator() {
            @Override protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
            }
        });
    }

    public static void sendVerificationEmail(String toEmail, String firstName, String code)
            throws MessagingException {
        String html =
                "<div style='font-family:Georgia,serif;max-width:480px;margin:auto;'>" +
                        "<div style='background:#1a2035;padding:24px 32px;'>" +
                        "  <h2 style='color:#f5a623;margin:0;'>EduCenter</h2></div>" +
                        "<div style='padding:32px;border:1px solid #dee8ff;'>" +
                        "  <p style='font-size:15px;color:#1a2035;'>Bonjour <strong>" + firstName + "</strong>,</p>" +
                        "  <p style='color:#555;'>Utilisez le code ci-dessous pour confirmer votre adresse e-mail :</p>" +
                        "  <div style='background:#f7f9ff;border:2px solid #dee8ff;border-left:4px solid #f5a623;padding:20px;text-align:center;margin:24px 0;'>" +
                        "    <span style='font-size:36px;font-weight:bold;color:#1a2035;letter-spacing:10px;'>" + code + "</span></div>" +
                        "  <p style='color:#8a9bb5;font-size:12px;'>Ce code expire dans 10 minutes.</p></div>" +
                        "<div style='background:#f8f9fa;padding:12px 32px;text-align:center;'>" +
                        "  <p style='color:#8a9bb5;font-size:11px;margin:0;'>© 2026 EduCenter.</p></div></div>";

        sendHtmlEmail(toEmail, "Votre code de vérification EduCenter", html);
    }

    public static void sendIpAlertEmail(String toEmail, String firstName, String code)
            throws MessagingException {
        String html =
                "<div style='font-family:Georgia,serif;max-width:480px;margin:auto;'>" +
                        "<div style='background:#1a2035;padding:24px 32px;'>" +
                        "  <h2 style='color:#f5a623;margin:0;'>EduCenter</h2></div>" +
                        "<div style='padding:32px;border:1px solid #dee8ff;'>" +
                        "  <p style='font-size:15px;color:#1a2035;'>Bonjour <strong>" + firstName + "</strong>,</p>" +
                        "  <p style='color:#555;'>Une connexion depuis un nouveau réseau a été détectée.<br>Utilisez le code ci-dessous pour confirmer votre identité :</p>" +
                        "  <div style='background:#f7f9ff;border:2px solid #dee8ff;border-left:4px solid #f5a623;padding:20px;text-align:center;margin:24px 0;'>" +
                        "    <span style='font-size:36px;font-weight:bold;color:#1a2035;letter-spacing:10px;'>" + code + "</span></div>" +
                        "  <p style='color:#8a9bb5;font-size:12px;'>Si ce n'était pas vous, changez votre mot de passe immédiatement.</p></div>" +
                        "<div style='background:#f8f9fa;padding:12px 32px;text-align:center;'>" +
                        "  <p style='color:#8a9bb5;font-size:11px;margin:0;'>© 2026 EduCenter.</p></div></div>";

        sendHtmlEmail(toEmail, "Nouvelle connexion détectée - EduCenter", html);
    }


    private static void sendHtmlEmail(String toEmail, String subject, String html)
            throws MessagingException {
        try {
            Message message = new MimeMessage(buildMailSession());
            message.setFrom(new InternetAddress(SENDER_EMAIL, "EduCenter"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            MimeBodyPart bodyPart = new MimeBodyPart();
            bodyPart.setContent(html, "text/html; charset=utf-8");
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(bodyPart);
            message.setContent(multipart);
            Transport.send(message);
        }catch (UnsupportedEncodingException e) {
            System.out.println(e.getMessage());
        }
    }


    public static String generateCode() {
        return String.format("%06d", new Random().nextInt(999999));
    }
}