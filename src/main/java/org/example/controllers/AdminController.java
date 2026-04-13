package org.example.controllers;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.entities.chapitres;
import org.example.entities.cours;
import org.example.services.chapitresservices;
import org.example.services.coursservices;

import java.sql.SQLException;
import java.util.List;

public class AdminController {

    @FXML private TableView<cours>           tableCours;
    @FXML private TableColumn<cours, String> colTitre;
    @FXML private TableColumn<cours, String> colDesc;
    @FXML private TableColumn<cours, String> colNiveau;
    @FXML private TableColumn<cours, String> colMatiere;
    @FXML private TableColumn<cours, String> colLangue;
    @FXML private TableColumn<cours, Void>   colActions;

    @FXML
    public void initialize() throws SQLException {
        List<cours> liste = new coursservices().afficher();

        //liaison col data
        colTitre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTitre_cours()));
        colDesc.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescription()));
        colNiveau.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNiv_cours()));
        colMatiere.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMatiere_cours()));
        colLangue.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getLangue_cours()));

        //bouton ouvrir chap
        colActions.setCellFactory(col -> new TableCell<>() {
            final Button btn = new Button("Chapitres");
            {
                btn.setStyle("-fx-background-color: #1abc9c; -fx-text-fill: white;" +
                        "-fx-background-radius: 6; -fx-font-size: 11px; -fx-cursor: hand;");
                btn.setOnAction(e -> ouvrirChapitres(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
//remplir tableau
        tableCours.setItems(FXCollections.observableArrayList(liste));
    }

    private void ouvrirChapitres(cours c) {
        try {
            TableView<chapitres> table = new TableView<>();

            TableColumn<chapitres, Integer> colNumero = new TableColumn<>("#");
            colNumero.setCellFactory(col -> new TableCell<>() {
                @Override protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : String.valueOf(getIndex() + 1));
                }
            });
//liaison data col
            TableColumn<chapitres, String> colTitreCh = new TableColumn<>("Titre");
            colTitreCh.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTitre_chap()));

            TableColumn<chapitres, String> colDescCh = new TableColumn<>("Description");
            colDescCh.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDesc_chap()));

            TableColumn<chapitres, Integer> colOrdre = new TableColumn<>("Ordre");
            colOrdre.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getOrdre_chap()).asObject());

            TableColumn<chapitres, String> colDuree = new TableColumn<>("Durée");
            colDuree.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDuree_chap()));

            TableColumn<chapitres, String> colStatut = new TableColumn<>("Statut");
            colStatut.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatut_chap()));
            colStatut.setCellFactory(col -> new TableCell<>() {
                @Override protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setGraphic(null); return; }
                    Label lbl = new Label(item);
                    lbl.setStyle("OUVERT".equalsIgnoreCase(item)
                            ? "-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 20;"
                            : "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 20;");
                    setGraphic(lbl);
                }
            });

            TableColumn<chapitres, String> colType = new TableColumn<>("Type");
            colType.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getType_contenu()));
            colType.setCellFactory(col -> new TableCell<>() {
                @Override protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setGraphic(null); return; }
                    Label lbl = new Label(item);
                    lbl.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 20;");
                    setGraphic(lbl);
                }
            });

            table.getColumns().addAll(colNumero, colTitreCh, colDescCh, colOrdre, colDuree, colStatut, colType);
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

            List<chapitres> chaps = new chapitresservices().getChapitresByCours(c.getId());
            table.setItems(FXCollections.observableArrayList(chaps));

            table.setFixedCellSize(40);
            table.prefHeightProperty().bind(table.fixedCellSizeProperty()
                    .multiply(javafx.beans.binding.Bindings.size(table.getItems()).add(1.15)));
            table.minHeightProperty().bind(table.prefHeightProperty());
            table.maxHeightProperty().bind(table.prefHeightProperty());

            // Bouton retour
            Button btnRetour = new Button("← Retour aux cours");
            btnRetour.setStyle("-fx-background-color: transparent; -fx-border-color: #f5a623;" +
                    "-fx-border-radius: 6; -fx-text-fill: #f5a623; -fx-cursor: hand; -fx-padding: 8 16;");
            btnRetour.setOnAction(ev -> {
                try {
                    Parent root = FXMLLoader.load(getClass().getResource("/admin.fxml"));
                    table.getScene().setRoot(root);
                } catch (Exception ex) { ex.printStackTrace(); }
            });

            Label titre = new Label("Chapitres : " + c.getTitre_cours());
            titre.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1a1a2e;");

            HBox header = new HBox(20, titre, btnRetour);
            header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            header.setStyle("-fx-padding: 20 30;");

            VBox container = new VBox(16, header, table);
            container.setStyle("-fx-background-color: #f4f6f8; -fx-padding: 0 30 30 30;");
            container.setPrefWidth(1200);
            container.setPrefHeight(700);
            VBox.setVgrow(table, javafx.scene.layout.Priority.ALWAYS);

            tableCours.getScene().setRoot(container);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}