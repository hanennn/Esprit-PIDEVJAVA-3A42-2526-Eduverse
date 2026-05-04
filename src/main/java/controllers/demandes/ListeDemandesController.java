package controllers.demandes;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import models.demande;
import services.boursesService;
import services.demandeService;

import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.List;
import java.util.ResourceBundle;

public class ListeDemandesController implements Initializable {

    @FXML private TableView<demande> tableView;
    @FXML private TableColumn<demande, String> colNiveau;
    @FXML private TableColumn<demande, String> colStatut;
    @FXML private TableColumn<demande, Timestamp> colDate;
    @FXML private TableColumn<demande, String> colBourseTitre;

    @FXML private TextField tfRecherche;
    @FXML private ComboBox<String> cbStatut;
    @FXML private ComboBox<String> cbNiveau;
    @FXML private Button btnAjouter;

    private demandeService service = new demandeService();
    private boursesService bService = new boursesService();
    private ObservableList<demande> masterList = FXCollections.observableArrayList();
    private FilteredList<demande> filteredList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colNiveau.setCellValueFactory(new PropertyValueFactory<>("niveau_etudes"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date_demande"));

        // Afficher le titre de la bourse au lieu de l'ID
        colBourseTitre.setCellValueFactory(cellData -> {
            int bourseId = cellData.getValue().getBourse_id();
            var bourse = bService.getById(bourseId);
            return new SimpleStringProperty(bourse != null ? bourse.getTitre() : "N/A");
        });

        initialiserFiltres();
        chargerDemandes();
        configurerRechercheAvancee();

        tableView.setOnMouseClicked((MouseEvent event) -> {
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
                demande selected = tableView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    ouvrirDetail(selected);
                }
            }
        });
    }

    private void initialiserFiltres() {
        cbStatut.setItems(FXCollections.observableArrayList(
                "Tous", "En attente", "Acceptée", "Refusée"
        ));
        cbStatut.setValue("Tous");

        cbNiveau.setItems(FXCollections.observableArrayList(
                "Tous", "Liscence", "Master", "Doctorat", "Ingénieur"
        ));
        cbNiveau.setValue("Tous");
    }

    private void configurerRechercheAvancee() {
        filteredList = new FilteredList<>(masterList, d -> true);

        tfRecherche.textProperty().addListener((obs, oldVal, newVal) -> appliquerFiltres());
        cbStatut.valueProperty().addListener((obs, oldVal, newVal) -> appliquerFiltres());
        cbNiveau.valueProperty().addListener((obs, oldVal, newVal) -> appliquerFiltres());

        SortedList<demande> sortedList = new SortedList<>(filteredList);
        sortedList.comparatorProperty().bind(tableView.comparatorProperty());
        tableView.setItems(sortedList);
    }

    private void appliquerFiltres() {
        filteredList.setPredicate(d -> {
            String texteRecherche = tfRecherche.getText();
            String statutFiltre = cbStatut.getValue();
            String niveauFiltre = cbNiveau.getValue();

            if (texteRecherche != null && !texteRecherche.isEmpty()) {
                var bourse = bService.getById(d.getBourse_id());
                String titreBourse = (bourse != null) ? bourse.getTitre() : "";
                if (!titreBourse.toLowerCase().contains(texteRecherche.toLowerCase())) {
                    return false;
                }
            }

            if (statutFiltre != null && !statutFiltre.equals("Tous")) {
                if (!d.getStatut().equalsIgnoreCase(statutFiltre)) {
                    return false;
                }
            }

            if (niveauFiltre != null && !niveauFiltre.equals("Tous")) {
                if (!d.getNiveau_etudes().equalsIgnoreCase(niveauFiltre)) {
                    return false;
                }
            }

            return true;
        });
    }

    @FXML
    private void reinitialiserFiltres(ActionEvent event) {
        tfRecherche.clear();
        cbStatut.setValue("Tous");
        cbNiveau.setValue("Tous");
    }

    private void chargerDemandes() {
        masterList.clear();
        List<demande> list = service.getAll();
        masterList.addAll(list);
    }

    @FXML
    private void ouvrirAjout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/demandes/AjoutDemande.fxml"));
            Parent root = loader.load();
            btnAjouter.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void ouvrirDetail(demande d) {
        try {
            demande fresh = service.getById(d.getId());
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/demandes/DetailDemande.fxml"));
            Parent root = loader.load();
            DetailDemandeController ctrl = loader.getController();
            ctrl.setDemande(fresh != null ? fresh : d);
            tableView.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void retourBourses(ActionEvent event) { naviguerVers("/bourses/ListeBourses.fxml"); }
    @FXML
    private void allerStatistiques(ActionEvent event) { naviguerVers("/bourses/Statistiques.fxml"); }
    @FXML
    private void allerInterviewIA(ActionEvent event) { naviguerVers("/demandes/ListeInterviews.fxml"); }
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
