package org.example.controllers;

import org.example.entities.Session;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.entities.AnalyseInterview;
import org.example.entities.demande;
import org.example.services.InterviewIAService;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class InterviewIAController implements Initializable {

    @FXML private Label lblTitreDemande;
    @FXML private Label lblStatut;
    @FXML private Label lblTimer;
    @FXML private Button btnEnregistrer;
    @FXML private Button btnArreter;
    @FXML private Button btnAnalyser;
    @FXML private ProgressBar progressBar;
    @FXML private Label lblProgress;

    @FXML private VBox vboxResultats;
    @FXML private Label lblTranscription;
    @FXML private Label lblProfil;
    @FXML private Label lblRecommandation;
    @FXML private VBox vboxEmotions;
    @FXML private Label lblDebit;
    @FXML private Label lblHesitations;
    @FXML private Label lblEnergie;
    @FXML private Label lblDuree;

    private demande currentDemande;
    private InterviewIAService iaService = new InterviewIAService();

    private TargetDataLine microphone;
    private ByteArrayOutputStream audioStream;
    private boolean isRecording = false;
    private long startTime;
    private Thread timerThread;
    private File recordedFile;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnArreter.setDisable(true);
        btnAnalyser.setDisable(true);
        vboxResultats.setVisible(false);
        progressBar.setVisible(false);
        lblProgress.setVisible(false);
    }

    public void setDemande(demande d) {
        this.currentDemande = d;
        lblTitreDemande.setText("Demande #" + d.getId());

        AnalyseInterview existing = iaService.getByDemandeId(d.getId());
        if (existing != null) {
            afficherResultats(existing);
            btnEnregistrer.setDisable(true);
            lblStatut.setText("Interview deja realisee");
            lblStatut.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
        }
    }

    @FXML
    private void demarrerEnregistrement(ActionEvent event) {
        try {
            AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            if (!AudioSystem.isLineSupported(info)) {
                showAlert("Erreur", "Aucun microphone detecte sur cet ordinateur.");
                return;
            }

            microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(format);
            microphone.start();

            audioStream = new ByteArrayOutputStream();
            isRecording = true;
            startTime = System.currentTimeMillis();

            btnEnregistrer.setDisable(true);
            btnArreter.setDisable(false);
            lblStatut.setText("Enregistrement en cours...");
            lblStatut.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");

            new Thread(() -> {
                byte[] buffer = new byte[4096];
                while (isRecording) {
                    int count = microphone.read(buffer, 0, buffer.length);
                    if (count > 0) {
                        audioStream.write(buffer, 0, count);
                    }
                }
            }).start();

            timerThread = new Thread(() -> {
                while (isRecording) {
                    long elapsed = (System.currentTimeMillis() - startTime) / 1000;
                    Platform.runLater(() -> lblTimer.setText(String.format("%02d:%02d", elapsed / 60, elapsed % 60)));
                    try { Thread.sleep(1000); } catch (InterruptedException e) { break; }
                }
            });
            timerThread.start();

        } catch (LineUnavailableException e) {
            showAlert("Erreur", "Impossible d'acceder au microphone: " + e.getMessage());
        }
    }

    @FXML
    private void arreterEnregistrement(ActionEvent event) {
        isRecording = false;
        if (microphone != null) {
            microphone.stop();
            microphone.close();
        }

        btnArreter.setDisable(true);
        lblStatut.setText("Enregistrement termine");
        lblStatut.setStyle("-fx-text-fill: #f5a623; -fx-font-weight: bold;");

        try {
            byte[] audioData = audioStream.toByteArray();
            AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
            ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
            AudioInputStream ais = new AudioInputStream(bais, format, audioData.length / format.getFrameSize());

            recordedFile = File.createTempFile("interview_", ".wav");
            recordedFile.deleteOnExit();
            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, recordedFile);

            long dureeSec = (System.currentTimeMillis() - startTime) / 1000;
            if (dureeSec < 10) {
                lblStatut.setText("Enregistrement trop court (min 10s). Recommencez.");
                lblStatut.setStyle("-fx-text-fill: #e74c3c;");
                btnEnregistrer.setDisable(false);
                recordedFile = null;
            } else {
                btnAnalyser.setDisable(false);
                lblStatut.setText("Pret a analyser (" + dureeSec + "s enregistrees)");
            }
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de sauvegarder l'audio: " + e.getMessage());
        }
    }

    @FXML
    private void lancerAnalyse(ActionEvent event) {
        if (recordedFile == null || currentDemande == null) return;

        btnAnalyser.setDisable(true);
        progressBar.setVisible(true);
        progressBar.setProgress(-1);
        lblProgress.setVisible(true);
        lblProgress.setText("Analyse en cours... (transcription + IA)");
        lblStatut.setText("Envoi au microservice IA...");
        lblStatut.setStyle("-fx-text-fill: #4facfe; -fx-font-weight: bold;");

        new Thread(() -> {
            try {
                AnalyseInterview analyse = iaService.analyserAudio(recordedFile, currentDemande.getId());
                iaService.save(analyse);

                Platform.runLater(() -> {
                    progressBar.setVisible(false);
                    lblProgress.setVisible(false);
                    lblStatut.setText("Analyse terminee !");
                    lblStatut.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
                    afficherResultats(analyse);
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    progressBar.setVisible(false);
                    lblProgress.setVisible(false);
                    lblStatut.setText("Erreur lors de l'analyse");
                    lblStatut.setStyle("-fx-text-fill: #e74c3c;");
                    showAlert("Erreur IA", "Verifiez que le microservice Python est lance sur le port 8001.\n\n" + e.getMessage());
                    btnAnalyser.setDisable(false);
                });
            }
        }).start();
    }

    private void afficherResultats(AnalyseInterview analyse) {
        vboxResultats.setVisible(true);
        lblTranscription.setText(analyse.getTranscription());
        lblProfil.setText(analyse.getProfilGlobal());
        lblRecommandation.setText(analyse.getRecommandation());

        vboxEmotions.getChildren().clear();
        try {
            JsonObject emotions = JsonParser.parseString(analyse.getScoresEmotions()).getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : emotions.entrySet()) {
                double score = entry.getValue().getAsDouble();
                int pct = (int) (score * 100);

                ProgressBar bar = new ProgressBar(score);
                bar.setPrefWidth(200);
                bar.setPrefHeight(18);
                if (score > 0.6) bar.setStyle("-fx-accent: #2ecc71;");
                else if (score > 0.3) bar.setStyle("-fx-accent: #f5a623;");
                else bar.setStyle("-fx-accent: #e74c3c;");

                Label lbl = new Label(entry.getKey() + " : " + pct + "%");
                lbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #333; -fx-min-width: 140;");

                HBox row = new HBox(12, lbl, bar);
                row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                vboxEmotions.getChildren().add(row);
            }
        } catch (Exception e) {
            vboxEmotions.getChildren().add(new Label("Erreur parsing emotions"));
        }

        try {
            JsonObject features = JsonParser.parseString(analyse.getFeaturesAudio()).getAsJsonObject();
            lblDebit.setText(features.get("debit_mots_par_min").getAsInt() + " mots/min");
            lblHesitations.setText(features.get("taux_hesitations_pct").getAsDouble() + "%");
            lblEnergie.setText(features.get("energie_vocale").getAsString());
            lblDuree.setText(features.get("duree_secondes").getAsDouble() + "s");
        } catch (Exception e) {
            lblDebit.setText("-");
            lblHesitations.setText("-");
            lblEnergie.setText("-");
            lblDuree.setText("-");
        }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    @FXML
    private void retourDetail(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demandes/DetailDemande.fxml"));
            Parent root = loader.load();
            DetailDemandeController ctrl = loader.getController();
            ctrl.setDemande(currentDemande);
            lblTitreDemande.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void allerAccueil(ActionEvent event) {
        try {
            Session.logout();
        } catch (Exception e) {
            {
                return;
            }
        }
    }

    @FXML
    private void allerBourses(ActionEvent event) { naviguerVers("/org/example/bourses/ListeBourses.fxml"); }
    @FXML
    private void allerDemandes(ActionEvent event) { naviguerVers("/org/example/demandes/ListeDemandes.fxml"); }
    @FXML
    private void allerStatistiques(ActionEvent event) { naviguerVers("/org/example/bourses/Statistiques.fxml"); }

    private void naviguerVers(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            lblTitreDemande.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
