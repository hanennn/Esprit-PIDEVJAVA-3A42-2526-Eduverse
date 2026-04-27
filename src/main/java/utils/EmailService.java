package utils;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import models.User;
import models.bourses;

import java.util.Properties;

public class EmailService {

    // --- CONFIGURATION SMTP ---
    // Gmail: host="smtp.gmail.com", port="587"
    // Office 365 / Outlook: host="smtp.office365.com", port="587"
    private static final String SMTP_HOST = "smtp.gmail.com"; 
    private static final String SMTP_PORT = "587";
    
    private static final String MY_EMAIL = "eduversee2026@gmail.com";
    private static final String MY_PASSWORD = "bxay woqk zbpf ouda"; 
    // ---------------------------

    public static void sendStatusUpdateEmail(User student, bourses bourse, String status, String note) {
        if (MY_PASSWORD.equals("VOTRE_MOT_DE_PASSE_D_APPLICATION")) {
            System.err.println("⚠️ EmailService : Veuillez configurer votre mot de passe d'application Gmail dans EmailService.java");
            return;
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(MY_EMAIL, MY_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(MY_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(student.getEmail()));
            message.setSubject("Eduverse - Mise à jour de votre demande de bourse");

            String htmlTemplate = getHtmlTemplate(student, bourse, status, note);
            message.setContent(htmlTemplate, "text/html; charset=utf-8");

            Transport.send(message);
            System.out.println("✅ Email envoyé avec succès à " + student.getEmail());

        } catch (MessagingException e) {
            System.err.println("❌ Erreur lors de l'envoi de l'email : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String getHtmlTemplate(User student, bourses bourse, String status, String note) {
        String statusLabel = status;
        String statusClass = status.equalsIgnoreCase("Acceptée") ? "status-Accepted" : "status-Refused";
        
        String resultText = "";
        if (status.equalsIgnoreCase("Acceptée")) {
            resultText = "Félicitations ! Votre demande a été acceptée. Notre équipe vous contactera prochainement pour les étapes suivantes.";
        } else if (status.equalsIgnoreCase("Refusée")) {
            resultText = "Nous regrettons de vous informer que votre demande n'a pas été retenue pour cette bourse. Nous vous encourageons à postuler à d'autres opportunités sur Eduverse.";
        }

        String adminNoteHtml = "";
        if (note != null && !note.trim().isEmpty()) {
            adminNoteHtml = "<p><strong>Note de l'administration :</strong></p><p>" + note.replace("\n", "<br>") + "</p>";
        }

        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>Mise à jour de votre demande de bourse</title>\n" +
                "    <style>\n" +
                "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }\n" +
                "        .container { width: 80%; margin: 20px auto; padding: 20px; border: 1px solid #ddd; border-radius: 5px; }\n" +
                "        .header { background-color: #f4f4f4; padding: 10px; text-align: center; border-bottom: 1px solid #ddd; }\n" +
                "        .content { padding: 20px; }\n" +
                "        .footer { font-size: 0.8em; text-align: center; color: #777; margin-top: 20px; }\n" +
                "        .status { font-weight: bold; }\n" +
                "        .status-Accepted { color: #27ae60; }\n" +
                "        .status-Refused { color: #c0392b; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <div class=\"header\">\n" +
                "            <h2>Eduverse - Mise à jour de votre demande de bourse</h2>\n" +
                "        </div>\n" +
                "        <div class=\"content\">\n" +
                "            <p>Bonjour " + student.getPrenom() + " " + student.getNom() + ",</p>\n" +
                "            <p>Nous vous informons que le statut de votre demande pour la bourse <strong>\"" + bourse.getTitre() + "\"</strong> a été mis à jour.</p>\n" +
                "            <p>Nouveau statut : <span class=\"status " + statusClass + "\">" + statusLabel + "</span></p>\n" +
                "            \n" +
                "            <p>" + resultText + "</p>\n" +
                "\n" +
                "            " + adminNoteHtml + "\n" +
                "\n" +
                "            <p>Vous pouvez consulter les détails de votre demande dans votre espace personnel.</p>\n" +
                "        </div>\n" +
                "        <div class=\"footer\">\n" +
                "            <p>Ceci est un message automatique, merci de ne pas y répondre.</p>\n" +
                "            <p>&copy; 2026 Eduverse. Tous droits réservés.</p>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }
}
