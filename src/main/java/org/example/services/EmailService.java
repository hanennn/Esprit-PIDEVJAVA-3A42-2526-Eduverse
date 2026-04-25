package org.example.services;

import java.net.URI;
import java.net.http.*;
import java.net.http.HttpRequest.BodyPublishers;

public class EmailService {

    private static final String API_KEY      = "re_c2TYJyJv_6LDHSBMX1LD7StNBDjAXC9EP";
    private static final String EXPEDITEUR   = "onboarding@resend.dev";
    private static final String DESTINATAIRE = "hanen.bennaceur@esprit.tn";

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
}