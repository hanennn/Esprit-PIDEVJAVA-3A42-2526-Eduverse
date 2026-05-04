package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.entities.Quiz;
import org.example.services.QuizService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CoursDetailEtudiantController {

    @FXML private Label    headerLabel;
    @FXML private Label    breadcrumb;
    @FXML private Label    coursTitreLabel;
    @FXML private Label    coursDescLabel;
    @FXML private FlowPane quizContainer;
    @FXML private VBox     commentairesContainer;
    @FXML private TextArea champCommentaire;
    @FXML private Label    lblLikes;
    @FXML private Label    lblDislikes;

    private final QuizService quizService = new QuizService();

    private int    coursId;
    private String coursTitre;
    private String coursNiveau;
    private String coursMatiere;
    private String coursLangue;
    private String coursDescription;

    // ── Stockage en mémoire (pas de table BD) ──
    private int likesCount    = 0;
    private int dislikesCount = 0;
    private boolean aLike    = false;
    private boolean aDislike = false;
    private final List<String[]> commentaires = new ArrayList<>();
    // String[] = { "pseudo", "texte", "heure" }

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
        rafraichirCommentaires();
        mettreAJourLikes();
    }

    // ─────────── QUIZ ───────────
    private void chargerQuiz() {
        quizContainer.getChildren().clear();
        try {
            for (Quiz q : quizService.afficher()) {
                if (q.getCoursAssocieId() == coursId)
                    quizContainer.getChildren().add(creerCarteQuiz(q));
            }
            if (quizContainer.getChildren().isEmpty()) {
                Label vide = new Label("Aucun quiz disponible pour ce cours.");
                vide.setStyle("-fx-text-fill: #888; -fx-font-size: 13; -fx-padding: 20;");
                quizContainer.getChildren().add(vide);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private VBox creerCarteQuiz(Quiz quiz) {
        VBox card = new VBox(12);
        card.setPrefWidth(280); card.setMinWidth(280); card.setMaxWidth(280);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 20;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        HBox header = new HBox(10);
        Label icon = new Label(quiz.getTypeQuiz().equals("Final") ? "🏆" : "📝");
        icon.setStyle("-fx-font-size: 20;");
        Label titre = new Label(quiz.getTitre());
        titre.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: #1a1f3c;");
        titre.setWrapText(true); titre.setMaxWidth(210);
        header.getChildren().addAll(icon, titre);

        Label badge = new Label(quiz.getTypeQuiz().equals("Final")
                ? "🏆 CERTIFICATION FINALE" : "📝 QUIZ INTERMÉDIAIRE");
        badge.setStyle(quiz.getTypeQuiz().equals("Final")
                ? "-fx-background-color: #f5a623; -fx-text-fill: white; -fx-font-size: 10; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 4 10;"
                : "-fx-background-color: #e3f2fd; -fx-text-fill: #1976d2; -fx-font-size: 10; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 4 10;");

        HBox barre = new HBox();
        barre.setPrefHeight(3); barre.setPrefWidth(240);
        barre.setStyle("-fx-background-color: " + (quiz.getTypeQuiz().equals("Final") ? "#f5a623" : "#1976d2") + "; -fx-background-radius: 2;");

        HBox infos = new HBox(15);
        Label duree = new Label("⏱ " + quiz.getDuree() + " min");
        duree.setStyle("-fx-font-size: 12; -fx-text-fill: #666;");
        Label score = new Label("🎯 " + quiz.getScoreMinimum() + "%");
        score.setStyle("-fx-font-size: 12; -fx-text-fill: #f5a623; -fx-font-weight: bold;");
        infos.getChildren().addAll(duree, score);

        Button btn = new Button("▶  Passer le Quiz");
        btn.setPrefWidth(240);
        btn.setStyle("-fx-background-color: #1a1f3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 11 20; -fx-cursor: hand; -fx-font-size: 13;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #f5a623; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 11 20; -fx-cursor: hand; -fx-font-size: 13;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #1a1f3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 11 20; -fx-cursor: hand; -fx-font-size: 13;"));
        btn.setOnAction(e -> passerQuiz(quiz));

        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #fff8ee; -fx-background-radius: 12; -fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(245,166,35,0.25), 12, 0, 0, 3);"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"));

        card.getChildren().addAll(header, badge, barre, infos, btn);
        return card;
    }

    // ─────────── LIKES ───────────
    @FXML
    public void clickLike() {
        if (aLike) {
            likesCount--;
            aLike = false;
        } else {
            likesCount++;
            aLike = true;
            if (aDislike) { dislikesCount--; aDislike = false; }
        }
        mettreAJourLikes();
    }

    @FXML
    public void clickDislike() {
        if (aDislike) {
            dislikesCount--;
            aDislike = false;
        } else {
            dislikesCount++;
            aDislike = true;
            if (aLike) { likesCount--; aLike = false; }
        }
        mettreAJourLikes();
    }

    private void mettreAJourLikes() {
        if (lblLikes   != null) lblLikes.setText(String.valueOf(likesCount));
        if (lblDislikes != null) lblDislikes.setText(String.valueOf(dislikesCount));
    }

    // ─────────── COMMENTAIRES ───────────
    @FXML
    public void publierCommentaire() {
        if (champCommentaire == null) return;
        String texte = champCommentaire.getText().trim();
        if (texte.isEmpty()) return;

        String heure = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        commentaires.add(new String[]{"Moi", texte, heure});
        champCommentaire.clear();
        rafraichirCommentaires();
    }

    private void rafraichirCommentaires() {
        if (commentairesContainer == null) return;
        commentairesContainer.getChildren().clear();

        if (commentaires.isEmpty()) {
            Label vide = new Label("Aucun commentaire pour l'instant. Soyez le premier !");
            vide.setStyle("-fx-text-fill: #aaa; -fx-font-size: 12; -fx-padding: 10 0;");
            commentairesContainer.getChildren().add(vide);
            return;
        }

        for (String[] c : commentaires) {
            VBox bulle = new VBox(4);
            bulle.setStyle(
                    "-fx-background-color: #f8f9fa; -fx-background-radius: 10;" +
                            "-fx-padding: 10 14; -fx-border-color: #e8e8e8;" +
                            "-fx-border-radius: 10; -fx-border-width: 1;");

            HBox meta = new HBox(8);
            meta.setAlignment(Pos.CENTER_LEFT);
            Label avatar = new Label("👤");
            avatar.setStyle("-fx-font-size: 14;");
            Label pseudo = new Label(c[0]);
            pseudo.setStyle("-fx-font-weight: bold; -fx-text-fill: #1a1f3c; -fx-font-size: 12;");
            HBox spacer = new HBox(); HBox.setHgrow(spacer, Priority.ALWAYS);
            Label heure = new Label("🕐 " + c[2]);
            heure.setStyle("-fx-text-fill: #aaa; -fx-font-size: 10;");
            meta.getChildren().addAll(avatar, pseudo, spacer, heure);

            Label texte = new Label(c[1]);
            texte.setStyle("-fx-text-fill: #444; -fx-font-size: 12;");
            texte.setWrapText(true);

            bulle.getChildren().addAll(meta, texte);
            commentairesContainer.getChildren().add(bulle);
        }
    }

    // ─────────── NAVIGATION ───────────
    private void passerQuiz(Quiz quiz) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/PasserQuiz.fxml"));
            Stage stage = (Stage) quizContainer.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Quiz : " + quiz.getTitre());
            PasserQuizController ctrl = loader.getController();
            ctrl.setQuiz(quiz, coursId, coursTitre, coursNiveau, coursMatiere, coursLangue, coursDescription);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void retourAccueil() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/AccueilEtudiant.fxml"));
            Stage stage = (Stage) quizContainer.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Espace Étudiant");
        } catch (Exception e) { e.printStackTrace(); }
    }
}