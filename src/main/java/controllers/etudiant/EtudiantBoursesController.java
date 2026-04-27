package controllers.etudiant;

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
import javafx.scene.control.cell.PropertyValueFactory;
import models.bourses;
import services.boursesService;

import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

public class EtudiantBoursesController implements Initializable {

    @FXML
    private TableView<bourses> tableView;
    @FXML
    private TableColumn<bourses, String> colTitre;
    @FXML
    private TableColumn<bourses, Double> colMontant;
    @FXML
    private TableColumn<bourses, Timestamp> colDateAttr;
    @FXML
    private TableColumn<bourses, Timestamp> colDateFin;

    // --- Composants de recherche avancee ---
    @FXML
    private TextField tfRecherche;     // Recherche par titre
    @FXML
    private ComboBox<String> cbTri;    // Tri rapide par critere

    @FXML
    private Button btnPostuler;

    private boursesService service = new boursesService();
    private ObservableList<bourses> masterList = FXCollections.observableArrayList();

    // Liste filtree pour la recherche multicriteres en temps reel
    private FilteredList<bourses> filteredList;
    // Liste triee qui enveloppe la liste filtree
    private SortedList<bourses> sortedList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // --- Configuration des colonnes ---
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colMontant.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colDateAttr.setCellValueFactory(new PropertyValueFactory<>("date_attribution"));
        colDateFin.setCellValueFactory(new PropertyValueFactory<>("date_fin"));

        // --- Initialisation du ComboBox de tri ---
        initialiserTri();

        // --- Chargement des bourses et activation des filtres ---
        chargerBourses();
        configurerRechercheAvancee();

        // Activer le bouton Postuler seulement quand une bourse est selectionnee
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            btnPostuler.setDisable(newSelection == null);
        });
    }

    // Remplir le ComboBox avec les options de tri disponibles
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

    // Configuration de la recherche : texte par titre + tri rapide via ComboBox
    private void configurerRechercheAvancee() {
        filteredList = new FilteredList<>(masterList, b -> true);

        // Ecouter les changements sur le champ de recherche texte
        tfRecherche.textProperty().addListener((obs, oldVal, newVal) -> appliquerFiltres());

        // Ecouter le changement de tri dans le ComboBox
        cbTri.valueProperty().addListener((obs, oldVal, newVal) -> appliquerTri());

        // Encapsuler la liste filtree dans une SortedList pour le tri
        sortedList = new SortedList<>(filteredList);
        sortedList.comparatorProperty().bind(tableView.comparatorProperty());
        tableView.setItems(sortedList);
    }

    // Filtre par titre de bourse uniquement
    private void appliquerFiltres() {
        filteredList.setPredicate(b -> {
            String texte = tfRecherche.getText();
            if (texte != null && !texte.isEmpty()) {
                if (!b.getTitre().toLowerCase().contains(texte.toLowerCase())) {
                    return false;
                }
            }
            return true;
        });
    }

    // Appliquer le tri selectionne dans le ComboBox
    private void appliquerTri() {
        String tri = cbTri.getValue();
        if (tri == null || tri.equals("Par defaut")) {
            // Tri par defaut : delier le comparateur pour laisser l'ordre naturel
            sortedList.comparatorProperty().unbind();
            sortedList.setComparator(null);
            sortedList.comparatorProperty().bind(tableView.comparatorProperty());
            return;
        }

        // Delier le comparateur du tableau pour appliquer le tri personnalise
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

    // Reinitialiser la recherche et le tri
    @FXML
    private void reinitialiserFiltres(ActionEvent event) {
        tfRecherche.clear();
        cbTri.setValue("Par defaut");
    }

    private void chargerBourses() {
        masterList.clear();
        List<bourses> list = service.getAll();
        masterList.addAll(list);
    }

    @FXML
    private void ouvrirPostuler(ActionEvent event) {
        bourses selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/etudiant/PostulerBourse.fxml"));
            Parent root = loader.load();

            PostulerController ctrl = loader.getController();
            ctrl.setBourseSelected(selected);

            btnPostuler.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Ouvrir la vue calendrier des bourses
    @FXML
    private void ouvrirCalendrier(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/etudiant/CalendrierBourses.fxml"));
            Parent root = loader.load();
            tableView.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void deconnexion(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/accueil/Accueil.fxml"));
            Parent root = loader.load();
            tableView.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
