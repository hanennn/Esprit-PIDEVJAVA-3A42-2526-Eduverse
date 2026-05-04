package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.utils.MyConnection;

import java.sql.*;

public class AccueilEtudiantController {

    @FXML private FlowPane coursContainer;

    @FXML
    public void initialize() {
        chargerCours();
    }

    // CHARGER COURS
    private void chargerCours() {
        coursContainer.getChildren().clear();
        try {
            Connection cnx = MyConnection.getInstance().getCnx();
            String sql =
                    "SELECT id, titre_cours, niv_cours, matiere_cours, " +
                            "langue_cours, description FROM cours";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            boolean found = false;
            while (rs.next()) {
                found = true;
                coursContainer.getChildren().add(
                        creerCarteCoursCard(
                                rs.getInt("id"),
                                rs.getString("titre_cours"),
                                rs.getString("niv_cours"),
                                rs.getString("matiere_cours"),
                                rs.getString("langue_cours"),
                                rs.getString("description")
                        )
                );
            }

            if (!found) {
                Label vide = new Label("Aucun cours disponible.");
                vide.setStyle("-fx-text-fill: #888; -fx-font-size: 14;");
                coursContainer.getChildren().add(vide);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // CARTE COURS
    private VBox creerCarteCoursCard(int id, String titre,
                                     String niveau, String matiere,
                                     String langue, String description) {
        VBox card = new VBox(10);
        card.setPrefWidth(290);
        card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 12;" +
                        "-fx-padding: 20; -fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );

        // Header
        HBox header = new HBox(8);
        Label icon = new Label("📖");
        icon.setStyle("-fx-font-size: 22;");
        Label titreLabel = new Label(titre);
        titreLabel.setStyle(
                "-fx-font-size: 14; -fx-font-weight: bold;" +
                        "-fx-text-fill: #1a1f3c;");
        titreLabel.setWrapText(true);
        titreLabel.setMaxWidth(220);
        header.getChildren().addAll(icon, titreLabel);

        // Barre orange
        HBox barre = new HBox();
        barre.setPrefHeight(3);
        barre.setStyle(
                "-fx-background-color: #f5a623; -fx-background-radius: 2;");

        // Badges
        HBox badges = new HBox(6);
        if (niveau != null && !niveau.isEmpty()) {
            Label b = new Label("📊 " + niveau);
            b.setStyle(
                    "-fx-background-color: #e3f2fd; -fx-text-fill: #1976d2;" +
                            "-fx-font-size: 10; -fx-font-weight: bold;" +
                            "-fx-background-radius: 4; -fx-padding: 3 8;");
            badges.getChildren().add(b);
        }
        if (matiere != null && !matiere.isEmpty()) {
            Label b = new Label("🔬 " + matiere);
            b.setStyle(
                    "-fx-background-color: #f3e5f5; -fx-text-fill: #7b1fa2;" +
                            "-fx-font-size: 10; -fx-font-weight: bold;" +
                            "-fx-background-radius: 4; -fx-padding: 3 8;");
            badges.getChildren().add(b);
        }
        if (langue != null && !langue.isEmpty()) {
            Label b = new Label("🌍 " + langue);
            b.setStyle(
                    "-fx-background-color: #e8f5e9; -fx-text-fill: #388e3c;" +
                            "-fx-font-size: 10; -fx-font-weight: bold;" +
                            "-fx-background-radius: 4; -fx-padding: 3 8;");
            badges.getChildren().add(b);
        }

        // Description
        Label descLabel = new Label(
                description != null && !description.isEmpty()
                        ? description : "Aucune description");
        descLabel.setStyle(
                "-fx-font-size: 11; -fx-text-fill: #888;");
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(270);
        descLabel.setMaxHeight(40);

        // Bouton
        Button btn = new Button("Ouvrir le cours →");
        btn.setPrefWidth(250);
        btn.setStyle(
                "-fx-background-color: #f5a623; -fx-text-fill: white;" +
                        "-fx-font-weight: bold; -fx-background-radius: 8;" +
                        "-fx-padding: 9 16; -fx-cursor: hand; -fx-font-size: 12;");
        btn.setOnAction(e ->
                ouvrirCours(id, titre, niveau, matiere, langue, description));

        card.getChildren().addAll(header, barre, badges, descLabel, btn);

        // Hover
        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: #fff8ee; -fx-background-radius: 12;" +
                        "-fx-padding: 20; -fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(245,166,35,0.3), 15, 0, 0, 3);"
        ));
        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 12;" +
                        "-fx-padding: 20; -fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        ));

        return card;
    }

    //OUVRIR COURS
    private void ouvrirCours(int coursId, String titre, String niveau,
                             String matiere, String langue,
                             String description) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/org/example/CoursDetailEtudiant.fxml"));
            Stage stage = (Stage) coursContainer.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle(titre);
            CoursDetailEtudiantController ctrl = loader.getController();
            ctrl.setCours(coursId, titre, niveau, matiere, langue, description);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //  CERTIFICATIONS
    @FXML
    private void ouvrirCertifications() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/org/example/CertificationsEtudiant.fxml"));
            Stage stage = (Stage) coursContainer.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Mes Certifications");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // RETOUR
    @FXML
    private void retourChoixEspace() {
        try {
            Stage stage = (Stage) coursContainer.getScene().getWindow();
            new org.example.MainFX().start(stage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}