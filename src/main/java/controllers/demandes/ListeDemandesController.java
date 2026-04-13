package controllers.demandes;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import models.demande;
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
    private TableColumn<demande, Integer> colBourseId;

    @FXML
    private Button btnAjouter;

    private demandeService service = new demandeService();
    private ObservableList<demande> masterList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colNiveau.setCellValueFactory(new PropertyValueFactory<>("niveau_etudes"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date_demande"));
        colBourseId.setCellValueFactory(new PropertyValueFactory<>("bourse_id"));

        chargerDemandes();

        // Double-clic sur une ligne → ouvrir DetailDemande.fxml
        tableView.setOnMouseClicked((MouseEvent event) -> {
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
                demande selected = tableView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    ouvrirDetail(selected);
                }
            }
        });
    }

    private void chargerDemandes() {
        masterList.clear();
        List<demande> list = service.getAll();
        masterList.addAll(list);
        tableView.setItems(masterList);
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
