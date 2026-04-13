package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import org.example.entities.cours;
import org.example.services.coursservices;
import java.sql.SQLException;

public class ModifierCoursController {

    @FXML private TextField titreCours;
    @FXML private TextArea  descCours;
    @FXML private TextField niveauCours;
    @FXML private TextField matiereCours;
    @FXML private TextField langueCours;

    private cours coursAModifier;

    // Pré-remplir les champs avec les données du cours
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

        // Contrôle de saisie
        if (titreCours.getText().isEmpty()) {
            showError("Le titre est obligatoire !");
            titreCours.setStyle("-fx-border-color: red;");
            return;
        }
        if (descCours.getText().isEmpty()) {
            showError("La description est obligatoire !");
            descCours.setStyle("-fx-border-color: red;");
            return;
        }
        if (niveauCours.getText().isEmpty()) {
            showError("Le niveau est obligatoire !");
            niveauCours.setStyle("-fx-border-color: red;");
            return;
        }
        if (matiereCours.getText().isEmpty()) {
            showError("La matière est obligatoire !");
            matiereCours.setStyle("-fx-border-color: red;");
            return;
        }
        if (langueCours.getText().isEmpty()) {
            showError("La langue est obligatoire !");
            langueCours.setStyle("-fx-border-color: red;");
            return;
        }

        // Modifier les données
        coursAModifier.setTitre_cours(titreCours.getText());
        coursAModifier.setDescription(descCours.getText());
        coursAModifier.setNiv_cours(niveauCours.getText());
        coursAModifier.setMatiere_cours(matiereCours.getText());
        coursAModifier.setLangue_cours(langueCours.getText());

        new coursservices().modifier(coursAModifier.getId(), coursAModifier);

        // Alerte succès
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setContentText("Cours modifié avec succès !");
        alert.show();

        retour();
    }

    @FXML
    void annuler() {
        titreCours.setStyle("");
        descCours.setStyle("");
        niveauCours.setStyle("");
        matiereCours.setStyle("");
        langueCours.setStyle("");
        // Remettre les valeurs originales
        if (coursAModifier != null) {
            setCours(coursAModifier);
        }
    }

    @FXML
    void retour() {
        try {
            Parent root = FXMLLoader.load(getClass()
                    .getResource("/listeCours.fxml"));
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