package org.example.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

public class PasserQuizController {

    @FXML private Label       quizTitreLabel;
    @FXML private Label       timerLabel;
    @FXML private Label       progressLabel;
    @FXML private ProgressBar progressBar;
    @FXML private VBox        mainContent;
    @FXML private VBox        questionsContainer;
    @FXML private Button      soumettreBtn;

    private final QuestionService      questionService = new QuestionService();
    private final CertificationService certifService   = new CertificationService();

    private Quiz   quiz;
    private int    coursId;
    private String coursTitre;
    private String coursNiveau;
    private String coursMatiere;
    private String coursLangue;
    private String coursDescription;
//liste pour question et reponse
    private final List<Question>            questions    = new ArrayList<>();
    //associe chaque question à son groupe de boutons radio
    private final Map<Integer, ToggleGroup> toggleGroups = new HashMap<>();

    private Timeline timer;
    private int      secondesRestantes;//compteur en secondes

    private static final int USER_ID = 1;

    // ═══════════════════════════════════════
    // SET QUIZ
    // ═══════════════════════════════════════
    //quand on click sur passer quiz les infos se chargent
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
        chargerQuestions();//aficher question
        demarrerTimer();
    }

    // ═══════════════════════════════════════
    // CHARGER QUESTIONS
    // ═══════════════════════════════════════
    private void chargerQuestions() {
        //vider tout
        questionsContainer.getChildren().clear();
        toggleGroups.clear();
        questions.clear();
//recuperer toutes les questions et filtrer du quiz corresondant
        try {
            for (Question q : questionService.afficher())
                if (q.getQuizId() == quiz.getId()) questions.add(q);
        } catch (Exception e) { e.printStackTrace(); }
//initalise progress bar a zero
        progressLabel.setText("0 / " + questions.size() + " répondu(es)");
        progressBar.setProgress(0);
//quiz sans question desactiver bouton soumettre
        if (questions.isEmpty()) {
            Label vide = new Label("Aucune question disponible.");
            vide.setStyle("-fx-text-fill: #888; -fx-font-size: 14;");
            questionsContainer.getChildren().add(vide);
            soumettreBtn.setDisable(true);
            return;
        }
//crrer les bloc pour les questions
        for (int i = 0; i < questions.size(); i++)
            questionsContainer.getChildren().add(
                    creerBlocQuestion(i + 1, questions.get(i)));
    }

    // ═══════════════════════════════════════
    // TIMER
    // ═══════════════════════════════════════
    private void demarrerTimer() {
        //convertir duree du min en sec
        secondesRestantes = quiz.getDuree() * 60;
        mettreAJourAffichageTimer();//affichage en temps reele
//execute le code tout les 1 secondes
        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            secondesRestantes--;
            mettreAJourAffichageTimer();
            if (secondesRestantes <= 0) {//si temps ecoule soumet auto
                timer.stop();
                soumettreQuiz();
            }
        }));
        timer.setCycleCount(Timeline.INDEFINITE);//repete indefiniment
        timer.play();
    }

    private void mettreAJourAffichageTimer() {
        int min = secondesRestantes / 60;
        int sec = secondesRestantes % 60;
        timerLabel.setText(String.format("⏱ %02d:%02d", min, sec));
//couleur change selon urgence
        if (secondesRestantes <= 60)
            timerLabel.setStyle(
                    "-fx-font-size: 16; -fx-font-weight: bold;" +
                            "-fx-text-fill: white; -fx-background-color: #e74c3c;" +
                            "-fx-background-radius: 8; -fx-padding: 5 16;");
        else if (secondesRestantes <= quiz.getDuree() * 30)
            timerLabel.setStyle(
                    "-fx-font-size: 16; -fx-font-weight: bold;" +
                            "-fx-text-fill: white; -fx-background-color: #f39c12;" +
                            "-fx-background-radius: 8; -fx-padding: 5 16;");
        else
            timerLabel.setStyle(
                    "-fx-font-size: 16; -fx-font-weight: bold;" +
                            "-fx-text-fill: white; -fx-background-color: #1a1f3c;" +
                            "-fx-background-radius: 8; -fx-padding: 5 16;");
    }


    // TRADUCTION MYMEMORY

    private String traduire(String texte, String langCible) {
        try {
            //encoder texte
            String encoded  = URLEncoder.encode(texte, StandardCharsets.UTF_8);
            String langPair = URLEncoder.encode("fr|" + langCible,
                    StandardCharsets.UTF_8);
            //api
            String url = "https://api.mymemory.translated.net/get?q="
                    + encoded + "&langpair=" + langPair;

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url)).GET().build();
            HttpResponse<String> response = client.send(
                    request, HttpResponse.BodyHandlers.ofString());
