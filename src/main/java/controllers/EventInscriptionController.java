package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Event;
import models.EventInscription;
import services.EventInscriptionService;
import services.EventService;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import java.sql.Timestamp;
import java.util.List;

public class EventInscriptionController {

    @FXML private TableView<EventInscription> tvInscriptions;
    @FXML private TableColumn<EventInscription, Integer> colId;
    @FXML private TableColumn<EventInscription, String>  colStatut;
    @FXML private TableColumn<EventInscription, Integer> colParticipant;
    @FXML private TableColumn<EventInscription, Integer> colEvent;
    @FXML private TableColumn<EventInscription, Timestamp> colDateInsc;

    @FXML private TextField           tfId;
    @FXML private ComboBox<String>    cbStatut;
    @FXML private TextField           tfParticipant;
    @FXML private TextField           tfEvent;
    @FXML private TextField           tfSearch;
    @FXML private ComboBox<String>    cbSort;
    @FXML private Label               lblError;

    private EventInscriptionService inscriptionService;
    private ObservableList<EventInscription> inscList;

    @FXML
    public void initialize() {
        inscriptionService = new EventInscriptionService();

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colParticipant.setCellValueFactory(new PropertyValueFactory<>("participantId"));
        colEvent.setCellValueFactory(new PropertyValueFactory<>("eventId"));
        colDateInsc.setCellValueFactory(new PropertyValueFactory<>("dateInscription"));

        cbStatut.setItems(FXCollections.observableArrayList("Inscrit", "En attente", "Annulé", "Présent", "Absent"));

        cbSort.setItems(FXCollections.observableArrayList(
            "Date (Plus récent)", "Date (Plus ancien)"
        ));

        tvInscriptions.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                tfId.setText(String.valueOf(newSel.getId()));
                cbStatut.setValue(newSel.getStatut());
                tfParticipant.setText(String.valueOf(newSel.getParticipantId()));
                tfEvent.setText(String.valueOf(newSel.getEventId()));
            }
        });

        loadData();
    }

    private void loadData() {
        List<EventInscription> list = inscriptionService.getAll();
        inscList = FXCollections.observableArrayList(list);

        FilteredList<EventInscription> filteredData = new FilteredList<>(inscList, p -> true);

        tfSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(ei -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                if (ei.getStatut().toLowerCase().contains(lowerCaseFilter)) return true;
                if (String.valueOf(ei.getParticipantId()).contains(newValue)) return true;
                if (String.valueOf(ei.getEventId()).contains(newValue)) return true;
                return false;
            });
        });

        SortedList<EventInscription> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tvInscriptions.comparatorProperty());

        cbSort.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                switch (newVal) {
                    case "Date (Plus récent)":
                        tvInscriptions.getSortOrder().clear();
                        colDateInsc.setSortType(TableColumn.SortType.DESCENDING);
                        tvInscriptions.getSortOrder().add(colDateInsc);
                        break;
                    case "Date (Plus ancien)":
                        tvInscriptions.getSortOrder().clear();
                        colDateInsc.setSortType(TableColumn.SortType.ASCENDING);
                        tvInscriptions.getSortOrder().add(colDateInsc);
                        break;
                }
            }
        });

        tvInscriptions.setItems(sortedData);
    }

    @FXML
    private void handleAdd(ActionEvent event) {
        EventInscription ei = buildFromForm();
        if (ei != null) {
            ei.setDateInscription(new Timestamp(System.currentTimeMillis()));
            inscriptionService.add(ei);
            loadData();
            handleClear();
        }
    }

    @FXML
    private void handleUpdate(ActionEvent event) {
        if (tfId.getText().isEmpty()) return;
        EventInscription ei = buildFromForm();
        if (ei != null) {
            ei.setId(Integer.parseInt(tfId.getText()));
            ei.setDateInscription(new Timestamp(System.currentTimeMillis()));
            inscriptionService.update(ei);
            loadData();
            handleClear();
        }
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        if (!tfId.getText().isEmpty()) {
            inscriptionService.delete(Integer.parseInt(tfId.getText()));
            loadData();
            handleClear();
        }
    }

    @FXML
    private void handleClear() {
        lblError.setVisible(false);
        tfId.clear();
        cbStatut.setValue(null);
        tfParticipant.clear();
        tfEvent.clear();
    }

    @FXML
    private void handleViewDetail(ActionEvent event) {
        EventInscription selected = tvInscriptions.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Aucune sélection");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez sélectionner une inscription dans le tableau.");
            alert.showAndWait();
            return;
        }
        EventService eventService = new EventService();
        Event linkedEvent = eventService.getById(selected.getEventId());
        if (linkedEvent != null) {
            EventDetailHelper.openDetail(linkedEvent);
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Événement introuvable");
            alert.setHeaderText(null);
            alert.setContentText("Impossible de trouver l'événement lié à cette inscription.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleTableClick() {
        // handled by listener in initialize()
    }

    private EventInscription buildFromForm() {
        lblError.setVisible(false);
        try {
            if (cbStatut.getValue() == null || tfParticipant.getText().isEmpty() || tfEvent.getText().isEmpty()) {
                showError("Veuillez remplir tous les champs (Statut, Participant, Event).");
                return null;
            }
            EventInscription ei = new EventInscription();
            ei.setStatut(cbStatut.getValue());
            ei.setParticipantId(Integer.parseInt(tfParticipant.getText()));
            ei.setEventId(Integer.parseInt(tfEvent.getText()));
            return ei;
        } catch (Exception ex) {
            showError("Format invalide (IDs). " + ex.getMessage());
            return null;
        }
    }

    private void showError(String message) {
        lblError.setText(message);
        lblError.setVisible(true);
        lblError.setManaged(true);
    }
}
