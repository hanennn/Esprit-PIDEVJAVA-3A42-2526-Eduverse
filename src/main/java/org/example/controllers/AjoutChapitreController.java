package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import org.example.entities.chapitres;
import org.example.entities.cours;
import org.example.services.chapitresservices;
import java.sql.SQLException;

//fichier pdf
import javafx.stage.FileChooser;
import java.io.File;

public class AjoutChapitreController {

    @FXML private TextField titreChap;
    @FXML private TextArea  descChap;
    @FXML private TextField ordreChap;
    @FXML private TextField dureeChap;
    @FXML private ComboBox<String> statutChap;
    @FXML private TextField contenuChap;
    @FXML private ComboBox<String> typeContenu;
    @FXML private Label titreCours;

    private cours coursSelectionne;

    public void setCours(cours c) {
        this.coursSelectionne = c;
        titreCours.setText("Ajouter un chapitre — " + c.getTitre_cours());
    }

    @FXML
    public void initialize() {
        statutChap.getItems().addAll("OUVERT", "NON OUVERT");
        statutChap.setValue("Non ouvert");
        typeContenu.getItems().addAll("pdf", "video", "image", "texte");
        typeContenu.setValue("texte");
    }

    @FXML
    void ajouterChapitre() throws SQLException {

        // Contrôle de saisie amélioré
        if (titreChap.getText().trim().length() < 3) {
            showError("Le titre doit contenir au moins 3 caractères !");
            titreChap.setStyle("-fx-border-color: red;");
            titreChap.requestFocus();
            return;
        }

        if (descChap.getText().trim().length() < 10) {
            showError("La description doit contenir au moins 10 caractères !");
            descChap.setStyle("-fx-border-color: red;");
            descChap.requestFocus();
            return;
        }

        if (Integer.parseInt(ordreChap.getText().trim()) < 0) {
            showError("L'ordre doit être un entier !");
            ordreChap.setStyle("-fx-border-color: red;");
            ordreChap.requestFocus();
            return;
        }

        if (dureeChap.getText().trim().length() < 2) {
            showError("La durée doit contenir au moins 2 caractères !");
            dureeChap.setStyle("-fx-border-color: red;");
            dureeChap.requestFocus();
            return;
        }

        chapitres ch = new chapitres(
                titreChap.getText(),
                descChap.getText(),
                Integer.parseInt(ordreChap.getText()),
                dureeChap.getText(),
                statutChap.getValue(),
                contenuChap.getText(),
                typeContenu.getValue(),
                ""
        );
        ch.setCours_id(coursSelectionne.getId());//idcours

        // doublon
        try {
            new chapitresservices().ajouter(ch);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setContentText("Chapitre ajouté avec succès !");
            alert.show();

            retour();
        } catch (SQLException e) {
            showError(e.getMessage()); // affiche "Ce chapitre existe déjà dans ce cours !"
            titreChap.setStyle("-fx-border-color: red;");
        }
    }

    @FXML
    void annuler() {
        titreChap.clear();
        descChap.clear();
        ordreChap.clear();
        dureeChap.clear();
        contenuChap.clear();
        titreChap.setStyle("");
        descChap.setStyle("");
        ordreChap.setStyle("");
        dureeChap.setStyle("");
    }

    @FXML
    void retour() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/ListeChapitres.fxml"));
            Parent root = loader.load();
            ListeChapitresController controller = loader.getController();
            controller.setCours(coursSelectionne);
            titreChap.getScene().setRoot(root);
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