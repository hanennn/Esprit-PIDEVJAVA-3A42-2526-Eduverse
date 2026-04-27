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

    @FXML
    private TableView<demande> tableView;
    @FXML
    private TableColumn<demande, String> colNiveau;
    @FXML
    private TableColumn<demande, String> colStatut;
    @FXML
    private TableColumn<demande, Timestamp> colDate;
    @FXML
    private TableColumn<demande, String> colBourseTitre;

    // --- Composants de recherche avancee ---
    @FXML
    private TextField tfRecherche;       // Champ de recherche par titre de bourse
    @FXML
    private ComboBox<String> cbStatut;   // Filtre par statut (En attente, Acceptee, Refusee)
    @FXML
    private ComboBox<String> cbNiveau;   // Filtre par niveau d'etudes

    @FXML
    private Button btnAjouter;

    private demandeService service = new demandeService();
    private boursesService bService = new boursesService();
    private ObservableList<demande> masterList = FXCollections.observableArrayList();

    // Liste filtree qui reagit aux changements des filtres en temps reel
    private FilteredList<demande> filteredList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // --- Configuration des colonnes du tableau ---
        colNiveau.setCellValueFactory(new PropertyValueFactory<>("niveau_etudes"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date_demande"));

        // Affichage du titre de la bourse au lieu de l'ID
        colBourseTitre.setCellValueFactory(cellData -> {
            int bourseId = cellData.getValue().getBourse_id();
            var bourse = bService.getById(bourseId);
            return new SimpleStringProperty(bourse != null ? bourse.getTitre() : "N/A");
        });

        // --- Initialisation des ComboBox de filtrage ---
        initialiserFiltres();

        // --- Chargement des donnees et activation de la recherche ---
        chargerDemandes();
        configurerRechercheAvancee();

        // Double-clic sur une ligne pour ouvrir le detail de la demande
        tableView.setOnMouseClicked((MouseEvent event) -> {
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
                demande selected = tableView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    ouvrirDetail(selected);
                }
            }
        });
    }

    // Remplir les ComboBox avec les valeurs possibles
    private void initialiserFiltres() {
        // Options de statut : "Tous" permet de ne pas filtrer
        cbStatut.setItems(FXCollections.observableArrayList(
                "Tous", "En attente", "Acceptee", "Refusee"
        ));
        cbStatut.setValue("Tous");

        // Options de niveau d'etudes
        cbNiveau.setItems(FXCollections.observableArrayList(
                "Tous", "Licence", "Master", "Doctorat", "Ingenieur"
        ));
        cbNiveau.setValue("Tous");
    }

    // Configuration de la recherche multicriteres :
    // - Recherche textuelle par titre de bourse
    // - Filtre par statut via ComboBox
    // - Filtre par niveau d'etudes via ComboBox
    // Tous les filtres fonctionnent ensemble en temps reel
    private void configurerRechercheAvancee() {
        // Creation de la liste filtree a partir de la liste principale
        filteredList = new FilteredList<>(masterList, d -> true);

        // Ecouter les changements sur le champ de recherche texte
        tfRecherche.textProperty().addListener((obs, oldVal, newVal) -> appliquerFiltres());

        // Ecouter les changements sur le filtre statut
        cbStatut.valueProperty().addListener((obs, oldVal, newVal) -> appliquerFiltres());

        // Ecouter les changements sur le filtre niveau
        cbNiveau.valueProperty().addListener((obs, oldVal, newVal) -> appliquerFiltres());

        // SortedList permet le tri en cliquant sur les en-tetes de colonnes
        SortedList<demande> sortedList = new SortedList<>(filteredList);
        sortedList.comparatorProperty().bind(tableView.comparatorProperty());
        tableView.setItems(sortedList);
    }

    // Applique tous les filtres combines (texte + statut + niveau)
    private void appliquerFiltres() {
        filteredList.setPredicate(d -> {
            // Recuperer les valeurs actuelles des filtres
            String texteRecherche = tfRecherche.getText();
            String statutFiltre = cbStatut.getValue();
            String niveauFiltre = cbNiveau.getValue();

            // Filtre 1 : Recherche par titre de bourse (texte libre)
            if (texteRecherche != null && !texteRecherche.isEmpty()) {
                var bourse = bService.getById(d.getBourse_id());
                String titreBourse = (bourse != null) ? bourse.getTitre() : "";
                if (!titreBourse.toLowerCase().contains(texteRecherche.toLowerCase())) {
                    return false;
                }
            }

            // Filtre 2 : Filtrage par statut de la demande
            if (statutFiltre != null && !statutFiltre.equals("Tous")) {
                if (!d.getStatut().equalsIgnoreCase(statutFiltre)) {
                    return false;
                }
            }

            // Filtre 3 : Filtrage par niveau d'etudes
            if (niveauFiltre != null && !niveauFiltre.equals("Tous")) {
                if (!d.getNiveau_etudes().equalsIgnoreCase(niveauFiltre)) {
                    return false;
                }
            }

            // Si tous les filtres passent, la demande est affichee
            return true;
        });
    }

    // Reinitialiser tous les filtres a leur valeur par defaut
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/demandes/DetailDemande.fxml"));
            Parent root = loader.load();
            DetailDemandeController ctrl = loader.getController();
            ctrl.setDemande(d);
            tableView.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void retourBourses(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/bourses/ListeBourses.fxml"));
            Parent root = loader.load();
            tableView.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
