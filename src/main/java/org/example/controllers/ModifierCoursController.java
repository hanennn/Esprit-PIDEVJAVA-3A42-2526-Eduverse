package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.entities.cours;
import org.example.services.coursservices;
import java.sql.SQLException;

public class ModifierCoursController {

    @FXML private TextField titreCours;
    @FXML private TextArea  descCours;
    @FXML private TextField niveauCours;
    @FXML private TextField matiereCours;
    @FXML private TextField langueCours;
    @FXML private Label     messageForm;

    private cours coursAModifier;

    public void setCours(cours c) {
        this.coursAModifier = c;
        titreCours.setText(c.getTitre_cours());
        descCours.setText(c.getDescription());
        niveauCours.setText(c.getNiv_cours());
        matiereCours.setText(c.getMatiere_cours());
        langueCours.setText(c.getLangue_cours());
    }

    @FXML
    void modifierCours() throws SQLException {
        resetStyles();

        if (titreCours.getText().trim().isEmpty()) {
            titreCours.setStyle("-fx-border-color: red;");
            showError("Le titre est obligatoire !"); return;
        }
        if (descCours.getText().trim().isEmpty()) {
            descCours.setStyle("-fx-border-color: red;");
            showError("La description est obligatoire !"); return;
        }
        if (niveauCours.getText().trim().isEmpty()) {
            niveauCours.setStyle("-fx-border-color: red;");
            showError("Le niveau est obligatoire !"); return;
        }
        if (matiereCours.getText().trim().isEmpty()) {
            matiereCours.setStyle("-fx-border-color: red;");
            showError("La matière est obligatoire !"); return;
        }
        if (langueCours.getText().trim().isEmpty()) {
            langueCours.setStyle("-fx-border-color: red;");
            showError("La langue est obligatoire !"); return;
        }

        coursAModifier.setTitre_cours(titreCours.getText().trim());
        coursAModifier.setDescription(descCours.getText().trim());
        coursAModifier.setNiv_cours(niveauCours.getText().trim());
        coursAModifier.setMatiere_cours(matiereCours.getText().trim());
        coursAModifier.setLangue_cours(langueCours.getText().trim());

        new coursservices().modifier(coursAModifier.getId(), coursAModifier);

        if (messageForm != null) {
            messageForm.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
            messageForm.setText("✅ Cours modifié avec succès !");
        }

        retour();
    }

    @FXML
    void annuler() {
        resetStyles();
        if (coursAModifier != null) setCours(coursAModifier);
        if (messageForm != null) messageForm.setText("");
    }

    @FXML
    void retour() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/AccueilFormateur.fxml"));
            Stage stage = (Stage) titreCours.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Espace Formateur");
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void resetStyles() {
        titreCours.setStyle(""); descCours.setStyle("");
        niveauCours.setStyle(""); matiereCours.setStyle("");
        langueCours.setStyle("");
    }

    private void showError(String message) {
        if (messageForm != null) {
            messageForm.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            messageForm.setText("⚠ " + message);
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText(message);
            alert.show();
        }
    }
}