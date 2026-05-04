package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.entities.chapitres;
import org.example.entities.cours;
import org.example.services.chapitresservices;

import java.io.File;
import java.sql.SQLException;

public class AjoutChapitreController {

    @FXML private TextField        titreChap;
    @FXML private TextArea         descChap;
    @FXML private TextField        ordreChap;
    @FXML private TextField        dureeChap;
    @FXML private ComboBox<String> statutChap;
    @FXML private TextField        contenuChap;
    @FXML private ComboBox<String> typeContenu;
    @FXML private Label            titreCours;

    private cours coursSelectionne;

    public void setCours(cours c) {
        this.coursSelectionne = c;
        if (titreCours != null)
            titreCours.setText(
                    "🏠 Accueil  ›  Cours  ›  " + c.getTitre_cours() + "  ›  Ajouter Chapitre");
    }

    @FXML
    public void initialize() {
        statutChap.getItems().addAll("OUVERT", "NON OUVERT");
        statutChap.setValue("NON OUVERT");
        typeContenu.getItems().addAll("pdf", "image");
        typeContenu.setValue("pdf");

        typeContenu.valueProperty().addListener((obs, oldVal, newVal) -> {
            if ("texte".equals(newVal)) {
                contenuChap.setPromptText("Saisissez le contenu texte...");
                contenuChap.setEditable(true);
                contenuChap.clear();
            } else {
                contenuChap.setPromptText("Cliquez sur 'Parcourir' pour choisir un fichier...");
                contenuChap.setEditable(false);
                contenuChap.clear();
            }
        });
    }

    @FXML
    void choisirFichier() {
        String type = typeContenu.getValue();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir un fichier");

        switch (type) {
            case "pdf" -> fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
            case "video" -> fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Fichiers Vidéo", "*.mp4", "*.avi", "*.mkv", "*.mov"));
            case "image" -> fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"));
        }

        File fichier = fileChooser.showOpenDialog(contenuChap.getScene().getWindow());
        if (fichier != null) {
            contenuChap.setText(fichier.getAbsolutePath());
        }
    }

    @FXML
    void ajouterChapitre() throws SQLException {
        resetStyles();

        // Validation titre
        if (titreChap.getText().trim().length() < 3) {
            titreChap.setStyle("-fx-border-color: red;");
            showError("Le titre doit contenir au moins 3 caractères !");
            return;
        }

        // Validation description
        if (descChap.getText().trim().length() < 10) {
            descChap.setStyle("-fx-border-color: red;");
            showError("La description doit contenir au moins 10 caractères !");
            return;
        }

        // Validation ordre
        int ordre;
        try {
            ordre = Integer.parseInt(ordreChap.getText().trim());
            if (ordre < 0) {
                ordreChap.setStyle("-fx-border-color: red;");
                showError("L'ordre doit être un entier positif !");
                return;
            }
        } catch (NumberFormatException e) {
            ordreChap.setStyle("-fx-border-color: red;");
            showError("L'ordre doit être un nombre entier valide !");
            return;
        }

        // Validation durée
        if (dureeChap.getText().trim().length() < 2) {
            dureeChap.setStyle("-fx-border-color: red;");
            showError("La durée doit contenir au moins 2 caractères !");
            return;
        }

        // Validation contenu
        if (contenuChap.getText().trim().isEmpty()) {
            contenuChap.setStyle("-fx-border-color: red;");
            showError("Le contenu ne peut pas être vide !");
            return;
        }

        // Validation fichier
        String type = typeContenu.getValue();
        if (!"texte".equals(type)) {
            File fichier = new File(contenuChap.getText().trim());
            if (!fichier.exists() || !fichier.isFile()) {
                contenuChap.setStyle("-fx-border-color: red;");
                showError("Le fichier sélectionné est introuvable !");
                return;
            }
        }

        chapitres ch = new chapitres(
                titreChap.getText().trim(),
                descChap.getText().trim(),
                ordre,
                dureeChap.getText().trim(),
                statutChap.getValue(),
                contenuChap.getText().trim(),
                typeContenu.getValue(),
                ""
        );
        ch.setCours_id(coursSelectionne.getId());

        try {
            new chapitresservices().ajouter(ch);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setContentText("Chapitre ajouté avec succès !");
            alert.show();
            annuler();
        } catch (SQLException e) {
            titreChap.setStyle("-fx-border-color: red;");
            showError(e.getMessage());
        }
    }

    @FXML
    void annuler() {
        titreChap.clear();
        descChap.clear();
        ordreChap.clear();
        dureeChap.clear();
        contenuChap.clear();
        resetStyles();
    }

    @FXML

    void retour() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/AccueilFormateur.fxml"));
            Stage stage = (Stage) titreChap.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Espace Formateur");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resetStyles() {
        titreChap.setStyle("");
        descChap.setStyle("");
        ordreChap.setStyle("");
        dureeChap.setStyle("");
        contenuChap.setStyle("");
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur de saisie");
        alert.setContentText(message);
        alert.show();
    }
}