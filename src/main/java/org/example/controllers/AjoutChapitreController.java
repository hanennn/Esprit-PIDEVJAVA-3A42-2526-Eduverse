package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import org.example.entities.chapitres;
import org.example.entities.cours;
import org.example.services.chapitresservices;
import java.sql.SQLException;

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
        statutChap.setValue("NON OUVERT");
        typeContenu.getItems().addAll("pdf", "image");
        typeContenu.setValue("pdf");

        // Adapter le champ contenu selon le type sélectionné
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
            case "pdf":
                fileChooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
                break;
            case "video":
                fileChooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("Fichiers Vidéo", "*.mp4", "*.avi", "*.mkv", "*.mov"));
                break;
            case "image":
                fileChooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"));
                break;
        }

        File fichier = fileChooser.showOpenDialog(contenuChap.getScene().getWindow());
        if (fichier != null) {
            contenuChap.setText(fichier.getAbsolutePath());
        }
    }

    @FXML
    void ajouterChapitre() throws SQLException {

        // Validation : titre
        if (titreChap.getText().trim().length() < 3) {
            showError("Le titre doit contenir au moins 3 caractères !");
            titreChap.setStyle("-fx-border-color: red;");
            titreChap.requestFocus();
            return;
        }

        // Validation : description
        if (descChap.getText().trim().length() < 10) {
            showError("La description doit contenir au moins 10 caractères !");
            descChap.setStyle("-fx-border-color: red;");
            descChap.requestFocus();
            return;
        }

        // Validation : ordre (doit être un entier positif)
        int ordre;
        try {
            ordre = Integer.parseInt(ordreChap.getText().trim());
            if (ordre < 0) {
                showError("L'ordre doit être un entier positif !");
                ordreChap.setStyle("-fx-border-color: red;");
                ordreChap.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            showError("L'ordre doit être un nombre entier valide !");
            ordreChap.setStyle("-fx-border-color: red;");
            ordreChap.requestFocus();
            return;
        }

        // Validation : durée
        if (dureeChap.getText().trim().length() < 2) {
            showError("La durée doit contenir au moins 2 caractères !");
            dureeChap.setStyle("-fx-border-color: red;");
            dureeChap.requestFocus();
            return;
        }

        // Validation : contenu
        if (contenuChap.getText().trim().isEmpty()) {
            showError("Le contenu ne peut pas être vide !");
            contenuChap.setStyle("-fx-border-color: red;");
            contenuChap.requestFocus();
            return;
        }

        // Validation : fichier PDF/video/image doit exister sur le disque
        String type = typeContenu.getValue();
        if (!"texte".equals(type)) {
            File fichier = new File(contenuChap.getText().trim());
            if (!fichier.exists() || !fichier.isFile()) {
                showError("Le fichier sélectionné est introuvable. Veuillez choisir un fichier valide.");
                contenuChap.setStyle("-fx-border-color: red;");
                return;
            }
        }

        // Réinitialiser les styles avant sauvegarde
        resetStyles();

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

            retour();
        } catch (SQLException e) {
            showError(e.getMessage());
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
        resetStyles();
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