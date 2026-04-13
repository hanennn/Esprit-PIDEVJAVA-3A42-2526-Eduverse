package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.entities.cours;
import org.example.services.coursservices;
import java.sql.SQLException;

public class AjoutCoursController {

    @FXML private TextField titreCours;
    @FXML private TextArea  descCours;
    @FXML private TextField niveauCours;
    @FXML private TextField matiereCours;
    @FXML private TextField langueCours;

    @FXML
    void ajouterCours()  {

        // ✅ CONTROLE DE SAISIE

        titreCours.setStyle("");
        descCours.setStyle("");
        niveauCours.setStyle("");
        matiereCours.setStyle("");
        langueCours.setStyle("");

// Récupération avec trim
        String titre = titreCours.getText().trim();
        String desc = descCours.getText().trim();
        String niveau = niveauCours.getText().trim();
        String matiere = matiereCours.getText().trim();
        String langue = langueCours.getText().trim();

        if (titre.isEmpty()) {
            showError("Le titre est obligatoire !");
            titreCours.setStyle("-fx-border-color: red;");
            return;
        }
        if (titre.length() < 3) {
            showError("Le titre doit contenir au moins 3 caractères !");
            titreCours.setStyle("-fx-border-color: red;");
            return;
        }

// DESCRIPTION
        if (desc.isEmpty()) {
            showError("La description est obligatoire !");
            descCours.setStyle("-fx-border-color: red;");
            return;
        }
        if (desc.length() < 10) {
            showError("La description doit contenir au moins 10 caractères !");
            descCours.setStyle("-fx-border-color: red;");
            return;
        }

// NIVEAU
        if (niveau.isEmpty()) {
            showError("Le niveau est obligatoire !");
            niveauCours.setStyle("-fx-border-color: red;");
            return;
        }
        if (niveau.length() < 5) {
            showError("Le niveau doit contenir au moins 5 caractères !");
            niveauCours.setStyle("-fx-border-color: red;");
            return;
        }

// MATIERE
        if (matiere.isEmpty()) {
            showError("La matière est obligatoire !");
            matiereCours.setStyle("-fx-border-color: red;");
            return;
        }
        if (matiere.length() < 5) {
            showError("La matière doit contenir au moins 5 caractères !");
            matiereCours.setStyle("-fx-border-color: red;");
            return;
        }

// LANGUE
        if (langue.isEmpty()) {
            showError("La langue est obligatoire !");
            langueCours.setStyle("-fx-border-color: red;");
            return;
        }
        if (langue.length() < 5) {
            showError("La langue doit contenir au moins 5 caractères !");
            langueCours.setStyle("-fx-border-color: red;");
            return;
        }

        // INSERTION EN BD
        cours c = new cours(
                titreCours.getText(),
                descCours.getText(),
                niveauCours.getText(),
                matiereCours.getText(),
                langueCours.getText()
        );

        // doublon
        try {
            new coursservices().ajouter(c);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setContentText("Cours ajouté avec succès !");
            alert.show();

            annuler();
        } catch (SQLException e) {
            showError(e.getMessage()); // affiche "Ce cours existe déjà !"
            titreCours.setStyle("-fx-border-color: red;");
        }
    }
    @FXML
    void annuler() {
        // Vide
        titreCours.clear();
        descCours.clear();
        niveauCours.clear();
        matiereCours.clear();
        langueCours.clear();

        // Réinitialiser les bordures
        titreCours.setStyle("");
        descCours.setStyle("");
        niveauCours.setStyle("");
        matiereCours.setStyle("");
        langueCours.setStyle("");
    }

    @FXML
    void retour() {
        try {
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(
                    getClass().getResource("/listeCours.fxml")
            );
            titreCours.getScene().setRoot(root);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
//alert error
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur de saisie");
        alert.setContentText(message);
        alert.show();
    }
}