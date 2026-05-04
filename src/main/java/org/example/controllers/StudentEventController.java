package org.example.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.entities.Event;
import org.example.entities.EventInscription;
import org.example.entities.Session;
import org.example.entities.User;
import org.example.services.EventService;
import org.example.services.EventInscriptionService;

import java.io.IOException;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

public class StudentEventController {

    // ── Sidebar UI Elements ─────────────────────────────────────
    @FXML private Button btnDispo;
    @FXML private Button btnMesInsc;
    @FXML private VBox viewDispo;
    @FXML private VBox viewInsc;
    @FXML private Label lblPagePath;

    // ── Événements disponibles ──────────────────────────────────
    @FXML private TableView<Event>         tvDispo;
    @FXML private TableColumn<Event, String>  colTitre;
    @FXML private TableColumn<Event, String>  colDesc;
    @FXML private TableColumn<Event, Date>    colDate;

    // ── Mes participations ──────────────────────────────────────
    @FXML private TableView<EventInscription>     tvMesInsc;
    @FXML private TableColumn<EventInscription, String>    colTitreInsc;
    @FXML private TableColumn<EventInscription, Timestamp> colDateInsc;
    @FXML private TableColumn<EventInscription, String>    colStatut;

    // ── Contrôles statut ────────────────────────────────────────
    @FXML private ComboBox<String>  cbStatut;

    private EventService             eventService;
    private EventInscriptionService  inscriptionService;

    /** Identifiant étudiant récupéré de la session */
    private int getCurrentUserId() {
        User user = Session.getCurrentUser();
        return (user != null) ? user.getId() : -1;
    }

