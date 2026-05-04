package org.example.controllers;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.example.entities.chapitres;
import org.example.entities.cours;
import org.example.services.chapitresservices;

import java.util.List;

public class ListeChapitresController {

    @FXML private TableView<chapitres>            tableChapitres;
    @FXML private TableColumn<chapitres, String>  colTitre;
    @FXML private TableColumn<chapitres, String>  colDesc;
    @FXML private TableColumn<chapitres, Integer> colOrdre;
    @FXML private TableColumn<chapitres, String>  colDuree;
    @FXML private TableColumn<chapitres, String>  colStatut;
    @FXML private TableColumn<chapitres, String>  colType;
    @FXML private TableColumn<chapitres, Void>    colActions;
    @FXML private Label                           titreCours;

    private int   idCours;
    private cours coursObj;

    public void setCours(cours c) {
        this.coursObj = c;
        this.idCours  = c.getId();
        if (titreCours != null)
            titreCours.setText(
                    "🏠 Accueil  ›  Cours  ›  " + c.getTitre_cours() + "  ›  Chapitres");
        loadChapitres();
    }

    void loadChapitres() {
        try {
            List<chapitres> list =
                    new chapitresservices().getChapitresByCours(idCours);

            colTitre.setCellValueFactory(data ->
                    new SimpleStringProperty(data.getValue().getTitre_chap()));
            colDesc.setCellValueFactory(data ->
                    new SimpleStringProperty(data.getValue().getDesc_chap()));
            colOrdre.setCellValueFactory(data ->
                    new SimpleIntegerProperty(data.getValue().getOrdre_chap()).asObject());
            colDuree.setCellValueFactory(data ->
                    new SimpleStringProperty(data.getValue().getDuree_chap()));
            colStatut.setCellValueFactory(data ->
                    new SimpleStringProperty(data.getValue().getStatut_chap()));
            colType.setCellValueFactory(data ->
                    new SimpleStringProperty(data.getValue().getType_contenu()));

            // Style titre
            colTitre.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setText(null); setStyle(""); }
                    else {
                        setText(item);
                        setStyle("-fx-font-weight: bold; -fx-text-fill: #1a1f3c; -fx-font-size: 13;");
                    }
                }
            });

            // Badge statut
            colStatut.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setGraphic(null); return; }
                    Label lbl = new Label(item);
                    boolean ouvert = item.toUpperCase().contains("OUVERT") &&
                            !item.toUpperCase().contains("NON");
                    lbl.setStyle(ouvert
                            ? "-fx-background-color: #2ecc71; -fx-text-fill: white;" +
                            "-fx-padding: 4 10; -fx-background-radius: 20;" +
                            "-fx-font-size: 11; -fx-font-weight: bold;"
                            : "-fx-background-color: #e74c3c; -fx-text-fill: white;" +
                            "-fx-padding: 4 10; -fx-background-radius: 20;" +
                            "-fx-font-size: 11; -fx-font-weight: bold;"
                    );
                    setAlignment(Pos.CENTER);
                    setGraphic(lbl);
                }
            });

            // Badge type
            colType.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setGraphic(null); return; }
                    Label lbl = new Label(item.toUpperCase());
                    lbl.setStyle(
                            "-fx-background-color: #3498db; -fx-text-fill: white;" +
                                    "-fx-padding: 3 10; -fx-background-radius: 4;" +
                                    "-fx-font-size: 10; -fx-font-weight: bold;"
                    );
                    setAlignment(Pos.CENTER);
                    setGraphic(lbl);
                }
            });

            // Boutons actions
            colActions.setCellFactory(col -> new TableCell<>() {
                final Button btnEditer    = new Button("✏️ Éditer");
                final Button btnSupprimer = new Button("🗑 Supprimer");
                {
                    btnEditer.setStyle(
                            "-fx-background-color: #f5a623; -fx-text-fill: white;" +
                                    "-fx-font-weight: bold; -fx-background-radius: 6;" +
                                    "-fx-font-size: 11; -fx-cursor: hand; -fx-padding: 5 10;"
                    );
                    btnSupprimer.setStyle(
                            "-fx-background-color: #fef2f2; -fx-text-fill: #e74c3c;" +
                                    "-fx-font-weight: bold; -fx-background-radius: 6;" +
                                    "-fx-border-color: #fecaca; -fx-border-radius: 6;" +
                                    "-fx-border-width: 1; -fx-font-size: 11;" +
                                    "-fx-cursor: hand; -fx-padding: 5 10;"
                    );
                    btnEditer.setOnAction(e -> {
                        chapitres ch = getTableView().getItems().get(getIndex());
                        modifierChapitre(ch);
                    });
                    btnSupprimer.setOnAction(e -> {
                        chapitres ch = getTableView().getItems().get(getIndex());
                        supprimerChapitre(ch.getId());
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) { setGraphic(null); return; }
                    HBox box = new HBox(6, btnEditer, btnSupprimer);
                    box.setAlignment(Pos.CENTER_LEFT);
                    setGraphic(box);
                }
            });

            // Style lignes
            tableChapitres.setRowFactory(tv -> {
                TableRow<chapitres> row = new TableRow<>() {
                    @Override
                    protected void updateItem(chapitres item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) setStyle("-fx-background-color: transparent;");
                        else setStyle(getIndex() % 2 == 0
                                ? "-fx-background-color: white;"
                                : "-fx-background-color: #fafafa;");
                    }
                };
                row.setOnMouseEntered(e -> {
                    if (!row.isEmpty())
                        row.setStyle("-fx-background-color: #fff8ee; -fx-cursor: hand;");
                });
                row.setOnMouseExited(e -> {
                    if (!row.isEmpty())
                        row.setStyle(row.getIndex() % 2 == 0
                                ? "-fx-background-color: white;"
                                : "-fx-background-color: #fafafa;");
                });
                return row;
            });

            tableChapitres.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-table-header-background: #f8f9fa;" +
                            "-fx-border-color: transparent;"
            );

            tableChapitres.setItems(FXCollections.observableArrayList(list));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void rafraichir() {
        loadChapitres();
    }

    private void supprimerChapitre(int id) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer ce chapitre ?");
        confirm.setContentText("Cette action est irréversible !");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    new chapitresservices().supprimer(id);
                    loadChapitres();
                } catch (Exception e) {
                    new Alert(Alert.AlertType.ERROR,
                            "Erreur lors de la suppression !").show();
                }
            }
        });
    }

    private void modifierChapitre(chapitres ch) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/modifChapitre.fxml"));
            Parent root = loader.load();
            ModifierChapitreController controller = loader.getController();
            controller.setChapitre(ch, coursObj);
            tableChapitres.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void ajouterChapitre() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/ajoutChapitre.fxml"));
            Parent root = loader.load();
            AjoutChapitreController controller = loader.getController();
            controller.setCours(coursObj);
            tableChapitres.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void retour() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/AccueilFormateur.fxml"));
            Stage stage = (Stage) tableChapitres.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Espace Formateur");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}