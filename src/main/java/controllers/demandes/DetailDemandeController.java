package controllers.demandes;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import models.User;
import models.bourses;
import models.demande;
import services.boursesService;
import services.demandeService;
import services.userService;
import utils.EmailService;
import utils.PdfService;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class DetailDemandeController implements Initializable {

    @FXML
    private TextField tfNiveauEtudes;
    @FXML
    private ComboBox<String> cbStatut;
    @FXML
    private TextArea taLettreMotivation;
    @FXML
    private Label lblErreur;

    @FXML
    private Button btnModifier;
    @FXML
    private Button btnSauvegarder;

    private demandeService service = new demandeService();
    private boursesService bService = new boursesService();
    private userService uService = new userService();
    private demande currentDemande;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cbStatut.setItems(FXCollections.observableArrayList("En attente", "Acceptée", "Refusée"));
    }

    public void setDemande(demande d) {
        this.currentDemande = d;
        remplirChamps();
    }

    private void remplirChamps() {
        if (currentDemande != null) {
            tfNiveauEtudes.setText(currentDemande.getNiveau_etudes());
            cbStatut.setValue(currentDemande.getStatut());
            taLettreMotivation.setText(currentDemande.getLettre_motivation());
        }
    }

    @FXML
    private void activerModification(ActionEvent event) {
        cbStatut.setDisable(false);
        btnSauvegarder.setDisable(false);
        btnModifier.setDisable(true);
    }

    @FXML
    private void sauvegarder(ActionEvent event) {
        resetStyles();
        
        if (!validerFormulaire()) {
            return;
        }

        String oldStatus = currentDemande.getStatut();
        String newStatus = cbStatut.getValue();
        currentDemande.setStatut(newStatus);

        service.update(currentDemande);

        // Envoi de l'email si le statut a changé vers Accepté ou Refusé
        if (!newStatus.equals(oldStatus) && (newStatus.equals("Acceptée") || newStatus.equals("Refusée"))) {
            User student = uService.getById(currentDemande.getEtudiant_id());
            bourses bourse = bService.getById(currentDemande.getBourse_id());
            
            if (student != null && bourse != null) {
                // Exécuter dans un nouveau thread pour ne pas bloquer l'UI
                new Thread(() -> {
                    EmailService.sendStatusUpdateEmail(student, bourse, newStatus, currentDemande.getNote());
                }).start();
            }
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText("Le statut a été mis à jour !");
        alert.showAndWait();

        cbStatut.setDisable(true);
        btnSauvegarder.setDisable(true);
        btnModifier.setDisable(false);
    }

    // Generer un PDF d'attestation pour cette demande
    // Le fichier est sauvegarde sur le bureau de l'utilisateur
    @FXML
    private void genererPDF(ActionEvent event) {
        if (currentDemande == null) return;

        // Recuperer les informations de l'etudiant et de la bourse
        User etudiant = uService.getById(currentDemande.getEtudiant_id());
        bourses bourse = bService.getById(currentDemande.getBourse_id());

        if (etudiant == null || bourse == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Impossible de recuperer les informations de l'etudiant ou de la bourse.");
            alert.showAndWait();
            return;
        }

        // Construire le chemin du fichier PDF sur le bureau
        String bureau = System.getProperty("user.home") + File.separator + "Desktop";
        String nomFichier = "Attestation_Demande_" + currentDemande.getId() + ".pdf";
        String cheminComplet = bureau + File.separator + nomFichier;

        try {
            // Appeler le service de generation PDF
            PdfService.genererAttestationDemande(currentDemande, bourse, etudiant, cheminComplet);

            // Confirmer a l'utilisateur que le PDF a ete genere
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("PDF Genere");
            alert.setHeaderText(null);
            alert.setContentText("L'attestation a ete generee avec succes !\nFichier : " + cheminComplet);
            alert.showAndWait();

        } catch (IOException e) {
            // Afficher une erreur si la generation echoue
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Erreur lors de la generation du PDF : " + e.getMessage());
            alert.showAndWait();
            e.printStackTrace();
        }
    }

    @FXML
    private void supprimer(ActionEvent event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer la demande ?");
        confirm.setContentText("Cette action est irréversible.");
        Optional<ButtonType> response = confirm.showAndWait();
        if (response.isPresent() && response.get() == ButtonType.OK) {
            service.delete(currentDemande.getId());
            retourListe(null);
        }
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
        taLettreMotivation.setStyle("");
    }

    @FXML
    private void retourListe(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/demandes/ListeDemandes.fxml"));
            Parent root = loader.load();
            tfNiveauEtudes.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
