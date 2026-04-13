package org.example.controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.entities.Certification;
import org.example.entities.Question;
import org.example.entities.Quiz;
import org.example.services.CertificationService;
import org.example.services.QuestionService;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

public class PasserQuizController {

    @FXML private Label       timerLabel;
    @FXML private Label       quizTitreLabel;
    @FXML private Label       progressLabel;
    @FXML private ProgressBar progressBar;
    @FXML private VBox        mainContent;
    @FXML private VBox        questionsContainer;
    @FXML private Button      soumettreBtn;

    private final QuestionService     questionService = new QuestionService();
    private final CertificationService certifService  = new CertificationService();

    private Quiz         quiz;
    private int          coursId;
    private String       coursTitre;
    private String       coursNiveau;
    private String       coursMatiere;
    private String       coursLangue;
    private String       coursDescription;

    private final List<Question>            questions    = new ArrayList<>();
    private final Map<Integer, ToggleGroup> toggleGroups = new HashMap<>();

    private Timeline timer;
    private int      secondesRestantes;

    private static final int USER_ID = 1;

    // ─────────── SET QUIZ ───────────
    public void setQuiz(Quiz quiz, int coursId, String coursTitre,
                        String coursNiveau, String coursMatiere,
                        String coursLangue, String coursDescription) {
        this.quiz             = quiz;
        this.coursId          = coursId;
        this.coursTitre       = coursTitre;
        this.coursNiveau      = coursNiveau;
        this.coursMatiere     = coursMatiere;
        this.coursLangue      = coursLangue;
        this.coursDescription = coursDescription;

        quizTitreLabel.setText(quiz.getTitre());
        chargerQuestions();
        demarrerTimer();
    }

