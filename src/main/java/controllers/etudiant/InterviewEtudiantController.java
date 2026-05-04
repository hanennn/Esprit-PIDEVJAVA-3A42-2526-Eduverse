package controllers.etudiant;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import models.AnalyseInterview;
import models.bourses;
import models.demande;
import services.InterviewIAService;
import services.boursesService;
import services.demandeService;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class InterviewEtudiantController implements Initializable {

    @FXML private TableView<demande> tableView;
    @FXML private TableColumn<demande, Integer> colBourse;
    @FXML private TableColumn<demande, String> colStatut;
    @FXML private TableColumn<demande, java.sql.Timestamp> colDate;

    @FXML private VBox vboxEnregistrement;
    @FXML private Label lblBourseTitre;
    @FXML private Label lblTimer;
    @FXML private Label lblStatutRecord;
    @FXML private Button btnEnregistrer;
    @FXML private Button btnArreter;
    @FXML private Button btnAnalyser;
    @FXML private ProgressBar progressBar;
    @FXML private Label lblProgress;

    @FXML private VBox vboxResultats;
    @FXML private Label lblTranscription;

    private demandeService dService = new demandeService();
    private boursesService bService = new boursesService();
    private InterviewIAService iaService = new InterviewIAService();
    private ObservableList<demande> masterList = FXCollections.observableArrayList();

    private demande selectedDemande;
    private TargetDataLine microphone;
    private ByteArrayOutputStream audioStream;
    private boolean isRecording = false;
    private long startTime;
    private File recordedFile;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colBourse.setCellValueFactory(new PropertyValueFactory<>("bourse_id"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date_demande"));

        // Afficher le titre de la bourse au lieu de l'ID
        colBourse.setCellFactory(col -> new TableCell<demande, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    bourses b = bService.getById(item);
                    setText(b != null ? b.getTitre() : "Bourse #" + item);
                }
            }
        });

        // Colorer le statut selon la valeur
        colStatut.setCellFactory(col -> new TableCell<demande, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.toLowerCase().contains("accept")) {
                        setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #888;");
                    }
                }
            }
        });

        chargerDemandes();

        System.out.println("Interview IA: " + masterList.size() + " demandes chargees");
        for (demande d : masterList) {
            System.out.println("  Demande #" + d.getId() + " statut=[" + d.getStatut() + "] bourse=" + d.getBourse_id());
        }

        vboxEnregistrement.setVisible(false);
        vboxEnregistrement.setManaged(false);
        vboxResultats.setVisible(false);
        vboxResultats.setManaged(false);

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                selectedDemande = newVal;
                String statut = newVal.getStatut();
                boolean isAccepted = isAccepte(statut);

                if (isAccepted) {
                    bourses b = bService.getById(newVal.getBourse_id());
                    lblBourseTitre.setText(b != null ? b.getTitre() : "Bourse #" + newVal.getBourse_id());

                    AnalyseInterview existing = iaService.getByDemandeId(newVal.getId());
                    if (existing != null) {
                        vboxEnregistrement.setVisible(false);
                        vboxEnregistrement.setManaged(false);
                        afficherResultats(existing);
                        lblStatutRecord.setText("Interview deja realisee");
                        lblStatutRecord.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
                    } else {
                        vboxEnregistrement.setVisible(true);
                        vboxEnregistrement.setManaged(true);
                        vboxResultats.setVisible(false);
                        vboxResultats.setManaged(false);
                        resetEnregistrement();
                    }
                } else {
                    vboxEnregistrement.setVisible(false);
                    vboxEnregistrement.setManaged(false);
                    vboxResultats.setVisible(false);
                    vboxResultats.setManaged(false);
                }
            }
        });
    }

    private void chargerDemandes() {
        masterList.clear();
        List<demande> all = dService.getAll();
        masterList.addAll(all);
        tableView.setItems(masterList);
    }

    private boolean isAccepte(String statut) {
        if (statut == null) return false;
        String s = statut.toLowerCase().trim();
        return s.contains("accept");
    }

    private void resetEnregistrement() {
        btnEnregistrer.setDisable(false);
        btnArreter.setDisable(true);
        btnAnalyser.setDisable(true);
        progressBar.setVisible(false);
        lblProgress.setVisible(false);
        lblTimer.setText("00:00");
        lblStatutRecord.setText("Selectionnez une demande acceptee pour passer l'interview");
        lblStatutRecord.setStyle("-fx-text-fill: #888;");
        recordedFile = null;
    }

    @FXML
    private void demarrerEnregistrement(ActionEvent event) {
        try {
            AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            if (!AudioSystem.isLineSupported(info)) {
                showAlert("Erreur", "Aucun microphone detecte.");
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
            lblStatutRecord.setText("Enregistrement en cours...");
            lblStatutRecord.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");

            new Thread(() -> {
                byte[] buffer = new byte[4096];
                while (isRecording) {
                    int count = microphone.read(buffer, 0, buffer.length);
                    if (count > 0) audioStream.write(buffer, 0, count);
                }
            }).start();

            new Thread(() -> {
                while (isRecording) {
                    long elapsed = (System.currentTimeMillis() - startTime) / 1000;
                    Platform.runLater(() -> lblTimer.setText(String.format("%02d:%02d", elapsed / 60, elapsed % 60)));
                    try { Thread.sleep(1000); } catch (InterruptedException e) { break; }
                }
            }).start();

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
                lblStatutRecord.setText("Trop court (min 10s). Recommencez.");
                lblStatutRecord.setStyle("-fx-text-fill: #e74c3c;");
                btnEnregistrer.setDisable(false);
                recordedFile = null;
            } else {
                btnAnalyser.setDisable(false);
                lblStatutRecord.setText("Pret a analyser (" + dureeSec + "s)");
                lblStatutRecord.setStyle("-fx-text-fill: #f5a623; -fx-font-weight: bold;");
            }
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de sauvegarder: " + e.getMessage());
        }
    }

    @FXML
    private void lancerAnalyse(ActionEvent event) {
        if (recordedFile == null || selectedDemande == null) return;

        btnAnalyser.setDisable(true);
        progressBar.setVisible(true);
        progressBar.setProgress(-1);
        lblProgress.setVisible(true);
        lblProgress.setText("Analyse en cours...");
        lblStatutRecord.setText("Envoi au microservice IA...");
        lblStatutRecord.setStyle("-fx-text-fill: #4facfe; -fx-font-weight: bold;");

        new Thread(() -> {
            try {
                AnalyseInterview analyse = iaService.analyserAudio(recordedFile, selectedDemande.getId());
                iaService.save(analyse);
                Platform.runLater(() -> {
                    progressBar.setVisible(false);
                    lblProgress.setVisible(false);
                    lblStatutRecord.setText("Analyse terminee !");
                    lblStatutRecord.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
                    vboxEnregistrement.setVisible(false);
                    vboxEnregistrement.setManaged(false);
                    afficherResultats(analyse);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    progressBar.setVisible(false);
                    lblProgress.setVisible(false);
                    lblStatutRecord.setText("Erreur");
                    showAlert("Erreur IA", "Verifiez que le microservice Python tourne sur le port 8001.\n" + e.getMessage());
                    btnAnalyser.setDisable(false);
                });
            }
        }).start();
    }

    private void afficherResultats(AnalyseInterview analyse) {
        vboxResultats.setVisible(true);
        vboxResultats.setManaged(true);
        lblTranscription.setText(analyse.getTranscription());
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    @FXML
    private void allerBourses(ActionEvent event) { naviguerVers("/etudiant/EtudiantBourses.fxml"); }
    @FXML
    private void allerCalendrier(ActionEvent event) { naviguerVers("/etudiant/CalendrierBourses.fxml"); }
    @FXML
    private void allerAccueil(ActionEvent event) { naviguerVers("/accueil/Accueil.fxml"); }

    private void naviguerVers(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            tableView.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
