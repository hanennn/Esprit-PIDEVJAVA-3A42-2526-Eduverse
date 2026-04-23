package org.example.controllers;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.example.entities.chapitres;
import org.example.entities.cours;
import org.example.services.chapitresservices;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class catalogueChapitresController {

    @FXML private TableView<chapitres>            tableChapitres;
    @FXML private TableColumn<chapitres, String>  colTitre;
    @FXML private TableColumn<chapitres, String>  colDesc;
    @FXML private TableColumn<chapitres, Integer> colOrdre;
    @FXML private TableColumn<chapitres, String>  colDuree;
    @FXML private TableColumn<chapitres, String>  colStatut;
    @FXML private TableColumn<chapitres, String>  colType;
    @FXML private TableColumn<chapitres, Void>    colActions;
    @FXML private TableColumn<chapitres, Void>    colNumero;
    @FXML private Label                           titreCours;

    private int   idCours;
    private cours coursObj;

    public void setCours(cours c) {
        this.coursObj = c;
        this.idCours  = c.getId();
        if (titreCours != null)
            titreCours.setText(c.getTitre_cours());
        loadChapitres();
    }

    void loadChapitres() {
        try {
            List<chapitres> list = new chapitresservices().getChapitresByCours(idCours);

            // Colonne numéro auto
            AtomicInteger index = new AtomicInteger(1);
            colNumero.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : String.valueOf(getIndex() + 1));
                    setAlignment(Pos.CENTER);
                    setStyle("-fx-text-fill: #888; -fx-font-size: 12px;");
                }
            });

            colTitre.setCellValueFactory(data ->
                    new SimpleStringProperty(data.getValue().getTitre_chap()));
            colTitre.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setText(null); return; }
                    setText(item);
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #1a1a2e; -fx-font-size: 13px;");
                }
            });

            colDesc.setCellValueFactory(data ->
                    new SimpleStringProperty(data.getValue().getDesc_chap()));
            colDesc.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setText(null); return; }
                    setText(item);
                    setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
                    setWrapText(true);
                }
            });

            colOrdre.setCellValueFactory(data ->
                    new SimpleIntegerProperty(data.getValue().getOrdre_chap()).asObject());
            colOrdre.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setGraphic(null); return; }
                    Label badge = new Label(String.valueOf(item));
                    badge.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white;" +
                            "-fx-background-radius: 20; -fx-padding: 2 10; -fx-font-size: 11px;");
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                    setText(null);
                }
            });

            colDuree.setCellValueFactory(data ->
                    new SimpleStringProperty(data.getValue().getDuree_chap()));
            colDuree.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setText(null); return; }
                    setText("⏱ " + item);
                    setAlignment(Pos.CENTER);
                    setStyle("-fx-text-fill: #555; -fx-font-size: 12px;");
                }
            });

            // Statut avec badge coloré
            colStatut.setCellValueFactory(data ->
                    new SimpleStringProperty(data.getValue().getStatut_chap()));
            colStatut.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setGraphic(null); return; }
                    Label badge = new Label(item);
                    if ("OUVERT".equalsIgnoreCase(item)) {
                        badge.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;" +
                                "-fx-background-radius: 20; -fx-padding: 3 12;" +
                                "-fx-font-size: 11px; -fx-font-weight: bold;");
                    } else {
                        badge.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;" +
                                "-fx-background-radius: 20; -fx-padding: 3 12;" +
                                "-fx-font-size: 11px; -fx-font-weight: bold;");
                    }
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                    setText(null);
                }
            });

            // Type avec badge
            colType.setCellValueFactory(data ->
                    new SimpleStringProperty(data.getValue().getType_contenu()));
            colType.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setGraphic(null); return; }
                    Label badge = new Label(item.toUpperCase());
                    badge.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white;" +
                            "-fx-background-radius: 20; -fx-padding: 3 10; -fx-font-size: 11px;");
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                    setText(null);
                }
            });

            // Bouton Voir + 🔒
            colActions.setCellFactory(col -> new TableCell<>() {
                final Button btnVoir = new Button("👁 Voir");
                {
                    btnVoir.setStyle("-fx-background-color: #f5a623; -fx-text-fill: white;" +
                            "-fx-background-radius: 6; -fx-font-size: 11px;" +
                            "-fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 5 12;");
                    btnVoir.setOnAction(e -> {
                        chapitres ch = getTableView().getItems().get(getIndex());
                        try {
                            FXMLLoader loader = new FXMLLoader(
                                    getClass().getResource("/voirChapitre.fxml"));
                            Parent root = loader.load();
                            VoirChapitreController ctrl = loader.getController();
                            ctrl.setChapitre(ch, coursObj);
                            tableChapitres.getScene().setRoot(root);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) { setGraphic(null); return; }
                    chapitres ch = getTableView().getItems().get(getIndex());
                    HBox box = new HBox(6);
                    box.setAlignment(Pos.CENTER);
                    if ("OUVERT".equalsIgnoreCase(ch.getStatut_chap())) {
                        box.getChildren().add(btnVoir);
                    } else {
                        Label lock = new Label("🔒");
                        lock.setStyle("-fx-font-size: 16px;");
                        box.getChildren().addAll(btnVoir, lock);
                    }
                    setGraphic(box);
                }
            });

            tableChapitres.setItems(FXCollections.observableArrayList(list));
            tableChapitres.setRowFactory(tv -> {
                TableRow<chapitres> row = new TableRow<>();
                row.setStyle("-fx-cell-size: 52px;");
                row.hoverProperty().addListener((obs, wasHovered, isHovered) -> {
                    if (!row.isEmpty())
                        row.setStyle(isHovered
                                ? "-fx-background-color: #fff8ee; -fx-cell-size: 52px;"
                                : "-fx-cell-size: 52px;");
                });
                return row;
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void retour() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/catalogueCours.fxml"));
            tableChapitres.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}