package org.example.controllers;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.example.entities.chapitres;
import org.example.entities.cours;
import org.example.services.chapitresservices;

import java.util.List;

public class ListeChapitresController {

    @FXML private TableView<chapitres> tableChapitres;
    @FXML private TableColumn<chapitres, String>  colTitre;
    @FXML private TableColumn<chapitres, String>  colDesc;
    @FXML private TableColumn<chapitres, Integer> colOrdre;
    @FXML private TableColumn<chapitres, String>  colDuree;
    @FXML private TableColumn<chapitres, String>  colStatut;
    @FXML private TableColumn<chapitres, String>  colType;
    @FXML private TableColumn<chapitres, Void>    colActions;
    @FXML private Label                           titreCours;

    private int idCours;
    private cours coursObj;
//reçoit cours
    public void setCours(cours c) {
        this.coursObj = c;
        titreCours.setText("Chapitres du cours : " + c.getTitre_cours());
        this.idCours = c.getId();
        loadChapitres();
    }

    void loadChapitres() {
        try {
            List<chapitres> list = new chapitresservices().getChapitresByCours(idCours);

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

            // ✅ BADGE STATUT
            colStatut.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setGraphic(null);
                    } else {
                        Label lbl = new Label(item);

                        if (item.equalsIgnoreCase("OUVERT")) {
                            lbl.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;" +
                                    "-fx-padding: 4 10; -fx-background-radius: 20;");
                        } else {
                            lbl.setStyle("-fx-background-color: #bdc3c7; -fx-text-fill: white;" +
                                    "-fx-padding: 4 10; -fx-background-radius: 20;");
                        }

                        setGraphic(lbl);
                    }
                }
            });

            // ✅ BADGE TYPE (PDF)
            colType.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setGraphic(null);
                    } else {
                        Label lbl = new Label(item);

                        lbl.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;" +
                                "-fx-padding: 4 10; -fx-background-radius: 20;");

                        setGraphic(lbl);
                    }
                }
            });

            // ✅ BOUTON VOIR + 🔒
            // BOUTONS ÉDITER + SUPPRIMER (formateur)
            colActions.setCellFactory(col -> new TableCell<>() {
                final Button btnEditer    = new Button("Éditer");
                final Button btnSupprimer = new Button("Supprimer");
                {
                    //color button
                    btnEditer.setStyle("-fx-background-color: #f5a623; -fx-text-fill: white;" +
                            "-fx-background-radius: 6; -fx-font-size: 11px; -fx-cursor: hand;");
                    btnSupprimer.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;" +
                            "-fx-background-radius: 6; -fx-font-size: 11px; -fx-cursor: hand;");
                    //action
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
                    setGraphic(box);
                }
            });
            //remplir tab
            tableChapitres.setItems(FXCollections.observableArrayList(list));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void supprimerChapitre(int id) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer ce chapitre ?");
        confirm.setContentText("Cette action est irréversible !");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) { //verif click ok
                try {
                    new chapitresservices().supprimer(id);
                    loadChapitres();
                } catch (Exception e) {
                    new Alert(Alert.AlertType.ERROR, "Erreur lors de la suppression !").show();
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
            Parent root = FXMLLoader.load(getClass().getResource("/catalogueCours.fxml"));
            tableChapitres.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}