package org.example.services;

import java.net.URI;
import java.net.http.*;
import java.net.http.HttpRequest.BodyPublishers;

public class ResumeService {

    private static final String API_KEY = "6gZY7P3TMgiO51koF0Dk0MhWznrhK5LuFG2qzFpT";

    public static String genererResume(String contenu) {
        String json = "{\"model\":\"command-r-plus-08-2024\",\"messages\":[{\"role\":\"user\",\"content\":\"Fais un résumé court en français de ce chapitre : "
                + contenu.replace("\"", "'").replace("\n", " ")
                + "\"}]}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.cohere.com/v2/chat"))
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(json))
                .build();

        try {
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();
            System.out.println("Cohere response: " + body);

            int start = body.indexOf("\"text\":\"") + 8;
            if (start < 8) return "Résumé indisponible.";

            StringBuilder result = new StringBuilder();
            int i = start;
            while (i < body.length()) {
                char c = body.charAt(i);
                if (c == '\\' && i + 1 < body.length()) {
                    char next = body.charAt(i + 1);
                    if (next == '"')  { result.append('"');  i += 2; continue; }
                    if (next == 'n')  { result.append('\n'); i += 2; continue; }
                    if (next == '\\') { result.append('\\'); i += 2; continue; }
                }
                if (c == '"') break;
                result.append(c);
                i++;
            }
            return result.toString().trim();

        } catch (Exception e) {
            System.err.println("Erreur : " + e.getMessage());
            return "Résumé indisponible.";
        }
    }
}