package org.example.controllers;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.entities.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.services.NotificationService;
import org.example.services.boursesService;
import org.example.services.demandeService;
import org.example.utils.AppContext;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.example.utils.Helpers.showAlert;

public class EtudiantBoursesController implements Initializable {

    // ── SECTION BOURSES ──────────────────────────────────────────────────────

    @FXML private TableView<bourses> tableView;
    @FXML private TableColumn<bourses, String>    colTitre;
    @FXML private TableColumn<bourses, Double>    colMontant;
    @FXML private TableColumn<bourses, Timestamp> colDateAttr;
    @FXML private TableColumn<bourses, Timestamp> colDateFin;

    @FXML private TextField        tfRecherche;
    @FXML private ComboBox<String> cbTri;
    @FXML private Button           btnPostuler;
    @FXML private Label            lblNotifBadge;

    private final boursesService      service      = new boursesService();
    private final NotificationService notifService = new NotificationService();
    private final ObservableList<bourses> masterList = FXCollections.observableArrayList();
    private FilteredList<bourses> filteredList;
    private SortedList<bourses>   sortedList;

    // ── SECTION CALENDRIER ───────────────────────────────────────────────────

    @FXML private GridPane gridCalendrier;
    @FXML private Label    lblMoisAnnee;

    private List<bourses> toutesLesBourses;
    private YearMonth     moisCourant;

    // ── SECTION INTERVIEW ────────────────────────────────────────────────────

    @FXML private TableView<demande>            tableViewDemandes;
    @FXML private TableColumn<demande, Integer>  colBourse;   // Integer — bourse_id is int
    @FXML private TableColumn<demande, String>   colStatut;
    @FXML private TableColumn<demande, Timestamp> colDate;

    @FXML private VBox        vboxEnregistrement;
    @FXML private VBox        vboxResultats;
    @FXML private Label       lblBourseTitre;
    @FXML private Label       lblStatutRecord;
    @FXML private Label       lblTimer;
    @FXML private Button      btnEnregistrer;
    @FXML private Button      btnArreter;
    @FXML private Button      btnAnalyser;
    @FXML private ProgressBar progressBar;
    @FXML private Label       lblProgress;
    @FXML private Label       lblTranscription;

    private final demandeService dService = new demandeService();
    private final org.example.services.InterviewIAService iaService = new org.example.services.InterviewIAService();

    private TargetDataLine microphone;
    private ByteArrayOutputStream audioStream;
    private boolean isRecording = false;
    private long startTime;
    private File recordedFile;
    private demande selectedDemande;

    // ── INITIALIZE ───────────────────────────────────────────────────────────

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // --- Bourses ---
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colMontant.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colDateAttr.setCellValueFactory(new PropertyValueFactory<>("date_attribution"));
        colDateFin.setCellValueFactory(new PropertyValueFactory<>("date_fin"));

