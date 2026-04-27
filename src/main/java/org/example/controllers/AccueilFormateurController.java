package org.example.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.entities.Question;
import org.example.entities.Quiz;
import org.example.services.QuestionService;
import org.example.services.QuizService;
import org.example.utils.MyConnection;

import java.sql.Connection;
import java.sql.ResultSet;
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
    @FXML private VBox             sidebar;
    @FXML private Button           burgerBtn;

    @FXML private HBox                        statsBox;
    @FXML private PieChart                    typeQuizChart;
    @FXML private BarChart<String, Number>    questionsParQuizChart;

    private boolean sidebarVisible = true;

    private final QuizService     quizService     = new QuizService();
    private final QuestionService questionService = new QuestionService();

    private ObservableList<Quiz> masterList   = FXCollections.observableArrayList();
    private FilteredList<Quiz>   filteredList;
    private final Map<String, Integer> coursMap = new HashMap<>();

    // ═══════════════════════════════════════
    // INIT
    // ═══════════════════════════════════════
    @FXML
    public void initialize() {
        filterTypeBox.getItems().addAll("Tous", "Intermédiaire", "Final");
        filterTypeBox.setValue("Tous");
        sortBox.getItems().addAll(
                "Titre A→Z", "Titre Z→A",
                "Durée croissante", "Durée décroissante",
                "Score croissant", "Score décroissant");
        chargerCours();
        chargerDonnees();
        chargerStatistiques();
        configurerFiltres();
    }

    // ═══════════════════════════════════════
    // CHARGER COURS
    // ═══════════════════════════════════════
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
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ═══════════════════════════════════════
    // CHARGER DONNEES
    // ═══════════════════════════════════════
    @FXML
    public void chargerDonnees() {
        try {
            List<Quiz> tousQuiz = quizService.afficher();
            masterList   = FXCollections.observableArrayList(tousQuiz);
            filteredList = new FilteredList<>(masterList, p -> true);
            afficherQuiz(filteredList);
            chargerStatistiques();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ═══════════════════════════════════════
    // STATISTIQUES
    // ═══════════════════════════════════════
    private void chargerStatistiques() {
        try {
            List<Quiz>     quizList     = quizService.afficher();
            List<Question> questionList = questionService.afficher();

            int    totalQuiz      = quizList.size();
            long   nbFinal        = quizList.stream()
                    .filter(q -> "Final".equals(q.getTypeQuiz())).count();
            long   nbInter        = quizList.stream()
                    .filter(q -> "Intermédiaire".equals(q.getTypeQuiz())).count();
            int    totalQuestions = questionList.size();
            double dureeMoy       = quizList.stream()
                    .mapToInt(Quiz::getDuree).average().orElse(0);
            double scoreMoy       = quizList.stream()
                    .mapToDouble(Quiz::getScoreMinimum).average().orElse(0);

            // ── Stat Cards ──
            statsBox.getChildren().clear();
            statsBox.getChildren().addAll(
                    creerStatCard("Total quiz",         String.valueOf(totalQuiz),
                            "#4361ee", "#eef2ff", "#4361ee"),
                    creerStatCard("Quiz finaux",         String.valueOf(nbFinal),
                            "#f5a623", "#fff8ee", "#f5a623"),
                    creerStatCard("Intermediaires",      String.valueOf(nbInter),
                            "#2196F3", "#e3f2fd", "#2196F3"),
                    creerStatCard("Questions",           String.valueOf(totalQuestions),
                            "#2ecc71", "#e8f5e9", "#2ecc71"),
                    creerStatCard("Duree moyenne",       String.format("%.1f min", dureeMoy),
                            "#9b59b6", "#f3e5f5", "#9b59b6"),
                    creerStatCard("Score moyen",         String.format("%.1f%%", scoreMoy),
                            "#e74c3c", "#fdecea", "#e74c3c")
            );

            // ── PieChart ──
            typeQuizChart.setTitle("");
            ObservableList<PieChart.Data> pieData =
                    FXCollections.observableArrayList(
                            new PieChart.Data("Final (" + nbFinal + ")", nbFinal),
                            new PieChart.Data("Intermediaire (" + nbInter + ")", nbInter)
                    );
            typeQuizChart.setData(pieData);
            typeQuizChart.setLabelsVisible(true);
            typeQuizChart.setLegendVisible(true);
            typeQuizChart.setStartAngle(90);

            // Couleurs PieChart
            javafx.application.Platform.runLater(() -> {
                ObservableList<PieChart.Data> data = typeQuizChart.getData();
                if (data.size() >= 1)
                    data.get(0).getNode().setStyle(
                            "-fx-pie-color: #f5a623;");
                if (data.size() >= 2)
                    data.get(1).getNode().setStyle(
                            "-fx-pie-color: #4361ee;");
            });

            // ── BarChart ──
            questionsParQuizChart.setTitle("");
            questionsParQuizChart.getData().clear();
            questionsParQuizChart.setLegendVisible(false);
            questionsParQuizChart.setAnimated(true);

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Questions");

            String[] barColors = {
                    "#4361ee", "#f5a623", "#2ecc71",
                    "#e74c3c", "#9b59b6", "#2196F3"
            };
            int colorIdx = 0;

            for (Quiz quiz : quizList) {
                long nb = questionList.stream()
                        .filter(q -> q.getQuizId() == quiz.getId())
                        .count();

                XYChart.Data<String, Number> barData =
                        new XYChart.Data<>(quiz.getTitre(), nb);
                series.getData().add(barData);

                final String color = barColors[colorIdx % barColors.length];
                colorIdx++;

                // Couleur barre individuelle
                barData.nodeProperty().addListener((obs, oldNode, newNode) -> {
                    if (newNode != null)
                        newNode.setStyle("-fx-bar-fill: " + color + ";");
                });
            }

            questionsParQuizChart.getData().add(series);

            // Appliquer couleurs après rendu
            javafx.application.Platform.runLater(() -> {
                int idx = 0;
                for (XYChart.Data<String, Number> d : series.getData()) {
                    if (d.getNode() != null) {
                        d.getNode().setStyle(
                                "-fx-bar-fill: " + barColors[idx % barColors.length]
                                        + "; -fx-background-radius: 6 6 0 0;");
                    }
                    idx++;
                }
            });

        } catch (Exception e) { e.printStackTrace(); }
    }

    // ═══════════════════════════════════════
    // STAT CARD
    // ═══════════════════════════════════════
    private VBox creerStatCard(String titre, String valeur,
                               String colorText, String colorBg,
                               String colorBorder) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPrefWidth(160);
        card.setStyle(
                "-fx-background-color: " + colorBg + ";" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 16 18;" +
                        "-fx-border-color: " + colorBorder + ";" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-width: 0 0 0 4;" +
                        "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),8,0,0,2);"
        );

        Label titreLabel = new Label(titre.toUpperCase());
        titreLabel.setStyle(
                "-fx-text-fill: " + colorText + ";" +
                        "-fx-font-size: 9;" +
                        "-fx-font-weight: bold;" +
                        "-fx-opacity: 0.8;"
        );

        Label valeurLabel = new Label(valeur);
        valeurLabel.setStyle(
                "-fx-text-fill: #1a1f3c;" +
                        "-fx-font-size: 22;" +
                        "-fx-font-weight: bold;"
        );

        // Barre de couleur en bas
        HBox barre = new HBox();
        barre.setPrefHeight(3);
        barre.setMaxWidth(Double.MAX_VALUE);
        barre.setStyle(
                "-fx-background-color: " + colorBorder + ";" +
                        "-fx-background-radius: 2;" +
                        "-fx-opacity: 0.3;"
        );

        card.getChildren().addAll(titreLabel, valeurLabel, barre);
        return card;
    }

    // ═══════════════════════════════════════
    // TOGGLE SIDEBAR
    // ═══════════════════════════════════════
    @FXML
    private void toggleSidebar() {
        javafx.animation.TranslateTransition slide =
                new javafx.animation.TranslateTransition(
                        javafx.util.Duration.millis(280), sidebar);
        javafx.animation.FadeTransition fade =
                new javafx.animation.FadeTransition(
                        javafx.util.Duration.millis(280), sidebar);

        if (sidebarVisible) {
            slide.setFromX(0); slide.setToX(-230);
            fade.setFromValue(1.0); fade.setToValue(0.0);
            slide.setOnFinished(e -> {
                sidebar.setManaged(false);
                sidebar.setVisible(false);
            });
            if (burgerBtn != null) burgerBtn.setText("☰");
        } else {
            sidebar.setManaged(true); sidebar.setVisible(true);
            slide.setFromX(-230); slide.setToX(0);
            fade.setFromValue(0.0); fade.setToValue(1.0);
            if (burgerBtn != null) burgerBtn.setText("x");
        }

        new javafx.animation.ParallelTransition(slide, fade).play();
        sidebarVisible = !sidebarVisible;
    }

    // ═══════════════════════════════════════
    // AFFICHER QUIZ
    // ═══════════════════════════════════════
    private void afficherQuiz(Iterable<Quiz> quizList) {
        quizListContainer.getChildren().clear();
        boolean hasQuiz = false;
        for (Quiz q : quizList) {
            quizListContainer.getChildren().add(creerCarteQuiz(q));
            hasQuiz = true;
        }
        if (!hasQuiz) {
            VBox vide = new VBox(8);
            vide.setAlignment(Pos.CENTER);
            vide.setStyle("-fx-padding: 40;");
            Label l1 = new Label("Aucun quiz trouve");
            l1.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 13; -fx-font-weight: bold;");
            Label l2 = new Label("Creez votre premier quiz");
            l2.setStyle("-fx-text-fill: #d1d5db; -fx-font-size: 11;");
            vide.getChildren().addAll(l1, l2);
            quizListContainer.getChildren().add(vide);
        }
    }

    // ═══════════════════════════════════════
    // CARTE QUIZ
    // ═══════════════════════════════════════
    private VBox creerCarteQuiz(Quiz quiz) {
        VBox card = new VBox(12);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 18;" +
                        "-fx-border-color: #f0f0f0;" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),8,0,0,2);"
        );

        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        HBox barre = new HBox();
        barre.setPrefWidth(4); barre.setPrefHeight(36);
        barre.setStyle("-fx-background-color: #f5a623; -fx-background-radius: 2;");

        VBox titreBox = new VBox(2);
        Label labelTitre = new Label("TITRE DU QUIZ");
        labelTitre.setStyle("-fx-font-size: 9; -fx-font-weight: bold; -fx-text-fill: #aaa;");
        Label titreLabel = new Label(quiz.getTitre());
        titreLabel.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: #1a1f3c;");
        titreBox.getChildren().addAll(labelTitre, titreLabel);

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        boolean isFinal = "Final".equals(quiz.getTypeQuiz());
        Label typeBadge = new Label(quiz.getTypeQuiz());
        typeBadge.setStyle(isFinal
                ? "-fx-background-color: #fff3e0; -fx-text-fill: #f5a623;" +
                "-fx-font-weight: bold; -fx-font-size: 11;" +
                "-fx-background-radius: 20; -fx-padding: 4 12;"
                : "-fx-background-color: #e3f2fd; -fx-text-fill: #1976d2;" +
                "-fx-font-weight: bold; -fx-font-size: 11;" +
                "-fx-background-radius: 20; -fx-padding: 4 12;"
        );

        header.getChildren().addAll(barre, titreBox, spacer, typeBadge);

        // Nb questions
        int nbQ = 0;
        try {
            nbQ = (int) questionService.afficher().stream()
                    .filter(q -> q.getQuizId() == quiz.getId()).count();
        } catch (Exception e) { e.printStackTrace(); }

        // Infos
        HBox infos = new HBox(16);
        infos.setAlignment(Pos.CENTER_LEFT);
        infos.getChildren().addAll(
                creerInfoBox("QUESTIONS", String.valueOf(nbQ)),
                creerInfoBox("DUREE",     quiz.getDuree() + " min"),
                creerInfoBox("SCORE MIN", quiz.getScoreMinimum() + "%"),
                creerStatutBox()
        );

        // Boutons
        HBox boutons = new HBox(10);
        boutons.setAlignment(Pos.CENTER_LEFT);

        Button btnQ = new Button("Questions");
        btnQ.setStyle(
                "-fx-background-color: #eff6ff; -fx-text-fill: #3b82f6;" +
                        "-fx-font-weight: bold; -fx-background-radius: 8;" +
                        "-fx-border-color: #bfdbfe; -fx-border-radius: 8; -fx-border-width: 1;" +
                        "-fx-padding: 8 16; -fx-cursor: hand;");
        btnQ.setOnAction(e -> allerVersQuestionsQuiz(quiz));

        Button btnM = new Button("Modifier");
        btnM.setStyle(
                "-fx-background-color: #1a1f3c; -fx-text-fill: white;" +
                        "-fx-font-weight: bold; -fx-background-radius: 8;" +
                        "-fx-padding: 8 16; -fx-cursor: hand;");
        btnM.setOnAction(e -> allerVersModificationQuiz(quiz));

        Button btnS = new Button("Supprimer");
        btnS.setStyle(
                "-fx-background-color: #fef2f2; -fx-text-fill: #e74c3c;" +
                        "-fx-font-weight: bold; -fx-background-radius: 8;" +
                        "-fx-border-color: #fecaca; -fx-border-radius: 8; -fx-border-width: 1;" +
                        "-fx-padding: 8 16; -fx-cursor: hand;");
        btnS.setOnAction(e -> supprimerQuiz(quiz));

        boutons.getChildren().addAll(btnQ, btnM, btnS);
        card.getChildren().addAll(header, infos, boutons);
        return card;
    }

    private VBox creerInfoBox(String label, String valeur) {
        VBox box = new VBox(3);
        box.setStyle(
                "-fx-background-color: #f9fafb;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 10 14;" +
                        "-fx-border-color: #f5a623;" +
                        "-fx-border-radius: 8;" +
                        "-fx-border-width: 0 0 0 3;"
        );
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 9; -fx-font-weight: bold; -fx-text-fill: #888;");
        Label val = new Label(valeur);
        val.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #1a1f3c;");
        box.getChildren().addAll(lbl, val);
        return box;
    }

    private VBox creerStatutBox() {
        VBox box = new VBox(3);
        box.setStyle(
                "-fx-background-color: #f9fafb;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 10 14;" +
                        "-fx-border-color: #2ecc71;" +
                        "-fx-border-radius: 8;" +
                        "-fx-border-width: 0 0 0 3;"
        );
        Label lbl = new Label("STATUT");
        lbl.setStyle("-fx-font-size: 9; -fx-font-weight: bold; -fx-text-fill: #888;");
        Label val = new Label("ACTIF");
        val.setStyle(
                "-fx-background-color: #d1fae5; -fx-text-fill: #065f46;" +
                        "-fx-font-size: 11; -fx-font-weight: bold;" +
                        "-fx-background-radius: 20; -fx-padding: 3 10;");
        box.getChildren().addAll(lbl, val);
        return box;
    }

    // ═══════════════════════════════════════
    // FILTRES
    // ═══════════════════════════════════════
    private void configurerFiltres() {
        searchTitreField.textProperty().addListener((obs, o, n) -> appliquerFiltres());
        filterTypeBox.valueProperty().addListener((obs, o, n) -> appliquerFiltres());
        filterCoursBox.valueProperty().addListener((obs, o, n) -> appliquerFiltres());
        sortBox.valueProperty().addListener((obs, o, n) -> appliquerFiltres());
    }

    private void appliquerFiltres() {
        if (filteredList == null) return;
        filteredList.setPredicate(quiz -> {
            String titre = searchTitreField.getText().trim().toLowerCase();
            if (!titre.isEmpty() && !quiz.getTitre().toLowerCase().contains(titre))
                return false;
            String type = filterTypeBox.getValue();
            if (type != null && !type.equals("Tous") && !quiz.getTypeQuiz().equals(type))
                return false;
            String cours = filterCoursBox.getValue();
            if (cours != null && !cours.equals("Tous")) {
                if (!quizService.getNomCours(quiz.getCoursAssocieId()).equals(cours))
                    return false;
            }
            return true;
        });

        String tri = sortBox.getValue();
        if (tri != null) {
            ObservableList<Quiz> sorted = FXCollections.observableArrayList(filteredList);
            switch (tri) {
                case "Titre A→Z"        -> sorted.sort(Comparator.comparing(q -> q.getTitre().toLowerCase()));
                case "Titre Z→A"        -> sorted.sort(Comparator.comparing((Quiz q) -> q.getTitre().toLowerCase()).reversed());
                case "Durée croissante"  -> sorted.sort(Comparator.comparingInt(Quiz::getDuree));
                case "Durée décroissante"-> sorted.sort(Comparator.comparingInt(Quiz::getDuree).reversed());
                case "Score croissant"   -> sorted.sort(Comparator.comparingDouble(Quiz::getScoreMinimum));
                case "Score décroissant" -> sorted.sort(Comparator.comparingDouble(Quiz::getScoreMinimum).reversed());
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

    // ═══════════════════════════════════════
    // NAVIGATION
    // ═══════════════════════════════════════
    private void allerVersQuestionsQuiz(Quiz quiz) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/AjouterQuestion.fxml"));
            Stage stage = (Stage) quizListContainer.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Questions — " + quiz.getTitre());
            AjouterQuestionController ctrl = loader.getController();
            ctrl.setQuiz(quiz.getId(), quiz.getTitre());
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void allerVersModificationQuiz(Quiz quiz) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/AjouterQuiz.fxml"));
            Stage stage = (Stage) quizListContainer.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Modifier Quiz");
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void supprimerQuiz(Quiz quiz) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer \"" + quiz.getTitre() + "\" ?",
                ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle("Confirmation");
        alert.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                try {
                    quizService.supprimer(quiz.getId());
                    chargerDonnees();
                } catch (Exception e) { e.printStackTrace(); }
            }
        });
    }

    @FXML
    private void allerVersAjoutQuiz() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/AjouterQuiz.fxml"));
            Stage stage = (Stage) quizListContainer.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Formateur — Gestion Quiz");
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void allerVersQuestions() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/AjouterQuestion.fxml"));
            Stage stage = (Stage) quizListContainer.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Gestion des Questions");
            AjouterQuestionController ctrl = loader.getController();
            ctrl.setQuiz(0, "—");
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void retourAccueil() {
        try {
            Stage stage = (Stage) quizListContainer.getScene().getWindow();
            new org.example.MainFX().start(stage);
        } catch (Exception e) { e.printStackTrace(); }
    }
}