package org.example.controllers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.beans.property.SimpleStringProperty;
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
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.entities.AnalyseInterview;
import org.example.entities.Session;
import org.example.entities.bourses;
import org.example.entities.demande;
import org.example.services.InterviewIAService;
import org.example.services.boursesService;
import org.example.services.demandeService;
import org.example.utils.BoursesDataBase;
import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class ListeDemandesController implements Initializable {

    // ── Demandes Table ─────────────────────────────────────────────────────
    @FXML private TableView<demande>             tableView;
    @FXML private TableColumn<demande, String>   colBourseTitre;
    @FXML private TableColumn<demande, String>   colNiveau;
    @FXML private TableColumn<demande, String>   colStatut;
    @FXML private TableColumn<demande, Timestamp> colDate;

    @FXML private TextField       tfRecherche;
    @FXML private ComboBox<String> cbStatut;
    @FXML private ComboBox<String> cbNiveau;

    // ── Interviews Table ───────────────────────────────────────────────────
    @FXML private TableView<AnalyseInterview>         tableInterviews;
    @FXML private TableColumn<AnalyseInterview, Integer> colId;
    @FXML private TableColumn<AnalyseInterview, Integer> colDemande;
    @FXML private TableColumn<AnalyseInterview, String>  colProfil;
    @FXML private TableColumn<AnalyseInterview, String>  colDateIA;

    // ── Interview Detail Panel ─────────────────────────────────────────────
    @FXML private VBox  vboxDetail;
    @FXML private Label lblBourseTitre;
    @FXML private Label lblProfil;
    @FXML private Label lblRecommandation;
    @FXML private Label lblTranscription;
    @FXML private VBox  vboxEmotions;
    @FXML private Label lblDebit;
    @FXML private Label lblHesitations;
    @FXML private Label lblEnergie;
    @FXML private Label lblDuree;

    // ── Services ───────────────────────────────────────────────────────────
    private final demandeService     dService    = new demandeService();
    private final boursesService     bService    = new boursesService();
    private final InterviewIAService iaService   = new InterviewIAService();

    private final ObservableList<demande>          masterList       = FXCollections.observableArrayList();
    private final ObservableList<AnalyseInterview> interviewList    = FXCollections.observableArrayList();
    private FilteredList<demande>                  filteredList;

    // ══════════════════════════════════════════════════════════════════════
    //  INIT
    // ══════════════════════════════════════════════════════════════════════
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupDemandesTable();
        setupInterviewsTable();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  DEMANDES SECTION
    // ══════════════════════════════════════════════════════════════════════
    private void setupDemandesTable() {
        colNiveau.setCellValueFactory(new PropertyValueFactory<>("niveau_etudes"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date_demande"));

        // Show bourse title instead of raw ID
        colBourseTitre.setCellValueFactory(cellData -> {
            int bourseId = cellData.getValue().getBourse_id();
            bourses b = bService.getById(bourseId);
            return new SimpleStringProperty(b != null ? b.getTitre() : "N/A");
        });

        initialiserFiltres();
        chargerDemandes();
        configurerRechercheAvancee();

        // Double-click → open DetailDemande page
        tableView.setOnMouseClicked((MouseEvent event) -> {
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
                demande selected = tableView.getSelectionModel().getSelectedItem();
                if (selected != null) ouvrirDetailDemande(selected);
            }
        });
    }

    private void initialiserFiltres() {
        cbStatut.setItems(FXCollections.observableArrayList("Tous", "En attente", "Acceptée", "Refusée"));
        cbStatut.setValue("Tous");
        cbNiveau.setItems(FXCollections.observableArrayList("Tous", "Liscence", "Master", "Doctorat", "Ingénieur"));
        cbNiveau.setValue("Tous");
    }

    private void chargerDemandes() {
        masterList.clear();
        masterList.addAll(dService.getAll());
    }

    private void configurerRechercheAvancee() {
        filteredList = new FilteredList<>(masterList, d -> true);
        tfRecherche.textProperty().addListener((obs, o, n) -> appliquerFiltres());
        cbStatut.valueProperty().addListener((obs, o, n) -> appliquerFiltres());
        cbNiveau.valueProperty().addListener((obs, o, n) -> appliquerFiltres());
        SortedList<demande> sortedList = new SortedList<>(filteredList);
        sortedList.comparatorProperty().bind(tableView.comparatorProperty());
        tableView.setItems(sortedList);
    }

    private void appliquerFiltres() {
        filteredList.setPredicate(d -> {
            String texte  = tfRecherche.getText();
            String statut = cbStatut.getValue();
            String niveau = cbNiveau.getValue();

            if (texte != null && !texte.isEmpty()) {
                bourses b = bService.getById(d.getBourse_id());
                String titre = (b != null) ? b.getTitre() : "";
                if (!titre.toLowerCase().contains(texte.toLowerCase())) return false;
            }
            if (statut != null && !statut.equals("Tous") && !d.getStatut().equalsIgnoreCase(statut)) return false;
            if (niveau != null && !niveau.equals("Tous") && !d.getNiveau_etudes().equalsIgnoreCase(niveau)) return false;
            return true;
        });
    }

    @FXML
    private void reinitialiserFiltres(ActionEvent event) {
        tfRecherche.clear();
        cbStatut.setValue("Tous");
        cbNiveau.setValue("Tous");
    }

    private void ouvrirDetailDemande(demande d) {
        try {
            demande fresh = dService.getById(d.getId());
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demandes/DetailDemande.fxml"));
            Parent root = loader.load();
            DetailDemandeController ctrl = loader.getController();
            ctrl.setDemande(fresh != null ? fresh : d);
            tableView.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  INTERVIEWS SECTION
    // ══════════════════════════════════════════════════════════════════════
    private void setupInterviewsTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colProfil.setCellValueFactory(new PropertyValueFactory<>("profilGlobal"));
        colDateIA.setCellValueFactory(new PropertyValueFactory<>("dateAnalyse"));

        // Show bourse title instead of demande ID
        colDemande.setCellFactory(col -> new TableCell<AnalyseInterview, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                demande d = dService.getById(item);
                if (d != null) {
                    bourses b = bService.getById(d.getBourse_id());
                    setText(b != null ? b.getTitre() : "Demande #" + item);
                } else {
                    setText("Demande #" + item);
                }
            }
        });

        vboxDetail.setVisible(false);
        vboxDetail.setManaged(false);

        chargerInterviews();

        // Single-click → show detail panel inline
        tableInterviews.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) afficherDetailInterview(newVal);
        });
    }

    private void chargerInterviews() {
        interviewList.clear();
        try {
            PreparedStatement pstmt = BoursesDataBase.getInstance().getConnection()
                    .prepareStatement("SELECT * FROM analyse_interview ORDER BY date_analyse DESC");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                AnalyseInterview a = new AnalyseInterview();
                a.setId(rs.getInt("id"));
                a.setDemandeId(rs.getInt("demande_id"));
                a.setTranscription(rs.getString("transcription"));
                a.setScoresEmotions(rs.getString("scores_emotions"));
                a.setFeaturesAudio(rs.getString("features_audio"));
                a.setProfilGlobal(rs.getString("profil_global"));
                a.setRecommandation(rs.getString("recommandation"));
                a.setDateAnalyse(rs.getTimestamp("date_analyse"));
                interviewList.add(a);
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) { e.printStackTrace(); }
        tableInterviews.setItems(interviewList);
    }

    private void afficherDetailInterview(AnalyseInterview analyse) {
        vboxDetail.setVisible(true);
        vboxDetail.setManaged(true);

        demande d = dService.getById(analyse.getDemandeId());
        if (d != null) {
            bourses b = bService.getById(d.getBourse_id());
            lblBourseTitre.setText(b != null ? b.getTitre() : "Bourse #" + d.getBourse_id());
        }

        lblProfil.setText(analyse.getProfilGlobal());
        lblRecommandation.setText(analyse.getRecommandation());
        lblTranscription.setText(analyse.getTranscription());

        vboxEmotions.getChildren().clear();
        try {
            JsonObject emotions = JsonParser.parseString(analyse.getScoresEmotions()).getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : emotions.entrySet()) {
                double score = entry.getValue().getAsDouble();
                int pct = (int) (score * 100);
                ProgressBar bar = new ProgressBar(score);
                bar.setPrefWidth(180);
                bar.setPrefHeight(16);
                if (score > 0.6)      bar.setStyle("-fx-accent: #2ecc71;");
                else if (score > 0.3) bar.setStyle("-fx-accent: #f5a623;");
                else                  bar.setStyle("-fx-accent: #e74c3c;");
                Label lbl = new Label(entry.getKey() + " : " + pct + "%");
                lbl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #333; -fx-min-width: 130;");
                HBox row = new HBox(10, lbl, bar);
                row.setAlignment(Pos.CENTER_LEFT);
                vboxEmotions.getChildren().add(row);
            }
        } catch (Exception ignored) {}

        try {
            JsonObject features = JsonParser.parseString(analyse.getFeaturesAudio()).getAsJsonObject();
            lblDebit.setText(features.get("debit_mots_par_min").getAsInt() + " mots/min");
            lblHesitations.setText(features.get("taux_hesitations_pct").getAsDouble() + "%");
            lblEnergie.setText(features.get("energie_vocale").getAsString());
            lblDuree.setText(features.get("duree_secondes").getAsDouble() + "s");
        } catch (Exception e) {
            lblDebit.setText("-"); lblHesitations.setText("-");
            lblEnergie.setText("-"); lblDuree.setText("-");
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  NAVIGATION
    // ══════════════════════════════════════════════════════════════════════
    @FXML
    void goToAdminUser(MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdminView.fxml"));
        Parent root = loader.load();
        ((javafx.scene.Node) event.getSource()).getScene().setRoot(root);
    }

    @FXML
    void goToAdminCours(MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin.fxml"));
        Parent root = loader.load();
        ((javafx.scene.Node) event.getSource()).getScene().setRoot(root);
    }

    @FXML
    void goToAdminCertification(MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/CertifAdmin.fxml"));
        Parent root = loader.load();
        ((javafx.scene.Node) event.getSource()).getScene().setRoot(root);
    }

    @FXML
    void goToAdminQuiz(MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/AdminQuiz.fxml"));
        Parent root = loader.load();
        ((javafx.scene.Node) event.getSource()).getScene().setRoot(root);
    }

    @FXML
    void goToAdminForum(MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/main.fxml"));
        Parent root = loader.load();

        MainController mainController = loader.getController();
        mainController.setCurrentUser(Session.getCurrentUser());

        ((javafx.scene.Node) event.getSource()).getScene().setRoot(root);
    }

    @FXML
    void goToAdminBadwords(MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/badword-view.fxml"));
        Parent root = loader.load();
        ((javafx.scene.Node) event.getSource()).getScene().setRoot(root);
    }


    @FXML
    void NavigateToBourses(MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/bourses/ListeBourses.fxml"));
        Parent root = loader.load();
        ((javafx.scene.Node) event.getSource()).getScene().setRoot(root);
    }
    @FXML
    void goToAdminEvents(MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Event.fxml"));
        Parent root = loader.load();
        ((javafx.scene.Node) event.getSource()).getScene().setRoot(root);
    }

    @FXML
    void NavigateToDemandes(MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demandes/ListeDemandes.fxml"));
        Parent root = loader.load();
        ((javafx.scene.Node) event.getSource()).getScene().setRoot(root);
    }


    @FXML
    private void allerAccueil(ActionEvent event) {
        try {
            Session.logout();
        } catch (Exception e) {
            return;
        }
    }

    private void naviguerVers(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            tableView.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }
}