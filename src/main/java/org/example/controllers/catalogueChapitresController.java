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

public class catalogueChapitresController {

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

    public void setCours(cours c) {
        this.coursObj = c;
        this.idCours = c.getId();
        if (titreCours != null)
            titreCours.setText(c.getTitre_cours());
        loadChapitres();
    }

    void loadChapitres() {
        try {
            //recup chap
            List<chapitres> list = new chapitresservices().getChapitresByCours(idCours);
            //liaison data col
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



            // BOUTON VOIR + 🔒
            colActions.setCellFactory(col -> new TableCell<>() {
                final Button btnVoir = new Button("Voir");
                {
                    btnVoir.setStyle("-fx-background-color: #f5a623; -fx-text-fill: white;" +
                            "-fx-background-radius: 6; -fx-font-size: 11px; -fx-cursor: hand;");
                    btnVoir.setOnAction(e -> {
                        chapitres ch = getTableView().getItems().get(getIndex());
                        try {
                            FXMLLoader loader = new FXMLLoader(
                                    getClass().getResource("/voirChapitre.fxml"));
                            Parent root = loader.load();
                            VoirChapitreController ctrl = loader.getController();
                            ctrl.setChapitre(ch, coursObj); // passe le chapitre ET le cours pour le retour
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
                    if ("OUVERT".equalsIgnoreCase(ch.getStatut_chap())) {
                        box.getChildren().add(btnVoir);
                    } else { //non ouvert
                        Label lock = new Label("🔒");
                        lock.setStyle("-fx-font-size: 14px;");
                        box.getChildren().addAll(btnVoir, lock);
                    }
                    setGraphic(box);
                }
            });
//remplir tab
            tableChapitres.setItems(FXCollections.observableArrayList(list));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void retour() {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/catalogueCours.fxml"));
            tableChapitres.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}