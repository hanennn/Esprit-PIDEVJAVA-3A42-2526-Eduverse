package org.example.services;

import java.net.URI;
import java.net.http.*;
import java.net.http.HttpRequest.BodyPublishers;

public class EmailService {

    private static final String API_KEY      = "re_c2TYJyJv_6LDHSBMX1LD7StNBDjAXC9EP";
    private static final String EXPEDITEUR   = "onboarding@resend.dev";
    private static final String DESTINATAIRE = "hanen.bennaceur@esprit.tn";

    public static void envoyerNouveauCours(String titreCours) {
        String json = """
            {
              "from": "%s",
              "to": "%s",
              "subject": "Nouveau cours disponible !",
              "text": "Un nouveau cours vient d'être ajouté : %s"
            }
            """.formatted(EXPEDITEUR, DESTINATAIRE, titreCours);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.resend.com/emails"))
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(json))
                .build();

        try {
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Status : " + response.statusCode());
            System.out.println("Réponse : " + response.body());
        } catch (Exception e) {
            System.err.println("Erreur : " + e.getMessage());
        }
    }
}