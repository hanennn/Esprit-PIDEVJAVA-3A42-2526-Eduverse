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
import java.util.Optional;

public class DetailBourseController {

    @FXML
    private TextField tfTitre;
    @FXML
    private TextArea taDescription;
    @FXML
    private TextField tfMontant;
    @FXML
    private DatePicker dpDateAttribution;
    @FXML
    private DatePicker dpDateFin;
    @FXML
    private Label lblErreur;

    @FXML
    private Button btnModifier;
    @FXML
    private Button btnSauvegarder;
    @FXML
    private Button btnSupprimer;

    private boursesService service = new boursesService();
    private bourses currentBourse;

    public void setBourse(bourses b) {
        this.currentBourse = b;
        remplirChamps();
    }

    private void remplirChamps() {
        if (currentBourse != null) {
            tfTitre.setText(currentBourse.getTitre());
            taDescription.setText(currentBourse.getDescription());
            tfMontant.setText(String.valueOf(currentBourse.getMontant()));
            if (currentBourse.getDate_attribution() != null) {
                dpDateAttribution.setValue(currentBourse.getDate_attribution().toLocalDateTime().toLocalDate());
            }
            if (currentBourse.getDate_fin() != null) {
                dpDateFin.setValue(currentBourse.getDate_fin().toLocalDateTime().toLocalDate());
            }
        }
    }

    @FXML
    private void activerModification(ActionEvent event) {
        tfTitre.setDisable(false);
        taDescription.setDisable(false);
        tfMontant.setDisable(false);
        dpDateAttribution.setDisable(false);
        dpDateFin.setDisable(false);
        
        btnSauvegarder.setDisable(false);
        btnModifier.setDisable(true);
    }

    @FXML
    private void sauvegarder(ActionEvent event) {
        resetStyles();
        
        if (!validerFormulaire()) {
            return;
        }

        currentBourse.setTitre(tfTitre.getText() != null ? tfTitre.getText().trim() : "");
        currentBourse.setDescription(taDescription.getText() != null ? taDescription.getText().trim() : "");
        currentBourse.setImage("Pas d'image");
        currentBourse.setMontant(Double.parseDouble(tfMontant.getText().trim()));
        currentBourse.setDate_attribution(Timestamp.valueOf(dpDateAttribution.getValue().atStartOfDay()));
        currentBourse.setDate_fin(Timestamp.valueOf(dpDateFin.getValue().atStartOfDay()));

        service.update(currentBourse);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText("Bourse mise à jour avec succès !");
        alert.showAndWait();

        tfTitre.setDisable(true);
        taDescription.setDisable(true);
        tfMontant.setDisable(true);
        dpDateAttribution.setDisable(true);
        dpDateFin.setDisable(true);
        
        btnSauvegarder.setDisable(true);
        btnModifier.setDisable(false);
    }

    @FXML
    private void supprimer(ActionEvent event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer la bourse ?");
        confirm.setContentText("Cette action est irréversible.");
        Optional<ButtonType> response = confirm.showAndWait();
        if (response.isPresent() && response.get() == ButtonType.OK) {
            service.delete(currentBourse.getId());
            retourListe(null);
        }
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

        if (isValid && !tfTitre.getText().trim().equalsIgnoreCase(currentBourse.getTitre())) {
            if (service.getAll().stream().anyMatch(b -> b.getTitre().equalsIgnoreCase(tfTitre.getText().trim()))) {
                tfTitre.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
                errors.append("• Une autre bourse avec ce titre existe déjà\n");
                isValid = false;
            }
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
    private void retourListe(ActionEvent event) { naviguerVers("/bourses/ListeBourses.fxml"); }
    @FXML
    private void allerDemandes(ActionEvent event) { naviguerVers("/demandes/ListeDemandes.fxml"); }
    @FXML
    private void allerStatistiques(ActionEvent event) { naviguerVers("/bourses/Statistiques.fxml"); }
    @FXML
    private void allerInterviewIA(ActionEvent event) { naviguerVers("/demandes/ListeInterviews.fxml"); }
    @FXML
    private void allerAccueil(ActionEvent event) { naviguerVers("/accueil/Accueil.fxml"); }

    private void naviguerVers(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            tfTitre.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
