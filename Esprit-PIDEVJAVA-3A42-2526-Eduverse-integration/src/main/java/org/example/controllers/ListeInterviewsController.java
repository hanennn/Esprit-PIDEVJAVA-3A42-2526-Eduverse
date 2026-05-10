package org.example.controllers;

import org.example.entities.Session;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.entities.AnalyseInterview;
import org.example.entities.bourses;
import org.example.entities.demande;
import org.example.services.InterviewIAService;
import org.example.services.boursesService;
import org.example.services.demandeService;

import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class ListeInterviewsController implements Initializable {

    @FXML private TableView<AnalyseInterview> tableView;
    @FXML private TableColumn<AnalyseInterview, Integer> colId;
    @FXML private TableColumn<AnalyseInterview, Integer> colDemande;
    @FXML private TableColumn<AnalyseInterview, String> colProfil;
    @FXML private TableColumn<AnalyseInterview, String> colDate;

    @FXML private VBox vboxDetail;
    @FXML private Label lblBourseTitre;
    @FXML private Label lblProfil;
    @FXML private Label lblRecommandation;
    @FXML private Label lblTranscription;
    @FXML private VBox vboxEmotions;
    @FXML private Label lblDebit;
    @FXML private Label lblHesitations;
    @FXML private Label lblEnergie;
    @FXML private Label lblDuree;

    private InterviewIAService iaService = new InterviewIAService();
    private demandeService dService = new demandeService();
    private boursesService bService = new boursesService();
    private ObservableList<AnalyseInterview> masterList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("id"));
        colDemande.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("demandeId"));
        colProfil.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("profilGlobal"));
        colDate.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("dateAnalyse"));

        // Afficher le titre de la bourse au lieu de l'ID
        colDemande.setCellFactory(col -> new TableCell<AnalyseInterview, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    demande d = dService.getById(item);
                    if (d != null) {
                        bourses b = bService.getById(d.getBourse_id());
                        setText(b != null ? b.getTitre() : "Demande #" + item);
                    } else {
                        setText("Demande #" + item);
                    }
                }
            }
        });

        vboxDetail.setVisible(false);
        vboxDetail.setManaged(false);

        chargerInterviews();

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                afficherDetail(newVal);
            }
        });
    }

    private void chargerInterviews() {
        masterList.clear();
        try {
            PreparedStatement pstmt = org.example.utils.BoursesDataBase.getInstance().getConnection()
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
                masterList.add(a);
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        tableView.setItems(masterList);
    }

    private void afficherDetail(AnalyseInterview analyse) {
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
                if (score > 0.6) bar.setStyle("-fx-accent: #2ecc71;");
                else if (score > 0.3) bar.setStyle("-fx-accent: #f5a623;");
                else bar.setStyle("-fx-accent: #e74c3c;");
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
            lblDebit.setText("-"); lblHesitations.setText("-"); lblEnergie.setText("-"); lblDuree.setText("-");
        }
    }

    @FXML
    private void allerBourses(ActionEvent event) { naviguerVers("/org/example/bourses/ListeBourses.fxml"); }
    @FXML
    private void allerDemandes(ActionEvent event) { naviguerVers("/org/example/demandes/ListeDemandes.fxml"); }
    @FXML
    private void allerStatistiques(ActionEvent event) { naviguerVers("/org/example/bourses/Statistiques.fxml"); }
    @FXML
    private void allerInterviewIA(ActionEvent event) { }
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
