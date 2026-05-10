package org.example.services;

import java.net.URI;
import java.net.http.*;
import java.net.http.HttpRequest.BodyPublishers;

public class ChatbotService {

    private static final String API_KEY = "6gZY7P3TMgiO51koF0Dk0MhWznrhK5LuFG2qzFpT"; // même clé que ResumeService

    public static String poserQuestion(String titreChapitre, String descChapitre, String question) {
        String contexte = "Tu es un assistant pédagogique pour le chapitre : '"
                + titreChapitre + "'. Description : " + descChapitre
                + ". Réponds en français, en texte simple sans Markdown, "
                + "sans formules mathématiques complexes, de manière claire. Question : "
                + question;

        String contenuPropre = contexte
                .replace("\\", "\\\\")
                .replace("\"", "'")
                .replace("\n", " ")
                .replace("\r", " ");

        String json = "{\"model\":\"command-r-plus-08-2024\","
                + "\"messages\":[{\"role\":\"user\","
                + "\"content\":\"" + contenuPropre + "\"}]}";

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

            int start = body.indexOf("\"text\":\"") + 8;
            if (start < 8) return "Je n'ai pas pu répondre.";

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
            return result.toString()
                    .replaceAll("\\\\u[0-9a-fA-F]{4}", "") // supprimer unicode échappé
                    .replaceAll("<[^>]+>", "")              // supprimer balises HTML
                    .replaceAll("\\*+", "")                 // supprimer markdown gras/italique
                    .replaceAll("\\\\n", "\n")              // corriger sauts de ligne
                    .trim();

        } catch (Exception e) {
            System.err.println("Erreur chatbot : " + e.getMessage());
            return "Erreur de connexion.";
        }
    }
}