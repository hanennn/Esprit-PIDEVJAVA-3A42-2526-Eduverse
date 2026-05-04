package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.entities.Quiz;
import org.example.entities.chapitres;
import org.example.entities.cours;
import org.example.services.QuizService;
import org.example.services.chapitresservices;
import org.example.social.QuizSocialManager;
import org.example.entities.Session;
import org.example.services.InscriptionService;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CoursDetailEtudiantController {

    @FXML private Label     headerLabel;
    @FXML private Label     breadcrumb;
    @FXML private Label     coursTitreLabel;
    @FXML private Label     coursDescLabel;
    @FXML private VBox      chapitresContainer;
    @FXML private FlowPane  quizContainer;

    // ── Social ──
    @FXML private VBox      commentairesContainer;
    @FXML private Button    btnLike;
    @FXML private Button    btnDislike;
    @FXML private Label     likeCountLabel;
    @FXML private Label     dislikeCountLabel;
    @FXML private Label     scoreLabel;
    @FXML private TextField nomUserField;
    @FXML private TextArea  commentaireField;

    private final InscriptionService inscriptionService = new InscriptionService();

    private final QuizService       quizService      = new QuizService();
    private final chapitresservices chapitresService = new chapitresservices();

    private int    coursId;
    private String coursTitre, coursNiveau, coursMatiere, coursLangue, coursDescription;
    private List<Quiz> tousLesQuiz;

    private static final int USER_ID = 1;

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
        coursDescLabel.setText(infos.length() > 0 ? infos.toString().trim() :
                (description != null ? description : ""));

        chargerChapitres();
        chargerQuiz(null, "Tous", "Plus récent");
        initialiserSocial();
    }

    // ─────────── CHAPITRES ───────────
    private void chargerChapitres() {
        chapitresContainer.getChildren().clear();
        try {
            List<chapitres> liste = chapitresService.afficher().stream()
                    .filter(c -> c.getCours_id() == coursId)
                    .collect(Collectors.toList());

            if (liste.isEmpty()) {
                Label vide = new Label("Aucun chapitre disponible.");
                vide.setStyle("-fx-text-fill: #888; -fx-padding: 10;");
                chapitresContainer.getChildren().add(vide);
                return;
            }

            VBox tableau = new VBox(0);
            tableau.setStyle("-fx-background-color: white; -fx-background-radius: 10;" +
                    "-fx-border-color: #e0e0e0; -fx-border-radius: 10;");

            HBox headerRow = new HBox();
            headerRow.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 10 15;" +
                    "-fx-background-radius: 10 10 0 0;" +
                    "-fx-border-color: transparent transparent #e0e0e0 transparent;");
            String[] cols   = {"#", "Titre", "Description", "Ordre", "Durée", "Statut", "Type", "Voir"};
            double[] widths = {40, 160, 220, 60, 80, 100, 60, 100};
            for (int i = 0; i < cols.length; i++) {
                Label h = new Label(cols[i]);
                h.setPrefWidth(widths[i]);
                h.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #555;");
                headerRow.getChildren().add(h);
            }
            tableau.getChildren().add(headerRow);

            for (int idx = 0; idx < liste.size(); idx++) {
                chapitres chap = liste.get(idx);
                HBox row = new HBox();
                row.setAlignment(Pos.CENTER_LEFT);
                row.setStyle("-fx-padding: 12 15;" +
                        "-fx-border-color: transparent transparent #f0f0f0 transparent;" +
                        (idx % 2 == 0 ? "-fx-background-color: white;" : "-fx-background-color: #fafafa;"));

                Label num   = creerCellule(String.valueOf(chap.getOrdre_chap()), 40, "-fx-font-size: 12; -fx-text-fill: #333;");
                Label titre = creerCellule(chap.getTitre_chap(), 160, "-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #1a1f3c;");
                titre.setWrapText(true);
                Label desc  = creerCellule(chap.getDesc_chap(), 220, "-fx-font-size: 11; -fx-text-fill: #666;");
                desc.setWrapText(true);
                Label ordre = creerCellule(String.valueOf(chap.getOrdre_chap()), 60, "-fx-font-size: 12; -fx-text-fill: #333;");
                Label duree = creerCellule(chap.getDuree_chap(), 80, "-fx-font-size: 12; -fx-text-fill: #333;");

                boolean ouvert = chap.getStatut_chap().toUpperCase().contains("OUVERT") &&
                        !chap.getStatut_chap().toUpperCase().contains("NON");
                Label statut = creerCellule(ouvert ? "OUVERT" : "NON OUVERT", 100,
                        "-fx-background-color: " + (ouvert ? "#2ecc71" : "#e74c3c") +
                                "; -fx-text-fill: white; -fx-font-size: 10; -fx-font-weight: bold;" +
                                "-fx-background-radius: 12; -fx-padding: 3 10;");

                Label type = creerCellule(chap.getType_contenu().toUpperCase(), 60,
                        "-fx-background-color: #3498db; -fx-text-fill: white;" +
                                "-fx-font-size: 10; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 3 8;");

                HBox voirBox = new HBox(5);
                voirBox.setPrefWidth(100);
                voirBox.setAlignment(Pos.CENTER_LEFT);
                Button btnVoir = new Button("Voir");
                btnVoir.setStyle("-fx-background-color: #f5a623; -fx-text-fill: white;" +
                        "-fx-font-size: 11; -fx-font-weight: bold;" +
                        "-fx-background-radius: 6; -fx-padding: 4 10; -fx-cursor: hand;");
                btnVoir.setOnAction(e -> ouvrirVoirChapitre(chap));
                Label statusIcon = new Label(ouvert ? "✅" : "🔒");
                statusIcon.setStyle("-fx-font-size: 14;");
                voirBox.getChildren().addAll(btnVoir, statusIcon);

                row.getChildren().addAll(num, titre, desc, ordre, duree, statut, type, voirBox);
                tableau.getChildren().add(row);
            }
            chapitresContainer.getChildren().add(tableau);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private Label creerCellule(String texte, double largeur, String style) {
        Label l = new Label(texte);
        l.setPrefWidth(largeur);
        l.setStyle(style);
        return l;
    }

    // ─────────── VOIR CHAPITRE ───────────
    private void ouvrirVoirChapitre(chapitres chap) {
        try {
            cours coursObj = new cours();
            coursObj.setCoursId(coursId);
            coursObj.setTitre_cours(coursTitre);
            coursObj.setNiv_cours(coursNiveau);
            coursObj.setMatiere_cours(coursMatiere);
            coursObj.setLangue_cours(coursLangue);
            coursObj.setDescription(coursDescription);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/voirChapitre.fxml"));
            Stage stage = (Stage) chapitresContainer.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle(chap.getTitre_chap());
            VoirChapitreController ctrl = loader.getController();
            ctrl.setChapitre(chap, coursObj);
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    // ─────────── QUIZ ───────────
    private void chargerQuiz(String recherche, String type, String tri) {
        quizContainer.getChildren().clear();
        quizContainer.getChildren().add(creerBarreRechercheQuiz());

        try {
            tousLesQuiz = quizService.afficher().stream()
                    .filter(q -> q.getCoursAssocieId() == coursId)
                    .collect(Collectors.toList());

            List<Quiz> filtre = tousLesQuiz.stream()
                    .filter(q -> {
                        if (recherche != null && !recherche.isEmpty())
                            return q.getTitre().toLowerCase().contains(recherche.toLowerCase());
                        return true;
                    })
                    .filter(q -> {
                        if (type == null || type.equals("Tous")) return true;
                        return q.getTypeQuiz().equals(type);
                    })
                    .collect(Collectors.toList());

            if (filtre.isEmpty()) {
                VBox vide = new VBox(8);
                vide.setAlignment(Pos.CENTER);
                vide.setPrefWidth(820);
                vide.setStyle("-fx-background-color: #e8f4f8; -fx-background-radius: 10; -fx-padding: 30;");
                Label msg1 = new Label("Aucun quiz disponible");
                msg1.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
                Label msg2 = new Label("Ce cours n'a pas encore de quiz.");
                msg2.setStyle("-fx-font-size: 12; -fx-text-fill: #666;");
                vide.getChildren().addAll(msg1, msg2);
                quizContainer.getChildren().add(vide);
                return;
            }

            for (Quiz q : filtre)
                quizContainer.getChildren().add(creerCarteQuiz(q));

        } catch (Exception e) { e.printStackTrace(); }
    }

    private VBox creerBarreRechercheQuiz() {
        VBox box = new VBox(8);
        box.setPrefWidth(850);
        box.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 15 20;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");

        Label titre = new Label("Rechercher un quiz");
        titre.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #333;");

        HBox barreBox = new HBox(15);
        barreBox.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("Titre du quiz...");
        searchField.setPrefWidth(280);
        searchField.setStyle("-fx-font-size: 12; -fx-background-radius: 6; -fx-border-color: #ddd; -fx-border-radius: 6; -fx-padding: 6 10;");

        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("-- Tous --", "Final", "Intermédiaire");
        typeBox.setValue("-- Tous --");
        typeBox.setStyle("-fx-font-size: 12;");

        ComboBox<String> triBox = new ComboBox<>();
        triBox.getItems().addAll("Plus récent", "Titre", "Score minimum");
        triBox.setValue("Plus récent");
        triBox.setStyle("-fx-font-size: 12;");

        Button btnOk = new Button("OK");
        btnOk.setStyle("-fx-background-color: #f5a623; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 6 18; -fx-cursor: hand;");
        btnOk.setOnAction(e -> chargerQuiz(searchField.getText(),
                typeBox.getValue().replace("-- Tous --", "Tous"), triBox.getValue()));

        barreBox.getChildren().addAll(searchField,
                new Label("Type") {{ setStyle("-fx-font-size: 12; -fx-text-fill: #555;"); }},
                typeBox,
                new Label("Trier par") {{ setStyle("-fx-font-size: 12; -fx-text-fill: #555;"); }},
                triBox, btnOk);
        box.getChildren().addAll(titre, barreBox);
        return box;
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

        Label badge = new Label(quiz.getTypeQuiz().equals("Final") ? "🏆 CERTIFICATION FINALE" : "📝 QUIZ INTERMÉDIAIRE");
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

    private void passerQuiz(Quiz quiz) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/PasserQuiz.fxml"));
            Stage stage = (Stage) chapitresContainer.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Quiz : " + quiz.getTitre());
            PasserQuizController ctrl = loader.getController();
            ctrl.setQuiz(quiz, coursId, coursTitre, coursNiveau, coursMatiere, coursLangue, coursDescription);
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ─────────── SOCIALE ───────────
    private void initialiserSocial() {
        rafraichirLikes();
        rafraichirCommentaires();
    }

    private void rafraichirLikes() {
        int likes    = QuizSocialManager.getLikes(coursId);
        int dislikes = QuizSocialManager.getDislikes(coursId);
        int total    = likes + dislikes;

        likeCountLabel.setText("👍 " + likes);
        dislikeCountLabel.setText("👎 " + dislikes);

        int pct = total > 0 ? (int)((double) likes / total * 100) : 0;
        scoreLabel.setText(pct + "%");
        scoreLabel.setStyle("-fx-font-size: 22; -fx-font-weight: bold; -fx-text-fill: "
                + (pct >= 70 ? "#2ecc71" : pct >= 40 ? "#f5a623" : "#e74c3c") + ";");

        boolean aLike = QuizSocialManager.aLiké(coursId, USER_ID);
        btnLike.setStyle(aLike
                ? "-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13; -fx-background-radius: 8; -fx-padding: 9 22; -fx-cursor: hand;"
                : "-fx-background-color: #e8f5e9; -fx-text-fill: #2ecc71; -fx-font-weight: bold; -fx-font-size: 13; -fx-background-radius: 8; -fx-border-color: #a8d5b0; -fx-border-radius: 8; -fx-border-width: 1; -fx-padding: 9 22; -fx-cursor: hand;"
        );

        boolean aDislike = QuizSocialManager.aDisliké(coursId, USER_ID);
        btnDislike.setStyle(aDislike
                ? "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13; -fx-background-radius: 8; -fx-padding: 9 22; -fx-cursor: hand;"
                : "-fx-background-color: #fef2f2; -fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 13; -fx-background-radius: 8; -fx-border-color: #fecaca; -fx-border-radius: 8; -fx-border-width: 1; -fx-padding: 9 22; -fx-cursor: hand;"
        );
    }

    @FXML
    private void toggleLike() {
        QuizSocialManager.toggleLike(coursId, USER_ID);
        rafraichirLikes();
    }

    @FXML
    private void toggleDislike() {
        QuizSocialManager.toggleDislike(coursId, USER_ID);
        rafraichirLikes();
    }

    @FXML
    private void ajouterCommentaire() {
        String nom   = nomUserField.getText().trim();
        String texte = commentaireField.getText().trim();
        if (nom.isEmpty() || texte.isEmpty()) return;
        QuizSocialManager.ajouterCommentaire(coursId, USER_ID, nom, texte);
        nomUserField.clear();
        commentaireField.clear();
        rafraichirCommentaires();
    }

    private void rafraichirCommentaires() {
        commentairesContainer.getChildren().clear();
        List<QuizSocialManager.Commentaire> liste =
                QuizSocialManager.getCommentaires(coursId);

        if (liste.isEmpty()) {
            Label vide = new Label("Aucun commentaire. Soyez le premier ! 💬");
            vide.setStyle("-fx-font-size: 12; -fx-text-fill: #aaa;");
            commentairesContainer.getChildren().add(vide);
            return;
        }

        List<QuizSocialManager.Commentaire> reversed = new ArrayList<>(liste);
        java.util.Collections.reverse(reversed);

        for (QuizSocialManager.Commentaire c : reversed) {
            VBox card = new VBox(6);
            card.setStyle("-fx-background-color: #f8f9fc; -fx-background-radius: 10;" +
                    "-fx-padding: 12 14; -fx-border-color: #ebebeb; -fx-border-radius: 10; -fx-border-width: 1;");

            HBox header = new HBox(8);
            header.setAlignment(Pos.CENTER_LEFT);

            StackPane avatar = new StackPane();
            avatar.setStyle("-fx-background-color: #f5a623; -fx-background-radius: 20;" +
                    "-fx-min-width: 32; -fx-min-height: 32; -fx-max-width: 32; -fx-max-height: 32;");
            Label initiale = new Label(c.nomUser.isEmpty() ? "?" :
                    String.valueOf(c.nomUser.charAt(0)).toUpperCase());
            initiale.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: white;");
            avatar.getChildren().add(initiale);

            Label nom = new Label(c.nomUser);
            nom.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #1a1f3c;");

            HBox spacer = new HBox();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label date = new Label("🕐 " + c.date);
            date.setStyle("-fx-font-size: 10; -fx-text-fill: #aaa;");

            header.getChildren().addAll(avatar, nom, spacer, date);

            Label texteLabel = new Label(c.texte);
            texteLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #555;");
            texteLabel.setWrapText(true);

            card.getChildren().addAll(header, texteLabel);
            commentairesContainer.getChildren().add(card);
        }
    }

    // ─────────── RETOUR ───────────
    @FXML
    private void retourAccueil() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/AccueilEtudiant.fxml"));
            Stage stage = (Stage) chapitresContainer.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Espace Étudiant");
        } catch (Exception e) { e.printStackTrace(); }
    }



    @FXML
    private void inscription() {
        try {
            int userId = Session.getCurrentUser().getId();

            if (inscriptionService.isAlreadyInscrit(userId, coursId)) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Déjà inscrit");
                alert.setContentText("Vous êtes déjà inscrit à ce cours.");
                alert.showAndWait();
                return;
            }

            inscriptionService.inscrire(userId, coursId);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Inscription réussie");
            alert.setContentText("Vous êtes maintenant inscrit au cours : " + coursTitre);
            alert.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}