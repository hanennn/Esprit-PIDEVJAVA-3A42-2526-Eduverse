package controllers.bourses;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import models.bourses;
import services.boursesService;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;

public class AjoutBourseController {

    @FXML
    private TextField tfTitre;
    @FXML
    private TextArea taDescription;
    @FXML
    private DatePicker dpDateAttribution;
    @FXML
    private DatePicker dpDateFin;
    @FXML
    private TextField tfMontant;
    @FXML
    private Label lblErreur;

    private boursesService service = new boursesService();

    @FXML
    private void ajouterBourse(ActionEvent event) {
        resetStyles();
        
        if (!validerFormulaire()) {
            return;
        }

        bourses b = new bourses();
        b.setTitre(tfTitre.getText() != null ? tfTitre.getText().trim() : "");
        b.setDescription(taDescription.getText() != null ? taDescription.getText().trim() : "");
        b.setImage("Pas d'image");
        b.setMontant(Double.parseDouble(tfMontant.getText().trim()));
        
        b.setDate_attribution(Timestamp.valueOf(dpDateAttribution.getValue().atStartOfDay()));
        b.setDate_fin(Timestamp.valueOf(dpDateFin.getValue().atStartOfDay()));

        service.add(b);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText("Bourse ajoutée avec succès !");
        alert.showAndWait();

        retourListe();
    }


    private boolean validerFormulaire() {
        StringBuilder errors = new StringBuilder();
        boolean isValid = true;

        if (tfTitre.getText().trim().isEmpty()) {
            tfTitre.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
            errors.append("• Le titre est obligatoire\n");
            isValid = false;
        }

        double montant = 0;
        try {
            montant = Double.parseDouble(tfMontant.getText().trim());
            if (montant <= 0) {
                tfMontant.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
                errors.append("• Le montant doit être un nombre positif\n");
                isValid = false;
            }
        } catch (NumberFormatException e) {
            tfMontant.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
            errors.append("• Le montant doit être numérique\n");
            isValid = false;
        }

        if (dpDateAttribution.getValue() == null) {
            errors.append("• La date d'attribution est obligatoire\n");
            isValid = false;
        } else if (dpDateAttribution.getValue().isBefore(LocalDate.now())) {
            errors.append("• La date d'attribution ne peut pas être dans le passé\n");
            isValid = false;
        }

        if (dpDateFin.getValue() == null) {
            errors.append("• La date de fin est obligatoire\n");
            isValid = false;
        } else if (dpDateFin.getValue().isBefore(LocalDate.now())) {
            errors.append("• La date de fin ne peut pas être dans le passé\n");
            isValid = false;
        }

        if (dpDateAttribution.getValue() != null && dpDateFin.getValue() != null && 
            dpDateFin.getValue().isBefore(dpDateAttribution.getValue())) {
            errors.append("• La date de fin doit être après la date d'attribution\n");
            isValid = false;
        }

        if (isValid && service.getAll().stream()
                .anyMatch(b -> b.getTitre().equalsIgnoreCase(tfTitre.getText().trim()))) {
            tfTitre.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
            errors.append("• Une bourse avec ce titre existe déjà\n");
            isValid = false;
        }

        if (!isValid) {
            lblErreur.setText(errors.toString().trim());
            lblErreur.setVisible(true);
        }

        return isValid;
    }

    private void showError(TextField field, String message) {
        field.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
        lblErreur.setText(message);
        lblErreur.setVisible(true);
    }
    
    private void resetStyles() {
        lblErreur.setVisible(false);
        tfTitre.setStyle("");
        tfMontant.setStyle("");
    }

    @FXML
    private void annuler(ActionEvent event) {
        retourListe();
    }

    private void retourListe() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/bourses/ListeBourses.fxml"));
            Parent root = loader.load();
            tfTitre.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
