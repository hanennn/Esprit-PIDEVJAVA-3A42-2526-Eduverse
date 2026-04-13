package org.example.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.example.entities.Question;
import org.example.services.QuestionService;

public class AjouterQuestionController {

    @FXML private Label    quizTitreLabel;
    @FXML private TextArea questionField;
    @FXML private TextField pointsField;
    @FXML private TextField reponseA;
    @FXML private TextField reponseB;
    @FXML private TextField reponseC;
    @FXML private TextField reponseD;
    @FXML private ComboBox<String> bonneReponseBox;
    @FXML private Label messageLabel;

    @FXML private Label questionError;
    @FXML private Label pointsError;
    @FXML private Label reponseAError;
    @FXML private Label reponseBError;
    @FXML private Label reponseCError;
    @FXML private Label reponseDError;
    @FXML private Label reponsesError;
    @FXML private Label bonneReponseError;

    @FXML private TableView<Question>            questionTable;
    @FXML private TableColumn<Question, Integer> idCol;
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

    private static final String NORMAL =
            "-fx-background-color: #f4f6f9; -fx-border-color: #dde1e7; " +
                    "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 9;";
    private static final String ERROR =
            "-fx-border-color: #e74c3c; -fx-border-radius: 8; " +
                    "-fx-background-color: #fff0f0; -fx-background-radius: 8; -fx-padding: 9;";
    private static final String SUCCESS =
            "-fx-border-color: #2ecc71; -fx-border-radius: 8; " +
                    "-fx-background-color: #f0fff4; -fx-background-radius: 8; -fx-padding: 9;";