    // ─────────── CHARGER QUESTIONS ───────────
    private void chargerQuestions() {
        questionsContainer.getChildren().clear();
        toggleGroups.clear();
        questions.clear();

        try {
            for (Question q : questionService.afficher()) {
                if (q.getQuizId() == quiz.getId()) questions.add(q);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        progressLabel.setText("0 / " + questions.size() + " répondu(es)");

        if (questions.isEmpty()) {
            Label vide = new Label("Aucune question disponible.");
            vide.setStyle("-fx-text-fill: #888; -fx-font-size: 14;");
            questionsContainer.getChildren().add(vide);
            soumettreBtn.setDisable(true);
            return;
        }

        for (int i = 0; i < questions.size(); i++) {
            questionsContainer.getChildren().add(
                    creerBlocQuestion(i + 1, questions.get(i)));
        }

        progressBar.setProgress(0);
    }

    // ─────────── CRÉER BLOC QUESTION ───────────
    private VBox creerBlocQuestion(int numero, Question q) {
        VBox bloc = new VBox(12);
        bloc.setStyle(
                "-fx-background-color: white; -fx-background-radius: 12;" +
                        "-fx-padding: 20;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);"
        );

        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label numLabel = new Label("Q" + numero);
        numLabel.setStyle(
                "-fx-background-color: #f5a623; -fx-text-fill: white;" +
                        "-fx-font-weight: bold; -fx-background-radius: 6;" +
                        "-fx-padding: 4 10; -fx-font-size: 12;");
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label ptsLabel = new Label(q.getPoints() + " pt"
                + (q.getPoints() > 1 ? "s" : ""));
        ptsLabel.setStyle(
                "-fx-background-color: #e3f2fd; -fx-text-fill: #1976d2;" +
                        "-fx-font-weight: bold; -fx-font-size: 11;" +
                        "-fx-background-radius: 6; -fx-padding: 4 10;");
        header.getChildren().addAll(numLabel, spacer, ptsLabel);

        // Texte question
        Label questionLabel = new Label(q.getQuestion());
        questionLabel.setStyle(
                "-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #1a1f3c;");
        questionLabel.setWrapText(true);

        // Réponses
        ToggleGroup group = new ToggleGroup();
        toggleGroups.put(q.getId(), group);

        VBox reponsesBox = new VBox(8);
        String[] lettres = {"A", "B", "C", "D"};
        for (String lettre : lettres) {
            String texte = extraireReponse(q.getReponses(), lettre);
            if (texte.isEmpty()) continue;

            HBox reponseBox = new HBox(12);
            reponseBox.setAlignment(Pos.CENTER_LEFT);
            reponseBox.setStyle(
                    "-fx-background-color: #f8f9fa; -fx-background-radius: 8;" +
                            "-fx-padding: 12 16; -fx-cursor: hand;" +
                            "-fx-border-color: #e9ecef; -fx-border-radius: 8;" +
                            "-fx-border-width: 1.5;"
            );
            reponseBox.setMaxWidth(Double.MAX_VALUE);

            RadioButton rb = new RadioButton();
            rb.setToggleGroup(group);
            rb.setUserData(lettre);

            Label lettreLabel = new Label(lettre + ".");
            lettreLabel.setStyle(
                    "-fx-font-size: 12; -fx-font-weight: bold;" +
                            "-fx-text-fill: #888; -fx-min-width: 18;");

            Label texteLabel = new Label(texte);
            texteLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #333;");
            texteLabel.setWrapText(true);
            HBox.setHgrow(texteLabel, Priority.ALWAYS);

            reponseBox.getChildren().addAll(rb, lettreLabel, texteLabel);

            // Click
            reponseBox.setOnMouseClicked(e -> {
                rb.setSelected(true);
                reponsesBox.getChildren().forEach(node -> {
                    if (node instanceof HBox h) {
                        h.setStyle(
                                "-fx-background-color: #f8f9fa;" +
                                        "-fx-background-radius: 8; -fx-padding: 12 16;" +
                                        "-fx-cursor: hand; -fx-border-color: #e9ecef;" +
                                        "-fx-border-radius: 8; -fx-border-width: 1.5;"
                        );
                    }
                });
                reponseBox.setStyle(
                        "-fx-background-color: #fff8ee;" +
                                "-fx-background-radius: 8; -fx-padding: 12 16;" +
                                "-fx-cursor: hand; -fx-border-color: #f5a623;" +
                                "-fx-border-radius: 8; -fx-border-width: 2;"
                );
            });

            reponseBox.setOnMouseEntered(e -> {
                if (group.getSelectedToggle() != rb) {
                    reponseBox.setStyle(
                            "-fx-background-color: #f0f4ff;" +
                                    "-fx-background-radius: 8; -fx-padding: 12 16;" +
                                    "-fx-cursor: hand; -fx-border-color: #b0c4de;" +
                                    "-fx-border-radius: 8; -fx-border-width: 1.5;"
                    );
                }
            });

            reponseBox.setOnMouseExited(e -> {
                if (group.getSelectedToggle() != rb) {
                    reponseBox.setStyle(
                            "-fx-background-color: #f8f9fa;" +
                                    "-fx-background-radius: 8; -fx-padding: 12 16;" +
                                    "-fx-cursor: hand; -fx-border-color: #e9ecef;" +
                                    "-fx-border-radius: 8; -fx-border-width: 1.5;"
                    );
                }
            });

            reponsesBox.getChildren().add(reponseBox);
        }

        // Listener progression
        group.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            long nb = toggleGroups.values().stream()
                    .filter(tg -> tg.getSelectedToggle() != null).count();
            progressLabel.setText(nb + " / "
                    + questions.size() + " répondu(es)");
            progressBar.setProgress(
                    (double) nb / questions.size());
        });

        bloc.getChildren().addAll(header, questionLabel, reponsesBox);
        return bloc;
    }

