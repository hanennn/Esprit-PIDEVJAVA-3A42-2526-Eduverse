package org.example.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.entities.Question;
import org.example.services.QuestionService;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class AjouterQuestionController {

    // Formulaire
    @FXML private Label    quizTitreLabel;
    @FXML private TextArea questionField;
    @FXML private TextField pointsField;
    @FXML private TextField reponseA;
    @FXML private TextField reponseB;
    @FXML private TextField reponseC;
    @FXML private TextField reponseD;
    @FXML private ComboBox<String> bonneReponseBox;
    @FXML private Label messageLabel;

    // Erreurs
    @FXML private Label questionError;
    @FXML private Label pointsError;
    @FXML private Label reponseAError;
    @FXML private Label reponseBError;
    @FXML private Label reponseCError;
    @FXML private Label reponseDError;
    @FXML private Label reponsesError;
    @FXML private Label bonneReponseError;

    // IA
    @FXML private TextField        aiThemeField;
    @FXML private ComboBox<String> aiNombreBox;
    @FXML private ComboBox<String> aiDifficulteBox;
    @FXML private Label            aiStatusLabel;
    @FXML private Button           aiGenererBtn;

    // Table
    @FXML private TableView<Question>            questionTable;
    @FXML private TableColumn<Question, String>  questionCol;
    @FXML private TableColumn<Question, Integer> pointsCol;
    @FXML private TableColumn<Question, String>  reponseACol;
    @FXML private TableColumn<Question, String>  reponseBCol;
    @FXML private TableColumn<Question, String>  reponseCCol;
    @FXML private TableColumn<Question, String>  reponseDCol;
    @FXML private TableColumn<Question, String>  correcteCol;

    private final QuestionService service = new QuestionService();
    private int    quizId    = 0;
    private String quizTitre = "—";

    private static final String GROQ_API_KEY =
            "REMOVED";

    private static final String NORMAL =
            "-fx-background-color: #f9fafb; -fx-border-color: #e5e7eb;" +
                    "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 9;";
    private static final String ERROR =
            "-fx-border-color: #e74c3c; -fx-border-radius: 8;" +
                    "-fx-background-color: #fff0f0; -fx-background-radius: 8; -fx-padding: 9;";
    private static final String SUCCESS =
            "-fx-border-color: #22c55e; -fx-border-radius: 8;" +
                    "-fx-background-color: #f0fdf4; -fx-background-radius: 8; -fx-padding: 9;";


    // INIT

    @FXML
    public void initialize() {
        // ComboBox
        bonneReponseBox.getItems().addAll("A", "B", "C", "D");
        aiNombreBox.getItems().addAll("1", "2", "3", "5", "10");
        aiNombreBox.setValue("3");
        aiDifficulteBox.getItems().addAll("Facile", "Moyen", "Difficile");
        aiDifficulteBox.setValue("Moyen");

        // TableView : juste les données, pas de style custom
        questionCol.setCellValueFactory(new PropertyValueFactory<>("question"));
        pointsCol.setCellValueFactory(new PropertyValueFactory<>("points"));
        reponseACol.setCellValueFactory(cell ->
                new SimpleStringProperty(
                        extraireReponse(cell.getValue().getReponses(), "A")));
        reponseBCol.setCellValueFactory(cell ->
                new SimpleStringProperty(
                        extraireReponse(cell.getValue().getReponses(), "B")));
        reponseCCol.setCellValueFactory(cell ->
                new SimpleStringProperty(
                        extraireReponse(cell.getValue().getReponses(), "C")));
        reponseDCol.setCellValueFactory(cell ->
                new SimpleStringProperty(
                        extraireReponse(cell.getValue().getReponses(), "D")));
        correcteCol.setCellValueFactory(cell ->
                new SimpleStringProperty(
                        extraireCorrecte(cell.getValue().getReponses())));

        // Listeners + sélection
        ajouterListeners();
        // Quand on sélectionne une question dans le tableau,
        // ses données remplissent automatiquement le formulaire
        questionTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        questionField.setText(newVal.getQuestion());
                        pointsField.setText(String.valueOf(newVal.getPoints()));
                        reponseA.setText(extraireReponse(newVal.getReponses(), "A"));
                        reponseB.setText(extraireReponse(newVal.getReponses(), "B"));
                        reponseC.setText(extraireReponse(newVal.getReponses(), "C"));
                        reponseD.setText(extraireReponse(newVal.getReponses(), "D"));
                        bonneReponseBox.setValue(extraireCorrecte(newVal.getReponses()));
                        resetErrors();
                    }
                });

        chargerQuestions();
    }




    // GÉNÉRATION IA

    @FXML
    private void genererQuestionsIA() {
        //recuperer theme
        String theme      = aiThemeField.getText().trim();
        String nombre     = aiNombreBox.getValue();
        String difficulte = aiDifficulteBox.getValue();
        // Vérifie que le thème pas vide
        if (theme.isEmpty()) {
            setAiStatus("⚠ Entrez un thème !", "#e74c3c", false);
            return;
        }
        // Vérifie qu’un quiz est sélectionné
        if (quizId == 0) {
            setAiStatus("⚠ Aucun quiz sélectionné !", "#e74c3c", false);
            return;
        }
//messsage de chargement en cours
        aiGenererBtn.setDisable(true);
        setAiStatus("🤖 Génération en cours...", "#f5a623", false);
        // Lance l’appel IA dans un thread séparé
        new Thread(() -> {
            try {
                // Construit prompt envoyée à l’IA
                String prompt    = construirePromptIA(theme, nombre, difficulte);
                // Appelle l’API Groq
                String reponseIA = appellerGroqAPI(prompt);
                // Transforme la réponse IA en liste de questions Java
                List<Question> questions = parserQuestionsIA(reponseIA);
                // Revient au thread JavaFX
                Platform.runLater(() -> {
                    int count = 0;
                    for (Question q : questions) {
                           try { service.ajouter(q); count++; }
                        catch (Exception e) { e.printStackTrace(); }
                    }
                    // Affiche le nombre de questions générées
                    setAiStatus("✅ " + count + " question(s) générée(s) !",
                            "#22c55e", true);
                    // Réactive le bouton IA
                    aiGenererBtn.setDisable(false);
                    chargerQuestions();
                    aiThemeField.clear();
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    setAiStatus("❌ Erreur : " + e.getMessage(),
                            "#e74c3c", false);
                    aiGenererBtn.setDisable(false);
                    e.printStackTrace();
                });
            }
        }).start();
    }
    //  message d’état de l’IA
    private void setAiStatus(String msg, String color, boolean bold) {
        aiStatusLabel.setText(msg);
        aiStatusLabel.setStyle("-fx-text-fill: " + color +
                "; -fx-font-size: 11;" + (bold ? " -fx-font-weight: bold;" : ""));
    }

    private String construirePromptIA(String theme, String nombre,
                                      String difficulte) {
        // On demande à l’IA de répondre uniquement en JSON
        // pour que Java puisse lire facilement la réponse
        return "Génère exactement " + nombre
                + " questions QCM sur le thème : " + theme
                + ". Niveau : " + difficulte + ".\n\n"
                + "Réponds UNIQUEMENT avec un tableau JSON valide, "
                + "sans texte avant ou après, sans markdown.\n\n"
                + "Format EXACT :\n"
                + "[\n  {\n"
                + "    \"question\": \"Texte ?\",\n"
                + "    \"points\": 10,\n"
                + "    \"reponses\": [\n"
                + "      {\"texte\": \"A\", \"correct\": true},\n"
                + "      {\"texte\": \"B\", \"correct\": false},\n"
                + "      {\"texte\": \"C\", \"correct\": false},\n"
                + "      {\"texte\": \"D\", \"correct\": false}\n"
                + "    ]\n  }\n]\n\n"
                + "Règles : 4 réponses, 1 seule correct:true, "
                + "points 5-20, pas de markdown.";
    }

    private String appellerGroqAPI(String prompt) throws Exception {
        // Crée un client HTTP pour envoyer une requête à Internet
        HttpClient client = HttpClient.newHttpClient();
        // Crée le corps JSON de la requête
        String requestBody = "{"
                + "\"model\": \"llama-3.3-70b-versatile\","
                + "\"messages\": [{\"role\": \"user\", \"content\": \""
                + prompt.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                + "\"}],"
                + "\"temperature\": 0.7, \"max_tokens\": 2048}";
        // Prépare la requête HTTP vers Groq
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.groq.com/openai/v1/chat/completions"))
                .header("Content-Type",  "application/json")
                .header("Authorization", "Bearer " + GROQ_API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        // Envoie la requête et récupère la réponse
        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200)
            throw new Exception("Groq " + response.statusCode()
                    + " — " + response.body());
        // ObjectMapper lit le JSON
        ObjectMapper mapper = new ObjectMapper();//un objet Jackson pour lire le JSON
        JsonNode root = mapper.readTree(response.body());//Transforme la réponse JSON en arbre lisible
        return root.path("choices").get(0)
                .path("message").path("content").asText();//récuperer contenu uniquement généré par l'ai
    }

    private List<Question> parserQuestionsIA(String jsonTexte) throws Exception {//transformer réponse en objet question
        List<Question> questions = new ArrayList<>();//liste qui contient les questions generees
        String clean = jsonTexte
                .replaceAll("```json", "").replaceAll("```", "").trim();//nettoie la réponse ai
        int debut = clean.indexOf('[');//recherche du premier [
        int fin   = clean.lastIndexOf(']') + 1;//recherche du derbier ]
        if (debut == -1 || fin == 0)
            throw new Exception("Format JSON invalide.");

        ObjectMapper mapper = new ObjectMapper();//lecteur json
        JsonNode array = mapper.readTree(clean.substring(debut, fin));//prend juste partie json entre [] puis transformer en tab json lisible

        for (JsonNode item : array) {//pour chaque question generer recuperer question point et reponse
            String   questionTexte = item.path("question").asText();
            int      points        = item.path("points").asInt(10);
            JsonNode reponsesNode  = item.path("reponses");

            StringBuilder jsonReponses = new StringBuilder("[");//construire un json pour reponse
            for (int i = 0; i < reponsesNode.size(); i++) {
                JsonNode r       = reponsesNode.get(i);
                String   texte   = r.path("texte").asText().replace("\"", "'");
                boolean  correct = r.path("correct").asBoolean();
                if (i > 0) jsonReponses.append(",");
                jsonReponses.append("{\"texte\":\"").append(texte)
                        .append("\",\"correct\":").append(correct).append("}");
            }
            jsonReponses.append("]");
            questions.add(new Question(quizId, questionTexte, points,
                    jsonReponses.toString()));//creer l'objet question
        }
        return questions;
    }


    // LISTENERS

    private void ajouterListeners() {
        questionField.textProperty().addListener((obs, old, nv) -> {
            if (!nv.trim().isEmpty()) {
                questionField.setStyle(SUCCESS); questionError.setText("");
            }
        });
        pointsField.textProperty().addListener((obs, old, nv) -> {
            if (nv.trim().isEmpty()) return;
            try {
                int p = Integer.parseInt(nv.trim());
                if (p > 0 && p <= 100) {
                    pointsField.setStyle(SUCCESS); pointsError.setText("");
                } else {
                    pointsField.setStyle(ERROR);
                    pointsError.setText("⚠ Entre 1 et 100");
                }
            } catch (NumberFormatException e) {
                pointsField.setStyle(ERROR);
                pointsError.setText("⚠ Nombre entier uniquement");
            }
        });
        reponseA.textProperty().addListener((obs, old, nv) -> {
            reponseA.setStyle(!nv.trim().isEmpty() ? SUCCESS : ERROR);
            reponseAError.setText(!nv.trim().isEmpty() ? "" : "⚠ Obligatoire");
        });
        reponseB.textProperty().addListener((obs, old, nv) -> {
            reponseB.setStyle(!nv.trim().isEmpty() ? SUCCESS : ERROR);
            reponseBError.setText(!nv.trim().isEmpty() ? "" : "⚠ Obligatoire");
        });
        reponseC.textProperty().addListener((obs, old, nv) -> {
            reponseC.setStyle(!nv.trim().isEmpty() ? SUCCESS : ERROR);
            reponseCError.setText(!nv.trim().isEmpty() ? "" : "⚠ Obligatoire");
        });
        reponseD.textProperty().addListener((obs, old, nv) -> {
            reponseD.setStyle(!nv.trim().isEmpty() ? SUCCESS : ERROR);
            reponseDError.setText(!nv.trim().isEmpty() ? "" : "⚠ Obligatoire");
        });
        bonneReponseBox.valueProperty().addListener((obs, old, nv) -> {
            if (nv != null) bonneReponseError.setText("");
        });
    }


    // SET QUIZ

    public void setQuiz(int quizId, String quizTitre) {
        this.quizId    = quizId;
        this.quizTitre = quizTitre;
        quizTitreLabel.setText(quizId == 0
                ? "Toutes les questions" : "Quiz : " + quizTitre);
        chargerQuestions();
    }


    // JSON HELPERS

    private String construireJson(String a, String b, String c,
                                  String d, String correcte) {
        return "[" +
                "{\"texte\":\"" + a + "\",\"correct\":" + correcte.equals("A") + "}," +
                "{\"texte\":\"" + b + "\",\"correct\":" + correcte.equals("B") + "}," +
                "{\"texte\":\"" + c + "\",\"correct\":" + correcte.equals("C") + "}," +
                "{\"texte\":\"" + d + "\",\"correct\":" + correcte.equals("D") + "}" +
                "]";
    }//convertir les reponses du formulaire en format stockable dans la base

    private String extraireReponse(String json, String lettre) {
        if (json == null || json.isEmpty()) return "";//si json est vide ou null on retourne chaine vide
        try {
            String[] parts = json.replace("[","").replace("]","").split("},");//supprimer [ et ] et decoupe texte a chaque }
            int index = lettre.equals("A") ? 0 : lettre.equals("B") ? 1 :
                    lettre.equals("C") ? 2 : 3;//transforme chaque lettre en position
            if (index >= parts.length) return "";//Sécurité : si l’index demandé n’existe pas, on retourne vide
            String part  = parts[index];//récupère la partie correspondant à la réponse demandée.
            int    start = part.indexOf("\"texte\":\"") + 9;//texte conteitn 9 donc on saute cette partie pour  arriver au vrai texte
            int    end   = part.indexOf("\"", start);//on cherche prochain guillemet ou se termine texte
            return part.substring(start, end);//Extrait le texte entre start et end
        } catch (Exception e) { return ""; }
    }

    private String extraireCorrecte(String json) {
        if (json == null || json.isEmpty()) return "";
        try {
            String[] parts   = json.replace("[","").replace("]","").split("},");
            String[] lettres = {"A","B","C","D"};
            for (int i = 0; i < parts.length; i++)
                if (parts[i].contains("\"correct\":true")) return lettres[i];
        } catch (Exception e) { return ""; }
        return "";
    }


    // CRUD

    @FXML
    public void chargerQuestions() {
        try {
            ObservableList<Question> list = FXCollections.observableArrayList();
            for (Question q : service.afficher())
                if (quizId == 0 || q.getQuizId() == quizId) list.add(q);
            questionTable.setItems(list);
        } catch (Exception e) { showError("❌ " + e.getMessage()); }
    }

    private boolean validerChamps() {
        resetErrors();
        boolean ok = true;

        if (questionField.getText().trim().isEmpty()) {
            questionField.setStyle(ERROR);
            questionError.setText("⚠ La question est obligatoire");
            ok = false;
        } else if (questionField.getText().trim().length() < 5) {
            questionField.setStyle(ERROR);
            questionError.setText("⚠ Minimum 5 caractères");
            ok = false;
        } else questionField.setStyle(SUCCESS);

        if (pointsField.getText().trim().isEmpty()) {
            pointsField.setStyle(ERROR);
            pointsError.setText("⚠ Les points sont obligatoires");
            ok = false;
        } else {
            try {
                int p = Integer.parseInt(pointsField.getText().trim());
                if (p <= 0 || p > 100) {
                    pointsField.setStyle(ERROR);
                    pointsError.setText("⚠ Entre 1 et 100");
                    ok = false;
                } else pointsField.setStyle(SUCCESS);
            } catch (NumberFormatException e) {
                pointsField.setStyle(ERROR);
                pointsError.setText("⚠ Nombre entier uniquement");
                ok = false;
            }
        }

        if (reponseA.getText().trim().isEmpty()) {
            reponseA.setStyle(ERROR); reponseAError.setText("⚠ Obligatoire"); ok = false;
        } else reponseA.setStyle(SUCCESS);
        if (reponseB.getText().trim().isEmpty()) {
            reponseB.setStyle(ERROR); reponseBError.setText("⚠ Obligatoire"); ok = false;
        } else reponseB.setStyle(SUCCESS);
        if (reponseC.getText().trim().isEmpty()) {
            reponseC.setStyle(ERROR); reponseCError.setText("⚠ Obligatoire"); ok = false;
        } else reponseC.setStyle(SUCCESS);
        if (reponseD.getText().trim().isEmpty()) {
            reponseD.setStyle(ERROR); reponseDError.setText("⚠ Obligatoire"); ok = false;
        } else reponseD.setStyle(SUCCESS);

        if (!reponseA.getText().trim().isEmpty() && !reponseB.getText().trim().isEmpty()
                && !reponseC.getText().trim().isEmpty()
                && !reponseD.getText().trim().isEmpty()) {
            String a = reponseA.getText().trim().toLowerCase();
            String b = reponseB.getText().trim().toLowerCase();
            String c = reponseC.getText().trim().toLowerCase();
            String d = reponseD.getText().trim().toLowerCase();
            if (a.equals(b)||a.equals(c)||a.equals(d)||
                    b.equals(c)||b.equals(d)||c.equals(d)) {
                reponsesError.setText("⚠ Les réponses doivent être différentes !");
                reponseA.setStyle(ERROR); reponseB.setStyle(ERROR);
                reponseC.setStyle(ERROR); reponseD.setStyle(ERROR);
                ok = false;
            }
        }

        if (bonneReponseBox.getValue() == null) {
            bonneReponseError.setText("⚠ Choisissez la bonne réponse");
            ok = false;
        }
        if (quizId == 0) { showError("❌ Aucun quiz sélectionné !"); ok = false; }
        return ok;
    }

    @FXML
    private void ajouterQuestion() {
        if (!validerChamps()) return;
        try {
            String json = construireJson(
                    reponseA.getText().trim(), reponseB.getText().trim(),
                    reponseC.getText().trim(), reponseD.getText().trim(),
                    bonneReponseBox.getValue());
            service.ajouter(new Question(quizId,
                    questionField.getText().trim(),
                    Integer.parseInt(pointsField.getText().trim()), json));
            showSuccess("✅ Question ajoutée !");
            annuler(); chargerQuestions();//les questions apparait dans tableview
        } catch (Exception e) { showError("❌ " + e.getMessage()); }
    }

    @FXML
    private void modifierQuestion() {
        Question sel = questionTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showError("❌ Sélectionnez une question !"); return; }
        if (!validerChamps()) return;
        try {
            String json = construireJson(
                    reponseA.getText().trim(), reponseB.getText().trim(),
                    reponseC.getText().trim(), reponseD.getText().trim(),
                    bonneReponseBox.getValue());
            sel.setQuestion(questionField.getText().trim());
            sel.setPoints(Integer.parseInt(pointsField.getText().trim()));
            sel.setReponses(json);
            service.modifier(sel);
            showSuccess("✅ Question modifiée !");
            annuler(); chargerQuestions();
        } catch (Exception e) { showError("❌ " + e.getMessage()); }
    }

    @FXML
    private void supprimerQuestion() {
        Question sel = questionTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showError("❌ Sélectionnez une question !"); return; }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer cette question ?", ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle("Confirmation");
        alert.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                try {
                    service.supprimer(sel.getId());
                    showSuccess("✅ Supprimée !"); annuler(); chargerQuestions();
                } catch (Exception e) { showError("❌ " + e.getMessage()); }
            }
        });
    }

    @FXML
    private void annuler() {
        questionField.clear(); pointsField.clear();
        reponseA.clear(); reponseB.clear(); reponseC.clear(); reponseD.clear();
        bonneReponseBox.setValue(null);
        messageLabel.setText("");
        resetErrors();
        questionTable.getSelectionModel().clearSelection();
    }


    // NAVIGATION

    @FXML
    private void retourQuiz() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/AjouterQuiz.fxml"));
            Stage stage = (Stage) questionField.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Formateur - Gestion Quiz");
        } catch (Exception e) { showError("❌ " + e.getMessage()); }
    }

    @FXML
    private void goAccueil() {
        try {
            Stage stage = (Stage) questionField.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/org/example/AccueilFormateur.fxml"));
            stage.setScene(new Scene(loader.load(), 1200, 750));
            stage.setTitle("Formateur — Tableau de bord");
        } catch (Exception e) { showError("❌ " + e.getMessage()); }
    }


    // HELPERS

    private void resetErrors() {
        questionField.setStyle(NORMAL); pointsField.setStyle(NORMAL);
        reponseA.setStyle(NORMAL); reponseB.setStyle(NORMAL);
        reponseC.setStyle(NORMAL); reponseD.setStyle(NORMAL);
        questionError.setText(""); pointsError.setText("");
        reponseAError.setText(""); reponseBError.setText("");
        reponseCError.setText(""); reponseDError.setText("");
        reponsesError.setText(""); bonneReponseError.setText("");
    }

    private void showSuccess(String msg) {
        messageLabel.setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");
        messageLabel.setText(msg);
    }

    private void showError(String msg) {
        messageLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        messageLabel.setText(msg);
    }
}