//parse le json de la rep avec jackson
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.body());
            //verifie que la reponse est valide
            int status = root.path("responseStatus").asInt();
            if (status == 200) {
                String t = root.path("responseData")
                        .path("translatedText").asText();
                if (!t.isEmpty() &&
                        !t.equalsIgnoreCase("INVALID LANGUAGE PAIR"))
                    return t;
            }
            //si errerrur retourner le texte original
        } catch (Exception e) { e.printStackTrace(); }
        return texte;
    }


    // DICTIONARY API — MÉTHODES


    /**
     * Traduit un mot français → anglais via MyMemory
     */
    private String traduireMotFrVersEn(String motFr) {
        try {
            //nettyoer tout sauf les lettres pas d'espace
            String motNettoye = motFr.trim()
                    .replaceAll("[^a-zA-ZÀ-ÿ\\s]", "")
                    .toLowerCase();
            if (motNettoye.isEmpty()) return motFr;

            String encoded  = URLEncoder.encode(
                    motNettoye, StandardCharsets.UTF_8);
            String langPair = URLEncoder.encode(
                    "fr|en", StandardCharsets.UTF_8);
            String url = "https://api.mymemory.translated.net/get?q="
                    + encoded + "&langpair=" + langPair;

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url)).GET().build();
            HttpResponse<String> response = client.send(
                    request, HttpResponse.BodyHandlers.ofString());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.body());
            int status = root.path("responseStatus").asInt();
            if (status == 200) {
                String t = root.path("responseData")
                        .path("translatedText").asText();
                if (!t.isEmpty() &&
                        !t.equalsIgnoreCase("INVALID LANGUAGE PAIR"))
                    return t.toLowerCase().trim();//retourne mot anglais en minus
            }
        } catch (Exception e) { e.printStackTrace(); }
        return motFr;
    }

    /**
     * Traduit un texte anglais → français via MyMemory
     */
    private String traduireEnVersFr(String texteEn) {
        try {//traduire definition genereee
            if (texteEn == null || texteEn.trim().isEmpty())
                return texteEn;

            String encoded  = URLEncoder.encode(
                    texteEn, StandardCharsets.UTF_8);
            String langPair = URLEncoder.encode(
                    "en|fr", StandardCharsets.UTF_8);
            String url = "https://api.mymemory.translated.net/get?q="
                    + encoded + "&langpair=" + langPair;

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url)).GET().build();
            HttpResponse<String> response = client.send(
                    request, HttpResponse.BodyHandlers.ofString());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.body());
            int status = root.path("responseStatus").asInt();
            if (status == 200) {
                String t = root.path("responseData")
                        .path("translatedText").asText();
                if (!t.isEmpty() &&
                        !t.equalsIgnoreCase("INVALID LANGUAGE PAIR"))
                    return t;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return texteEn;
    }

    /**
     * Récupère depuis Dictionary API :
     * [0] = définition FR
     * [1] = définition EN
     * [2] = exemple EN
     * [3] = exemple FR
     * [4] = partOfSpeech
     */
    private String[] getDefinitionEtExemple(String motEn) {
        try {//nettoie mot
            String motNettoye = motEn.trim()
                    .replaceAll("[^a-zA-Z\\s]", "")
                    .toLowerCase().trim();

            if (motNettoye.isEmpty())
                return new String[]{"", "", "", "", ""};
//url de l'api
            String url = "https://api.dictionaryapi.dev/api/v2/entries/en/"
                    + URLEncoder.encode(
                    motNettoye, StandardCharsets.UTF_8);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url)).GET().build();
            HttpResponse<String> response = client.send(
                    request, HttpResponse.BodyHandlers.ofString());
// si mot non trouvé dans le dictionnaire
            if (response.statusCode() != 200)
                return new String[]{"", "", "", "", ""};
//parser le json
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.body());

            if (!root.isArray() || root.isEmpty())
                return new String[]{"", "", "", "", ""};
