package controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import models.Event;
import services.EventService;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AIAssistantController {

    // ── Clé API Groq (gratuite) ──────────────────────────────────
    's/private static final String GROQ_API_KEY = ".*"/private static final String GROQ_API_KEY = System.getenv("GROQ_API_KEY")/'
    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String GROQ_MODEL = "llama-3.3-70b-versatile";

    // ── Contrôles FXML ──────────────────────────────────────────
    @FXML private Button btnGenerer;
    @FXML private Button btnCreer;
    @FXML private TextArea taReponse;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Label lblStatus;
    @FXML private Label lblErreur;

    // ── Services ────────────────────────────────────────────────
    private final EventService eventService = new EventService();

    /** Dernière suggestion brute retournée par Claude */
    private String lastSuggestionText = null;

    // ── Parsed fields from last AI suggestion ───────────────────
    private String parsedTitre = null;
    private String parsedDescription = null;
    private String parsedLieu = null;
    private String parsedType = null;
    private String parsedNiveau = null;

    // ── Génération de suggestion ─────────────────────────────────

    @FXML
    private void handleGenerer(ActionEvent event) {
        setLoadingState(true);
        showErreur(null);
        taReponse.clear();
        btnCreer.setDisable(true);
        lastSuggestionText = null;

        // Exécution en thread séparé pour ne pas bloquer l'UI JavaFX
        Thread thread = new Thread(() -> {
            try {
                // 1. Récupérer les données historiques
                List<Map<String, Object>> historique = eventService.getHistoricalData();
                String prompt = buildPrompt(historique);

                // 2. Construire la requête JSON pour Groq (format OpenAI)
                JSONObject requestBody = new JSONObject();
                requestBody.put("model", GROQ_MODEL);
                requestBody.put("max_tokens", 1024);
                requestBody.put("temperature", 0.7);

                // Messages au format OpenAI : role + content (string simple)
                JSONArray messages = new JSONArray();

                // Message système : persona de l'assistant
                JSONObject systemMsg = new JSONObject();
                systemMsg.put("role", "system");
                systemMsg.put("content",
                        "Tu es un assistant expert en organisation d'événements éducatifs " +
                                "pour une plateforme universitaire appelée Eduverse. " +
                                "Tes suggestions sont précises, structurées et basées sur les données fournies.");
                messages.put(systemMsg);

                // Message utilisateur avec le prompt complet
                JSONObject userMsg = new JSONObject();
                userMsg.put("role", "user");
                userMsg.put("content", prompt);
                messages.put(userMsg);

                requestBody.put("messages", messages);

                // 3. Appel HTTP vers l'API Groq
                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(30))
                        .build();

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(GROQ_API_URL))
                        .timeout(Duration.ofSeconds(60))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + GROQ_API_KEY)
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                // 4. Parser la réponse Groq
                String responseBody = response.body();
                if (response.statusCode() == 200) {
                    String resultText = parseGroqResponse(responseBody);
                    lastSuggestionText = resultText;
                    parseFieldsFromSuggestion(resultText);

                    Platform.runLater(() -> {
                        taReponse.setText(resultText);
                        btnCreer.setDisable(false);
                        setLoadingState(false);
                        showStatus("✅ Suggestion générée avec succès !", false);
                    });
                } else {
                    // Extraire le message d'erreur de l'API Groq
                    String errMsg = extractApiError(responseBody, response.statusCode());
                    Platform.runLater(() -> {
                        setLoadingState(false);
                        showErreur("❌ Erreur API Groq (HTTP " + response.statusCode() + ") : " + errMsg);
                    });
                }

            } catch (IOException e) {
                Platform.runLater(() -> {
                    setLoadingState(false);
                    showErreur("❌ Erreur de connexion ou réseau.");
                });
            } catch (Exception e) {
                String msg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                Platform.runLater(() -> {
                    setLoadingState(false);
                    showErreur("❌ Erreur inattendue : " + msg);
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    // ── Pré-remplissage du formulaire Événements ────────────────

    @FXML
    private void handleCreerEvenement(ActionEvent event) {
        if (lastSuggestionText == null) {
            showErreur("⚠ Aucune suggestion disponible. Cliquez d'abord sur 'Générer'.");
            return;
        }

        // Naviguer vers l'onglet "Gestion des Événements" et injecter les données
        try {
            TabPane tabPane = (TabPane) btnCreer.getScene().getRoot().lookup(".tab-pane");
            if (tabPane == null) {
                showErreur("⚠ Impossible de trouver le panneau d'onglets.");
                return;
            }

            Tab eventTab = tabPane.getTabs().get(0);
            tabPane.getSelectionModel().select(eventTab);

            Node content = eventTab.getContent();
            if (content != null) {
                EventController ec = (EventController) content.getProperties()
                        .get("fx:controller");
                if (ec == null) {
                    Object userData = content.getUserData();
                    if (userData instanceof EventController) {
                        ec = (EventController) userData;
                    }
                }
                if (ec != null) {
                    ec.prefillFromAISuggestion(parsedTitre, parsedDescription,
                            parsedLieu, parsedType, parsedNiveau);
                    showStatus("✅ Formulaire pré-rempli dans l'onglet 'Gestion des Événements'.", false);
                } else {
                    showStatus("ℹ Copiez la suggestion ci-dessus et remplissez le formulaire manuellement.", true);
                }
            }
        } catch (Exception e) {
            showErreur("⚠ Erreur lors de la navigation : " + e.getMessage());
        }
    }

    // ── Construction du prompt ───────────────────────────────────

    private String buildPrompt(List<Map<String, Object>> historique) {
        StringBuilder sb = new StringBuilder();
        sb.append(
                "Tu es un assistant spécialisé dans la création d'événements éducatifs pour une plateforme universitaire appelée Eduverse.\n\n");
        sb.append(
                "Voici les données historiques des événements passés (titre et nombre d'inscrits) :\n\n");

        if (historique.isEmpty()) {
            sb.append(
                    "(Aucun événement existant pour l'instant — propose un premier événement adapté à un public universitaire.)\n");
        } else {
            for (Map<String, Object> row : historique) {
                sb.append("• Titre : ").append(row.get("titre")).append("\n");
                sb.append("  Inscrits : ").append(row.get("nbInscrits")).append("\n\n");
            }
        }

        sb.append("---\n\n");
        sb.append(
                "En te basant sur ces données, propose un NOUVEL événement éducatif original et pertinent pour ce public.\n");
        sb.append("Ta réponse doit être structurée exactement ainsi :\n\n");
        sb.append("TITRE: [titre de l'événement]\n");
        sb.append("TYPE: [type : Conférence / Atelier / Webinaire / Hackathon / etc.]\n");
        sb.append("NIVEAU: [niveau : Débutant / Intermédiaire / Avancé / Tous niveaux]\n");
        sb.append("LIEU: [lieu ou mode : En ligne / Campus principal / Salle B204 / etc.]\n");
        sb.append("CAPACITÉ RECOMMANDÉE: [nombre de participants idéal]\n");
        sb.append("DESCRIPTION: [description complète et engageante de l'événement, 3-5 phrases]\n\n");
        sb.append(
                "JUSTIFICATION: [explique en 2-3 phrases pourquoi tu proposes cet événement]\n");

        return sb.toString();
    }

    // ── Parsing de la réponse Groq (format OpenAI) ───────────────

    private String parseGroqResponse(String responseBody) {
        // Format : { "choices": [ { "message": { "content": "..." } } ] }
        JSONObject json = new JSONObject(responseBody);
        JSONArray choices = json.getJSONArray("choices");
        if (choices.length() > 0) {
            return choices.getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");
        }
        return "(Réponse vide reçue de l'API Groq.)";
    }

    /**
     * Extrait les champs structurés de la suggestion IA pour pré-remplir le
     * formulaire.
     */
    private void parseFieldsFromSuggestion(String text) {
        parsedTitre = extractLine(text, "TITRE:");
        parsedType = extractLine(text, "TYPE:");
        parsedNiveau = extractLine(text, "NIVEAU:");
        parsedLieu = extractLine(text, "LIEU:");
        parsedDescription = extractMultiline(text, "DESCRIPTION:", "JUSTIFICATION:");
    }

    private String extractLine(String text, String key) {
        for (String line : text.split("\n")) {
            if (line.trim().toUpperCase().startsWith(key)) {
                return line.substring(line.indexOf(':') + 1).trim();
            }
        }
        return null;
    }

    private String extractMultiline(String text, String startKey, String endKey) {
        int start = text.toUpperCase().indexOf(startKey.toUpperCase());
        if (start < 0)
            return null;
        start = text.indexOf(':', start) + 1;
        int end = text.toUpperCase().indexOf(endKey.toUpperCase());
        if (end < 0)
            end = text.length();
        return text.substring(start, end).trim();
    }

    private String extractApiError(String responseBody, int statusCode) {
        try {
            JSONObject json = new JSONObject(responseBody);
            if (json.has("error")) {
                JSONObject err = json.getJSONObject("error");
                return err.optString("message", responseBody);
            }
        } catch (Exception ignored) {
        }
        return responseBody.length() > 200 ? responseBody.substring(0, 200) + "..." : responseBody;
    }

    // ── État de chargement ───────────────────────────────────────

    private void setLoadingState(boolean loading) {
        btnGenerer.setDisable(loading);
        progressIndicator.setVisible(loading);
        progressIndicator.setManaged(loading);
        if (loading) {
            showStatus("⏳ Interrogation de l'IA en cours...", true);
        } else {
            lblStatus.setVisible(false);
            lblStatus.setManaged(false);
        }
    }

    private void showStatus(String msg, boolean neutral) {
        lblStatus.setText(msg);
        lblStatus.setStyle(neutral
                ? "-fx-text-fill:#94a3b8;"
                : "-fx-text-fill:#51cf66;");
        lblStatus.setVisible(true);
        lblStatus.setManaged(true);
    }

    private void showErreur(String msg) {
        if (msg == null || msg.isBlank()) {
            lblErreur.setVisible(false);
            lblErreur.setManaged(false);
        } else {
            lblErreur.setText(msg);
            lblErreur.setVisible(true);
            lblErreur.setManaged(true);
        }
    }
}