        initialiserTri();
        chargerBourses();
        configurerRechercheAvancee();
        afficherNotifications();

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) ->
                btnPostuler.setDisable(newSel == null));

        // --- Calendrier ---
        toutesLesBourses = service.getAll();
        moisCourant = YearMonth.now();
        afficherCalendrier();

        // --- Interview ---
        initialiserTableauDemandes();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  BOURSES
    // ══════════════════════════════════════════════════════════════════════════

    private void initialiserTri() {
        cbTri.setItems(FXCollections.observableArrayList(
                "Par defaut",
                "Montant croissant",
                "Montant decroissant",
                "Date attribution (proche)",
                "Date fin (proche)"
        ));
        cbTri.setValue("Par defaut");
    }

    private void configurerRechercheAvancee() {
        filteredList = new FilteredList<>(masterList, b -> true);

        tfRecherche.textProperty().addListener((obs, oldVal, newVal) -> appliquerFiltres());
        cbTri.valueProperty().addListener((obs, oldVal, newVal) -> appliquerTri());

        sortedList = new SortedList<>(filteredList);
        sortedList.comparatorProperty().bind(tableView.comparatorProperty());
        tableView.setItems(sortedList);
    }

    private void appliquerFiltres() {
        filteredList.setPredicate(b -> {
            String texte = tfRecherche.getText();
            if (texte != null && !texte.isEmpty()) {
                return b.getTitre().toLowerCase().contains(texte.toLowerCase());
            }
            return true;
        });
    }

    private void appliquerTri() {
        String tri = cbTri.getValue();
        if (tri == null || tri.equals("Par defaut")) {
            sortedList.comparatorProperty().unbind();
            sortedList.setComparator(null);
            sortedList.comparatorProperty().bind(tableView.comparatorProperty());
            return;
        }

        sortedList.comparatorProperty().unbind();

        switch (tri) {
            case "Montant croissant":
                sortedList.setComparator(Comparator.comparingDouble(bourses::getMontant));
                break;
            case "Montant decroissant":
                sortedList.setComparator(Comparator.comparingDouble(bourses::getMontant).reversed());
                break;
            case "Date attribution (proche)":
                sortedList.setComparator(Comparator.comparing(bourses::getDate_attribution,
                        Comparator.nullsLast(Comparator.naturalOrder())));
                break;
            case "Date fin (proche)":
                sortedList.setComparator(Comparator.comparing(bourses::getDate_fin,
                        Comparator.nullsLast(Comparator.naturalOrder())));
                break;
        }
    }

    @FXML
    private void reinitialiserFiltres(ActionEvent event) {
        tfRecherche.clear();
        cbTri.setValue("Par defaut");
    }

    private void chargerBourses() {
        masterList.clear();
        masterList.addAll(service.getAll());
    }

    @FXML
    private void ouvrirPostuler(ActionEvent event) {
        bourses selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/etudiant/PostulerBourse.fxml"));
            Parent root = loader.load();

            PostulerController ctrl = loader.getController();
            ctrl.setBourseSelected(selected);

            btnPostuler.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void afficherNotifications() {
        int count = notifService.compterNonLues();
        if (lblNotifBadge != null) {
            if (count > 0) {
                lblNotifBadge.setText(count + " nouvelle(s) notification(s)");
                lblNotifBadge.setVisible(true);

                List<Notification> notifs = notifService.getNonLues();
                StringBuilder sb = new StringBuilder();
                for (Notification n : notifs) {
                    sb.append("- ").append(n.getMessage()).append("\n\n");
                }

                javafx.application.Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Notifications");
                    alert.setHeaderText(count + " nouvelle(s) notification(s)");
                    alert.setContentText(sb.toString());
                    alert.showAndWait();
                    notifService.marquerToutesLues();
                    lblNotifBadge.setVisible(false);
                });
            } else {
                lblNotifBadge.setVisible(false);
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  CALENDRIER
    // ══════════════════════════════════════════════════════════════════════════

    @FXML
    private void moisPrecedent(ActionEvent event) {
        moisCourant = moisCourant.minusMonths(1);
        afficherCalendrier();
    }

    @FXML
    private void moisSuivant(ActionEvent event) {
        moisCourant = moisCourant.plusMonths(1);
        afficherCalendrier();
    }

    private void afficherCalendrier() {
        gridCalendrier.getChildren().clear();

        String nomMois = moisCourant.getMonth().getDisplayName(TextStyle.FULL, Locale.FRANCE);
        nomMois = nomMois.substring(0, 1).toUpperCase() + nomMois.substring(1);
        lblMoisAnnee.setText(nomMois + " " + moisCourant.getYear());

        String[] joursNoms = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"};
        for (int col = 0; col < 7; col++) {
            Label lblJour = new Label(joursNoms[col]);
            lblJour.setStyle("-fx-font-weight: bold; -fx-font-size: 13; -fx-text-fill: #2c3e50;");
            lblJour.setPrefWidth(108);
            lblJour.setAlignment(Pos.CENTER);
            gridCalendrier.add(lblJour, col, 0);
        }

        LocalDate premierJour = moisCourant.atDay(1);
        int nbJours  = moisCourant.lengthOfMonth();
        int decalage = premierJour.getDayOfWeek().getValue() - 1; // Lundi = 0

        for (int jour = 1; jour <= nbJours; jour++) {
            LocalDate dateJour = moisCourant.atDay(jour);
            int col = (decalage + jour - 1) % 7;
            int row = (decalage + jour - 1) / 7 + 1;
            gridCalendrier.add(creerCelluleJour(jour, dateJour), col, row);
        }
    }

    private VBox creerCelluleJour(int jour, LocalDate dateJour) {
        VBox cellule = new VBox(2);
        cellule.setPrefWidth(108);
        cellule.setPrefHeight(70);
        cellule.setAlignment(Pos.TOP_CENTER);

        String styleBase = "-fx-background-color: white; -fx-background-radius: 8; "
                + "-fx-border-color: #ddd; -fx-border-radius: 8; -fx-padding: 3;";

        if (dateJour.equals(LocalDate.now())) {
            styleBase = "-fx-background-color: #ffeaa7; -fx-background-radius: 8; "
                    + "-fx-border-color: #f39c12; -fx-border-radius: 8; -fx-padding: 3;";
        }

        Label lblNumero = new Label(String.valueOf(jour));
        lblNumero.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        cellule.setStyle(styleBase);
        cellule.getChildren().add(lblNumero);

        StringBuilder tooltipText = new StringBuilder();

        for (bourses b : toutesLesBourses) {
            LocalDate dateAttr = b.getDate_attribution() != null
                    ? b.getDate_attribution().toLocalDateTime().toLocalDate() : null;
            LocalDate dateFin = b.getDate_fin() != null
                    ? b.getDate_fin().toLocalDateTime().toLocalDate() : null;

            if (dateAttr != null && dateAttr.equals(dateJour)) {
                cellule.getChildren().add(creerEtiquette(tronquer(b.getTitre(), 12), "#2ecc71"));
                tooltipText.append("Debut : ").append(b.getTitre())
                        .append(" (").append(b.getMontant()).append(" DT)\n");
            }

            if (dateFin != null && dateFin.equals(dateJour)) {
                cellule.getChildren().add(creerEtiquette(tronquer(b.getTitre(), 12), "#e74c3c"));
                tooltipText.append("Fin : ").append(b.getTitre())
                        .append(" (").append(b.getMontant()).append(" DT)\n");
            }

            if (dateAttr != null && dateFin != null
                    && dateJour.isAfter(dateAttr) && dateJour.isBefore(dateFin)) {
                if (dateJour.getDayOfWeek().getValue() == 1) {
                    cellule.getChildren().add(creerEtiquette(tronquer(b.getTitre(), 12), "#3498db"));
                }
                tooltipText.append("En cours : ").append(b.getTitre())
                        .append(" (").append(b.getMontant()).append(" DT)\n");
            }
        }

        if (tooltipText.length() > 0) {
            Tooltip tooltip = new Tooltip(tooltipText.toString().trim());
            tooltip.setStyle("-fx-font-size: 12;");
            Tooltip.install(cellule, tooltip);
            cellule.setStyle(cellule.getStyle() + " -fx-cursor: hand;");
        }

        return cellule;
    }

    private Label creerEtiquette(String texte, String couleur) {
        Label tag = new Label(texte);
        tag.setStyle("-fx-background-color: " + couleur + "; -fx-text-fill: white; "
                + "-fx-padding: 1 4; -fx-background-radius: 5; -fx-font-size: 9;");
        tag.setMaxWidth(100);
        return tag;
    }

    private String tronquer(String texte, int maxLength) {
        if (texte == null) return "";
        if (texte.length() <= maxLength) return texte;
        return texte.substring(0, maxLength - 2) + "..";
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  INTERVIEW IA
    // ══════════════════════════════════════════════════════════════════════════

    private void initialiserTableauDemandes() {
        // bourse_id is an int — column typed Integer to avoid ClassCastException
        colBourse.setCellValueFactory(new PropertyValueFactory<>("bourse_id"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date_demande"));

        // Resolve Integer bourse_id → bourse title for display
        colBourse.setCellFactory(col -> new TableCell<demande, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    bourses b = service.getById(item);
                    setText(b != null ? b.getTitre() : "Bourse #" + item);
                }
            }
        });

        ObservableList<demande> demandes = FXCollections.observableArrayList(dService.getAll());
        tableViewDemandes.setItems(demandes);

        vboxEnregistrement.setVisible(false);
        vboxEnregistrement.setManaged(false);
        vboxResultats.setVisible(false);
        vboxResultats.setManaged(false);

        tableViewDemandes.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                selectedDemande = newVal;
                bourses b = service.getById(newVal.getBourse_id());
                lblBourseTitre.setText(b != null ? b.getTitre() : "Bourse #" + newVal.getBourse_id());

                String statut = newVal.getStatut();
                boolean isAccepted = (statut != null && statut.toLowerCase().contains("accept"));

                if (isAccepted) {
                    org.example.entities.AnalyseInterview existing = iaService.getByDemandeId(newVal.getId());
                    if (existing != null) {
                        vboxEnregistrement.setVisible(false);
                        vboxEnregistrement.setManaged(false);
                        vboxResultats.setVisible(true);
                        vboxResultats.setManaged(true);
                        lblTranscription.setText(existing.getTranscription());
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

    private void resetEnregistrement() {
        btnEnregistrer.setDisable(false);
        btnArreter.setDisable(true);
        btnAnalyser.setDisable(true);
        progressBar.setVisible(false);
        lblProgress.setVisible(false);
        lblTimer.setText("00:00");
        lblStatutRecord.setText("Pret a enregistrer");
        lblStatutRecord.setStyle("-fx-text-fill: #888;");
        recordedFile = null;
    }

    @FXML
    private void demarrerEnregistrement(ActionEvent event) {
        selectedDemande = tableViewDemandes.getSelectionModel().getSelectedItem();
        if (selectedDemande == null) {
            showAlert("Erreur", "Selectionnez une demande acceptee avant d'enregistrer.");
            return;
        }
        
        if (selectedDemande.getStatut() == null || !selectedDemande.getStatut().toLowerCase().contains("accept")) {
            showAlert("Erreur", "Vous ne pouvez passer l'interview que si votre demande est acceptée.");
            return;
        }


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
        lblProgress.setText("Analyse en cours... (transcription + IA)");
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
                    vboxResultats.setVisible(true);
                    vboxResultats.setManaged(true);
                    lblTranscription.setText(analyse.getTranscription());
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    progressBar.setVisible(false);
                    lblProgress.setVisible(false);
                    lblStatutRecord.setText("Erreur lors de l'analyse");
                    lblStatutRecord.setStyle("-fx-text-fill: #e74c3c;");
                    showAlert("Erreur IA", "Verifiez que le microservice Python est lance sur le port 8001.\n\n" + e.getMessage());
                    btnAnalyser.setDisable(false);
                });
            }
        }).start();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  NAVIGATION
    // ══════════════════════════════════════════════════════════════════════════

    @FXML
    private void deconnexion(ActionEvent event) {
        try {
            Session.logout();
        } catch (Exception e) {
            // silent
        }
    }

    @FXML
    public void navigateToProfile(javafx.scene.input.MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/StudentView.fxml"));
            Parent root = loader.load();
            ((javafx.scene.Node) event.getSource()).getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void ouvrirCertifications(javafx.scene.input.MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/CertificationsEtudiant.fxml"));
            Parent root = loader.load();
            ((javafx.scene.Node) event.getSource()).getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleForumButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main.fxml"));
            Parent root = loader.load();

            MainController controller = loader.getController();
            User currentUser = Session.getCurrentUser();
            controller.setCurrentUser(currentUser);
            AppContext.setCurrentUser(currentUser);

            ((javafx.scene.Node) event.getSource()).getScene().setRoot(root);
        } catch (Throwable t) {
            t.printStackTrace();
            System.err.println("FORUM ERROR: " + t.getMessage());
        }
    }

    @FXML
    public void navigateToCours(javafx.scene.input.MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/AccueilEtudiant.fxml"));
            Parent root = loader.load();
            ((javafx.scene.Node) event.getSource()).getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void navigateToEvents(javafx.scene.input.MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Student.fxml"));
            Parent root = loader.load();
            ((javafx.scene.Node) event.getSource()).getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void ouvrirCours(int coursId, String titre, String niveau,
                             String matiere, String langue, String description) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/CoursDetailEtudiant.fxml"));
            Stage stage = (Stage) tableView.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle(titre);
            CoursDetailEtudiantController ctrl = loader.getController();
            ctrl.setCours(coursId, titre, niveau, matiere, langue, description);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
