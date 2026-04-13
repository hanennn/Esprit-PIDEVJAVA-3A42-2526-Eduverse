package org.example.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.entities.Quiz;
import org.example.services.QuizService;
import org.example.utils.MyConnection;

import java.sql.*;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class AjouterQuizController {

    // ── Formulaire ──
    @FXML private TextField        titreField;
    @FXML private ComboBox<String> typeQuizBox;
    @FXML private TextField        dureeField;
    @FXML private TextField        scoreField;
    @FXML private ComboBox<String> coursBox;
    @FXML private Label            messageLabel;

    @FXML private Label titreError;
    @FXML private Label typeError;
    @FXML private Label dureeError;
    @FXML private Label scoreError;
    @FXML private Label coursError;

    // ── Table ──
    @FXML private TableView<Quiz>            quizTable;
    @FXML private TableColumn<Quiz, Integer> idCol;
    @FXML private TableColumn<Quiz, String>  titreCol;
    @FXML private TableColumn<Quiz, String>  typeCol;
    @FXML private TableColumn<Quiz, Integer> dureeCol;
    @FXML private TableColumn<Quiz, Float>   scoreCol;
    @FXML private TableColumn<Quiz, String>  coursCol;

    // ── Recherche & Tri ──
    @FXML private TextField        searchTitreField;
    @FXML private ComboBox<String> filterTypeBox;
    @FXML private ComboBox<String> filterCoursBox;
    @FXML private TextField        searchDureeMinField;
    @FXML private TextField        searchDureeMaxField;
    @FXML private TextField        searchScoreMinField;
    @FXML private ComboBox<String> sortBox;
    @FXML private Label            resultCountLabel;

    private final QuizService          service  = new QuizService();
    private final Map<String, Integer> coursMap = new HashMap<>();

    private ObservableList<Quiz> masterList   = FXCollections.observableArrayList();
    private FilteredList<Quiz>   filteredList;

    private static final String NORMAL =
            "-fx-background-color: #f4f6f9; -fx-border-color: #dde1e7; " +
                    "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 9;";
    private static final String ERROR =
            "-fx-border-color: #e74c3c; -fx-border-radius: 8; " +
                    "-fx-background-color: #fff0f0; -fx-background-radius: 8; -fx-padding: 9;";

    // ─────────── INIT ───────────
    @FXML
    public void initialize() {
        typeQuizBox.getItems().addAll("Intermédiaire", "Final");

        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        titreCol.setCellValueFactory(new PropertyValueFactory<>("titre"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("typeQuiz"));
        dureeCol.setCellValueFactory(new PropertyValueFactory<>("duree"));
        scoreCol.setCellValueFactory(new PropertyValueFactory<>("scoreMinimum"));
        coursCol.setCellValueFactory(cell ->
                new SimpleStringProperty(
                        service.getNomCours(cell.getValue().getCoursAssocieId())
                )
        );

        sortBox.getItems().addAll(
                "Titre A→Z", "Titre Z→A",
                "Durée croissante", "Durée décroissante",
                "Score croissant", "Score décroissant"
        );

        filterTypeBox.getItems().addAll("Tous", "Intermédiaire", "Final");
        filterTypeBox.setValue("Tous");

        chargerCours();
        chargerQuiz();
        configurerFiltres();

        quizTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        titreField.setText(newVal.getTitre());
                        typeQuizBox.setValue(newVal.getTypeQuiz());
                        dureeField.setText(String.valueOf(newVal.getDuree()));
                        scoreField.setText(String.valueOf(newVal.getScoreMinimum()));
                        coursMap.forEach((nom, id) -> {
                            if (id == newVal.getCoursAssocieId())
                                coursBox.setValue(nom);
                        });
                        resetErrors();
                    }
                }
        );
    }

    // ─────────── CHARGER COURS ───────────
    private void chargerCours() {
        try {
            Connection cnx = MyConnection.getInstance().getCnx();
            ResultSet rs = cnx.createStatement()
                    .executeQuery("SELECT id, titre_cours FROM cours");
            filterCoursBox.getItems().add("Tous");
            while (rs.next()) {
                int    id  = rs.getInt("id");
                String nom = rs.getString("titre_cours");
                coursMap.put(nom, id);
                coursBox.getItems().add(nom);
                filterCoursBox.getItems().add(nom);
            }
            filterCoursBox.setValue("Tous");
        } catch (Exception e) {
            showError("❌ Erreur chargement cours : " + e.getMessage());
        }
    }

    // ─────────── CHARGER TABLE ───────────
    @FXML
    public void chargerQuiz() {
        try {
            masterList = FXCollections.observableArrayList(service.afficher());
            filteredList = new FilteredList<>(masterList, p -> true);
            SortedList<Quiz> sortedList = new SortedList<>(filteredList);
            quizTable.setItems(sortedList);
            mettreAJourCompteur();
        } catch (Exception e) {
            showError("❌ Erreur chargement : " + e.getMessage());
        }
    }

    // ─────────── CONFIGURER FILTRES ───────────
    private void configurerFiltres() {
        searchTitreField.textProperty().addListener(
                (obs, o, n) -> appliquerFiltres());
        filterTypeBox.valueProperty().addListener(
                (obs, o, n) -> appliquerFiltres());
        filterCoursBox.valueProperty().addListener(
                (obs, o, n) -> appliquerFiltres());
        searchDureeMinField.textProperty().addListener(
                (obs, o, n) -> appliquerFiltres());
        searchDureeMaxField.textProperty().addListener(
                (obs, o, n) -> appliquerFiltres());
        searchScoreMinField.textProperty().addListener(
                (obs, o, n) -> appliquerFiltres());
        sortBox.valueProperty().addListener(
                (obs, o, n) -> appliquerFiltres());
    }

    // ─────────── APPLIQUER FILTRES ───────────
    private void appliquerFiltres() {
        if (filteredList == null) return;

        filteredList.setPredicate(quiz -> {
            String titre = searchTitreField.getText().trim().toLowerCase();
            if (!titre.isEmpty() &&
                    !quiz.getTitre().toLowerCase().contains(titre))
                return false;

            String type = filterTypeBox.getValue();
            if (type != null && !type.equals("Tous") &&
                    !quiz.getTypeQuiz().equals(type))
                return false;

            String cours = filterCoursBox.getValue();
            if (cours != null && !cours.equals("Tous")) {
                String nomCours = service.getNomCours(quiz.getCoursAssocieId());
                if (!nomCours.equals(cours)) return false;
            }

            String dureeMinStr = searchDureeMinField.getText().trim();
            if (!dureeMinStr.isEmpty()) {
                try {
                    int dureeMin = Integer.parseInt(dureeMinStr);
                    if (quiz.getDuree() < dureeMin) return false;
                } catch (NumberFormatException ignored) {}
            }

            String dureeMaxStr = searchDureeMaxField.getText().trim();
            if (!dureeMaxStr.isEmpty()) {
                try {
                    int dureeMax = Integer.parseInt(dureeMaxStr);
                    if (quiz.getDuree() > dureeMax) return false;
                } catch (NumberFormatException ignored) {}
            }

            String scoreMinStr = searchScoreMinField.getText().trim();
            if (!scoreMinStr.isEmpty()) {
                try {
                    float scoreMin = Float.parseFloat(scoreMinStr);
                    if (quiz.getScoreMinimum() < scoreMin) return false;
                } catch (NumberFormatException ignored) {}
            }

            return true;
        });

        appliquerTri();
        mettreAJourCompteur();
    }

    // ─────────── APPLIQUER TRI ───────────
    private void appliquerTri() {
        if (filteredList == null) return;

        String tri = sortBox.getValue();
        if (tri == null) {
            quizTable.setItems(new SortedList<>(filteredList));
            return;
        }

        ObservableList<Quiz> sorted =
                FXCollections.observableArrayList(filteredList);

        switch (tri) {
            case "Titre A→Z" ->
                    sorted.sort(Comparator.comparing(
                            q -> q.getTitre().toLowerCase()));
            case "Titre Z→A" ->
                    sorted.sort(Comparator.comparing(
                            (Quiz q) -> q.getTitre().toLowerCase()).reversed());
            case "Durée croissante" ->
                    sorted.sort(Comparator.comparingInt(Quiz::getDuree));
            case "Durée décroissante" ->
                    sorted.sort(Comparator.comparingInt(Quiz::getDuree).reversed());
            case "Score croissant" ->
                    sorted.sort(Comparator.comparingDouble(Quiz::getScoreMinimum));
            case "Score décroissant" ->
                    sorted.sort(Comparator.comparingDouble(
                            Quiz::getScoreMinimum).reversed());
        }

        quizTable.setItems(sorted);
    }

    // ─────────── COMPTEUR ───────────
    private void mettreAJourCompteur() {
        if (filteredList == null) return;
        int total   = masterList.size();
        int filtres = filteredList.size();
        if (filtres == total) {
            resultCountLabel.setText(total + " quiz au total");
            resultCountLabel.setStyle(
                    "-fx-font-size: 11; -fx-text-fill: #888;");
        } else {
            resultCountLabel.setText(filtres + " résultat(s) sur " + total);
            resultCountLabel.setStyle(
                    "-fx-font-size: 11; -fx-text-fill: #f5a623;" +
                            "-fx-font-weight: bold;");
        }
    }

    // ─────────── RÉINITIALISER FILTRES ───────────
    @FXML
    private void reinitialiserFiltres() {
        searchTitreField.clear();
        filterTypeBox.setValue("Tous");
        filterCoursBox.setValue("Tous");
        searchDureeMinField.clear();
        searchDureeMaxField.clear();
        searchScoreMinField.clear();
        sortBox.setValue(null);
        if (filteredList != null) {
            filteredList.setPredicate(p -> true);
            quizTable.setItems(new SortedList<>(filteredList));
        }
        mettreAJourCompteur();
    }

    // ─────────── VALIDATION ───────────
    private boolean validerChamps() {
        resetErrors();
        boolean ok = true;

        if (titreField.getText().trim().isEmpty()) {
            titreField.setStyle(ERROR);
            titreError.setText("⚠ Le titre est obligatoire");
            ok = false;
        }
        if (typeQuizBox.getValue() == null) {
            typeError.setText("⚠ Veuillez choisir un type");
            ok = false;
        }
        if (dureeField.getText().trim().isEmpty()) {
            dureeField.setStyle(ERROR);
            dureeError.setText("⚠ La durée est obligatoire");
            ok = false;
        } else {
            try {
                int d = Integer.parseInt(dureeField.getText().trim());
                if (d <= 0) {
                    dureeField.setStyle(ERROR);
                    dureeError.setText("⚠ La durée doit être > 0");
                    ok = false;
                }
            } catch (NumberFormatException e) {
                dureeField.setStyle(ERROR);
                dureeError.setText("⚠ La durée doit être un entier");
                ok = false;
            }
        }
        if (scoreField.getText().trim().isEmpty()) {
            scoreField.setStyle(ERROR);
            scoreError.setText("⚠ Le score est obligatoire");
            ok = false;
        } else {
            try {
                float s = Float.parseFloat(scoreField.getText().trim());
                if (s < 0) {
                    scoreField.setStyle(ERROR);
                    scoreError.setText("⚠ Le score ne peut pas être négatif");
                    ok = false;
                }
            } catch (NumberFormatException e) {
                scoreField.setStyle(ERROR);
                scoreError.setText("⚠ Score invalide (ex: 10.5)");
                ok = false;
            }
        }
        if (coursBox.getValue() == null) {
            coursError.setText("⚠ Veuillez choisir un cours");
            ok = false;
        }
        return ok;
    }

    // ─────────── POPUP DOUBLON ───────────
    private void afficherPopupDoublon(String message) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Doublon détecté");
        popup.setResizable(false);

        VBox root = new VBox(18);
        root.setAlignment(Pos.CENTER);
        root.setStyle(
                "-fx-background-color: white; -fx-padding: 35;" +
                        "-fx-min-width: 380;"
        );

        Label icone = new Label("⚠️");
        icone.setStyle("-fx-font-size: 45;");

        Label titre = new Label("Doublon détecté !");
        titre.setStyle(
                "-fx-font-size: 18; -fx-font-weight: bold;" +
                        "-fx-text-fill: #e74c3c;");

        HBox barre = new HBox();
        barre.setPrefHeight(3);
        barre.setPrefWidth(300);
        barre.setStyle(
                "-fx-background-color: #e74c3c; -fx-background-radius: 2;");

        Label msg = new Label(message);
        msg.setStyle(
                "-fx-font-size: 13; -fx-text-fill: #555;" +
                        "-fx-text-alignment: center;");
        msg.setWrapText(true);
        msg.setAlignment(Pos.CENTER);

        Button btnOk = new Button("Compris !");
        btnOk.setPrefWidth(160);
        btnOk.setStyle(
                "-fx-background-color: #e74c3c; -fx-text-fill: white;" +
                        "-fx-font-weight: bold; -fx-font-size: 13;" +
                        "-fx-background-radius: 8; -fx-padding: 10 20; -fx-cursor: hand;"
        );
        btnOk.setOnMouseEntered(e -> btnOk.setStyle(
                "-fx-background-color: #c0392b; -fx-text-fill: white;" +
                        "-fx-font-weight: bold; -fx-font-size: 13;" +
                        "-fx-background-radius: 8; -fx-padding: 10 20; -fx-cursor: hand;"
        ));
        btnOk.setOnMouseExited(e -> btnOk.setStyle(
                "-fx-background-color: #e74c3c; -fx-text-fill: white;" +
                        "-fx-font-weight: bold; -fx-font-size: 13;" +
                        "-fx-background-radius: 8; -fx-padding: 10 20; -fx-cursor: hand;"
        ));
        btnOk.setOnAction(e -> popup.close());

        root.getChildren().addAll(icone, titre, barre, msg, btnOk);

        popup.setScene(new Scene(root));
        popup.show();

        javafx.application.Platform.runLater(() -> {
            Stage parent = (Stage) titreField.getScene().getWindow();
            popup.setX(parent.getX()
                    + (parent.getWidth()  - popup.getWidth())  / 2);
            popup.setY(parent.getY()
                    + (parent.getHeight() - popup.getHeight()) / 2);
        });
    }

    // ─────────── POPUP SUCCÈS ───────────
    private void afficherPopupSucces(String message) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Succès");
        popup.setResizable(false);

        VBox root = new VBox(18);
        root.setAlignment(Pos.CENTER);
        root.setStyle(
                "-fx-background-color: white; -fx-padding: 35;" +
                        "-fx-min-width: 380;"
        );

        Label icone = new Label("✅");
        icone.setStyle("-fx-font-size: 45;");

        Label titre = new Label("Opération réussie !");
        titre.setStyle(
                "-fx-font-size: 18; -fx-font-weight: bold;" +
                        "-fx-text-fill: #2ecc71;");

        HBox barre = new HBox();
        barre.setPrefHeight(3);
        barre.setPrefWidth(300);
        barre.setStyle(
                "-fx-background-color: #2ecc71; -fx-background-radius: 2;");

        Label msg = new Label(message);
        msg.setStyle(
                "-fx-font-size: 13; -fx-text-fill: #555;" +
                        "-fx-text-alignment: center;");
        msg.setWrapText(true);
        msg.setAlignment(Pos.CENTER);

        Button btnOk = new Button("OK");
        btnOk.setPrefWidth(160);
        btnOk.setStyle(
                "-fx-background-color: #2ecc71; -fx-text-fill: white;" +
                        "-fx-font-weight: bold; -fx-font-size: 13;" +
                        "-fx-background-radius: 8; -fx-padding: 10 20; -fx-cursor: hand;"
        );
        btnOk.setOnAction(e -> popup.close());

        root.getChildren().addAll(icone, titre, barre, msg, btnOk);

        popup.setScene(new Scene(root));
        popup.show();

        // Fermer auto après 2s
        javafx.animation.PauseTransition pause =
                new javafx.animation.PauseTransition(
                        javafx.util.Duration.seconds(2));
        pause.setOnFinished(e -> popup.close());
        pause.play();

        javafx.application.Platform.runLater(() -> {
            Stage parent = (Stage) titreField.getScene().getWindow();
            popup.setX(parent.getX()
                    + (parent.getWidth()  - popup.getWidth())  / 2);
            popup.setY(parent.getY()
                    + (parent.getHeight() - popup.getHeight()) / 2);
        });
    }

    // ─────────── AJOUTER ───────────
    @FXML
    private void ajouterQuiz() {
        if (!validerChamps()) return;
        try {
            Quiz quiz = new Quiz(
                    titreField.getText().trim(),
                    typeQuizBox.getValue(),
                    Integer.parseInt(dureeField.getText().trim()),
                    Float.parseFloat(scoreField.getText().trim()),
                    coursMap.get(coursBox.getValue())
            );
            service.ajouter(quiz);
            afficherPopupSucces(
                    "Quiz \"" + quiz.getTitre() + "\" ajouté avec succès !");
            annuler();
            chargerQuiz();
        } catch (Exception e) {
            if (e.getMessage() != null &&
                    e.getMessage().contains("existe déjà")) {
                afficherPopupDoublon(e.getMessage());
            } else {
                showError(" Erreur : " + e.getMessage());
            }
        }
    }

    // ─────────── AJOUTER + QUESTIONS ───────────
    @FXML
    private void ajouterEtOuvrirQuestions() {
        if (!validerChamps()) return;
        try {
            String titre = titreField.getText().trim();
            Quiz quiz = new Quiz(
                    titre,
                    typeQuizBox.getValue(),
                    Integer.parseInt(dureeField.getText().trim()),
                    Float.parseFloat(scoreField.getText().trim()),
                    coursMap.get(coursBox.getValue())
            );
            service.ajouter(quiz);

            Quiz inserted = trouverDernierQuizParTitre(titre);
            if (inserted == null) {
                showError(" Impossible de récupérer le quiz !");
                return;
            }
            naviguerQuestions(inserted.getId(), inserted.getTitre());
        } catch (Exception e) {
            if (e.getMessage() != null &&
                    e.getMessage().contains("existe déjà")) {
                afficherPopupDoublon(e.getMessage());
            } else {
                showError(" Erreur : " + e.getMessage());
            }
        }
    }

    private Quiz trouverDernierQuizParTitre(String titre) {
        try {
            Quiz dernierTrouve = null;
            for (Quiz q : service.afficher()) {
                if (q.getTitre().equals(titre)) dernierTrouve = q;
            }
            return dernierTrouve;
        } catch (Exception e) {
            return null;
        }
    }

    // ─────────── MODIFIER ───────────
    @FXML
    private void modifierQuiz() {
        Quiz selected = quizTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError(" Sélectionnez un quiz à modifier !");
            return;
        }
        if (!validerChamps()) return;
        try {
            selected.setTitre(titreField.getText().trim());
            selected.setTypeQuiz(typeQuizBox.getValue());
            selected.setDuree(Integer.parseInt(dureeField.getText().trim()));
            selected.setScoreMinimum(
                    Float.parseFloat(scoreField.getText().trim()));
            selected.setCoursAssocieId(coursMap.get(coursBox.getValue()));
            service.modifier(selected);
            afficherPopupSucces(
                    "Quiz \"" + selected.getTitre() + "\" modifié avec succès !");
            annuler();
            chargerQuiz();
        } catch (Exception e) {
            if (e.getMessage() != null &&
                    e.getMessage().contains("existe déjà")) {
                afficherPopupDoublon(e.getMessage());
            } else {
                showError("Erreur : " + e.getMessage());
            }
        }
    }

    // ─────────── SUPPRIMER ───────────
    @FXML
    private void supprimerQuiz() {
        Quiz selected = quizTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError(" Sélectionnez un quiz à supprimer !");
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer le quiz ?");
        alert.setContentText(
                "Voulez-vous supprimer \"" + selected.getTitre() + "\" ?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    service.supprimer(selected.getId());
                    afficherPopupSucces(
                            "Quiz \"" + selected.getTitre()
                                    + "\" supprimé avec succès !");
                    annuler();
                    chargerQuiz();
                } catch (Exception e) {
                    showError("Erreur : " + e.getMessage());
                }
            }
        });
    }

    // ─────────── ANNULER ───────────
    @FXML
    private void annuler() {
        titreField.clear();
        typeQuizBox.setValue(null);
        dureeField.clear();
        scoreField.clear();
        coursBox.setValue(null);
        messageLabel.setText("");
        resetErrors();
        quizTable.getSelectionModel().clearSelection();
    }

    // ─────────── NAVIGATION QUESTIONS ───────────
    @FXML
    private void goQuestions() {
        Quiz selected = quizTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            naviguerQuestions(selected.getId(), selected.getTitre());
        } else {
            naviguerQuestions(0, "—");
        }
    }

    private void naviguerQuestions(int quizId, String quizTitre) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/AjouterQuestion.fxml"));
            Stage stage = (Stage) titreField.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Gestion des Questions");
            AjouterQuestionController ctrl = loader.getController();
            ctrl.setQuiz(quizId, quizTitre);
        } catch (Exception e) {
            showError(" Erreur navigation : " + e.getMessage());
        }
    }

    // ─────────── ACCUEIL ───────────
    @FXML
    private void goAccueil() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/AccueilFormateur.fxml"));
            Stage stage = (Stage) titreField.getScene().getWindow();
            stage.setScene(new Scene(loader.load(), 1200, 750));
            stage.setTitle("Formateur — Tableau de bord");
        } catch (Exception e) {
            showError(" Erreur navigation : " + e.getMessage());
        }
    }

    // ─────────── HELPERS ───────────
    private void resetErrors() {
        titreField.setStyle(NORMAL);
        dureeField.setStyle(NORMAL);
        scoreField.setStyle(NORMAL);
        titreError.setText("");
        typeError.setText("");
        dureeError.setText("");
        scoreError.setText("");
        coursError.setText("");
    }

    private void showSuccess(String msg) {
        messageLabel.setStyle(
                "-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
        messageLabel.setText(msg);
    }

    private void showError(String msg) {
        messageLabel.setStyle(
                "-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        messageLabel.setText(msg);
    }
}