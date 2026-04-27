package controllers.demandes;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import models.bourses;
import models.demande;
import services.boursesService;
import services.demandeService;

import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.List;
import java.util.ResourceBundle;

public class AjoutDemandeController implements Initializable {

    @FXML
    private TextField tfNiveauEtudes;
    @FXML
    private ComboBox<String> cbStatut;
    @FXML
    private TextArea taLettreMotivation;
    @FXML
    private TextField tfNote;
    @FXML
    private ComboBox<bourses> cbBourse;
    @FXML
    private Label lblErreur;

    private demandeService service = new demandeService();
    private boursesService bService = new boursesService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cbStatut.setItems(FXCollections.observableArrayList("En attente", "Acceptée", "Refusée"));
        
        List<bourses> boursesList = bService.getAll();
        cbBourse.setItems(FXCollections.observableArrayList(boursesList));
        
        cbBourse.setConverter(new StringConverter<bourses>() {
            @Override
            public String toString(bourses object) {
                return object == null ? null : object.getTitre();
            }

            @Override
            public bourses fromString(String string) {
                return null;
            }
        });
    }

    @FXML
    private void ajouterDemande(ActionEvent event) {
        resetStyles();
        
        if (!validerFormulaire()) {
            return;
        }

        demande d = new demande();
        d.setNiveau_etudes(tfNiveauEtudes.getText().trim());
        d.setStatut(cbStatut.getValue());
        d.setLettre_motivation(taLettreMotivation.getText().trim());
        d.setNote(tfNote.getText().trim());
        d.setBourse_id(cbBourse.getValue().getId());
        d.setDate_demande(new Timestamp(System.currentTimeMillis()));
        d.setEtudiant_id(1);

        service.add(d);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText("Demande ajoutée avec succès !");
        alert.showAndWait();

        retourListe();
    }

    private boolean validerFormulaire() {
        if (tfNiveauEtudes.getText().trim().isEmpty()) {
            showError(tfNiveauEtudes, "Le niveau d'études est obligatoire");
            return false;
        }

        if (cbStatut.getValue() == null) {
            lblErreur.setText("Le statut est obligatoire");
            lblErreur.setVisible(true);
            return false;
        }

        String lm = taLettreMotivation.getText().trim();
        if (lm.isEmpty() || lm.length() < 50) {
            lblErreur.setText("La lettre de motivation est obligatoire (> 50 caractères)");
            lblErreur.setVisible(true);
            taLettreMotivation.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
            return false;
        }

        if (cbBourse.getValue() == null) {
            lblErreur.setText("La bourse est obligatoire");
            lblErreur.setVisible(true);
            return false;
        }
        
        String noteStr = tfNote.getText().trim();
        if (!noteStr.isEmpty()) {
            try {
                double note = Double.parseDouble(noteStr);
                if (note < 0 || note > 20) {
                    showError(tfNote, "La note doit être entre 0 et 20");
                    return false;
                }
            } catch (NumberFormatException e) {
                showError(tfNote, "La note doit être numérique");
                return false;
            }
        }

        return true;
    }

    private void showError(TextField field, String message) {
        field.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
        lblErreur.setText(message);
        lblErreur.setVisible(true);
    }
    
    private void resetStyles() {
        lblErreur.setVisible(false);
        tfNiveauEtudes.setStyle("");
        tfNote.setStyle("");
        taLettreMotivation.setStyle("");
    }

    @FXML
    private void annuler(ActionEvent event) {
        retourListe();
    }

    private void retourListe() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/demandes/ListeDemandes.fxml"));
            Parent root = loader.load();
            tfNiveauEtudes.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
