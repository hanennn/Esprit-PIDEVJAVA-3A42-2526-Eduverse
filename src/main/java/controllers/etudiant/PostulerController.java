package controllers.etudiant;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import models.User;
import models.bourses;
import models.demande;
import services.demandeService;
import services.userService;
import utils.SmsService;

import java.io.IOException;
import java.sql.Timestamp;

public class PostulerController {

    @FXML
    private Label lblBourseTitre;
    @FXML
    private TextField tfNiveauEtudes;
    @FXML
    private TextArea taLettreMotivation;
    @FXML
    private Label lblErreur;

    private bourses bourseConcernee;
    private demandeService service = new demandeService();
    private userService uService = new userService();

    public void setBourseSelected(bourses b) {
        this.bourseConcernee = b;
        lblBourseTitre.setText("Postuler pour : " + b.getTitre());
    }

    @FXML
    private void soumettreDemande(ActionEvent event) {
        resetStyles();
        
        if (!validerFormulaire()) {
            return;
        }

        demande d = new demande();
        d.setNiveau_etudes(tfNiveauEtudes.getText().trim());
        d.setLettre_motivation(taLettreMotivation.getText().trim());
        
        // Champs automatiques ou ignorés pour un étudiant
        d.setStatut("En attente"); // Statut par défaut
        d.setBourse_id(bourseConcernee.getId());
        d.setDate_demande(new Timestamp(System.currentTimeMillis()));
        d.setEtudiant_id(1); // Utilisateur fictif pour le sprint

        service.add(d);

        // --- Envoi WhatsApp de confirmation via Twilio ---
        // Le message est envoye dans un thread separe pour ne pas bloquer l'interface
        User etudiant = uService.getById(d.getEtudiant_id());
        if (etudiant != null) {
            new Thread(() -> {
                // Numero WhatsApp de l'etudiant (format international)
                String numeroEtudiant = "+21698318463";
                SmsService.envoyerConfirmationDemande(
                        numeroEtudiant,
                        etudiant.getPrenom(),
                        bourseConcernee.getTitre()
                );
            }).start();
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Candidature envoyee");
        alert.setHeaderText(null);
        alert.setContentText("Votre dossier a bien ete soumis ! Un SMS de confirmation vous sera envoye.");
        alert.showAndWait();

        retourListe();
    }

    private boolean validerFormulaire() {
        if (tfNiveauEtudes.getText().trim().isEmpty()) {
            showError(tfNiveauEtudes, "Veuillez préciser votre niveau d'études");
            return false;
        }

        String lm = taLettreMotivation.getText().trim();
        if (lm.isEmpty() || lm.length() < 50) {
            lblErreur.setText("La lettre de motivation doit faire au moins 50 caractères");
            lblErreur.setVisible(true);
            taLettreMotivation.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
            return false;
        }

        // Vérification de l'unicité de la candidature pour l'étudiant (ID = 1) et cette bourse
        boolean alreadyApplied = service.getAll().stream()
                .anyMatch(d -> d.getEtudiant_id() == 1 && d.getBourse_id() == bourseConcernee.getId());
        
        if (alreadyApplied) {
            lblErreur.setText("Vous avez déjà postulé à cette bourse !");
            lblErreur.setVisible(true);
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
    private void annuler(ActionEvent event) {
        retourListe();
    }

    private void retourListe() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/etudiant/EtudiantBourses.fxml"));
            Parent root = loader.load();
            tfNiveauEtudes.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