//liste des sens des mots
            JsonNode meanings = root.get(0).path("meanings");
            if (meanings.isEmpty())
                return new String[]{"", "", "", "", ""};

            // Prend le premier sens disponible
            JsonNode meaning      = meanings.get(0);
            String   partOfSpeech = meaning.path("partOfSpeech").asText();
            JsonNode definitions  = meaning.path("definitions");

            if (definitions.isEmpty())
                return new String[]{"", "", "", "", partOfSpeech};
            // Prend la première définition
            JsonNode def0      = definitions.get(0);
            String   defEn     = def0.path("definition").asText();
            String   exempleEn = def0.path("example").asText();

            // Traduire en français
            String defFr     = defEn.isEmpty()     ? "" : traduireEnVersFr(defEn);
            String exempleFr = exempleEn.isEmpty() ? "" : traduireEnVersFr(exempleEn);

            return new String[]{
                    defFr,        // [0] définition FR
                    defEn,        // [1] définition EN
                    exempleEn,    // [2] exemple EN
                    exempleFr,    // [3] exemple FR
                    partOfSpeech  // [4] type grammatical
            };

        } catch (Exception e) {
            e.printStackTrace();
            return new String[]{"", "", "", "", ""};
        }
    }

    /**
     * Active double-clic sur le label question
     */
    private void activerDefinitionSurDoubleClick(Label questionLabel) {
        questionLabel.setOnMouseClicked(ev -> {
            if (ev.getClickCount() == 2)
                afficherDialogueSaisieMot(
                        ev.getScreenX(), ev.getScreenY());
        });
        Tooltip tip = new Tooltip(
                "Double-cliquez pour définir un mot");
        tip.setStyle(
                "-fx-font-size: 10; -fx-background-color: #1a1f3c;" +
                        "-fx-text-fill: white; -fx-background-radius: 6;");
        Tooltip.install(questionLabel, tip);
    }

    /**
     * Dialogue de saisie du mot
     */
    //on double click sur mot
    private void afficherDialogueSaisieMot(double x, double y) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Définir un mot");
        dialog.setResizable(false);

        VBox root = new VBox(16);
        root.setAlignment(Pos.CENTER);
        root.setStyle(
                "-fx-background-color: white; -fx-padding: 28;" +
                        "-fx-min-width: 340;");

        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        StackPane iconBox = new StackPane();
        iconBox.setStyle(
                "-fx-background-color: #eef2ff;" +
                        "-fx-background-radius: 8; -fx-padding: 8 10;");
        Label icon = new Label("📖");
        icon.setStyle("-fx-font-size: 18;");
        iconBox.getChildren().add(icon);
        VBox titreBox = new VBox(2);
        Label titre = new Label("Définir un mot");
        titre.setStyle(
                "-fx-font-size: 14; -fx-font-weight: bold;" +
                        "-fx-text-fill: #1a1f3c;");
        Label sousTitre = new Label(
                "Entrez le mot à définir (fr ou en)");
        sousTitre.setStyle(
                "-fx-font-size: 10; -fx-text-fill: #888;");
        titreBox.getChildren().addAll(titre, sousTitre);
        header.getChildren().addAll(iconBox, titreBox);

        // Champ
        TextField motField = new TextField();
        motField.setPromptText("Ex: algorithme, boucle, variable...");
        motField.setStyle(
                "-fx-background-color: #f9fafb;" +
                        "-fx-border-color: #e5e7eb;" +
                        "-fx-border-radius: 8; -fx-background-radius: 8;" +
                        "-fx-padding: 10; -fx-font-size: 12;");

        // Boutons
        HBox btnBox = new HBox(10);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        Button btnAnnuler = new Button("Annuler");
        btnAnnuler.setStyle(
                "-fx-background-color: #f9fafb;" +
                        "-fx-text-fill: #6b7280; -fx-font-size: 12;" +
                        "-fx-border-color: #e5e7eb; -fx-border-radius: 8;" +
                        "-fx-background-radius: 8; -fx-padding: 8 16;" +
                        "-fx-cursor: hand;");
        btnAnnuler.setOnAction(e -> dialog.close());

        Button btnDefinir = new Button("Rechercher");
        btnDefinir.setStyle(
                "-fx-background-color: #4361ee;" +
                        "-fx-text-fill: white; -fx-font-weight: bold;" +
                        "-fx-font-size: 12; -fx-background-radius: 8;" +
                        "-fx-padding: 8 20; -fx-cursor: hand;");

        Runnable action = () -> {
            String mot = motField.getText().trim();
            if (!mot.isEmpty()) {
                dialog.close();
                afficherDefinition(mot, x, y);
            }
        };

        btnDefinir.setOnAction(e -> action.run());
        motField.setOnAction(e -> action.run());

        btnBox.getChildren().addAll(btnAnnuler, btnDefinir);
        root.getChildren().addAll(header, motField, btnBox);

        dialog.setScene(new Scene(root));
        dialog.setX(x - 170);
        dialog.setY(y + 10);
        dialog.show();
        Platform.runLater(motField::requestFocus);
    }

    /**
     * Lance la recherche avec popup de chargement
     */
    private void afficherDefinition(String mot, double x, double y) {

        // Popup chargement
        Stage loadingStage = new Stage();
        loadingStage.initModality(Modality.NONE);
        loadingStage.setTitle("Recherche...");
        loadingStage.setResizable(false);

        VBox loadingRoot = new VBox(12);
        loadingRoot.setAlignment(Pos.CENTER);
        loadingRoot.setStyle(
                "-fx-background-color: white; -fx-padding: 30;");
        Label loadingIcon = new Label("🔍");
        loadingIcon.setStyle("-fx-font-size: 28;");
        Label loadingTxt = new Label(
                "Recherche de la définition...");
        loadingTxt.setStyle(
                "-fx-font-size: 12; -fx-text-fill: #6b7280;");
        ProgressBar pb = new ProgressBar();
        pb.setPrefWidth(220);
        pb.setStyle("-fx-accent: #4361ee;");
        pb.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        loadingRoot.getChildren().addAll(loadingIcon, loadingTxt, pb);
        loadingStage.setScene(new Scene(loadingRoot, 280, 130));
        loadingStage.setX(x - 140);
        loadingStage.setY(y + 10);
        loadingStage.show();

        new Thread(() -> {
            try {
                // 1. Traduire mot fr → en
                String motAnglais = traduireMotFrVersEn(mot);

                // 2. Récupérer définition

                String[] result = getDefinitionEtExemple(motAnglais);

                String defFr        = result[0];
                String defEn        = result[1];
                String exempleEn    = result[2];
                String exempleFr    = result[3];
                String partOfSpeech = result[4];

                Platform.runLater(() -> {
                    loadingStage.close();
                    if (defFr.isEmpty() && defEn.isEmpty()) {
                        afficherPopupErreur(mot, x, y);
                    } else {
                        afficherPopupDefinition(
                                mot, motAnglais, partOfSpeech,
                                defFr, defEn,
                                exempleFr, exempleEn,
                                x, y);
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    loadingStage.close();
                    afficherPopupErreur(mot, x, y);
                });
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Popup résultat avec définition FR + EN
     */
    private void afficherPopupDefinition(
            String motOriginal, String motAnglais,
            String partOfSpeech,
            String defFr, String defEn,
            String exempleFr, String exempleEn,
            double x, double y) {

        Stage popup = new Stage();
        popup.initModality(Modality.NONE);
        popup.setTitle("Definition");
        popup.setResizable(false);

        VBox root = new VBox(14);
        root.setStyle(
                "-fx-background-color: white; -fx-padding: 24;" +
                        "-fx-min-width: 420; -fx-max-width: 460;");

        // ── Header ──
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        StackPane iconBox = new StackPane();
        iconBox.setStyle(
                "-fx-background-color: #eef2ff;" +
                        "-fx-background-radius: 10; -fx-padding: 10 12;");
        Label iconLbl = new Label("📖");
        iconLbl.setStyle("-fx-font-size: 22;");
        iconBox.getChildren().add(iconLbl);

        VBox titreBox = new VBox(4);
        Label motLabel = new Label(motOriginal.toUpperCase());
        motLabel.setStyle(
                "-fx-font-size: 20; -fx-font-weight: bold;" +
                        "-fx-text-fill: #1a1f3c;");

        HBox subBox = new HBox(8);
        subBox.setAlignment(Pos.CENTER_LEFT);

        // Badge traduction
        if (!motAnglais.equalsIgnoreCase(motOriginal)) {
            Label tradLabel = new Label(motAnglais);
            tradLabel.setStyle(
                    "-fx-font-size: 11; -fx-text-fill: #4361ee;" +
                            "-fx-font-style: italic;");
            subBox.getChildren().add(tradLabel);
        }

        // Badge type grammatical
        if (!partOfSpeech.isEmpty()) {
            Label posLabel = new Label(partOfSpeech);
            posLabel.setStyle(
                    "-fx-background-color: #f0fdf4;" +
                            "-fx-text-fill: #16a34a;" +
                            "-fx-font-size: 10; -fx-font-weight: bold;" +
                            "-fx-background-radius: 20; -fx-padding: 2 8;");
            subBox.getChildren().add(posLabel);
        }

        titreBox.getChildren().addAll(motLabel, subBox);
        header.getChildren().addAll(iconBox, titreBox);

        // ── Séparateur ──
        HBox sep = new HBox();
        sep.setStyle(
                "-fx-background-color: linear-gradient(" +
                        "to right, #4361ee, #818cf8, transparent);" +
                        "-fx-min-height: 2; -fx-background-radius: 1;");

        // ── Définition FRANÇAISE ──
        VBox defFrBox = new VBox(8);
        defFrBox.setStyle(
                "-fx-background-color: #f8f9ff;" +
                        "-fx-background-radius: 10; -fx-padding: 14;" +
                        "-fx-border-color: #e0e7ff;" +
                        "-fx-border-radius: 10; -fx-border-width: 1;");

        HBox defFrHeader = new HBox(6);
        defFrHeader.setAlignment(Pos.CENTER_LEFT);
        Label flagFr = new Label("🇫🇷");
        flagFr.setStyle("-fx-font-size: 13;");
        Label defFrTitre = new Label("DÉFINITION EN FRANÇAIS");
        defFrTitre.setStyle(
                "-fx-font-size: 9; -fx-font-weight: bold;" +
                        "-fx-text-fill: #4361ee;");
        defFrHeader.getChildren().addAll(flagFr, defFrTitre);

        Label defFrLabel = new Label(
                defFr.isEmpty()
                        ? "Traduction non disponible pour \"" + motOriginal + "\""
                        : defFr);
        defFrLabel.setStyle(
                "-fx-font-size: 12; -fx-text-fill: #1a1f3c;" +
                        "-fx-line-spacing: 2;");
        defFrLabel.setWrapText(true);
        defFrLabel.setMaxWidth(400);

        defFrBox.getChildren().addAll(defFrHeader, defFrLabel);

        // Exemple français
        if (!exempleFr.isEmpty()) {
            HBox exFrBox = new HBox(6);
            exFrBox.setAlignment(Pos.CENTER_LEFT);
            Label exFrIcon = new Label("💬");
            exFrIcon.setStyle("-fx-font-size: 11;");
            Label exFrLabel = new Label(exempleFr);
            exFrLabel.setStyle(
                    "-fx-font-size: 11; -fx-text-fill: #6b7280;" +
                            "-fx-font-style: italic;");
            exFrLabel.setWrapText(true);
            exFrLabel.setMaxWidth(370);
            HBox.setHgrow(exFrLabel, Priority.ALWAYS);
            exFrBox.getChildren().addAll(exFrIcon, exFrLabel);
            defFrBox.getChildren().add(exFrBox);
        }

        root.getChildren().addAll(header, sep, defFrBox);

        // ── Définition ANGLAISE (si dispo) ──
        if (!defEn.isEmpty()) {
            VBox defEnBox = new VBox(8);
            defEnBox.setStyle(
                    "-fx-background-color: #f0fdf4;" +
                            "-fx-background-radius: 10; -fx-padding: 14;" +
                            "-fx-border-color: #bbf7d0;" +
                            "-fx-border-radius: 10; -fx-border-width: 1;");

            HBox defEnHeader = new HBox(6);
            defEnHeader.setAlignment(Pos.CENTER_LEFT);
            Label flagEn = new Label("🇬🇧");
            flagEn.setStyle("-fx-font-size: 13;");
            Label defEnTitre = new Label("DEFINITION IN ENGLISH");
            defEnTitre.setStyle(
                    "-fx-font-size: 9; -fx-font-weight: bold;" +
                            "-fx-text-fill: #16a34a;");
            defEnHeader.getChildren().addAll(flagEn, defEnTitre);

            Label defEnLabel = new Label(defEn);
            defEnLabel.setStyle(
                    "-fx-font-size: 11; -fx-text-fill: #374151;" +
                            "-fx-font-style: italic;");
            defEnLabel.setWrapText(true);
            defEnLabel.setMaxWidth(400);

            defEnBox.getChildren().addAll(defEnHeader, defEnLabel);

            if (!exempleEn.isEmpty()) {
                HBox exEnBox = new HBox(6);
                exEnBox.setAlignment(Pos.CENTER_LEFT);
                Label exEnIcon = new Label("💬");
                exEnIcon.setStyle("-fx-font-size: 11;");
                Label exEnLabel = new Label(exempleEn);
                exEnLabel.setStyle(
                        "-fx-font-size: 10; -fx-text-fill: #6b7280;" +
                                "-fx-font-style: italic;");
                exEnLabel.setWrapText(true);
                exEnLabel.setMaxWidth(370);
                HBox.setHgrow(exEnLabel, Priority.ALWAYS);
                exEnBox.getChildren().addAll(exEnIcon, exEnLabel);
                defEnBox.getChildren().add(exEnBox);
            }

            root.getChildren().add(defEnBox);
        }

        // ── Source + Boutons ──
        Label source = new Label(
                "Source : Free Dictionary API + MyMemory Translation");
        source.setStyle(
                "-fx-font-size: 9; -fx-text-fill: #d1d5db;");

        HBox btnBox = new HBox(10);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        Button btnNouveau = new Button("Autre mot");
        btnNouveau.setStyle(
                "-fx-background-color: #f9fafb; -fx-text-fill: #4361ee;" +
                        "-fx-font-weight: bold; -fx-font-size: 11;" +
                        "-fx-background-radius: 8; -fx-border-color: #e0e7ff;" +
                        "-fx-border-radius: 8; -fx-border-width: 1;" +
                        "-fx-padding: 8 16; -fx-cursor: hand;");
        btnNouveau.setOnAction(e -> {
            popup.close();
            afficherDialogueSaisieMot(x, y);
        });

        Button btnFermer = new Button("Fermer");
        btnFermer.setStyle(
                "-fx-background-color: #4361ee; -fx-text-fill: white;" +
                        "-fx-font-weight: bold; -fx-font-size: 12;" +
                        "-fx-background-radius: 8; -fx-padding: 8 24;" +
                        "-fx-cursor: hand;");
        btnFermer.setOnAction(e -> popup.close());

        btnBox.getChildren().addAll(btnNouveau, btnFermer);
        root.getChildren().addAll(source, btnBox);

        popup.setScene(new Scene(root));
        popup.setX(x - 210);
        popup.setY(y + 10);
        popup.show();
    }

    /**
     * Popup erreur mot non trouvé
     */
    private void afficherPopupErreur(String mot, double x, double y) {
        Stage popup = new Stage();
        popup.initModality(Modality.NONE);
        popup.setTitle("Mot non trouve");
        popup.setResizable(false);

        VBox root = new VBox(14);
        root.setAlignment(Pos.CENTER);
        root.setStyle(
                "-fx-background-color: white; -fx-padding: 28;" +
                        "-fx-min-width: 320;");

        Label icon = new Label("🔍");
        icon.setStyle("-fx-font-size: 36;");

        Label titre = new Label("Mot non trouvé");
        titre.setStyle(
                "-fx-font-size: 14; -fx-font-weight: bold;" +
                        "-fx-text-fill: #1a1f3c;");

        Label msg = new Label(
                "Aucune définition trouvée pour :\n\"" + mot + "\"");
        msg.setStyle(
                "-fx-font-size: 12; -fx-text-fill: #6b7280;" +
                        "-fx-text-alignment: center;");
        msg.setWrapText(true);
        msg.setAlignment(Pos.CENTER);

        Label conseil = new Label(
                "Conseil : essayez avec un seul mot simple");
        conseil.setStyle(
                "-fx-font-size: 10; -fx-text-fill: #9ca3af;" +
                        "-fx-font-style: italic;");

        HBox btnBox = new HBox(10);
        btnBox.setAlignment(Pos.CENTER);

        Button btnReessayer = new Button("Réessayer");
        btnReessayer.setStyle(
                "-fx-background-color: #4361ee; -fx-text-fill: white;" +
                        "-fx-font-weight: bold; -fx-font-size: 11;" +
                        "-fx-background-radius: 8; -fx-padding: 8 16;" +
                        "-fx-cursor: hand;");
        btnReessayer.setOnAction(e -> {
            popup.close();
            afficherDialogueSaisieMot(x, y);
        });

        Button btnFermer = new Button("Fermer");
        btnFermer.setStyle(
                "-fx-background-color: #f9fafb; -fx-text-fill: #6b7280;" +
                        "-fx-font-size: 11; -fx-border-color: #e5e7eb;" +
                        "-fx-border-radius: 8; -fx-background-radius: 8;" +
                        "-fx-padding: 8 16; -fx-cursor: hand;");
        btnFermer.setOnAction(e -> popup.close());

        btnBox.getChildren().addAll(btnReessayer, btnFermer);
        root.getChildren().addAll(icon, titre, msg, conseil, btnBox);

        popup.setScene(new Scene(root));
        popup.setX(x - 160);
        popup.setY(y + 10);
        popup.show();
    }


    // CRÉER BLOC QUESTION
    private VBox creerBlocQuestion(int numero, Question q) {
        VBox bloc = new VBox(12);
        bloc.setStyle(
                "-fx-background-color: white; -fx-background-radius: 12;" +
                        "-fx-padding: 20;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");

        // ── Header Q + pts ──
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label numLabel = new Label("Q" + numero);
        numLabel.setStyle(
                "-fx-background-color: #f5a623; -fx-text-fill: white;" +
                        "-fx-font-weight: bold; -fx-background-radius: 6;" +
                        "-fx-padding: 4 10; -fx-font-size: 12;");
        HBox spacerH = new HBox();
        HBox.setHgrow(spacerH, Priority.ALWAYS);
        Label ptsLabel = new Label(q.getPoints() + " pt"
                + (q.getPoints() > 1 ? "s" : ""));
        ptsLabel.setStyle(
                "-fx-background-color: #e3f2fd; -fx-text-fill: #1976d2;" +
                        "-fx-font-weight: bold; -fx-font-size: 11;" +
                        "-fx-background-radius: 6; -fx-padding: 4 10;");
        header.getChildren().addAll(numLabel, spacerH, ptsLabel);

        // ── Barre outils : Définir + Traduction ──
        HBox tradBox = new HBox(8);
        tradBox.setAlignment(Pos.CENTER_RIGHT);
        tradBox.setStyle("-fx-padding: 0 0 2 0;");

        // Bouton Définir un mot
        Button btnDefinir = new Button("📖 Définir un mot");
        btnDefinir.setStyle(
                "-fx-background-color: #eef2ff; -fx-text-fill: #4361ee;" +
                        "-fx-font-size: 11; -fx-font-weight: bold;" +
                        "-fx-background-radius: 6; -fx-border-color: #c7d2fe;" +
                        "-fx-border-radius: 6; -fx-border-width: 1;" +
                        "-fx-padding: 5 12; -fx-cursor: hand;");

        Label tradIcon = new Label("🌐");
        tradIcon.setStyle("-fx-font-size: 13;");

        ComboBox<String> langBox = new ComboBox<>();
        langBox.getItems().addAll("en", "ar", "es", "de", "it");
        langBox.setValue("en");
        langBox.setStyle(
                "-fx-font-size: 11; -fx-background-color: white;" +
                        "-fx-border-color: #dde1e7; -fx-border-radius: 6;" +
                        "-fx-background-radius: 6;");
        langBox.setPrefWidth(75);

        Button btnTrad = new Button("Traduire");
        btnTrad.setStyle(
                "-fx-background-color: #1a1f3c; -fx-text-fill: white;" +
                        "-fx-font-size: 11; -fx-font-weight: bold;" +
                        "-fx-background-radius: 6; -fx-padding: 5 12; -fx-cursor: hand;");
        btnTrad.setOnMouseEntered(e -> btnTrad.setStyle(
                "-fx-background-color: #f5a623; -fx-text-fill: white;" +
                        "-fx-font-size: 11; -fx-font-weight: bold;" +
                        "-fx-background-radius: 6; -fx-padding: 5 12; -fx-cursor: hand;"));
        btnTrad.setOnMouseExited(e -> btnTrad.setStyle(
                "-fx-background-color: #1a1f3c; -fx-text-fill: white;" +
                        "-fx-font-size: 11; -fx-font-weight: bold;" +
                        "-fx-background-radius: 6; -fx-padding: 5 12; -fx-cursor: hand;"));

        Button btnReset = new Button("Original");
        btnReset.setStyle(
                "-fx-background-color: #e9ecef; -fx-text-fill: #555;" +
                        "-fx-font-size: 11; -fx-background-radius: 6;" +
                        "-fx-padding: 5 10; -fx-cursor: hand;");

        tradBox.getChildren().addAll(
                btnDefinir, tradIcon, langBox, btnTrad, btnReset);

        // ── Texte question ──
        Label questionLabel = new Label(q.getQuestion());
        questionLabel.setStyle(
                "-fx-font-size: 14; -fx-font-weight: bold;" +
                        "-fx-text-fill: #1a1f3c;");
        questionLabel.setWrapText(true);

        // Activer double-clic + bouton définir
        activerDefinitionSurDoubleClick(questionLabel);
        btnDefinir.setOnAction(ev -> {
            double sx = btnDefinir.localToScreen(
                    btnDefinir.getBoundsInLocal()).getMinX();
            double sy = btnDefinir.localToScreen(
                    btnDefinir.getBoundsInLocal()).getMaxY();
            afficherDialogueSaisieMot(sx, sy);
        });

        // ── Réponses ──
        ToggleGroup group = new ToggleGroup();
        toggleGroups.put(q.getId(), group);

        VBox reponsesBox = new VBox(8);
        String[] lettres = {"A", "B", "C", "D"};
        String[] repOrig = new String[4];
        for (int i = 0; i < lettres.length; i++)
            repOrig[i] = extraireReponse(q.getReponses(), lettres[i]);

        for (int i = 0; i < lettres.length; i++) {
            String texte  = repOrig[i];
            String lettre = lettres[i];
            if (texte.isEmpty()) continue;

            HBox reponseBox = new HBox(12);
            reponseBox.setAlignment(Pos.CENTER_LEFT);
            reponseBox.setStyle(
                    "-fx-background-color: #f8f9fa;" +
                            "-fx-background-radius: 8; -fx-padding: 12 16;" +
                            "-fx-cursor: hand; -fx-border-color: #e9ecef;" +
                            "-fx-border-radius: 8; -fx-border-width: 1.5;");
            reponseBox.setMaxWidth(Double.MAX_VALUE);

            RadioButton rb = new RadioButton();
            rb.setToggleGroup(group);
            rb.setUserData(lettre);

            Label lettreLabel = new Label(lettre + ".");
            lettreLabel.setStyle(
                    "-fx-font-size: 12; -fx-font-weight: bold;" +
                            "-fx-text-fill: #888; -fx-min-width: 18;");

            Label texteLabel = new Label(texte);
            texteLabel.setStyle(
                    "-fx-font-size: 13; -fx-text-fill: #333;");
            texteLabel.setWrapText(true);
            HBox.setHgrow(texteLabel, Priority.ALWAYS);

            reponseBox.getChildren().addAll(rb, lettreLabel, texteLabel);

            reponseBox.setOnMouseClicked(ev -> {
                rb.setSelected(true);
                reponsesBox.getChildren().forEach(node -> {
                    if (node instanceof HBox h)
                        h.setStyle(
                                "-fx-background-color: #f8f9fa;" +
                                        "-fx-background-radius: 8; -fx-padding: 12 16;" +
                                        "-fx-cursor: hand; -fx-border-color: #e9ecef;" +
                                        "-fx-border-radius: 8; -fx-border-width: 1.5;");
                });
                reponseBox.setStyle(
                        "-fx-background-color: #fff8ee;" +
                                "-fx-background-radius: 8; -fx-padding: 12 16;" +
                                "-fx-cursor: hand; -fx-border-color: #f5a623;" +
                                "-fx-border-radius: 8; -fx-border-width: 2;");
            });

            reponseBox.setOnMouseEntered(ev -> {
                if (group.getSelectedToggle() != rb)
                    reponseBox.setStyle(
                            "-fx-background-color: #f0f4ff;" +
                                    "-fx-background-radius: 8; -fx-padding: 12 16;" +
                                    "-fx-cursor: hand; -fx-border-color: #b0c4de;" +
                                    "-fx-border-radius: 8; -fx-border-width: 1.5;");
            });

            reponseBox.setOnMouseExited(ev -> {
                if (group.getSelectedToggle() != rb)
                    reponseBox.setStyle(
                            "-fx-background-color: #f8f9fa;" +
                                    "-fx-background-radius: 8; -fx-padding: 12 16;" +
                                    "-fx-cursor: hand; -fx-border-color: #e9ecef;" +
                                    "-fx-border-radius: 8; -fx-border-width: 1.5;");
            });

            reponsesBox.getChildren().add(reponseBox);
        }

        // ── Listener progression ──
        //compte combien de togglegroups ont une rep selectionnee
        group.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            long nb = toggleGroups.values().stream()
                    .filter(tg -> tg.getSelectedToggle() != null).count();
            progressLabel.setText(nb + " / "
                    + questions.size() + " répondu(es)");//mettre a jour progress bar
            double progress = (double) nb / questions.size();
            progressBar.setProgress(progress);
            if (progress < 0.5)
                progressBar.setStyle("-fx-accent: #e74c3c;");
            else if (progress < 1.0)
                progressBar.setStyle("-fx-accent: #f39c12;");
            else
                progressBar.setStyle("-fx-accent: #2ecc71;");
        });

        final String   questionOriginale = q.getQuestion();
        final String[] repOrigFinal      = Arrays.copyOf(repOrig, 4);

        // ── Action Traduire ──
        btnTrad.setOnAction(ev -> {
            String lang = langBox.getValue();
            btnTrad.setText("...");
            btnTrad.setDisable(true);
            //lance traduction dans un thread separer
            new Thread(() -> {
                try {
                    String   qTrad   = traduire(questionOriginale, lang);
                    String[] repTrad = new String[4];
                    for (int i = 0; i < 4; i++)
                        repTrad[i] = repOrigFinal[i].isEmpty()
                                ? "" : traduire(repOrigFinal[i], lang);

                    Platform.runLater(() -> {
                        questionLabel.setText(qTrad);
                        int idx = 0;
                        for (javafx.scene.Node node : reponsesBox.getChildren()) {
                            if (node instanceof HBox hb && idx < 4) {
                                for (javafx.scene.Node child : hb.getChildren()) {
                                    if (child instanceof Label lbl
                                            && lbl.getStyle().contains("#333")
                                            && !repTrad[idx].isEmpty())
                                        lbl.setText(repTrad[idx]);
                                }
                                idx++;
                            }
                        }
                        btnTrad.setText("Traduire");
                        btnTrad.setDisable(false);
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        btnTrad.setText("Erreur");
                        btnTrad.setDisable(false);
                    });
                    ex.printStackTrace();
                }
            }).start();
        });

        // ── Action Reset ──
        btnReset.setOnAction(ev -> {
            questionLabel.setText(questionOriginale);
            int idx = 0;
            for (javafx.scene.Node node : reponsesBox.getChildren()) {
                if (node instanceof HBox hb && idx < 4) {
                    for (javafx.scene.Node child : hb.getChildren()) {
                        if (child instanceof Label lbl
                                && lbl.getStyle().contains("#333")
                                && !repOrigFinal[idx].isEmpty())
                            lbl.setText(repOrigFinal[idx]);
                    }
                    idx++;
                }
            }
        });

        bloc.getChildren().addAll(
                header, tradBox, questionLabel, reponsesBox);
        return bloc;
    }


    // SOUMETTRE

    @FXML
    private void soumettreQuiz() {
        // Arrête le timer dès la soumission
        if (timer != null) timer.stop();
//vérifie que toutes les questions ont une réponse
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
//calcul du score
        int totalPoints  = 0;
        int pointsObtenu = 0;
        Map<Integer, Boolean> resultats = new HashMap<>();

        for (Question q : questions) {
            totalPoints += q.getPoints();
            // Récupère la lettre choisie
            String  choix   = (String) toggleGroups.get(q.getId())
                    .getSelectedToggle().getUserData();
            String  bonne   = extraireCorrecte(q.getReponses());
            boolean correct = choix.equals(bonne);
            if (correct) pointsObtenu += q.getPoints();//si correct plus de point
            resultats.put(q.getId(), correct);
        }
// SCORE EN % : (points obtenus / total) × 100
        float   scorePercent = totalPoints > 0
                ? (float) pointsObtenu / totalPoints * 100f : 0f;
        // RÉUSSITE : score >= score minimum requis du quiz
        boolean reussi = scorePercent >= quiz.getScoreMinimum();
//badge
        String badge = scorePercent >= 90 ? "Or"
                : scorePercent >= 70 ? "Argent"
                : scorePercent >= 50 ? "Bronze" : "";

        try {
            certifService.ajouter(new Certification(
                    scorePercent,
                    reussi ? "Réussi" : "Échoué",
                    badge,
                    Timestamp.valueOf(LocalDateTime.now()),
                    USER_ID, quiz.getId()));
        } catch (Exception e) { e.printStackTrace(); }

        afficherFeedbackQuestions(resultats);
        soumettreBtn.setVisible(false);
        soumettreBtn.setManaged(false);
        afficherPopupResultat(reussi, scorePercent,
                pointsObtenu, totalPoints, badge);
    }


    // FEEDBACK

    private void afficherFeedbackQuestions(
            Map<Integer, Boolean> resultats) {
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            boolean correct      = resultats.getOrDefault(q.getId(), false);
            String  bonneLettre  = extraireCorrecte(q.getReponses());
            String  bonneTexte   = extraireReponse(q.getReponses(), bonneLettre);

            if (questionsContainer.getChildren().get(i) instanceof VBox bloc) {
                if (bloc.getChildren().size() >= 4
                        && bloc.getChildren().get(3) instanceof VBox repBox) {
                    String[] lettres = {"A","B","C","D"};
                    for (int j = 0; j < repBox.getChildren().size(); j++) {
                        if (repBox.getChildren().get(j) instanceof HBox rb) {
                            String  lettre     = j < lettres.length ? lettres[j] : "";
                            boolean estCorrecte = lettre.equals(bonneLettre);
                            boolean estChoisie  =
                                    toggleGroups.get(q.getId()).getSelectedToggle() != null
                                            && lettre.equals((String) toggleGroups.get(q.getId())
                                            .getSelectedToggle().getUserData());
                            if (estCorrecte)
                                rb.setStyle(
                                        "-fx-background-color: #e8f5e9;" +
                                                "-fx-background-radius: 8; -fx-padding: 12 16;" +
                                                "-fx-border-color: #2ecc71;" +
                                                "-fx-border-radius: 8; -fx-border-width: 2;");
                            else if (estChoisie)
                                rb.setStyle(
                                        "-fx-background-color: #fdecea;" +
                                                "-fx-background-radius: 8; -fx-padding: 12 16;" +
                                                "-fx-border-color: #e74c3c;" +
                                                "-fx-border-radius: 8; -fx-border-width: 2;");
                            rb.setDisable(true);
                        }
                    }
                }

                VBox feedback = new VBox(4);
                if (correct) {
                    feedback.setStyle(
                            "-fx-background-color: #e8f5e9;" +
                                    "-fx-background-radius: 8; -fx-padding: 10;" +
                                    "-fx-border-color: #2ecc71; -fx-border-radius: 8;" +
                                    "-fx-border-width: 1;");
                    Label fl = new Label("Bonne réponse !");
                    fl.setStyle(
                            "-fx-font-size: 12; -fx-font-weight: bold;" +
                                    "-fx-text-fill: #2ecc71;");
                    feedback.getChildren().add(fl);
                } else {
                    feedback.setStyle(
                            "-fx-background-color: #fdecea;" +
                                    "-fx-background-radius: 8; -fx-padding: 10;" +
                                    "-fx-border-color: #e74c3c; -fx-border-radius: 8;" +
                                    "-fx-border-width: 1;");
                    Label fl = new Label("Mauvaise réponse !");
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


    // POPUP RÉSULTAT

    private void afficherPopupResultat(boolean reussi, float score,
                                       int points, int total, String badge) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle(reussi ? "Felicitations" : "Quiz termine");
        popup.setResizable(false);

        VBox root = new VBox(18);
        root.setAlignment(Pos.CENTER);
        root.setStyle(
                "-fx-background-color: white; -fx-padding: 40;" +
                        "-fx-min-width: 420;");

        Label icone = new Label(reussi ? "🎊" : "😔");
        icone.setStyle("-fx-font-size: 55;");
        Label titre = new Label(reussi ? "Felicitations !" : "Quiz termine");
        titre.setStyle(
                "-fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: "
                        + (reussi ? "#1a1f3c" : "#e74c3c") + ";");
        Label scoreLabel = new Label(points + " / " + total + " pts");
        scoreLabel.setStyle(
                "-fx-font-size: 36; -fx-font-weight: bold; -fx-text-fill: "
                        + (reussi ? "#f5a623" : "#e74c3c") + ";");
        Label statutLabel = new Label(
                reussi ? "Statut : Reussi" : "Statut : Echoue");
        statutLabel.setStyle(
                "-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: "
                        + (reussi ? "#2ecc71" : "#e74c3c") + ";");
        Label scoreMinLabel = new Label(
                "Score minimum requis : " + (int) quiz.getScoreMinimum() + "%");
        scoreMinLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #888;");

        root.getChildren().addAll(
                icone, titre, scoreLabel, statutLabel, scoreMinLabel);

        if (reussi) {
            VBox badgeBox = new VBox(5);
            badgeBox.setAlignment(Pos.CENTER);
            badgeBox.setStyle(
                    "-fx-background-color: #fff8ee;" +
                            "-fx-background-radius: 12; -fx-padding: 15 30;" +
                            "-fx-border-color: #f5a623; -fx-border-radius: 12;" +
                            "-fx-border-width: 2;");
            Label badgeTitre = new Label("Badge obtenu");
            badgeTitre.setStyle("-fx-font-size: 11; -fx-text-fill: #888;");
            Label badgeVal = new Label(badge.isEmpty() ? "Aucun" : badge);
            badgeVal.setStyle(
                    "-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: #f5a623;");
            badgeBox.getChildren().addAll(badgeTitre, badgeVal);
            root.getChildren().add(badgeBox);
        } else {
            Label encour = new Label(
                    "Ne vous decouragez pas ! Vous pouvez reessayer.");
            encour.setStyle(
                    "-fx-font-size: 13; -fx-text-fill: #555;" +
                            "-fx-font-style: italic;");
            encour.setWrapText(true);
            encour.setAlignment(Pos.CENTER);
            root.getChildren().add(encour);
        }

        Label certifLabel = new Label("Tentative enregistree !");
        certifLabel.setStyle(
                "-fx-font-size: 12; -fx-text-fill: #2ecc71;" +
                        "-fx-font-weight: bold;");
        root.getChildren().add(certifLabel);

        HBox btnBox = new HBox(12);
        btnBox.setAlignment(Pos.CENTER);

        Button btnRetour = new Button("Retour au cours");
        btnRetour.setStyle(
                "-fx-background-color: #1a1f3c; -fx-text-fill: white;" +
                        "-fx-font-weight: bold; -fx-font-size: 13;" +
                        "-fx-background-radius: 10; -fx-padding: 11 20; -fx-cursor: hand;");
        btnRetour.setOnAction(e -> { popup.close(); retourCours(); });

        Button btnAccueil = new Button("Accueil");
        btnAccueil.setStyle(
                "-fx-background-color: #f5a623; -fx-text-fill: white;" +
                        "-fx-font-weight: bold; -fx-font-size: 13;" +
                        "-fx-background-radius: 10; -fx-padding: 11 20; -fx-cursor: hand;");
        btnAccueil.setOnAction(e -> { popup.close(); retourAccueil(); });

        btnBox.getChildren().addAll(btnRetour, btnAccueil);
        root.getChildren().add(btnBox);

        popup.setScene(new Scene(root));
        popup.show();

        Platform.runLater(() -> {
            Stage parent = (Stage) questionsContainer.getScene().getWindow();
            popup.setX(parent.getX() +
                    (parent.getWidth()  - popup.getWidth())  / 2);
            popup.setY(parent.getY() +
                    (parent.getHeight() - popup.getHeight()) / 2);
        });
    }


    // NAVIGATION

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
        } catch (Exception e) { e.printStackTrace(); }
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
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void retourCatalogue() {
        if (timer != null) timer.stop();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/org/example/AccueilEtudiant.fxml"));
            Stage stage = (Stage) questionsContainer.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Espace Etudiant — Catalogue");
        } catch (Exception e) { e.printStackTrace(); }
    }


    // JSON HELPERS

    private String extraireReponse(String json, String lettre) {
        if (json == null || json.isEmpty()) return "";
        try {
            String[] parts = json.replace("[","").replace("]","").split("},");
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
            String[] parts   = json.replace("[","").replace("]","").split("},");
            String[] lettres = {"A","B","C","D"};
            for (int i = 0; i < parts.length; i++)
                if (parts[i].contains("\"correct\":true")) return lettres[i];
        } catch (Exception e) { return ""; }
        return "";
    }
}