    // ─────────── INIT ───────────
    @FXML
    public void initialize() {
        bonneReponseBox.getItems().addAll("A", "B", "C", "D");

        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        questionCol.setCellValueFactory(new PropertyValueFactory<>("question"));
        pointsCol.setCellValueFactory(new PropertyValueFactory<>("points"));
        reponseACol.setCellValueFactory(cell ->
                new SimpleStringProperty(extraireReponse(cell.getValue().getReponses(), "A")));
        reponseBCol.setCellValueFactory(cell ->
                new SimpleStringProperty(extraireReponse(cell.getValue().getReponses(), "B")));
        reponseCCol.setCellValueFactory(cell ->
                new SimpleStringProperty(extraireReponse(cell.getValue().getReponses(), "C")));
        reponseDCol.setCellValueFactory(cell ->
                new SimpleStringProperty(extraireReponse(cell.getValue().getReponses(), "D")));
        correcteCol.setCellValueFactory(cell ->
                new SimpleStringProperty(extraireCorrecte(cell.getValue().getReponses())));

        // Listeners temps réel
        ajouterListeners();

        questionTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
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
                }
        );

        chargerQuestions();
    }

    // ─────────── LISTENERS TEMPS RÉEL ───────────
    private void ajouterListeners() {
        // Question
        questionField.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.trim().isEmpty()) {
                questionField.setStyle(SUCCESS);
                questionError.setText("");
            }
        });

        // Points
        pointsField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal.trim().isEmpty()) return;
            try {
                int p = Integer.parseInt(newVal.trim());
                if (p > 0) {
                    pointsField.setStyle(SUCCESS);
                    pointsError.setText("");
                } else {
                    pointsField.setStyle(ERROR);
                    pointsError.setText("⚠ Doit être supérieur à 0");
                }
            } catch (NumberFormatException e) {
                pointsField.setStyle(ERROR);
                pointsError.setText("⚠ Nombre entier uniquement");
            }
        });

        // Réponse A
        reponseA.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.trim().isEmpty()) {
                reponseA.setStyle(SUCCESS);
                reponseAError.setText("");
            } else {
                reponseA.setStyle(ERROR);
                reponseAError.setText("⚠ Réponse A obligatoire");
            }
        });

        // Réponse B
        reponseB.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.trim().isEmpty()) {
                reponseB.setStyle(SUCCESS);
                reponseBError.setText("");
            } else {
                reponseB.setStyle(ERROR);
                reponseBError.setText("⚠ Réponse B obligatoire");
            }
        });

        // Réponse C
        reponseC.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.trim().isEmpty()) {
                reponseC.setStyle(SUCCESS);
                reponseCError.setText("");
            } else {
                reponseC.setStyle(ERROR);
                reponseCError.setText("⚠ Réponse C obligatoire");
            }
        });

        // Réponse D
        reponseD.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.trim().isEmpty()) {
                reponseD.setStyle(SUCCESS);
                reponseDError.setText("");
            } else {
                reponseD.setStyle(ERROR);
                reponseDError.setText("⚠ Réponse D obligatoire");
            }
        });

        // Bonne réponse
        bonneReponseBox.valueProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                bonneReponseError.setText("");
            }
        });
    }

    // ─────────── SET QUIZ ───────────
    public void setQuiz(int quizId, String quizTitre) {
        this.quizId    = quizId;
        this.quizTitre = quizTitre;
        if (quizId == 0) {
            quizTitreLabel.setText("Toutes les questions");
        } else {
            quizTitreLabel.setText("Quiz : " + quizTitre);
        }
        chargerQuestions();
    }

    // ─────────── JSON HELPERS ───────────
    private String construireJson(String a, String b, String c, String d, String correcte) {
        return "[" +
                "{\"texte\":\"" + a + "\",\"correct\":" + correcte.equals("A") + "}," +
                "{\"texte\":\"" + b + "\",\"correct\":" + correcte.equals("B") + "}," +
                "{\"texte\":\"" + c + "\",\"correct\":" + correcte.equals("C") + "}," +
                "{\"texte\":\"" + d + "\",\"correct\":" + correcte.equals("D") + "}" +
                "]";
    }

    private String extraireReponse(String json, String lettre) {
        if (json == null || json.isEmpty()) return "";
        try {
            String[] parts = json.replace("[", "").replace("]", "").split("},");
            int index = lettre.equals("A") ? 0 : lettre.equals("B") ? 1 :
                    lettre.equals("C") ? 2 : 3;
            if (index >= parts.length) return "";
            String part  = parts[index];
            int    start = part.indexOf("\"texte\":\"") + 9;
            int    end   = part.indexOf("\"", start);
            return part.substring(start, end);
        } catch (Exception e) { return ""; }
    }

    private String extraireCorrecte(String json) {
        if (json == null || json.isEmpty()) return "";
        try {
            String[] parts   = json.replace("[", "").replace("]", "").split("},");
            String[] lettres = {"A", "B", "C", "D"};
            for (int i = 0; i < parts.length; i++) {
                if (parts[i].contains("\"correct\":true")) return lettres[i];
            }
        } catch (Exception e) { return ""; }
        return "";
    }

    // ─────────── CHARGER ───────────
    @FXML
    public void chargerQuestions() {
        try {
            ObservableList<Question> list = FXCollections.observableArrayList();
            for (Question q : service.afficher()) {
                if (quizId == 0 || q.getQuizId() == quizId) list.add(q);
            }
            questionTable.setItems(list);
        } catch (Exception e) {
            showError(" Erreur : " + e.getMessage());
        }
    }

    // ─────────── VALIDATION ───────────
    private boolean validerChamps() {
        resetErrors();
        boolean ok = true;

        // Question vide
        if (questionField.getText().trim().isEmpty()) {
            questionField.setStyle(ERROR);
            questionError.setText("⚠ La question est obligatoire");
            ok = false;
        } else if (questionField.getText().trim().length() < 5) {
            questionField.setStyle(ERROR);
            questionError.setText("⚠ La question doit contenir au moins 5 caractères");
            ok = false;
        } else {
            questionField.setStyle(SUCCESS);
        }

        // Points
        if (pointsField.getText().trim().isEmpty()) {
            pointsField.setStyle(ERROR);
            pointsError.setText("⚠ Les points sont obligatoires");
            ok = false;
        } else {
            try {
                int p = Integer.parseInt(pointsField.getText().trim());
                if (p <= 0) {
                    pointsField.setStyle(ERROR);
                    pointsError.setText("⚠ Les points doivent être supérieurs à 0");
                    ok = false;
                } else if (p > 100) {
                    pointsField.setStyle(ERROR);
                    pointsError.setText("⚠ Les points ne peuvent pas dépasser 100");
                    ok = false;
                } else {
                    pointsField.setStyle(SUCCESS);
                }
            } catch (NumberFormatException e) {
                pointsField.setStyle(ERROR);
                pointsError.setText("⚠ Les points doivent être un nombre entier");
                ok = false;
            }
        }

        // Réponse A
        if (reponseA.getText().trim().isEmpty()) {
            reponseA.setStyle(ERROR);
            reponseAError.setText("⚠ La réponse A est obligatoire");
            ok = false;
        } else {
            reponseA.setStyle(SUCCESS);
        }

        // Réponse B
        if (reponseB.getText().trim().isEmpty()) {
            reponseB.setStyle(ERROR);
            reponseBError.setText("⚠ La réponse B est obligatoire");
            ok = false;
        } else {
            reponseB.setStyle(SUCCESS);
        }

        // Réponse C
        if (reponseC.getText().trim().isEmpty()) {
            reponseC.setStyle(ERROR);
            reponseCError.setText("⚠ La réponse C est obligatoire");
            ok = false;
        } else {
            reponseC.setStyle(SUCCESS);
        }

        // Réponse D
        if (reponseD.getText().trim().isEmpty()) {
            reponseD.setStyle(ERROR);
            reponseDError.setText("⚠ La réponse D est obligatoire");
            ok = false;
        } else {
            reponseD.setStyle(SUCCESS);
        }

        // Réponses identiques
        if (!reponseA.getText().trim().isEmpty() &&
                !reponseB.getText().trim().isEmpty() &&
                !reponseC.getText().trim().isEmpty() &&
                !reponseD.getText().trim().isEmpty()) {

            String a = reponseA.getText().trim().toLowerCase();
            String b = reponseB.getText().trim().toLowerCase();
            String c = reponseC.getText().trim().toLowerCase();
            String d = reponseD.getText().trim().toLowerCase();

            if (a.equals(b) || a.equals(c) || a.equals(d) ||
                    b.equals(c) || b.equals(d) || c.equals(d)) {
                reponsesError.setText("⚠ Les réponses doivent être toutes différentes !");
                reponseA.setStyle(ERROR);
                reponseB.setStyle(ERROR);
                reponseC.setStyle(ERROR);
                reponseD.setStyle(ERROR);
                ok = false;
            }
        }

        // Bonne réponse
        if (bonneReponseBox.getValue() == null) {
            bonneReponseError.setText("⚠ Veuillez choisir la bonne réponse");
            ok = false;
        }

        // Quiz sélectionné
        if (quizId == 0) {
            showError(" Aucun quiz sélectionné — revenez aux quiz !");
            ok = false;
        }

        return ok;
    }

    // ─────────── AJOUTER ───────────
    @FXML
    private void ajouterQuestion() {
        if (!validerChamps()) return;
        try {
            String json = construireJson(
                    reponseA.getText().trim(), reponseB.getText().trim(),
                    reponseC.getText().trim(), reponseD.getText().trim(),
                    bonneReponseBox.getValue()
            );
            Question q = new Question(quizId, questionField.getText().trim(),
                    Integer.parseInt(pointsField.getText().trim()), json);
            service.ajouter(q);
            showSuccess(" Question ajoutée avec succès !");
            annuler();
            chargerQuestions();
        } catch (Exception e) {
            showError(" Erreur : " + e.getMessage());
        }
    }

    // ─────────── MODIFIER ───────────
    @FXML
    private void modifierQuestion() {
        Question selected = questionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError(" Sélectionnez une question à modifier !");
            return;
        }
        if (!validerChamps()) return;
        try {
            String json = construireJson(
                    reponseA.getText().trim(), reponseB.getText().trim(),
                    reponseC.getText().trim(), reponseD.getText().trim(),
                    bonneReponseBox.getValue()
            );
            selected.setQuestion(questionField.getText().trim());
            selected.setPoints(Integer.parseInt(pointsField.getText().trim()));
            selected.setReponses(json);
            service.modifier(selected);
            showSuccess(" Question modifiée avec succès !");
            annuler();
            chargerQuestions();
        } catch (Exception e) {
            showError(" Erreur : " + e.getMessage());
        }
    }

    // ─────────── SUPPRIMER ───────────
    @FXML
    private void supprimerQuestion() {
        Question selected = questionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError(" Sélectionnez une question à supprimer !");
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer la question ?");
        alert.setContentText("Voulez-vous vraiment supprimer cette question ?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    service.supprimer(selected.getId());
                    showSuccess(" Question supprimée !");
                    annuler();
                    chargerQuestions();
                } catch (Exception e) {
                    showError(" Erreur : " + e.getMessage());
                }
            }
        });
    }

    // ─────────── ANNULER ───────────
    @FXML
    private void annuler() {
        questionField.clear();
        pointsField.clear();
        reponseA.clear();
        reponseB.clear();
        reponseC.clear();
        reponseD.clear();
        bonneReponseBox.setValue(null);
        messageLabel.setText("");
        resetErrors();
        questionTable.getSelectionModel().clearSelection();
    }

    // ─────────── RETOUR QUIZ ───────────
    @FXML
    private void retourQuiz() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/AjouterQuiz.fxml"));
            Stage stage = (Stage) questionField.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Formateur - Gestion Quiz");
        } catch (Exception e) {
            showError(" Erreur retour : " + e.getMessage());
        }
    }

    // ─────────── ACCUEIL ───────────
    @FXML
    private void goAccueil() {
        try {
            Stage stage = (Stage) questionField.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/AccueilFormateur.fxml"));
            stage.setScene(new Scene(loader.load(), 1200, 750));
            stage.setTitle("Formateur — Tableau de bord");
        } catch (Exception e) {
            showError(" Erreur : " + e.getMessage());
        }
    }

    // ─────────── HELPERS ───────────
    private void resetErrors() {
        questionField.setStyle(NORMAL);
        pointsField.setStyle(NORMAL);
        reponseA.setStyle(NORMAL);
        reponseB.setStyle(NORMAL);
        reponseC.setStyle(NORMAL);
        reponseD.setStyle(NORMAL);
        questionError.setText("");
        pointsError.setText("");
        reponseAError.setText("");
        reponseBError.setText("");
        reponseCError.setText("");
        reponseDError.setText("");
        reponsesError.setText("");
        bonneReponseError.setText("");
    }

    private void showSuccess(String msg) {
        messageLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
        messageLabel.setText(msg);
    }

    private void showError(String msg) {
        messageLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        messageLabel.setText(msg);
    }
}