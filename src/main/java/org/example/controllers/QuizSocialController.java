package org.example.controllers;

import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import org.example.entities.Quiz;
import org.example.social.QuizSocialManager;
import org.example.social.QuizSocialManager.Commentaire;

import java.util.List;

public class QuizSocialController {

    @FXML private Label    quizTitreLabel;
    @FXML private Button   btnLike, btnDislike;
    @FXML private Label    likesCount, dislikesCount;
    @FXML private VBox     commentairesBox;
    @FXML private TextArea commentaireField;
    @FXML private Label    messageLabel;

    private int quizId;
    private int userId;
    private String userNom;

    public void setQuiz(Quiz quiz, int userId, String userNom) {
        this.quizId  = quiz.getId();
        this.userId  = userId;
        this.userNom = userNom;

        quizTitreLabel.setText("💬 " + quiz.getTitre());
        rafraichirLikes();
        chargerCommentaires();
    }

    // ─── LIKES ───
    @FXML
    void likerQuiz() {
        QuizSocialManager.toggleLike(quizId, userId);
        rafraichirLikes();

        // Animation
        ScaleTransition st = new ScaleTransition(Duration.millis(200), btnLike);
        st.setFromX(1.0); st.setFromY(1.0);
        st.setToX(1.3);   st.setToY(1.3);
        st.setAutoReverse(true); st.setCycleCount(2); st.play();
    }

    @FXML
    void dislikerQuiz() {
        QuizSocialManager.toggleDislike(quizId, userId);
        rafraichirLikes();

        ScaleTransition st = new ScaleTransition(Duration.millis(200), btnDislike);
        st.setFromX(1.0); st.setFromY(1.0);
        st.setToX(1.3);   st.setToY(1.3);
        st.setAutoReverse(true); st.setCycleCount(2); st.play();
    }

    private void rafraichirLikes() {
        int l = QuizSocialManager.getLikes(quizId);
        int d = QuizSocialManager.getDislikes(quizId);
        boolean aLiké     = QuizSocialManager.aLiké(quizId, userId);
        boolean aDisliké  = QuizSocialManager.aDisliké(quizId, userId);

        likesCount.setText(String.valueOf(l));
        dislikesCount.setText(String.valueOf(d));

        btnLike.setStyle(aLiké
                ? "-fx-background-color: #2ecc71; -fx-text-fill: white;" +
                "-fx-font-size: 20; -fx-background-radius: 30; -fx-padding: 8 18; -fx-cursor: hand;"
                : "-fx-background-color: #e0e0e0; -fx-text-fill: #333;" +
                "-fx-font-size: 20; -fx-background-radius: 30; -fx-padding: 8 18; -fx-cursor: hand;");

        btnDislike.setStyle(aDisliké
                ? "-fx-background-color: #e74c3c; -fx-text-fill: white;" +
                "-fx-font-size: 20; -fx-background-radius: 30; -fx-padding: 8 18; -fx-cursor: hand;"
                : "-fx-background-color: #e0e0e0; -fx-text-fill: #333;" +
                "-fx-font-size: 20; -fx-background-radius: 30; -fx-padding: 8 18; -fx-cursor: hand;");
    }

    // ─── COMMENTAIRES ───
    @FXML
    void posterCommentaire() {
        String texte = commentaireField.getText().trim();
        if (texte.isEmpty()) {
            messageLabel.setText("❌ Écris quelque chose !");
            messageLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }
        QuizSocialManager.ajouterCommentaire(quizId, userId, userNom, texte);
        commentaireField.clear();
        chargerCommentaires();
        messageLabel.setText("✅ Commentaire posté !");
        messageLabel.setStyle("-fx-text-fill: #2ecc71;");
    }

    private void chargerCommentaires() {
        commentairesBox.getChildren().clear();
        List<Commentaire> liste = QuizSocialManager.getCommentaires(quizId);

        if (liste.isEmpty()) {
            Label vide = new Label("Sois le premier à commenter 💬");
            vide.setStyle("-fx-text-fill: #aaa; -fx-font-size: 13; -fx-padding: 10;");
            commentairesBox.getChildren().add(vide);
            return;
        }

        // Afficher du plus récent au plus ancien
        for (int i = liste.size() - 1; i >= 0; i--) {
            commentairesBox.getChildren().add(creerBulleCommentaire(liste.get(i)));
        }
    }

    private VBox creerBulleCommentaire(Commentaire c) {
        VBox card = new VBox(4);
        boolean estMoi = c.userId == userId;
        card.setStyle(
                "-fx-background-color: " + (estMoi ? "#fff8ee" : "white") + ";" +
                        "-fx-background-radius: 10; -fx-padding: 10 14;" +
                        "-fx-border-color: " + (estMoi ? "#f5a623" : "#e0e0e0") + ";" +
                        "-fx-border-radius: 10; -fx-border-width: 1.5;");

        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        Label avatar = new Label(String.valueOf(c.nomUser.charAt(0)).toUpperCase());
        avatar.setStyle(
                "-fx-background-color: " + (estMoi ? "#f5a623" : "#1a1f3c") + ";" +
                        "-fx-text-fill: white; -fx-font-weight: bold;" +
                        "-fx-background-radius: 20; -fx-min-width: 30; -fx-min-height: 30;" +
                        "-fx-alignment: center; -fx-padding: 5 9;");
        Label nom  = new Label(c.nomUser);
        nom.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #1a1f3c;");
        Label date = new Label(c.date);
        date.setStyle("-fx-font-size: 10; -fx-text-fill: #aaa;");
        HBox spacer = new HBox(); HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        header.getChildren().addAll(avatar, nom, spacer, date);

        Label texte = new Label(c.texte);
        texte.setWrapText(true);
        texte.setStyle("-fx-font-size: 13; -fx-text-fill: #444; -fx-padding: 2 0 0 0;");

        card.getChildren().addAll(header, texte);
        return card;
    }
}