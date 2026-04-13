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
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import models.bourses;
import services.boursesService;

import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
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

    @FXML
    private TextField tfRecherche;
    @FXML
    private Button btnPostuler;

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

        // Enable Postuler button only if a row is selected
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                btnPostuler.setDisable(false);
            } else {
                btnPostuler.setDisable(true);
            }
        });
    }

    private void chargerBourses() {
        masterList.clear();
        List<bourses> list = service.getAll();
        masterList.addAll(list);
    }

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
