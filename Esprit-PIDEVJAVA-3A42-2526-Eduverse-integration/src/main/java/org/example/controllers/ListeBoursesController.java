package org.example.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.example.entities.bourses;
import org.example.entities.demande;
import org.example.entities.Session;
import org.example.services.boursesService;
import org.example.services.demandeService;

import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class ListeBoursesController implements Initializable {

    // ── Table ──────────────────────────────────────────────────────────────
    @FXML private TableView<bourses>             tableView;
    @FXML private TableColumn<bourses, String>   colTitre;
    @FXML private TableColumn<bourses, Double>   colMontant;
    @FXML private TableColumn<bourses, Timestamp> colDateAttr;
    @FXML private TableColumn<bourses, Timestamp> colDateFin;
    @FXML private TextField                       tfRecherche;
    @FXML private Button                          btnAjouter;

    // ── KPI Labels ─────────────────────────────────────────────────────────
    @FXML private Label lblTotalBourses;
    @FXML private Label lblTotalDemandes;
    @FXML private Label lblTauxAcceptation;
    @FXML private Label lblMontantMoyen;
    @FXML private Label lblMontantTotal;

    // ── Charts ─────────────────────────────────────────────────────────────
    @FXML private PieChart                        pieStatut;
    @FXML private BarChart<String, Number>        barMontants;
    @FXML private PieChart                        pieNiveau;
    @FXML private BarChart<String, Number>        barDemandesParBourse;

    // ── Services ───────────────────────────────────────────────────────────
    private final boursesService  bService    = new boursesService();
    private final demandeService  dService    = new demandeService();
    private final ObservableList<bourses> masterList = FXCollections.observableArrayList();

    // ══════════════════════════════════════════════════════════════════════
    //  INIT
    // ══════════════════════════════════════════════════════════════════════
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Table columns
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colMontant.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colDateAttr.setCellValueFactory(new PropertyValueFactory<>("date_attribution"));
        colDateFin.setCellValueFactory(new PropertyValueFactory<>("date_fin"));

        chargerBourses();
        configurerRecherche();

        // Double-click to open detail
        tableView.setOnMouseClicked((MouseEvent event) -> {
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
                bourses selected = tableView.getSelectionModel().getSelectedItem();
                if (selected != null) ouvrirDetail(selected);
            }
        });

        // Statistics
        List<demande> listeDemandes = dService.getAll();
        calculerCartes(masterList, listeDemandes);
        remplirPieStatut(listeDemandes);
        remplirBarMontants(masterList);
        remplirPieNiveau(listeDemandes);
        remplirBarDemandesParBourse(listeDemandes, masterList);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  TABLE LOGIC
    // ══════════════════════════════════════════════════════════════════════
    private void chargerBourses() {
        masterList.clear();
        masterList.addAll(bService.getAll());
    }

    private void configurerRecherche() {
        FilteredList<bourses> filteredData = new FilteredList<>(masterList, b -> true);
        tfRecherche.textProperty().addListener((obs, oldVal, newVal) ->
                filteredData.setPredicate(b -> {
                    if (newVal == null || newVal.isEmpty()) return true;
                    return b.getTitre().toLowerCase().contains(newVal.toLowerCase());
                })
        );
        SortedList<bourses> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableView.comparatorProperty());
        tableView.setItems(sortedData);
    }

    @FXML
    private void ouvrirAjout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/bourses/AjoutBourse.fxml"));
            Parent root = loader.load();
            btnAjouter.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void ouvrirDetail(bourses b) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/bourses/DetailBourse.fxml"));
            Parent root = loader.load();
            DetailBourseController ctrl = loader.getController();
            ctrl.setBourse(b);
            tableView.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  STATISTICS LOGIC  (ported from StatistiquesController)
    // ══════════════════════════════════════════════════════════════════════
    private void calculerCartes(List<bourses> listeBourses, List<demande> listeDemandes) {
        int totalBourses   = listeBourses.size();
        int totalDemandes  = listeDemandes.size();

        double montantTotal = 0;
        for (bourses b : listeBourses) montantTotal += b.getMontant();
        double montantMoyen = totalBourses > 0 ? montantTotal / totalBourses : 0;

        long nbAcceptees = listeDemandes.stream()
                .filter(d -> "Acceptee".equalsIgnoreCase(d.getStatut()))
                .count();
        double tauxAcceptation = totalDemandes > 0 ? (nbAcceptees * 100.0 / totalDemandes) : 0;

        lblTotalBourses.setText(String.valueOf(totalBourses));
        lblTotalDemandes.setText(String.valueOf(totalDemandes));
        lblTauxAcceptation.setText(String.format("%.0f%%", tauxAcceptation));
        lblMontantMoyen.setText(String.format("%.0f DT", montantMoyen));
        lblMontantTotal.setText(String.format("%.0f DT", montantTotal));
    }

    private void remplirPieStatut(List<demande> listeDemandes) {
        Map<String, Integer> statutCount = new HashMap<>();
        for (demande d : listeDemandes) {
            String statut = d.getStatut() != null ? d.getStatut() : "Inconnu";
            statutCount.merge(statut, 1, Integer::sum);
        }
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        statutCount.forEach((k, v) -> pieData.add(new PieChart.Data(k + " (" + v + ")", v)));
        pieStatut.setData(pieData);
    }

    private void remplirBarMontants(List<bourses> listeBourses) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (bourses b : listeBourses) {
            String titre = b.getTitre().length() > 15 ? b.getTitre().substring(0, 13) + ".." : b.getTitre();
            series.getData().add(new XYChart.Data<>(titre, b.getMontant()));
        }
        barMontants.getData().add(series);
    }

    private void remplirPieNiveau(List<demande> listeDemandes) {
        Map<String, Integer> niveauCount = new HashMap<>();
        for (demande d : listeDemandes) {
            String niveau = d.getNiveau_etudes() != null ? d.getNiveau_etudes() : "Inconnu";
            niveauCount.merge(niveau, 1, Integer::sum);
        }
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        niveauCount.forEach((k, v) -> pieData.add(new PieChart.Data(k + " (" + v + ")", v)));
        pieNiveau.setData(pieData);
    }

    private void remplirBarDemandesParBourse(List<demande> listeDemandes, List<bourses> listeBourses) {
        Map<Integer, String> bourseTitres = new HashMap<>();
        for (bourses b : listeBourses) bourseTitres.put(b.getId(), b.getTitre());

        Map<Integer, Integer> demandeCount = new HashMap<>();
        for (demande d : listeDemandes) demandeCount.merge(d.getBourse_id(), 1, Integer::sum);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        demandeCount.forEach((id, count) -> {
            String titre = bourseTitres.getOrDefault(id, "Bourse #" + id);
            if (titre.length() > 15) titre = titre.substring(0, 13) + "..";
            series.getData().add(new XYChart.Data<>(titre, count));
        });
        barDemandesParBourse.getData().add(series);
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
}