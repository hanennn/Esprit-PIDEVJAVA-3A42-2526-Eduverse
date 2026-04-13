package controllers.bourses;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import models.bourses;
import services.boursesService;

import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.List;
import java.util.ResourceBundle;

public class ListeBoursesController implements Initializable {

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

    @FXML
    private TextField tfRecherche;
    @FXML
    private Label lblTotal;
    @FXML
    private Label lblMontantMoyen;
    @FXML
    private Label lblMontantMax;
    @FXML
    private Button btnAjouter;

    private boursesService service = new boursesService();
    private ObservableList<bourses> masterList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colMontant.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colDateAttr.setCellValueFactory(new PropertyValueFactory<>("date_attribution"));
        colDateFin.setCellValueFactory(new PropertyValueFactory<>("date_fin"));

        chargerBourses();
        configurerRecherche();

        // Double-clic sur une ligne → ouvrir DetailBourse.fxml
        tableView.setOnMouseClicked((MouseEvent event) -> {
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
                bourses selected = tableView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    ouvrirDetail(selected);
                }
            }
        });
    }

    /**
     * Charge toutes les bourses depuis la BD et les affiche dans la TableView.
     * Met à jour les statistiques (total, moyenne, max) en bas de l'écran.
     */
    private void chargerBourses() {
        masterList.clear();
        List<bourses> list = service.getAll();
        masterList.addAll(list);
        
        calculerStatistiques();
    }

    private void calculerStatistiques() {
        int total = masterList.size();
        double sum = 0;
        double max = 0;
        
        for (bourses b : masterList) {
            double current = b.getMontant();
            sum += current;
            if (current > max) {
                max = current;
            }
        }
        
        double avg = total > 0 ? sum / total : 0;
        
        lblTotal.setText("Total bourses : " + total);
        lblMontantMoyen.setText(String.format("Montant moyen : %.2f DT", avg));
        lblMontantMax.setText(String.format("Bourse max : %.2f DT", max));
    }

    /**
     * Filtre la liste en temps réel selon le texte saisi dans tfRecherche.
     * Utilise FilteredList pour ne pas recharger la BD à chaque frappe.
     */
    private void configurerRecherche() {
        FilteredList<bourses> filteredData = new FilteredList<>(masterList, b -> true);
        
        tfRecherche.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(b -> {
                if (newVal == null || newVal.isEmpty()) return true;
                return b.getTitre().toLowerCase().contains(newVal.toLowerCase());
            });
        });
        
        SortedList<bourses> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableView.comparatorProperty());
        tableView.setItems(sortedData);
    }

    @FXML
    private void ouvrirAjout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/bourses/AjoutBourse.fxml"));
            Parent root = loader.load();
            btnAjouter.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void ouvrirDetail(bourses b) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/bourses/DetailBourse.fxml"));
            Parent root = loader.load();
            DetailBourseController ctrl = loader.getController();
            ctrl.setBourse(b);
            tableView.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void ouvrirDemandes(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/demandes/ListeDemandes.fxml"));
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