    @FXML
    public void initialize() {
        eventService       = new EventService();
        inscriptionService = new EventInscriptionService();

        // Colonnes tableau des événements disponibles
        colTitre  .setCellValueFactory(new PropertyValueFactory<>("titre"));
        colDesc   .setCellValueFactory(new PropertyValueFactory<>("description"));
        colDate   .setCellValueFactory(new PropertyValueFactory<>("date"));

        // Colonnes tableau de mes inscriptions
        colDateInsc   .setCellValueFactory(new PropertyValueFactory<>("dateInscription"));
        colStatut     .setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Colonne Titre événement (calculée)
        colTitreInsc.setCellValueFactory(cellData -> {
            int eId = cellData.getValue().getEventId();
            Event e = eventService.getById(eId);
            String titre = (e != null) ? e.getTitre() : "(inconnu)";
            return new SimpleStringProperty(titre);
        });

        // ComboBoxes
        cbStatut.setItems(FXCollections.observableArrayList("Inscrit", "En attente", "Annulé"));

        // Pré-remplir statut quand on sélectionne une inscription
        tvMesInsc.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                cbStatut.setValue(newSel.getStatut());
            }
        });

        switchView("Dispo");
        loadData();
    }

    // ── Navigation UI ──────────────────────────────────────────
    private void resetSidebar() {
        btnDispo.getStyleClass().setAll("button", "sidebar-btn");
        btnMesInsc.getStyleClass().setAll("button", "sidebar-btn");

        viewDispo.setVisible(false);
        viewInsc.setVisible(false);
    }

    private void switchView(String viewName) {
        resetSidebar();
        switch(viewName) {
            case "Dispo":
                btnDispo.getStyleClass().setAll("button", "sidebar-btn-active");
                viewDispo.setVisible(true);
                lblPagePath.setText("🏠 Accueil > Événements Disponibles");
                break;
            case "Insc":
                btnMesInsc.getStyleClass().setAll("button", "sidebar-btn-active");
                viewInsc.setVisible(true);
                lblPagePath.setText("🏠 Accueil > Mes Participations");
                break;
        }
    }

    @FXML private void showDispo(ActionEvent e) { switchView("Dispo"); }
    @FXML private void showMesInsc(ActionEvent e) { switchView("Insc"); }

    // ── Chargement des données ──────────────────────────────────

    private void loadData() {
        List<Event> events = eventService.getAll();
        tvDispo.setItems(FXCollections.observableArrayList(events));

        List<EventInscription> myInscriptions = inscriptionService.getAll().stream()
                .filter(i -> i.getParticipantId() == getCurrentUserId())
                .collect(Collectors.toList());
        tvMesInsc.setItems(FXCollections.observableArrayList(myInscriptions));
    }

    // ── Voir Détails ────────────────────────────────────────────

    @FXML
    private void handleViewDetail(ActionEvent event) {
        Event selected = tvDispo.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Aucune sélection", "Veuillez sélectionner un événement pour voir ses détails.");
            return;
        }
        EventDetailHelper.openDetail(selected);
    }

    // ── S'inscrire ──────────────────────────────────────────────

    @FXML
    private void handleInscription(ActionEvent event) {
        Event selected = tvDispo.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Action impossible", "Veuillez sélectionner un événement.");
            return;
        }
        boolean alreadyInscribed = tvMesInsc.getItems().stream()
                .anyMatch(i -> i.getEventId() == selected.getId());
        if (alreadyInscribed) {
            showAlert("Information", "Vous êtes déjà inscrit à cet événement.");
            return;
        }
        EventInscription insc = new EventInscription();
        int userId = getCurrentUserId();
        if (userId == -1) {
            showAlert("Erreur", "Session invalide. Veuillez vous reconnecter.");
            return;
        }
        insc.setParticipantId(userId);
        insc.setEventId(selected.getId());
        insc.setDateInscription(new Timestamp(System.currentTimeMillis()));
        insc.setStatut("Inscrit");

        try {
            inscriptionService.add(insc);
            loadData();
            showAlert("Succès", "Vous êtes maintenant inscrit à l'événement « " + selected.getTitre() + " ».");
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de s'inscrire : " + e.getMessage());
        }
    }

    // ── Se désinscrire ──────────────────────────────────────────

    @FXML
    private void handleDesinscription(ActionEvent event) {
        EventInscription selected = tvMesInsc.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Action impossible", "Veuillez sélectionner une inscription.");
            return;
        }
        inscriptionService.delete(selected.getId());
        loadData();
    }

    // ── Modifier statut ─────────────────────────────────────────

    @FXML
    private void handleUpdateInscription(ActionEvent event) {
        EventInscription selected = tvMesInsc.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Action impossible", "Veuillez sélectionner une inscription à modifier.");
            return;
        }
        String newStatut = cbStatut.getValue();
        if (newStatut != null && !newStatut.isEmpty()) {
            selected.setStatut(newStatut);
            inscriptionService.update(selected);
            loadData();
            showAlert("Succès", "Le statut de votre inscription a bien été mis à jour.");
        } else {
            showAlert("Attention", "Veuillez choisir un statut valide.");
        }
    }

    // ── Navigation ──────────────────────────────────────────────

    @FXML
    private void handleRetour(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/org/example/AccueilEtudiant.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Eduverse - Dashboard Étudiant");
            stage.setScene(new Scene(root, 1280, 800));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ── Utilitaires ─────────────────────────────────────────────

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    @FXML
    void logout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainView.fxml"));
            Parent root = loader.load();
            ((javafx.scene.Node) event.getSource()).getScene().setRoot(root);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @FXML
    void navigateToAccueil(javafx.scene.input.MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/AccueilEtudiant.fxml"));
        Parent root = loader.load();
        ((javafx.scene.Node) event.getSource()).getScene().setRoot(root);
    }

    @FXML
    void navigateToCours(javafx.scene.input.MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/AccueilEtudiant.fxml"));
        Parent root = loader.load();
        ((javafx.scene.Node) event.getSource()).getScene().setRoot(root);
    }

    @FXML
    void navigateToCertifications(javafx.scene.input.MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/CertificationsEtudiant.fxml"));
        Parent root = loader.load();
        ((javafx.scene.Node) event.getSource()).getScene().setRoot(root);
    }

    @FXML
    void navigateToEvents(javafx.scene.input.MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Student.fxml"));
        Parent root = loader.load();
        ((javafx.scene.Node) event.getSource()).getScene().setRoot(root);
    }

    @FXML
    void deleteAccount(ActionEvent event) {
        // Logique de suppression de compte
    }
}
