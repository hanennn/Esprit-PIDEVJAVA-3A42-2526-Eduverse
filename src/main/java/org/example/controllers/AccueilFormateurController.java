package org.example.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.entities.Quiz;
import org.example.services.QuestionService;
import org.example.services.QuizService;
import org.example.utils.MyConnection;

import java.sql.*;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccueilFormateurController {

    @FXML private TextField        searchTitreField;
    @FXML private ComboBox<String> filterTypeBox;
    @FXML private ComboBox<String> filterCoursBox;
    @FXML private ComboBox<String> sortBox;
    @FXML private VBox             quizListContainer;

    private final QuizService     quizService     = new QuizService();
    private final QuestionService questionService = new QuestionService();

    private ObservableList<Quiz> masterList   = FXCollections.observableArrayList();
    private FilteredList<Quiz>   filteredList;
    private final Map<String, Integer> coursMap = new HashMap<>();

    // ─────────── INIT ───────────
    @FXML
    public void initialize() {
        filterTypeBox.getItems().addAll("Tous", "Intermédiaire", "Final");
        filterTypeBox.setValue("Tous");

        sortBox.getItems().addAll(
                "Titre A→Z", "Titre Z→A",
                "Durée croissante", "Durée décroissante",
                "Score croissant", "Score décroissant"
        );

        chargerCours();
        chargerDonnees();
        configurerFiltres();
    }

    // ─────────── CHARGER COURS ───────────
    private void chargerCours() {
        try {
            Connection cnx = MyConnection.getInstance().getCnx();
            ResultSet rs = cnx.createStatement()
                    .executeQuery("SELECT id, titre_cours FROM cours");
            filterCoursBox.getItems().add("Tous");
            while (rs.next()) {
                String nom = rs.getString("titre_cours");
                coursMap.put(nom, rs.getInt("id"));
                filterCoursBox.getItems().add(nom);
            }
            filterCoursBox.setValue("Tous");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ─────────── CHARGER DONNÉES ───────────
    @FXML
    public void chargerDonnees() {
        try {
            List<Quiz> tousQuiz = quizService.afficher();
            masterList   = FXCollections.observableArrayList(tousQuiz);
            filteredList = new FilteredList<>(masterList, p -> true);
            afficherQuiz(filteredList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ─────────── AFFICHER QUIZ ───────────
    private void afficherQuiz(Iterable<Quiz> quizList) {
        quizListContainer.getChildren().clear();
        boolean hasQuiz = false;

        for (Quiz q : quizList) {
            quizListContainer.getChildren().add(creerCarteQuiz(q));
            hasQuiz = true;
        }

        if (!hasQuiz) {
            Label vide = new Label("Aucun quiz trouvé.");
            vide.setStyle(
                    "-fx-text-fill: #888; -fx-font-size: 13; -fx-padding: 20;");
            quizListContainer.getChildren().add(vide);
        }
    }

    // ─────────── CARTE QUIZ ───────────
    private VBox creerCarteQuiz(Quiz quiz) {
        VBox card = new VBox(12);
        card.setStyle(
                "-fx-background-color: #fafafa; -fx-background-radius: 10;" +
                        "-fx-padding: 18; -fx-border-color: #e9ecef;" +
                        "-fx-border-radius: 10; -fx-border-width: 1;"
        );

        // ── Header titre ──
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        HBox barreTitre = new HBox();
        barreTitre.setPrefWidth(4);
        barreTitre.setPrefHeight(30);
        barreTitre.setStyle(
                "-fx-background-color: #f5a623; -fx-background-radius: 2;");

        VBox titreBox = new VBox(2);
        Label labelTitre = new Label("TITRE DU QUIZ");
        labelTitre.setStyle(
                "-fx-font-size: 10; -fx-font-weight: bold; -fx-text-fill: #888;");
        Label titreLabel = new Label(quiz.getTitre());
        titreLabel.setStyle(
                "-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #1a1f3c;");
        titreBox.getChildren().addAll(labelTitre, titreLabel);

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label typeBadge = new Label(quiz.getTypeQuiz());
        typeBadge.setStyle(quiz.getTypeQuiz().equals("Final")
                ? "-fx-background-color: #fff3e0; -fx-text-fill: #f5a623;" +
                "-fx-font-weight: bold; -fx-font-size: 11;" +
                "-fx-background-radius: 4; -fx-padding: 4 10;"
                : "-fx-background-color: #e3f2fd; -fx-text-fill: #1976d2;" +
                "-fx-font-weight: bold; -fx-font-size: 11;" +
                "-fx-background-radius: 4; -fx-padding: 4 10;"
        );

        header.getChildren().addAll(barreTitre, titreBox, spacer, typeBadge);

        // ── Nombre de questions ──
        int nbQuestions = 0;
        try {
            nbQuestions = (int) questionService.afficher().stream()
                    .filter(q -> q.getQuizId() == quiz.getId()).count();
        } catch (Exception e) { e.printStackTrace(); }

        // ── Infos ──
        HBox infos = new HBox(20);
        infos.setAlignment(Pos.CENTER_LEFT);
        infos.getChildren().addAll(
                creerInfoBox("QUESTIONS", String.valueOf(nbQuestions)),
                creerInfoBox("DURÉE",     quiz.getDuree() + " min"),
                creerInfoBox("SCORE MIN", quiz.getScoreMinimum() + "%"),
                creerStatutBox()
        );

        // ── Boutons ──
        HBox boutons = new HBox(10);
        boutons.setAlignment(Pos.CENTER_LEFT);

        Button btnQuestions = new Button("❓ Questions");
        btnQuestions.setStyle(
                "-fx-background-color: #2196F3; -fx-text-fill: white;" +
                        "-fx-font-weight: bold; -fx-background-radius: 6;" +
                        "-fx-padding: 8 16; -fx-cursor: hand;");
        btnQuestions.setOnMouseEntered(e -> btnQuestions.setStyle(
                "-fx-background-color: #1565C0; -fx-text-fill: white;" +
                        "-fx-font-weight: bold; -fx-background-radius: 6;" +
                        "-fx-padding: 8 16; -fx-cursor: hand;"));
        btnQuestions.setOnMouseExited(e -> btnQuestions.setStyle(
                "-fx-background-color: #2196F3; -fx-text-fill: white;" +
                        "-fx-font-weight: bold; -fx-background-radius: 6;" +
                        "-fx-padding: 8 16; -fx-cursor: hand;"));
        btnQuestions.setOnAction(e -> allerVersQuestionsQuiz(quiz));

        Button btnModifier = new Button("✏️ Modifier");
        btnModifier.setStyle(
                "-fx-background-color: #1a1f3c; -fx-text-fill: white;" +
                        "-fx-font-weight: bold; -fx-background-radius: 6;" +
                        "-fx-padding: 8 16; -fx-cursor: hand;");
        btnModifier.setOnMouseEntered(e -> btnModifier.setStyle(
                "-fx-background-color: #2d3561; -fx-text-fill: white;" +
                        "-fx-font-weight: bold; -fx-background-radius: 6;" +
                        "-fx-padding: 8 16; -fx-cursor: hand;"));
        btnModifier.setOnMouseExited(e -> btnModifier.setStyle(
                "-fx-background-color: #1a1f3c; -fx-text-fill: white;" +
                        "-fx-font-weight: bold; -fx-background-radius: 6;" +
                        "-fx-padding: 8 16; -fx-cursor: hand;"));
        btnModifier.setOnAction(e -> allerVersModificationQuiz(quiz));

        Button btnSupprimer = new Button("🗑️ Supprimer");
        btnSupprimer.setStyle(
                "-fx-background-color: #e74c3c; -fx-text-fill: white;" +
                        "-fx-font-weight: bold; -fx-background-radius: 6;" +
                        "-fx-padding: 8 16; -fx-cursor: hand;");
        btnSupprimer.setOnMouseEntered(e -> btnSupprimer.setStyle(
                "-fx-background-color: #c0392b; -fx-text-fill: white;" +
                        "-fx-font-weight: bold; -fx-background-radius: 6;" +
                        "-fx-padding: 8 16; -fx-cursor: hand;"));
        btnSupprimer.setOnMouseExited(e -> btnSupprimer.setStyle(
                "-fx-background-color: #e74c3c; -fx-text-fill: white;" +
                        "-fx-font-weight: bold; -fx-background-radius: 6;" +
                        "-fx-padding: 8 16; -fx-cursor: hand;"));
        btnSupprimer.setOnAction(e -> supprimerQuiz(quiz));

        boutons.getChildren().addAll(btnQuestions, btnModifier, btnSupprimer);

        card.getChildren().addAll(header, infos, boutons);
        return card;
    }

    // ─────────── HELPERS CARTE ───────────
    private VBox creerInfoBox(String label, String valeur) {
        VBox box = new VBox(3);
        box.setStyle(
                "-fx-background-color: white; -fx-background-radius: 6;" +
                        "-fx-padding: 10 15; -fx-border-color: #f5a623;" +
                        "-fx-border-radius: 6; -fx-border-width: 0 0 0 3;");
        Label lbl = new Label(label);
        lbl.setStyle(
                "-fx-font-size: 9; -fx-font-weight: bold; -fx-text-fill: #888;");
        Label val = new Label(valeur);
        val.setStyle(
                "-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #1a1f3c;");
        box.getChildren().addAll(lbl, val);
        return box;
    }

    private VBox creerStatutBox() {
        VBox box = new VBox(3);
        box.setStyle(
                "-fx-background-color: white; -fx-background-radius: 6;" +
                        "-fx-padding: 10 15; -fx-border-color: #f5a623;" +
                        "-fx-border-radius: 6; -fx-border-width: 0 0 0 3;");
        Label lbl = new Label("STATUT");
        lbl.setStyle(
                "-fx-font-size: 9; -fx-font-weight: bold; -fx-text-fill: #888;");
        Label val = new Label("ACTIF");
        val.setStyle(
                "-fx-background-color: #2ecc71; -fx-text-fill: white;" +
                        "-fx-font-size: 11; -fx-font-weight: bold;" +
                        "-fx-background-radius: 10; -fx-padding: 3 10;");
        box.getChildren().addAll(lbl, val);
        return box;
    }

    // ─────────── FILTRES ───────────
    private void configurerFiltres() {
        searchTitreField.textProperty().addListener(
                (obs, o, n) -> appliquerFiltres());
        filterTypeBox.valueProperty().addListener(
                (obs, o, n) -> appliquerFiltres());
        filterCoursBox.valueProperty().addListener(
                (obs, o, n) -> appliquerFiltres());
        sortBox.valueProperty().addListener(
                (obs, o, n) -> appliquerFiltres());
    }

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
                String nomCours = quizService.getNomCours(
                        quiz.getCoursAssocieId());
                if (!nomCours.equals(cours)) return false;
            }

            return true;
        });

        String tri = sortBox.getValue();
        if (tri != null) {
            ObservableList<Quiz> sorted =
                    FXCollections.observableArrayList(filteredList);
            switch (tri) {
                case "Titre A→Z" -> sorted.sort(
                        Comparator.comparing(
                                q -> q.getTitre().toLowerCase()));
                case "Titre Z→A" -> sorted.sort(
                        Comparator.comparing(
                                        (Quiz q) -> q.getTitre().toLowerCase())
                                .reversed());
                case "Durée croissante" -> sorted.sort(
                        Comparator.comparingInt(Quiz::getDuree));
                case "Durée décroissante" -> sorted.sort(
                        Comparator.comparingInt(Quiz::getDuree).reversed());
                case "Score croissant" -> sorted.sort(
                        Comparator.comparingDouble(Quiz::getScoreMinimum));
                case "Score décroissant" -> sorted.sort(
                        Comparator.comparingDouble(
                                Quiz::getScoreMinimum).reversed());
            }
            afficherQuiz(sorted);
        } else {
            afficherQuiz(filteredList);
        }
    }

    @FXML
    private void reinitialiserFiltres() {
        searchTitreField.clear();
        filterTypeBox.setValue("Tous");
        filterCoursBox.setValue("Tous");
        sortBox.setValue(null);
        if (filteredList != null) filteredList.setPredicate(p -> true);
        afficherQuiz(filteredList != null ? filteredList : masterList);
    }

    // ─────────── ACTIONS ───────────
    private void allerVersQuestionsQuiz(Quiz quiz) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/org/example/AjouterQuestion.fxml"));
            Stage stage = (Stage) quizListContainer.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Questions — " + quiz.getTitre());
            AjouterQuestionController ctrl = loader.getController();
            ctrl.setQuiz(quiz.getId(), quiz.getTitre());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void allerVersModificationQuiz(Quiz quiz) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/AjouterQuiz.fxml"));
            Stage stage = (Stage) quizListContainer.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Modifier Quiz");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void supprimerQuiz(Quiz quiz) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer le quiz ?");
        alert.setContentText(
                "Voulez-vous vraiment supprimer \""
                        + quiz.getTitre() + "\" ?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    quizService.supprimer(quiz.getId());
                    chargerDonnees();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // ─────────── NAVIGATION ───────────
    @FXML
    private void allerVersAjoutQuiz() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/AjouterQuiz.fxml"));
            Stage stage = (Stage) quizListContainer.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Formateur — Gestion Quiz");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void allerVersQuestions() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/org/example/AjouterQuestion.fxml"));
            Stage stage = (Stage) quizListContainer.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Gestion des Questions");
            AjouterQuestionController ctrl = loader.getController();
            ctrl.setQuiz(0, "—");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void retourAccueil() {
        try {
            Stage stage = (Stage) quizListContainer.getScene().getWindow();
            new org.example.MainFX().start(stage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}