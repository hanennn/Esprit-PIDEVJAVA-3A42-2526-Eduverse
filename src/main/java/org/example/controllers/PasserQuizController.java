package org.example.controllers;

// import org.example.utils.LiveCameraPanel;
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
import org.example.entities.Session;
import org.example.services.CertificationService;
import org.example.services.CheatDetectorService;
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

    private static final int MAX_CHEATS = 2;
    private final QuestionService      questionService = new QuestionService();
    private final CertificationService certifService   = new CertificationService();
    // private LiveCameraPanel liveCamera;

    private Quiz   quiz;
    private int    coursId;
    private String coursTitre;
    private String coursNiveau;
    private String coursMatiere;
    private String coursLangue;
    private String coursDescription;

    private final List<Question>            questions    = new ArrayList<>();
    private final Map<Integer, ToggleGroup> toggleGroups = new HashMap<>();

    private Timeline timer;
    private int      secondesRestantes;

    private CheatDetectorService cheatDetector;
    private static final String PYTHON_EXE  = findPython();
    private static final String SCRIPT_PATH = findScript();

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
        //startCheatDetection();

    }

    /*
    private void startCheatDetection() {
        liveCamera = new LiveCameraPanel(MAX_CHEATS);

        if (questionsContainer.getParent() instanceof HBox parentHBox) {
            parentHBox.getChildren().add(liveCamera);
        } else {
            javafx.scene.Parent oldParent = questionsContainer.getParent();
            if (oldParent instanceof Pane pane) {
                int idx = pane.getChildren().indexOf(questionsContainer);
                pane.getChildren().remove(questionsContainer);
                HBox wrapper = new HBox(16);
                HBox.setHgrow(questionsContainer, Priority.ALWAYS);
                wrapper.getChildren().addAll(questionsContainer, liveCamera);
                pane.getChildren().add(idx, wrapper);
            }
        }

        cheatDetector = new CheatDetectorService(
                PYTHON_EXE, SCRIPT_PATH,
                warningMsg -> {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Attention");
                    alert.setHeaderText(null);
                    alert.setContentText(warningMsg);
                    alert.initModality(Modality.NONE);
                    alert.show();
                    timerLabel.setStyle(
                            "-fx-font-size: 16; -fx-font-weight: bold;" +
                            "-fx-text-fill: white; -fx-background-color: #e74c3c;" +
                            "-fx-background-radius: 8; -fx-padding: 5 16;");
                },
                cheatCount -> {
                    if (timer != null) timer.stop();
                    if (liveCamera != null) liveCamera.showStopped();
                    try {
                        certifService.ajouter(new Certification(
                                0f, "Triche", "",
                                Timestamp.valueOf(LocalDateTime.now()),
                                Session.getCurrentUser().getId(),
                                quiz.getId()));
                    } catch (Exception e) { e.printStackTrace(); }
                    waitForSceneAndNavigate();
                },
                errorMsg -> System.err.println("[CheatDetector] " + errorMsg),
                frameEvent -> { if (liveCamera != null) liveCamera.onFrame(frameEvent); }
        );
        cheatDetector.start();
    }

    private void waitForSceneAndNavigate() {
        if (questionsContainer.getScene() != null &&
                questionsContainer.getScene().getWindow() != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "/org/example/CoursDetailEtudiant.fxml"));
                Stage stage = (Stage) questionsContainer.getScene().getWindow();
                stage.setScene(new Scene(loader.load()));
                CoursDetailEtudiantController ctrl = loader.getController();
                ctrl.setCours(coursId, coursTitre, coursNiveau,
                        coursMatiere, coursLangue, coursDescription);
            } catch (Exception e) { e.printStackTrace(); }
        } else {
            new Thread(() -> {
                try { Thread.sleep(200); } catch (InterruptedException ignored) {}
                Platform.runLater(this::waitForSceneAndNavigate);
            }).start();
        }
    }
    */

    private void chargerQuestions() {
        questionsContainer.getChildren().clear();
        toggleGroups.clear();
        questions.clear();

        try {
            for (Question q : questionService.afficher())
                if (q.getQuizId() == quiz.getId()) questions.add(q);
        } catch (Exception e) { e.printStackTrace(); }

        progressLabel.setText("0 / " + questions.size() + " répondu(es)");
        progressBar.setProgress(0);

        if (questions.isEmpty()) {
            Label vide = new Label("Aucune question disponible.");
            vide.setStyle("-fx-text-fill: #888; -fx-font-size: 14;");
            questionsContainer.getChildren().add(vide);
            soumettreBtn.setDisable(true);
            return;
        }

        for (int i = 0; i < questions.size(); i++)
            questionsContainer.getChildren().add(creerBlocQuestion(i + 1, questions.get(i)));
    }

    private void demarrerTimer() {
        secondesRestantes = quiz.getDuree() * 60;
        mettreAJourAffichageTimer();

        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            secondesRestantes--;
            mettreAJourAffichageTimer();
            if (secondesRestantes <= 0) {
                timer.stop();
                Platform.runLater(this::soumettreQuiz);
            }
        }));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }

    private void mettreAJourAffichageTimer() {
        int min = secondesRestantes / 60;
        int sec = secondesRestantes % 60;
        timerLabel.setText(String.format("⏱ %02d:%02d", min, sec));

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

    private String traduire(String texte, String langCible) {
        try {
            String encoded  = URLEncoder.encode(texte, StandardCharsets.UTF_8);
            String langPair = URLEncoder.encode("fr|" + langCible, StandardCharsets.UTF_8);
            String url = "https://api.mymemory.translated.net/get?q=" + encoded + "&langpair=" + langPair;

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.body());
            int status = root.path("responseStatus").asInt();
            if (status == 200) {
                String t = root.path("responseData").path("translatedText").asText();
                if (!t.isEmpty() && !t.equalsIgnoreCase("INVALID LANGUAGE PAIR")) return t;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return texte;
    }

    private String traduireMotFrVersEn(String motFr) {
        try {
            String motNettoye = motFr.trim().replaceAll("[^a-zA-ZÀ-ÿ\\s]", "").toLowerCase();
            if (motNettoye.isEmpty()) return motFr;

            String encoded  = URLEncoder.encode(motNettoye, StandardCharsets.UTF_8);
            String langPair = URLEncoder.encode("fr|en", StandardCharsets.UTF_8);
            String url = "https://api.mymemory.translated.net/get?q=" + encoded + "&langpair=" + langPair;

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.body());
            int status = root.path("responseStatus").asInt();
            if (status == 200) {
                String t = root.path("responseData").path("translatedText").asText();
                if (!t.isEmpty() && !t.equalsIgnoreCase("INVALID LANGUAGE PAIR"))
                    return t.toLowerCase().trim();
            }
        } catch (Exception e) { e.printStackTrace(); }
        return motFr;
    }

    private String traduireEnVersFr(String texteEn) {
        try {
            if (texteEn == null || texteEn.trim().isEmpty()) return texteEn;

            String encoded  = URLEncoder.encode(texteEn, StandardCharsets.UTF_8);
            String langPair = URLEncoder.encode("en|fr", StandardCharsets.UTF_8);
            String url = "https://api.mymemory.translated.net/get?q=" + encoded + "&langpair=" + langPair;

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.body());
            int status = root.path("responseStatus").asInt();
            if (status == 200) {
                String t = root.path("responseData").path("translatedText").asText();
                if (!t.isEmpty() && !t.equalsIgnoreCase("INVALID LANGUAGE PAIR")) return t;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return texteEn;
    }

    private String[] getDefinitionEtExemple(String motEn) {
        try {
            String motNettoye = motEn.trim().replaceAll("[^a-zA-Z\\s]", "").toLowerCase().trim();
            if (motNettoye.isEmpty()) return new String[]{"", "", "", "", ""};

            String url = "https://api.dictionaryapi.dev/api/v2/entries/en/"
                    + URLEncoder.encode(motNettoye, StandardCharsets.UTF_8);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) return new String[]{"", "", "", "", ""};

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.body());
            if (!root.isArray() || root.isEmpty()) return new String[]{"", "", "", "", ""};

            JsonNode meanings = root.get(0).path("meanings");
            if (meanings.isEmpty()) return new String[]{"", "", "", "", ""};

            JsonNode meaning      = meanings.get(0);
            String   partOfSpeech = meaning.path("partOfSpeech").asText();
            JsonNode definitions  = meaning.path("definitions");
            if (definitions.isEmpty()) return new String[]{"", "", "", "", partOfSpeech};

            JsonNode def0      = definitions.get(0);
            String   defEn     = def0.path("definition").asText();
            String   exempleEn = def0.path("example").asText();
            String   defFr     = defEn.isEmpty()     ? "" : traduireEnVersFr(defEn);
            String   exempleFr = exempleEn.isEmpty() ? "" : traduireEnVersFr(exempleEn);

            return new String[]{defFr, defEn, exempleEn, exempleFr, partOfSpeech};
        } catch (Exception e) {
            e.printStackTrace();
            return new String[]{"", "", "", "", ""};
        }
    }

    private void activerDefinitionSurDoubleClick(Label questionLabel) {
        questionLabel.setOnMouseClicked(ev -> {
            if (ev.getClickCount() == 2)
                afficherDialogueSaisieMot(ev.getScreenX(), ev.getScreenY());
        });
        Tooltip tip = new Tooltip("Double-cliquez pour définir un mot");
        tip.setStyle("-fx-font-size: 10; -fx-background-color: #1a1f3c; -fx-text-fill: white; -fx-background-radius: 6;");
        Tooltip.install(questionLabel, tip);
    }

    private void afficherDialogueSaisieMot(double x, double y) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Définir un mot");
        dialog.setResizable(false);

        VBox root = new VBox(16);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: white; -fx-padding: 28; -fx-min-width: 340;");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        StackPane iconBox = new StackPane();
        iconBox.setStyle("-fx-background-color: #eef2ff; -fx-background-radius: 8; -fx-padding: 8 10;");
        Label icon = new Label("📖");
        icon.setStyle("-fx-font-size: 18;");
        iconBox.getChildren().add(icon);
        VBox titreBox = new VBox(2);
        Label titre = new Label("Définir un mot");
        titre.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #1a1f3c;");
        Label sousTitre = new Label("Entrez le mot à définir (fr ou en)");
        sousTitre.setStyle("-fx-font-size: 10; -fx-text-fill: #888;");
        titreBox.getChildren().addAll(titre, sousTitre);
        header.getChildren().addAll(iconBox, titreBox);

        TextField motField = new TextField();
        motField.setPromptText("Ex: algorithme, boucle, variable...");
        motField.setStyle("-fx-background-color: #f9fafb; -fx-border-color: #e5e7eb; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10; -fx-font-size: 12;");

        HBox btnBox = new HBox(10);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        Button btnAnnuler = new Button("Annuler");
        btnAnnuler.setStyle("-fx-background-color: #f9fafb; -fx-text-fill: #6b7280; -fx-font-size: 12; -fx-border-color: #e5e7eb; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;");
        btnAnnuler.setOnAction(e -> dialog.close());

        Button btnDefinir = new Button("Rechercher");
        btnDefinir.setStyle("-fx-background-color: #4361ee; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12; -fx-background-radius: 8; -fx-padding: 8 20; -fx-cursor: hand;");

        Runnable action = () -> {
            String mot = motField.getText().trim();
            if (!mot.isEmpty()) { dialog.close(); afficherDefinition(mot, x, y); }
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

    private void afficherDefinition(String mot, double x, double y) {
        Stage loadingStage = new Stage();
        loadingStage.initModality(Modality.NONE);
        loadingStage.setTitle("Recherche...");
        loadingStage.setResizable(false);

        VBox loadingRoot = new VBox(12);
        loadingRoot.setAlignment(Pos.CENTER);
        loadingRoot.setStyle("-fx-background-color: white; -fx-padding: 30;");
        Label loadingIcon = new Label("🔍");
        loadingIcon.setStyle("-fx-font-size: 28;");
        Label loadingTxt = new Label("Recherche de la définition...");
        loadingTxt.setStyle("-fx-font-size: 12; -fx-text-fill: #6b7280;");
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
                String motAnglais = traduireMotFrVersEn(mot);
                String[] result = getDefinitionEtExemple(motAnglais);
                String defFr = result[0], defEn = result[1], exempleEn = result[2],
                        exempleFr = result[3], partOfSpeech = result[4];

                Platform.runLater(() -> {
                    loadingStage.close();
                    if (defFr.isEmpty() && defEn.isEmpty()) afficherPopupErreur(mot, x, y);
                    else afficherPopupDefinition(mot, motAnglais, partOfSpeech, defFr, defEn, exempleFr, exempleEn, x, y);
                });
            } catch (Exception e) {
                Platform.runLater(() -> { loadingStage.close(); afficherPopupErreur(mot, x, y); });
                e.printStackTrace();
            }
        }).start();
    }

    private void afficherPopupDefinition(String motOriginal, String motAnglais, String partOfSpeech,
                                         String defFr, String defEn, String exempleFr, String exempleEn,
                                         double x, double y) {
        Stage popup = new Stage();
        popup.initModality(Modality.NONE);
        popup.setTitle("Definition");
        popup.setResizable(false);

        VBox root = new VBox(14);
        root.setStyle("-fx-background-color: white; -fx-padding: 24; -fx-min-width: 420; -fx-max-width: 460;");

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        StackPane iconBox = new StackPane();
        iconBox.setStyle("-fx-background-color: #eef2ff; -fx-background-radius: 10; -fx-padding: 10 12;");
        Label iconLbl = new Label("📖");
        iconLbl.setStyle("-fx-font-size: 22;");
        iconBox.getChildren().add(iconLbl);
        VBox titreBox = new VBox(4);
        Label motLabel = new Label(motOriginal.toUpperCase());
        motLabel.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: #1a1f3c;");
        HBox subBox = new HBox(8);
        subBox.setAlignment(Pos.CENTER_LEFT);
        if (!motAnglais.equalsIgnoreCase(motOriginal)) {
            Label tradLabel = new Label(motAnglais);
            tradLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #4361ee; -fx-font-style: italic;");
            subBox.getChildren().add(tradLabel);
        }
        if (!partOfSpeech.isEmpty()) {
            Label posLabel = new Label(partOfSpeech);
            posLabel.setStyle("-fx-background-color: #f0fdf4; -fx-text-fill: #16a34a; -fx-font-size: 10; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 2 8;");
            subBox.getChildren().add(posLabel);
        }
        titreBox.getChildren().addAll(motLabel, subBox);
        header.getChildren().addAll(iconBox, titreBox);

        HBox sep = new HBox();
        sep.setStyle("-fx-background-color: linear-gradient(to right, #4361ee, #818cf8, transparent); -fx-min-height: 2; -fx-background-radius: 1;");

        VBox defFrBox = new VBox(8);
        defFrBox.setStyle("-fx-background-color: #f8f9ff; -fx-background-radius: 10; -fx-padding: 14; -fx-border-color: #e0e7ff; -fx-border-radius: 10; -fx-border-width: 1;");
        HBox defFrHeader = new HBox(6);
        defFrHeader.setAlignment(Pos.CENTER_LEFT);
        Label flagFr = new Label("🇫🇷");
        flagFr.setStyle("-fx-font-size: 13;");
        Label defFrTitre = new Label("DÉFINITION EN FRANÇAIS");
        defFrTitre.setStyle("-fx-font-size: 9; -fx-font-weight: bold; -fx-text-fill: #4361ee;");
        defFrHeader.getChildren().addAll(flagFr, defFrTitre);
        Label defFrLabel = new Label(defFr.isEmpty() ? "Traduction non disponible pour \"" + motOriginal + "\"" : defFr);
        defFrLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #1a1f3c; -fx-line-spacing: 2;");
        defFrLabel.setWrapText(true);
        defFrLabel.setMaxWidth(400);
        defFrBox.getChildren().addAll(defFrHeader, defFrLabel);
        if (!exempleFr.isEmpty()) {
            HBox exFrBox = new HBox(6);
            exFrBox.setAlignment(Pos.CENTER_LEFT);
            Label exFrIcon = new Label("💬");
            exFrIcon.setStyle("-fx-font-size: 11;");
            Label exFrLabel = new Label(exempleFr);
            exFrLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #6b7280; -fx-font-style: italic;");
            exFrLabel.setWrapText(true);
            exFrLabel.setMaxWidth(370);
            HBox.setHgrow(exFrLabel, Priority.ALWAYS);
            exFrBox.getChildren().addAll(exFrIcon, exFrLabel);
            defFrBox.getChildren().add(exFrBox);
        }
        root.getChildren().addAll(header, sep, defFrBox);

        if (!defEn.isEmpty()) {
            VBox defEnBox = new VBox(8);
            defEnBox.setStyle("-fx-background-color: #f0fdf4; -fx-background-radius: 10; -fx-padding: 14; -fx-border-color: #bbf7d0; -fx-border-radius: 10; -fx-border-width: 1;");
            HBox defEnHeader = new HBox(6);
            defEnHeader.setAlignment(Pos.CENTER_LEFT);
            Label flagEn = new Label("🇬🇧");
            flagEn.setStyle("-fx-font-size: 13;");
            Label defEnTitre = new Label("DEFINITION IN ENGLISH");
            defEnTitre.setStyle("-fx-font-size: 9; -fx-font-weight: bold; -fx-text-fill: #16a34a;");
            defEnHeader.getChildren().addAll(flagEn, defEnTitre);
            Label defEnLabel = new Label(defEn);
            defEnLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #374151; -fx-font-style: italic;");
            defEnLabel.setWrapText(true);
            defEnLabel.setMaxWidth(400);
            defEnBox.getChildren().addAll(defEnHeader, defEnLabel);
            if (!exempleEn.isEmpty()) {
                HBox exEnBox = new HBox(6);
                exEnBox.setAlignment(Pos.CENTER_LEFT);
                Label exEnIcon = new Label("💬");
                exEnIcon.setStyle("-fx-font-size: 11;");
                Label exEnLabel = new Label(exempleEn);
                exEnLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #6b7280; -fx-font-style: italic;");
                exEnLabel.setWrapText(true);
                exEnLabel.setMaxWidth(370);
                HBox.setHgrow(exEnLabel, Priority.ALWAYS);
                exEnBox.getChildren().addAll(exEnIcon, exEnLabel);
                defEnBox.getChildren().add(exEnBox);
            }
            root.getChildren().add(defEnBox);
        }

        Label source = new Label("Source : Free Dictionary API + MyMemory Translation");
        source.setStyle("-fx-font-size: 9; -fx-text-fill: #d1d5db;");
        HBox btnBox = new HBox(10);
        btnBox.setAlignment(Pos.CENTER_RIGHT);
        Button btnNouveau = new Button("Autre mot");
        btnNouveau.setStyle("-fx-background-color: #f9fafb; -fx-text-fill: #4361ee; -fx-font-weight: bold; -fx-font-size: 11; -fx-background-radius: 8; -fx-border-color: #e0e7ff; -fx-border-radius: 8; -fx-border-width: 1; -fx-padding: 8 16; -fx-cursor: hand;");
        btnNouveau.setOnAction(e -> { popup.close(); afficherDialogueSaisieMot(x, y); });
        Button btnFermer = new Button("Fermer");
        btnFermer.setStyle("-fx-background-color: #4361ee; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12; -fx-background-radius: 8; -fx-padding: 8 24; -fx-cursor: hand;");
        btnFermer.setOnAction(e -> popup.close());
        btnBox.getChildren().addAll(btnNouveau, btnFermer);
        root.getChildren().addAll(source, btnBox);

        popup.setScene(new Scene(root));
        popup.setX(x - 210);
        popup.setY(y + 10);
        popup.show();
    }

    private void afficherPopupErreur(String mot, double x, double y) {
        Stage popup = new Stage();
        popup.initModality(Modality.NONE);
        popup.setTitle("Mot non trouve");
        popup.setResizable(false);

        VBox root = new VBox(14);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: white; -fx-padding: 28; -fx-min-width: 320;");

        Label icon = new Label("🔍");
        icon.setStyle("-fx-font-size: 36;");
        Label titre = new Label("Mot non trouvé");
        titre.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #1a1f3c;");
        Label msg = new Label("Aucune définition trouvée pour :\n\"" + mot + "\"");
        msg.setStyle("-fx-font-size: 12; -fx-text-fill: #6b7280; -fx-text-alignment: center;");
        msg.setWrapText(true);
        msg.setAlignment(Pos.CENTER);
        Label conseil = new Label("Conseil : essayez avec un seul mot simple");
        conseil.setStyle("-fx-font-size: 10; -fx-text-fill: #9ca3af; -fx-font-style: italic;");

        HBox btnBox = new HBox(10);
        btnBox.setAlignment(Pos.CENTER);
        Button btnReessayer = new Button("Réessayer");
        btnReessayer.setStyle("-fx-background-color: #4361ee; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;");
        btnReessayer.setOnAction(e -> { popup.close(); afficherDialogueSaisieMot(x, y); });
        Button btnFermer = new Button("Fermer");
        btnFermer.setStyle("-fx-background-color: #f9fafb; -fx-text-fill: #6b7280; -fx-font-size: 11; -fx-border-color: #e5e7eb; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;");
        btnFermer.setOnAction(e -> popup.close());
        btnBox.getChildren().addAll(btnReessayer, btnFermer);
        root.getChildren().addAll(icon, titre, msg, conseil, btnBox);

        popup.setScene(new Scene(root));
        popup.setX(x - 160);
        popup.setY(y + 10);
        popup.show();
    }

    private VBox creerBlocQuestion(int numero, Question q) {
        VBox bloc = new VBox(12);
        bloc.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label numLabel = new Label("Q" + numero);
        numLabel.setStyle("-fx-background-color: #f5a623; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 4 10; -fx-font-size: 12;");
        HBox spacerH = new HBox();
        HBox.setHgrow(spacerH, Priority.ALWAYS);
        Label ptsLabel = new Label(q.getPoints() + " pt" + (q.getPoints() > 1 ? "s" : ""));
        ptsLabel.setStyle("-fx-background-color: #e3f2fd; -fx-text-fill: #1976d2; -fx-font-weight: bold; -fx-font-size: 11; -fx-background-radius: 6; -fx-padding: 4 10;");
        header.getChildren().addAll(numLabel, spacerH, ptsLabel);

        HBox tradBox = new HBox(8);
        tradBox.setAlignment(Pos.CENTER_RIGHT);
        tradBox.setStyle("-fx-padding: 0 0 2 0;");

        Button btnDefinir = new Button("📖 Définir un mot");
        btnDefinir.setStyle("-fx-background-color: #eef2ff; -fx-text-fill: #4361ee; -fx-font-size: 11; -fx-font-weight: bold; -fx-background-radius: 6; -fx-border-color: #c7d2fe; -fx-border-radius: 6; -fx-border-width: 1; -fx-padding: 5 12; -fx-cursor: hand;");

        Label tradIcon = new Label("🌐");
        tradIcon.setStyle("-fx-font-size: 13;");

        ComboBox<String> langBox = new ComboBox<>();
        langBox.getItems().addAll("en", "ar", "es", "de", "it");
        langBox.setValue("en");
        langBox.setStyle("-fx-font-size: 11; -fx-background-color: white; -fx-border-color: #dde1e7; -fx-border-radius: 6; -fx-background-radius: 6;");
        langBox.setPrefWidth(75);

        Button btnTrad = new Button("Traduire");
        btnTrad.setStyle("-fx-background-color: #1a1f3c; -fx-text-fill: white; -fx-font-size: 11; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 5 12; -fx-cursor: hand;");
        btnTrad.setOnMouseEntered(e -> btnTrad.setStyle("-fx-background-color: #f5a623; -fx-text-fill: white; -fx-font-size: 11; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 5 12; -fx-cursor: hand;"));
        btnTrad.setOnMouseExited(e -> btnTrad.setStyle("-fx-background-color: #1a1f3c; -fx-text-fill: white; -fx-font-size: 11; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 5 12; -fx-cursor: hand;"));

        Button btnReset = new Button("Original");
        btnReset.setStyle("-fx-background-color: #e9ecef; -fx-text-fill: #555; -fx-font-size: 11; -fx-background-radius: 6; -fx-padding: 5 10; -fx-cursor: hand;");

        tradBox.getChildren().addAll(btnDefinir, tradIcon, langBox, btnTrad, btnReset);

        Label questionLabel = new Label(q.getQuestion());
        questionLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #1a1f3c;");
        questionLabel.setWrapText(true);

        activerDefinitionSurDoubleClick(questionLabel);
        btnDefinir.setOnAction(ev -> {
            double sx = btnDefinir.localToScreen(btnDefinir.getBoundsInLocal()).getMinX();
            double sy = btnDefinir.localToScreen(btnDefinir.getBoundsInLocal()).getMaxY();
            afficherDialogueSaisieMot(sx, sy);
        });

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
            reponseBox.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8; -fx-padding: 12 16; -fx-cursor: hand; -fx-border-color: #e9ecef; -fx-border-radius: 8; -fx-border-width: 1.5;");
            reponseBox.setMaxWidth(Double.MAX_VALUE);

            RadioButton rb = new RadioButton();
            rb.setToggleGroup(group);
            rb.setUserData(lettre);

            Label lettreLabel = new Label(lettre + ".");
            lettreLabel.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #888; -fx-min-width: 18;");

            Label texteLabel = new Label(texte);
            texteLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #333;");
            texteLabel.setWrapText(true);
            HBox.setHgrow(texteLabel, Priority.ALWAYS);

            reponseBox.getChildren().addAll(rb, lettreLabel, texteLabel);

            reponseBox.setOnMouseClicked(ev -> {
                rb.setSelected(true);
                reponsesBox.getChildren().forEach(node -> {
                    if (node instanceof HBox h)
                        h.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8; -fx-padding: 12 16; -fx-cursor: hand; -fx-border-color: #e9ecef; -fx-border-radius: 8; -fx-border-width: 1.5;");
                });
                reponseBox.setStyle("-fx-background-color: #fff8ee; -fx-background-radius: 8; -fx-padding: 12 16; -fx-cursor: hand; -fx-border-color: #f5a623; -fx-border-radius: 8; -fx-border-width: 2;");
            });
            reponseBox.setOnMouseEntered(ev -> {
                if (group.getSelectedToggle() != rb)
                    reponseBox.setStyle("-fx-background-color: #f0f4ff; -fx-background-radius: 8; -fx-padding: 12 16; -fx-cursor: hand; -fx-border-color: #b0c4de; -fx-border-radius: 8; -fx-border-width: 1.5;");
            });
            reponseBox.setOnMouseExited(ev -> {
                if (group.getSelectedToggle() != rb)
                    reponseBox.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8; -fx-padding: 12 16; -fx-cursor: hand; -fx-border-color: #e9ecef; -fx-border-radius: 8; -fx-border-width: 1.5;");
            });

            reponsesBox.getChildren().add(reponseBox);
        }

        group.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            long nb = toggleGroups.values().stream().filter(tg -> tg.getSelectedToggle() != null).count();
            progressLabel.setText(nb + " / " + questions.size() + " répondu(es)");
            double progress = (double) nb / questions.size();
            progressBar.setProgress(progress);
            if (progress < 0.5) progressBar.setStyle("-fx-accent: #e74c3c;");
            else if (progress < 1.0) progressBar.setStyle("-fx-accent: #f39c12;");
            else progressBar.setStyle("-fx-accent: #2ecc71;");
        });

        final String   questionOriginale = q.getQuestion();
        final String[] repOrigFinal      = Arrays.copyOf(repOrig, 4);

        btnTrad.setOnAction(ev -> {
            String lang = langBox.getValue();
            btnTrad.setText("...");
            btnTrad.setDisable(true);
            new Thread(() -> {
                try {
                    String   qTrad   = traduire(questionOriginale, lang);
                    String[] repTrad = new String[4];
                    for (int i = 0; i < 4; i++)
                        repTrad[i] = repOrigFinal[i].isEmpty() ? "" : traduire(repOrigFinal[i], lang);

                    Platform.runLater(() -> {
                        questionLabel.setText(qTrad);
                        int idx = 0;
                        for (javafx.scene.Node node : reponsesBox.getChildren()) {
                            if (node instanceof HBox hb && idx < 4) {
                                for (javafx.scene.Node child : hb.getChildren()) {
                                    if (child instanceof Label lbl && lbl.getStyle().contains("#333") && !repTrad[idx].isEmpty())
                                        lbl.setText(repTrad[idx]);
                                }
                                idx++;
                            }
                        }
                        btnTrad.setText("Traduire");
                        btnTrad.setDisable(false);
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> { btnTrad.setText("Erreur"); btnTrad.setDisable(false); });
                    ex.printStackTrace();
                }
            }).start();
        });

        btnReset.setOnAction(ev -> {
            questionLabel.setText(questionOriginale);
            int idx = 0;
            for (javafx.scene.Node node : reponsesBox.getChildren()) {
                if (node instanceof HBox hb && idx < 4) {
                    for (javafx.scene.Node child : hb.getChildren()) {
                        if (child instanceof Label lbl && lbl.getStyle().contains("#333") && !repOrigFinal[idx].isEmpty())
                            lbl.setText(repOrigFinal[idx]);
                    }
                    idx++;
                }
            }
        });

        bloc.getChildren().addAll(header, tradBox, questionLabel, reponsesBox);
        return bloc;
    }

    @FXML
    private void soumettreQuiz() {
        if (timer != null) timer.stop();
        stopDetector();

        for (Question q : questions) {
            ToggleGroup tg = toggleGroups.get(q.getId());
            if (tg == null || tg.getSelectedToggle() == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Réponse manquante");
                alert.setHeaderText(null);
                alert.setContentText("Veuillez répondre à toutes les questions !");
                alert.showAndWait();
                if (timer != null) timer.play();
                return;
            }
        }

        int totalPoints = 0, pointsObtenu = 0;
        Map<Integer, Boolean> resultats = new HashMap<>();

        for (Question q : questions) {
            totalPoints += q.getPoints();
            String  choix   = (String) toggleGroups.get(q.getId()).getSelectedToggle().getUserData();
            String  bonne   = extraireCorrecte(q.getReponses());
            boolean correct = choix.equals(bonne);
            if (correct) pointsObtenu += q.getPoints();
            resultats.put(q.getId(), correct);
        }

        float   scorePercent = totalPoints > 0 ? (float) pointsObtenu / totalPoints * 100f : 0f;
        boolean reussi       = scorePercent >= quiz.getScoreMinimum();
        String  badge        = scorePercent >= 90 ? "Or" : scorePercent >= 70 ? "Argent" : scorePercent >= 50 ? "Bronze" : "";

        try {
            certifService.ajouter(new Certification(
                    scorePercent, reussi ? "Réussi" : "Échoué", badge,
                    Timestamp.valueOf(LocalDateTime.now()),
                    Session.getCurrentUser().getId(), quiz.getId()));
        } catch (Exception e) { e.printStackTrace(); }

        afficherFeedbackQuestions(resultats);
        soumettreBtn.setVisible(false);
        soumettreBtn.setManaged(false);
        afficherPopupResultat(reussi, scorePercent, pointsObtenu, totalPoints, badge);
    }

    private void afficherFeedbackQuestions(Map<Integer, Boolean> resultats) {
        for (int i = 0; i < questions.size(); i++) {
            Question q           = questions.get(i);
            boolean  correct     = resultats.getOrDefault(q.getId(), false);
            String   bonneLettre = extraireCorrecte(q.getReponses());
            String   bonneTexte  = extraireReponse(q.getReponses(), bonneLettre);

            if (questionsContainer.getChildren().get(i) instanceof VBox bloc) {
                if (bloc.getChildren().size() >= 4 && bloc.getChildren().get(3) instanceof VBox repBox) {
                    String[] lettres = {"A", "B", "C", "D"};
                    for (int j = 0; j < repBox.getChildren().size(); j++) {
                        if (repBox.getChildren().get(j) instanceof HBox rb) {
                            String  lettre      = j < lettres.length ? lettres[j] : "";
                            boolean estCorrecte = lettre.equals(bonneLettre);
                            boolean estChoisie  = toggleGroups.get(q.getId()).getSelectedToggle() != null
                                    && lettre.equals((String) toggleGroups.get(q.getId()).getSelectedToggle().getUserData());
                            if (estCorrecte)
                                rb.setStyle("-fx-background-color: #e8f5e9; -fx-background-radius: 8; -fx-padding: 12 16; -fx-border-color: #2ecc71; -fx-border-radius: 8; -fx-border-width: 2;");
                            else if (estChoisie)
                                rb.setStyle("-fx-background-color: #fdecea; -fx-background-radius: 8; -fx-padding: 12 16; -fx-border-color: #e74c3c; -fx-border-radius: 8; -fx-border-width: 2;");
                            rb.setDisable(true);
                        }
                    }
                }

                VBox feedback = new VBox(4);
                if (correct) {
                    feedback.setStyle("-fx-background-color: #e8f5e9; -fx-background-radius: 8; -fx-padding: 10; -fx-border-color: #2ecc71; -fx-border-radius: 8; -fx-border-width: 1;");
                    Label fl = new Label("Bonne réponse !");
                    fl.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #2ecc71;");
                    feedback.getChildren().add(fl);
                } else {
                    feedback.setStyle("-fx-background-color: #fdecea; -fx-background-radius: 8; -fx-padding: 10; -fx-border-color: #e74c3c; -fx-border-radius: 8; -fx-border-width: 1;");
                    Label fl  = new Label("Mauvaise réponse !");
                    fl.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");
                    Label fl2 = new Label("La bonne réponse était : " + bonneLettre + ". " + bonneTexte);
                    fl2.setStyle("-fx-font-size: 11; -fx-text-fill: #c0392b;");
                    fl2.setWrapText(true);
                    feedback.getChildren().addAll(fl, fl2);
                }
                bloc.getChildren().add(feedback);
            }
        }
    }

    private void afficherPopupResultat(boolean reussi, float score, int points, int total, String badge) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle(reussi ? "Felicitations" : "Quiz termine");
        popup.setResizable(false);

        VBox root = new VBox(18);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: white; -fx-padding: 40; -fx-min-width: 420;");

        Label icone = new Label(reussi ? "🎊" : "😔");
        icone.setStyle("-fx-font-size: 55;");
        Label titre = new Label(reussi ? "Felicitations !" : "Quiz termine");
        titre.setStyle("-fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: " + (reussi ? "#1a1f3c" : "#e74c3c") + ";");
        Label scoreLabel = new Label(points + " / " + total + " pts");
        scoreLabel.setStyle("-fx-font-size: 36; -fx-font-weight: bold; -fx-text-fill: " + (reussi ? "#f5a623" : "#e74c3c") + ";");
        Label statutLabel = new Label(reussi ? "Statut : Reussi" : "Statut : Echoue");
        statutLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: " + (reussi ? "#2ecc71" : "#e74c3c") + ";");
        Label scoreMinLabel = new Label("Score minimum requis : " + (int) quiz.getScoreMinimum() + "%");
        scoreMinLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #888;");
        root.getChildren().addAll(icone, titre, scoreLabel, statutLabel, scoreMinLabel);

        if (reussi) {
            VBox badgeBox = new VBox(5);
            badgeBox.setAlignment(Pos.CENTER);
            badgeBox.setStyle("-fx-background-color: #fff8ee; -fx-background-radius: 12; -fx-padding: 15 30; -fx-border-color: #f5a623; -fx-border-radius: 12; -fx-border-width: 2;");
            Label badgeTitre = new Label("Badge obtenu");
            badgeTitre.setStyle("-fx-font-size: 11; -fx-text-fill: #888;");
            Label badgeVal = new Label(badge.isEmpty() ? "Aucun" : badge);
            badgeVal.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: #f5a623;");
            badgeBox.getChildren().addAll(badgeTitre, badgeVal);
            root.getChildren().add(badgeBox);
        } else {
            Label encour = new Label("Ne vous decouragez pas ! Vous pouvez reessayer.");
            encour.setStyle("-fx-font-size: 13; -fx-text-fill: #555; -fx-font-style: italic;");
            encour.setWrapText(true);
            encour.setAlignment(Pos.CENTER);
            root.getChildren().add(encour);
        }

        Label certifLabel = new Label("Tentative enregistree !");
        certifLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #2ecc71; -fx-font-weight: bold;");
        root.getChildren().add(certifLabel);

        HBox btnBox = new HBox(12);
        btnBox.setAlignment(Pos.CENTER);
        Button btnRetour = new Button("Retour au cours");
        btnRetour.setStyle("-fx-background-color: #1a1f3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13; -fx-background-radius: 10; -fx-padding: 11 20; -fx-cursor: hand;");
        btnRetour.setOnAction(e -> { popup.close(); retourCours(); });
        Button btnAccueil = new Button("Accueil");
        btnAccueil.setStyle("-fx-background-color: #f5a623; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13; -fx-background-radius: 10; -fx-padding: 11 20; -fx-cursor: hand;");
        btnAccueil.setOnAction(e -> { popup.close(); retourAccueil(); });
        btnBox.getChildren().addAll(btnRetour, btnAccueil);
        root.getChildren().add(btnBox);

        popup.setScene(new Scene(root));
        popup.show();

        Platform.runLater(() -> {
            Stage parent = (Stage) questionsContainer.getScene().getWindow();
            popup.setX(parent.getX() + (parent.getWidth()  - popup.getWidth())  / 2);
            popup.setY(parent.getY() + (parent.getHeight() - popup.getHeight()) / 2);
        });
    }

    @FXML
    private void retourCours() {
        if (timer != null) timer.stop();
        stopDetector();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/CoursDetailEtudiant.fxml"));
            Stage stage = (Stage) questionsContainer.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            CoursDetailEtudiantController ctrl = loader.getController();
            ctrl.setCours(coursId, coursTitre, coursNiveau, coursMatiere, coursLangue, coursDescription);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void retourAccueil() {
        if (timer != null) timer.stop();
        stopDetector();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/AccueilEtudiant.fxml"));
            Stage stage = (Stage) questionsContainer.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Espace Etudiant");
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void retourCatalogue() {
        if (timer != null) timer.stop();
        stopDetector();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/AccueilEtudiant.fxml"));
            Stage stage = (Stage) questionsContainer.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Espace Etudiant — Catalogue");
        } catch (Exception e) { e.printStackTrace(); }
    }

    private String extraireReponse(String json, String lettre) {
        if (json == null || json.isEmpty()) return "";
        try {
            String[] parts = json.replace("[","").replace("]","").split("},");
            int index = lettre.equals("A") ? 0 : lettre.equals("B") ? 1 : lettre.equals("C") ? 2 : 3;
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

    private void afficherSocial(Quiz quiz) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/QuizSocial.fxml"));
            VBox social = loader.load();
            QuizSocialController ctrl = loader.getController();
            ctrl.setQuiz(quiz, 1, "Asma");
            mainContent.getChildren().add(social);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void startCheatDetection() {
        cheatDetector = new CheatDetectorService(PYTHON_EXE, SCRIPT_PATH, () -> {
            System.out.println("[CheatDetector] *** CHEAT CALLBACK FIRED ***");
            if (timer != null) timer.stop();

            try {
                certifService.ajouter(new Certification(
                        0f, "Triche", "",
                        Timestamp.valueOf(LocalDateTime.now()),
                        Session.getCurrentUser().getId(),
                        quiz.getId()));
            } catch (Exception e) { e.printStackTrace(); }

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Triche détectée");
            alert.setHeaderText("Comportement suspect détecté !");
            alert.setContentText("Le quiz a été arrêté.");
            alert.showAndWait();

            retourCours();
        });
        cheatDetector.start();
    }

    private static String findPython() {
        try {
            Process p = Runtime.getRuntime().exec("python --version");
            if (p.waitFor() == 0) return "python";
        } catch (Exception ignored) {}
        return "python3";
    }

    private static String findScript() {
        String path = System.getProperty("user.dir") + "/src/main/Python/QuizCheatDetectorService.py";
        System.out.println("[CheatDetector] Script path: " + path);
        return path;
    }
    private void stopDetector() {
        if (cheatDetector != null && cheatDetector.isRunning()) cheatDetector.stop();
    }
}