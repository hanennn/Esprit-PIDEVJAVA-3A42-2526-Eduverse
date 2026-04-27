package org.example.services;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

public class EmailService {

    private static final String FROM_EMAIL    = "trabelsiasma20@gmail.com";
    private static final String FROM_PASSWORD = "eifyezyitpyzczey"; // ← sans espaces
    private static final String FROM_NAME     = "eduverse Platform";

    public static void envoyerEmailCertification(
            String toEmail,
            String nomEtudiant,
            String badge,
            int certifId,
            String dateEmission,
            int quizId) {

        Properties props = new Properties();
        props.put("mail.smtp.auth",            "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host",            "smtp.gmail.com");
        props.put("mail.smtp.port",            "587");
        props.put("mail.smtp.ssl.trust",       "*");
        props.put("mail.smtp.ssl.protocols",   "TLSv1.2");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, FROM_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL, FROM_NAME));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(toEmail)
            );
            message.setSubject(
                    "Votre Certification Finale est prete - eduverse"
            );
            message.setContent(
                    construireCorpsEmail(nomEtudiant, badge, certifId,
                            dateEmission, quizId),
                    "text/html; charset=UTF-8"
            );

            Transport.send(message);
            System.out.println("Email envoye a : " + toEmail);

        } catch (Exception e) {
            System.err.println("Erreur envoi email : " + e.getMessage());
            throw new RuntimeException("Erreur email : " + e.getMessage());
        }
    }

    private static String construireCorpsEmail(
            String nomEtudiant,
            String badge,
            int certifId,
            String dateEmission,
            int quizId) {

        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset='UTF-8'/>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; background-color: #f4f6f9;" +
                "       margin: 0; padding: 0; }" +
                ".container { max-width: 600px; margin: 30px auto; background: white;" +
                "             border-radius: 12px; overflow: hidden;" +
                "             box-shadow: 0 4px 15px rgba(0,0,0,0.1); }" +
                ".header { background-color: #1a1f3c; padding: 25px 30px;" +
                "          text-align: center; }" +
                ".header h1 { color: white; margin: 0; font-size: 22px; }" +
                ".header span { color: #f5a623; }" +
                ".gold-bar { height: 5px;" +
                "  background: linear-gradient(to right, #f5a623, #f39c12); }" +
                ".content { padding: 35px 40px; }" +
                "h2 { color: #1a1f3c; text-align: center;" +
                "     font-size: 20px; margin: 10px 0; }" +
                ".badge-box { background: #fff8ee; border: 2px solid #f5a623;" +
                "             border-radius: 10px; padding: 15px 20px;" +
                "             text-align: center; margin: 20px 0; }" +
                ".badge-box .badge { font-size: 24px; font-weight: bold;" +
                "                    color: #f5a623; }" +
                ".info-table { width: 100%; border-collapse: collapse; margin: 20px 0; }" +
                ".info-table td { padding: 10px 15px;" +
                "                 border-bottom: 1px solid #f0f0f0;" +
                "                 font-size: 14px; color: #555; }" +
                ".info-table td:first-child { font-weight: bold;" +
                "                             color: #1a1f3c; width: 40%; }" +
                ".note { background: #f0fff4; border-left: 4px solid #2ecc71;" +
                "        padding: 12px 16px; border-radius: 4px;" +
                "        font-size: 13px; color: #2ecc71; margin: 20px 0; }" +
                ".footer { background: #f4f6f9; padding: 20px 30px;" +
                "          text-align: center; font-size: 11px; color: #aaa; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +

                "<div class='header'>" +
                "<h1>eduverse - <span>Plateforme d apprentissage</span></h1>" +
                "</div>" +

                "<div class='gold-bar'></div>" +

                "<div class='content'>" +
                "<h2>Felicitations " + nomEtudiant + " !</h2>" +
                "<p style='text-align:center; color:#666; font-size:14px;'>" +
                "Votre certification finale a ete validee et est disponible." +
                "</p>" +

                "<div class='badge-box'>" +
                "<p style='color:#555; margin:0 0 8px; font-size:13px;'>" +
                "Badge obtenu</p>" +
                "<div class='badge'>" + badge + "</div>" +
                "</div>" +

                "<table class='info-table'>" +
                "<tr><td>N Certification</td><td>#" + certifId + "</td></tr>" +
                "<tr><td>Quiz</td><td>Quiz #" + quizId + "</td></tr>" +
                "<tr><td>Date emission</td><td>" + dateEmission + "</td></tr>" +
                "</table>" +

                "<div class='note'>" +
                "Votre certification est prete a etre imprimee ou exportee en PDF " +
                "depuis votre espace etudiant sur la plateforme eduverse." +
                "</div>" +

                "<p style='text-align:center; color:#888; font-size:12px;" +
                "           margin-top:20px;'>" +
                "Connectez-vous a eduverse - Espace Etudiant - Certifications " +
                "pour imprimer votre certificat." +
                "</p>" +
                "</div>" +

                "<div class='footer'>" +
                "<p>2024 eduverse - Tous droits reserves</p>" +
                "<p>Cet email a ete envoye automatiquement.</p>" +
                "</div>" +

                "</div>" +
                "</body>" +
                "</html>";
    }
}