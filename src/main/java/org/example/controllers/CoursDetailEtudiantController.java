package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.entities.Quiz;
import org.example.services.QuizService;

public class CoursDetailEtudiantController {

    @FXML private Label    headerLabel;
    @FXML private Label    breadcrumb;
    @FXML private Label    coursTitreLabel;
    @FXML private Label    coursDescLabel;
    @FXML private FlowPane quizContainer;

    private final QuizService quizService = new QuizService();

    private int    coursId;
    private String coursTitre;
    private String coursNiveau;
    private String coursMatiere;
    private String coursLangue;
    private String coursDescription;

    // ─────────── SET COURS ───────────
    public void setCours(int coursId, String titre, String niveau,
                         String matiere, String langue, String description) {
        this.coursId          = coursId;
        this.coursTitre       = titre;
        this.coursNiveau      = niveau;
        this.coursMatiere     = matiere;
        this.coursLangue      = langue;
        this.coursDescription = description;

        headerLabel.setText(" — " + titre);
        breadcrumb.setText("🏠 Accueil  ›  Cours  ›  " + titre);
        coursTitreLabel.setText("📖 " + titre);

        StringBuilder infos = new StringBuilder();
        if (niveau  != null && !niveau.isEmpty())
            infos.append("📊 ").append(niveau).append("   ");
        if (matiere != null && !matiere.isEmpty())
            infos.append("🔬 ").append(matiere).append("   ");
        if (langue  != null && !langue.isEmpty())
            infos.append("🌍 ").append(langue);

        if (infos.length() > 0)
            coursDescLabel.setText(infos.toString().trim());
        else if (description != null && !description.isEmpty())
            coursDescLabel.setText(description);
        else
            coursDescLabel.setText("Aucune description disponible.");

        chargerQuiz();
    }

    // ─────────── CHARGER QUIZ ───────────
    private void chargerQuiz() {
        quizContainer.getChildren().clear();
        try {
            for (Quiz q : quizService.afficher()) {
                if (q.getCoursAssocieId() == coursId) {
                    quizContainer.getChildren().add(creerCarteQuiz(q));
                }
            }
            if (quizContainer.getChildren().isEmpty()) {
                Label vide = new Label("Aucun quiz disponible pour ce cours.");
                vide.setStyle("-fx-text-fill: #888; -fx-font-size: 13; -fx-padding: 20;");
                quizContainer.getChildren().add(vide);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ─────────── CRÉER CARTE QUIZ ───────────
    private VBox creerCarteQuiz(Quiz quiz) {
        VBox card = new VBox(12);
        card.setPrefWidth(280);
        card.setMinWidth(280);
        card.setMaxWidth(280);
        card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 20;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );

        // Header icône + titre
        HBox header = new HBox(10);
        Label icon = new Label(quiz.getTypeQuiz().equals("Final") ? "🏆" : "📝");
        icon.setStyle("-fx-font-size: 20;");
        Label titre = new Label(quiz.getTitre());
        titre.setStyle(
                "-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: #1a1f3c;");
        titre.setWrapText(true);
        titre.setMaxWidth(210);
        header.getChildren().addAll(icon, titre);

        // Badge type
        Label badge = new Label(quiz.getTypeQuiz().equals("Final")
                ? "🏆 CERTIFICATION FINALE" : "📝 QUIZ INTERMÉDIAIRE");
        badge.setStyle(quiz.getTypeQuiz().equals("Final")
                ? "-fx-background-color: #f5a623; -fx-text-fill: white; -fx-font-size: 10;" +
                "-fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 4 10;"
                : "-fx-background-color: #e3f2fd; -fx-text-fill: #1976d2; -fx-font-size: 10;" +
                "-fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 4 10;"
        );

        // Barre colorée
        HBox barre = new HBox();
        barre.setPrefHeight(3);
        barre.setPrefWidth(240);
        barre.setStyle("-fx-background-color: "
                + (quiz.getTypeQuiz().equals("Final") ? "#f5a623" : "#1976d2")
                + "; -fx-background-radius: 2;");

        // Infos durée + score
        HBox infos = new HBox(15);
        Label duree = new Label("⏱ " + quiz.getDuree() + " min");
        duree.setStyle("-fx-font-size: 12; -fx-text-fill: #666;");
        Label score = new Label("🎯 " + quiz.getScoreMinimum() + "%");
        score.setStyle(
                "-fx-font-size: 12; -fx-text-fill: #f5a623; -fx-font-weight: bold;");
        infos.getChildren().addAll(duree, score);

        // Bouton passer quiz
        Button btn = new Button("▶  Passer le Quiz");
        btn.setPrefWidth(240);
        btn.setStyle(
                "-fx-background-color: #1a1f3c; -fx-text-fill: white;" +
                        "-fx-font-weight: bold; -fx-background-radius: 8;" +
                        "-fx-padding: 11 20; -fx-cursor: hand; -fx-font-size: 13;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: #f5a623; -fx-text-fill: white;" +
                        "-fx-font-weight: bold; -fx-background-radius: 8;" +
                        "-fx-padding: 11 20; -fx-cursor: hand; -fx-font-size: 13;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: #1a1f3c; -fx-text-fill: white;" +
                        "-fx-font-weight: bold; -fx-background-radius: 8;" +
                        "-fx-padding: 11 20; -fx-cursor: hand; -fx-font-size: 13;"
        ));
        btn.setOnAction(e -> passerQuiz(quiz));

        // Hover carte
        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: #fff8ee; -fx-background-radius: 12; -fx-padding: 20;" +
                        "-fx-effect: dropshadow(gaussian, rgba(245,166,35,0.25), 12, 0, 0, 3);"
        ));
        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 20;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        ));

        card.getChildren().addAll(header, badge, barre, infos, btn);
        return card;
    }

    // ─────────── PASSER QUIZ ───────────
    private void passerQuiz(Quiz quiz) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/PasserQuiz.fxml"));
            Stage stage = (Stage) quizContainer.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Quiz : " + quiz.getTitre());
            PasserQuizController ctrl = loader.getController();
            ctrl.setQuiz(quiz, coursId, coursTitre,
                    coursNiveau, coursMatiere, coursLangue, coursDescription);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ─────────── RETOUR ACCUEIL ───────────
    @FXML
    private void retourAccueil() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/AccueilEtudiant.fxml"));
            Stage stage = (Stage) quizContainer.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Espace Étudiant");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}