    // ─────────── TIMER ───────────
    private void demarrerTimer() {
        secondesRestantes = quiz.getDuree() * 60;
        mettreAJourAffichageTimer();

        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            secondesRestantes--;
            mettreAJourAffichageTimer();
            if (secondesRestantes <= 0) {
                timer.stop();
                soumettreQuiz();
            }
        }));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }

    private void mettreAJourAffichageTimer() {
        int min = secondesRestantes / 60;
        int sec = secondesRestantes % 60;
        timerLabel.setText(String.format("⏱ %02d:%02d", min, sec));

        if (secondesRestantes <= 60) {
            timerLabel.setStyle(
                    "-fx-font-size: 16; -fx-font-weight: bold;" +
                            "-fx-text-fill: white; -fx-background-color: #e74c3c;" +
                            "-fx-background-radius: 8; -fx-padding: 6 16;");
        } else if (secondesRestantes <= quiz.getDuree() * 30) {
            timerLabel.setStyle(
                    "-fx-font-size: 16; -fx-font-weight: bold;" +
                            "-fx-text-fill: white; -fx-background-color: #f39c12;" +
                            "-fx-background-radius: 8; -fx-padding: 6 16;");
        }
    }

    // ─────────── SOUMETTRE ───────────
    @FXML
    private void soumettreQuiz() {
        if (timer != null) timer.stop();

        // Vérifier toutes réponses
        for (Question q : questions) {
            ToggleGroup tg = toggleGroups.get(q.getId());
            if (tg == null || tg.getSelectedToggle() == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Réponse manquante");
                alert.setHeaderText(null);
                alert.setContentText(
                        "Veuillez répondre à toutes les questions !");
                alert.showAndWait();
                if (timer != null) timer.play();
                return;
            }
        }

        // Calculer score
        int totalPoints  = 0;
        int pointsObtenu = 0;
        Map<Integer, Boolean> resultatsParQuestion = new HashMap<>();

        for (Question q : questions) {
            totalPoints += q.getPoints();
            String choix = (String) toggleGroups.get(q.getId())
                    .getSelectedToggle().getUserData();
            String bonne = extraireCorrecte(q.getReponses());
            boolean correct = choix.equals(bonne);
            if (correct) pointsObtenu += q.getPoints();
            resultatsParQuestion.put(q.getId(), correct);
        }

        float   scorePercent = totalPoints > 0
                ? (float) pointsObtenu / totalPoints * 100f : 0f;
        boolean reussi = scorePercent >= quiz.getScoreMinimum();

        String badge;
        if      (scorePercent >= 90) badge = "Or";
        else if (scorePercent >= 70) badge = "Argent";
        else if (scorePercent >= 50) badge = "Bronze";
        else                         badge = "";

        // Enregistrer certification
        try {
            certifService.ajouter(new Certification(
                    scorePercent,
                    reussi ? "Réussi" : "Échoué",
                    badge,
                    Timestamp.valueOf(LocalDateTime.now()),
                    USER_ID,
                    quiz.getId()
            ));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Afficher feedback sur les questions
        afficherFeedbackQuestions(resultatsParQuestion);

        // Masquer bouton soumettre
        soumettreBtn.setVisible(false);
        soumettreBtn.setManaged(false);

        // Popup résultat
        afficherPopupResultat(reussi, scorePercent,
                pointsObtenu, totalPoints, badge);
    }

    // ─────────── FEEDBACK SUR QUESTIONS ───────────
    private void afficherFeedbackQuestions(
            Map<Integer, Boolean> resultats) {
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            boolean correct = resultats.getOrDefault(q.getId(), false);
            String bonneLettre = extraireCorrecte(q.getReponses());
            String bonneTexte  = extraireReponse(q.getReponses(),
                    bonneLettre);

            if (questionsContainer.getChildren().get(i)
                    instanceof VBox bloc) {

                // Colorer les réponses
                if (bloc.getChildren().size() >= 3
                        && bloc.getChildren().get(2) instanceof VBox repBox) {

                    String[] lettres = {"A", "B", "C", "D"};
                    for (int j = 0; j < repBox.getChildren().size(); j++) {
                        if (repBox.getChildren().get(j) instanceof HBox rb) {
                            String lettre = j < lettres.length
                                    ? lettres[j] : "";
                            boolean estCorrecte = lettre.equals(bonneLettre);
                            boolean estChoisie  = toggleGroups
                                    .get(q.getId()).getSelectedToggle() != null
                                    && lettre.equals((String) toggleGroups
                                    .get(q.getId())
                                    .getSelectedToggle().getUserData());

                            if (estCorrecte) {
                                rb.setStyle(
                                        "-fx-background-color: #e8f5e9;" +
                                                "-fx-background-radius: 8; -fx-padding: 12 16;" +
                                                "-fx-border-color: #2ecc71;" +
                                                "-fx-border-radius: 8; -fx-border-width: 2;"
                                );
                            } else if (estChoisie) {
                                rb.setStyle(
                                        "-fx-background-color: #fdecea;" +
                                                "-fx-background-radius: 8; -fx-padding: 12 16;" +
                                                "-fx-border-color: #e74c3c;" +
                                                "-fx-border-radius: 8; -fx-border-width: 2;"
                                );
                            }
                            rb.setDisable(true);
                        }
                    }
                }

                // Ajouter feedback texte
                VBox feedback = new VBox(4);
                if (correct) {
                    feedback.setStyle(
                            "-fx-background-color: #e8f5e9;" +
                                    "-fx-background-radius: 8; -fx-padding: 10;" +
                                    "-fx-border-color: #2ecc71; -fx-border-radius: 8;" +
                                    "-fx-border-width: 1;"
                    );
                    Label fl = new Label("✅  Bonne réponse !");
                    fl.setStyle(
                            "-fx-font-size: 12; -fx-font-weight: bold;" +
                                    "-fx-text-fill: #2ecc71;");
                    feedback.getChildren().add(fl);
                } else {
                    feedback.setStyle(
                            "-fx-background-color: #fdecea;" +
                                    "-fx-background-radius: 8; -fx-padding: 10;" +
                                    "-fx-border-color: #e74c3c; -fx-border-radius: 8;" +
                                    "-fx-border-width: 1;"
                    );
                    Label fl = new Label("❌  Mauvaise réponse !");
                    fl.setStyle(
                            "-fx-font-size: 12; -fx-font-weight: bold;" +
                                    "-fx-text-fill: #e74c3c;");
                    Label fl2 = new Label(
                            "La bonne réponse était : "
                                    + bonneLettre + ". " + bonneTexte);
                    fl2.setStyle(
                            "-fx-font-size: 11; -fx-text-fill: #c0392b;");
                    fl2.setWrapText(true);
                    feedback.getChildren().addAll(fl, fl2);
                }
                bloc.getChildren().add(feedback);
            }
        }
    }

    // ─────────── POPUP RÉSULTAT ───────────
    private void afficherPopupResultat(boolean reussi,
                                       float score,
                                       int points,
                                       int total,
                                       String badge) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle(reussi ? "Felicitations" : "Quiz termine");
        popup.setResizable(false);

        VBox root = new VBox(18);
        root.setAlignment(Pos.CENTER);
        root.setStyle(
                "-fx-background-color: white; -fx-padding: 40;" +
                        "-fx-min-width: 420;"
        );

        // Icône principale
        Label icone = new Label(reussi ? "🎊" : "😔");
        icone.setStyle("-fx-font-size: 55;");

        // Titre
        Label titre = new Label(reussi
                ? "Felicitations !"
                : "Quiz termine");
        titre.setStyle(
                "-fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: "
                        + (reussi ? "#1a1f3c" : "#e74c3c") + ";");

        // Score
        Label scoreLabel = new Label(
                String.format("%.1f%%", score));
        scoreLabel.setStyle(
                "-fx-font-size: 42; -fx-font-weight: bold; -fx-text-fill: "
                        + (reussi ? "#f5a623" : "#e74c3c") + ";");

        Label ptsLabel = new Label(
                points + " / " + total + " pts");
        ptsLabel.setStyle(
                "-fx-font-size: 14; -fx-text-fill: #888;");

        // Statut
        Label statutLabel = new Label(
                reussi ? "Statut : Reussi" : "Statut : Echoue");
        statutLabel.setStyle(
                "-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: "
                        + (reussi ? "#2ecc71" : "#e74c3c") + ";");

        // Score minimum
        Label scoreMinLabel = new Label(
                "Score minimum requis : "
                        + (int) quiz.getScoreMinimum() + "%");
        scoreMinLabel.setStyle(
                "-fx-font-size: 12; -fx-text-fill: #888;");

        root.getChildren().addAll(
                icone, titre, scoreLabel, ptsLabel, statutLabel, scoreMinLabel);

        if (reussi) {
            // Badge
            VBox badgeBox = new VBox(5);
            badgeBox.setAlignment(Pos.CENTER);
            badgeBox.setStyle(
                    "-fx-background-color: #fff8ee;" +
                            "-fx-background-radius: 12; -fx-padding: 15 30;" +
                            "-fx-border-color: #f5a623; -fx-border-radius: 12;" +
                            "-fx-border-width: 2;"
            );
            Label badgeTitre = new Label("Badge obtenu");
            badgeTitre.setStyle(
                    "-fx-font-size: 11; -fx-text-fill: #888;");
            String badgeAffiche = badge.isEmpty() ? "Aucun" : badge;
            Label badgeVal = new Label(badgeAffiche);
            badgeVal.setStyle(
                    "-fx-font-size: 20; -fx-font-weight: bold;" +
                            "-fx-text-fill: #f5a623;");
            badgeBox.getChildren().addAll(badgeTitre, badgeVal);
            root.getChildren().add(badgeBox);
        } else {
            // Message encouragement
            Label encouragement = new Label(
                    "Ne vous decouragez pas ! Vous pouvez reessayer.");
            encouragement.setStyle(
                    "-fx-font-size: 13; -fx-text-fill: #555;" +
                            "-fx-font-style: italic;");
            encouragement.setWrapText(true);
            encouragement.setAlignment(Pos.CENTER);
            root.getChildren().add(encouragement);
        }

        // Certif enregistrée
        Label certifLabel = new Label("Tentative enregistree !");
        certifLabel.setStyle(
                "-fx-font-size: 12; -fx-text-fill: #2ecc71;" +
                        "-fx-font-weight: bold;");
        root.getChildren().add(certifLabel);

        // Boutons
        HBox btnBox = new HBox(12);
        btnBox.setAlignment(Pos.CENTER);

        Button btnRetour = new Button("Retour au cours");
        btnRetour.setStyle(
                "-fx-background-color: #1a1f3c; -fx-text-fill: white;" +
                        "-fx-font-weight: bold; -fx-font-size: 13;" +
                        "-fx-background-radius: 10; -fx-padding: 11 20; -fx-cursor: hand;");
        btnRetour.setOnAction(e -> {
            popup.close();
            retourCours();
        });

        Button btnAccueil = new Button("Accueil");
        btnAccueil.setStyle(
                "-fx-background-color: #f5a623; -fx-text-fill: white;" +
                        "-fx-font-weight: bold; -fx-font-size: 13;" +
                        "-fx-background-radius: 10; -fx-padding: 11 20; -fx-cursor: hand;");
        btnAccueil.setOnAction(e -> {
            popup.close();
            retourAccueil();
        });

        btnBox.getChildren().addAll(btnRetour, btnAccueil);
        root.getChildren().add(btnBox);

        popup.setScene(new Scene(root));
        popup.show();

        Platform.runLater(() -> {
            Stage parent =
                    (Stage) questionsContainer.getScene().getWindow();
            popup.setX(parent.getX()
                    + (parent.getWidth()  - popup.getWidth())  / 2);
            popup.setY(parent.getY()
                    + (parent.getHeight() - popup.getHeight()) / 2);
        });
    }

    // ─────────── NAVIGATION ───────────
    @FXML
    private void retourCours() {
        if (timer != null) timer.stop();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/org/example/CoursDetailEtudiant.fxml"));
            Stage stage = (Stage) questionsContainer.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            CoursDetailEtudiantController ctrl = loader.getController();
            ctrl.setCours(coursId, coursTitre, coursNiveau,
                    coursMatiere, coursLangue, coursDescription);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void retourAccueil() {
        if (timer != null) timer.stop();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/org/example/AccueilEtudiant.fxml"));
            Stage stage = (Stage) questionsContainer.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Espace Etudiant");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ─────────── JSON HELPERS ───────────
    private String extraireReponse(String json, String lettre) {
        if (json == null || json.isEmpty()) return "";
        try {
            String[] parts = json.replace("[", "").replace("]", "")
                    .split("},");
            int index = lettre.equals("A") ? 0 : lettre.equals("B") ? 1 :
                    lettre.equals("C") ? 2 : 3;
            if (index >= parts.length) return "";
            String part  = parts[index];
            int    start = part.indexOf("\"texte\":\"") + 9;
            int    end   = part.indexOf("\"", start);
            if (start < 9 || end < 0) return "";
            return part.substring(start, end);
        } catch (Exception e) { return ""; }
    }

    private String extraireCorrecte(String json) {
        if (json == null || json.isEmpty()) return "";
        try {
            String[] parts   = json.replace("[", "").replace("]", "")
                    .split("},");
            String[] lettres = {"A", "B", "C", "D"};
            for (int i = 0; i < parts.length; i++) {
                if (parts[i].contains("\"correct\":true"))
                    return lettres[i];
            }
        } catch (Exception e) { return ""; }
        return "";
    }
}