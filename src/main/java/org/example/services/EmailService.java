package org.example.services;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.net.URI;
import java.net.http.*;
import java.net.http.HttpRequest.BodyPublishers;
import java.util.Properties;

public class EmailService {

    private static final String FROM_EMAIL    = "trabelsiasma20@gmail.com";
    private static final String FROM_PASSWORD = "eifyezyitpyzczey";
    private static final String FROM_NAME     = "eduverse Platform";
    private static final String API_KEY       = "re_c2TYJyJv_6LDHSBMX1LD7StNBDjAXC9EP";
    private static final String EXPEDITEUR    = "onboarding@resend.dev";
    private static final String DESTINATAIRE  = "hanen.bennaceur@esprit.tn";

    // ✅ Méthode de HANEN - gestion_des_cours
    public static void envoyerNouveauCours(String titreCours) {
        String texte = "Chers etudiants,\\n\\nOn vous informe qu un nouveau cours vient d etre ajoute : "
                + titreCours + "\\n\\nBonne revision !";

        String json = "{\"from\":\"" + EXPEDITEUR + "\","
                + "\"to\":\"" + DESTINATAIRE + "\","
                + "\"subject\":\"Nouveau cours disponible !\","
                + "\"text\":\"" + texte + "\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.resend.com/emails"))
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(json))
                .build();

        try {
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Email Status : " + response.statusCode());
            System.out.println("Email Réponse : " + response.body());
        } catch (Exception e) {
            System.err.println("Erreur email : " + e.getMessage());
        }
    }

    // ✅ Méthode de ASMA - gestion_des_quizs
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
            message.setSubject("Votre Certification Finale est prete - eduverse");
            message.setContent(
                    construireCorpsEmail(nomEtudiant, badge, certifId, dateEmission, quizId),
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

        return "<!DOCTYPE html><html><body>" +
                "<h2>Felicitations " + nomEtudiant + " !</h2>" +
                "<p>Badge : " + badge + "</p>" +
                "<p>Certification #" + certifId + "</p>" +
                "<p>Quiz #" + quizId + "</p>" +
                "<p>Date : " + dateEmission + "</p>" +
                "</body></html>";
    }
}