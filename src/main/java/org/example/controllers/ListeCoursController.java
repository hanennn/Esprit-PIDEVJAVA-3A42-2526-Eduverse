package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.entities.cours;
import org.example.services.coursservices;

import javafx.event.ActionEvent;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class ListeCoursController {

    @FXML private VBox listContainer;
    @FXML private Label nbCours;

    @FXML
    public void initialize() throws SQLException {
        chargerCours();
    }
    //aff tous les cours
    private void chargerCours() throws SQLException {
        listContainer.getChildren().clear();
        coursservices cs = new coursservices();
        List<cours> liste = cs.afficher();

        // Mettre à jour le nbre de cours
        nbCours.setText(String.valueOf(liste.size()));

        // Créer  carte pour chaque cours
        for (cours c : liste) {
            listContainer.getChildren().add(creerCartesCours(c));
        }
    }

    private VBox creerCartesCours(cours c) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10;" +
                "-fx-border-color: #e0e0e0; -fx-border-radius: 10; -fx-padding: 16;");

        // Titre + bouton pinceau
        Label labelTitre = new Label("TITRE DU COURS");
        labelTitre.setStyle("-fx-font-size: 10px; -fx-text-fill: #888; -fx-font-weight: bold;");

        Label titre = new Label(c.getTitre_cours());
        titre.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1a1a2e;");
        HBox.setHgrow(titre, javafx.scene.layout.Priority.ALWAYS);

        Button btnEdit = new Button("✏");
        btnEdit.setStyle("-fx-background-color: transparent; -fx-text-fill: #f5a623;" +
                "-fx-font-size: 14px; -fx-cursor: hand;");
        btnEdit.setOnAction(e -> modifierCours(c));

        HBox headerBox = new HBox(8);
        headerBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        headerBox.getChildren().addAll(labelTitre, titre, btnEdit);

        // Niveau / Matière / Langue
        HBox fields = new HBox(10);
        fields.getChildren().addAll(
                creerField("NIVEAU", c.getNiv_cours()),
                creerField("MATIÈRE", c.getMatiere_cours()),
                creerField("LANGUE", c.getLangue_cours())
        );

        // Description
        VBox desc = new VBox(4);
        Label labelDesc = new Label("DESCRIPTION");
        labelDesc.setStyle("-fx-font-size: 10px; -fx-text-fill: #888; -fx-font-weight: bold;");
        Label descText = new Label(c.getDescription());
        descText.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");
        descText.setWrapText(true);
        desc.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 6;" +
                "-fx-border-color: #f5a623; -fx-border-width: 0 0 0 3; -fx-padding: 10;");
        desc.getChildren().addAll(labelDesc, descText);

        // Boutons
        Button btnSuppr = new Button("🗑 Supprimer Ce Cours");
        btnSuppr.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;" +
                "-fx-background-radius: 6; -fx-font-size: 12px;");
        btnSuppr.setOnAction(e -> supprimerCours(c.getId()));

        Button btnChap = new Button("📋 Chapitres");
        btnChap.setStyle("-fx-background-color: #1abc9c; -fx-text-fill: white;" +
                "-fx-background-radius: 6; -fx-font-size: 12px; -fx-font-weight: bold;" +
                "-fx-padding: 6 14; -fx-cursor: hand;");
        btnChap.setOnAction(e -> ouvrirChapitres(c));

        HBox actions = new HBox(10);
        actions.getChildren().addAll(btnSuppr, btnChap);

        // ajouter tout à card
        card.getChildren().addAll(headerBox, fields, desc, actions);

        return card;
    }
    private VBox creerField(String label, String valeur) {
        VBox box = new VBox(4);
        box.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 6;" +
                "-fx-border-color: #f5a623; -fx-border-width: 0 0 0 3;" +
                "-fx-padding: 10; -fx-pref-width: 160;");
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #888; -fx-font-weight: bold;");
        Label val = new Label(valeur != null ? valeur : "-");
        val.setStyle("-fx-font-size: 13px; -fx-text-fill: #1a1a2e;");
        box.getChildren().addAll(lbl, val);
        return box;
    }

    private void supprimerCours(int id) {
        try {
            new coursservices().supprimer(id);
            chargerCours();
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Erreur lors de la suppression !");
            alert.show();
        }
    }

    @FXML
    void ajouterCours() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ajoutCours.fxml"));
            listContainer.getScene().setRoot(root);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private void modifierCours(cours c) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/modifCours.fxml"));
            Parent root = loader.load();

            // Passer le cours au controller de modification
            ModifierCoursController controller = loader.getController();
            controller.setCours(c);

            listContainer.getScene().setRoot(root);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @FXML
    void versEtudiant() {
        try {
            Parent root = FXMLLoader.load(getClass()
                    .getResource("/catalogueCours.fxml"));
            listContainer.getScene().setRoot(root);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
//list chaps
    private void ouvrirChapitres(cours c) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ListeChapitres.fxml"));
            Parent root = loader.load();

            // récup controller
            ListeChapitresController controller = loader.getController();

            controller.setCours(c);

            listContainer.getScene().setRoot(root);

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
    @FXML
    void goToAdmin(ActionEvent event) throws IOException {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource())
                .getScene()
                .getWindow();

        Scene scene = new Scene(root, 900, 650); // taille
        stage.setScene(scene);
        stage.setTitle("Admin ");
        stage.setMaximized(true);
        stage.show();
    }


}