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
import models.Notification;
import models.bourses;
import services.NotificationService;
import services.boursesService;

import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

public class EtudiantBoursesController implements Initializable {

    @FXML private TableView<bourses> tableView;
    @FXML private TableColumn<bourses, String> colTitre;
    @FXML private TableColumn<bourses, Double> colMontant;
    @FXML private TableColumn<bourses, Timestamp> colDateAttr;
    @FXML private TableColumn<bourses, Timestamp> colDateFin;

    @FXML private TextField tfRecherche;
    @FXML private ComboBox<String> cbTri;
    @FXML private Button btnPostuler;
    @FXML private Label lblNotifBadge;

    private boursesService service = new boursesService();
    private NotificationService notifService = new NotificationService();
    private ObservableList<bourses> masterList = FXCollections.observableArrayList();
    private FilteredList<bourses> filteredList;
    private SortedList<bourses> sortedList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colMontant.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colDateAttr.setCellValueFactory(new PropertyValueFactory<>("date_attribution"));
        colDateFin.setCellValueFactory(new PropertyValueFactory<>("date_fin"));

        initialiserTri();
        chargerBourses();
        configurerRechercheAvancee();
        afficherNotifications();

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            btnPostuler.setDisable(newSelection == null);
        });
    }

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
                if (!b.getTitre().toLowerCase().contains(texte.toLowerCase())) {
                    return false;
                }
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

    private void afficherNotifications() {
        int count = notifService.compterNonLues();
        if (lblNotifBadge != null) {
            if (count > 0) {
                lblNotifBadge.setText(count + " nouvelle(s) notification(s)");
                lblNotifBadge.setVisible(true);

                java.util.List<Notification> notifs = notifService.getNonLues();
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

    @FXML
    private void allerInterviewIA(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/etudiant/InterviewEtudiant.fxml"));
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
