package org.example.controllers;

import org.example.entities.Session;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;
import org.example.entities.bourses;
import org.example.entities.demande;
import org.example.services.boursesService;
import org.example.services.demandeService;
import org.example.services.UserService;
import org.example.services.NotificationService;
import org.example.utils.BoursesEmailService;
import org.example.utils.PdfService;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class DetailDemandeController implements Initializable {

    @FXML private TextField tfNiveauEtudes;
    @FXML private ComboBox<String> cbStatut;
    @FXML private TextArea taLettreMotivation;
    @FXML private Label lblErreur;
    @FXML private Button btnModifier;
    @FXML private Button btnSauvegarder;
    @FXML private javafx.scene.layout.VBox vboxCvAnalyse;
    @FXML private Label lblCvAnalyse;

    private demandeService service = new demandeService();
    private boursesService bService = new boursesService();
    private UserService uService = new UserService();
    private NotificationService notifService = new NotificationService();
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

            if (currentDemande.getCvAnalyse() != null && !currentDemande.getCvAnalyse().isEmpty()) {
                try {
                    com.google.gson.JsonObject cvJson = com.google.gson.JsonParser.parseString(currentDemande.getCvAnalyse()).getAsJsonObject();
                    String profil = cvJson.has("profil") ? cvJson.get("profil").getAsString() : "";
                    int score = cvJson.has("score_pertinence") ? cvJson.get("score_pertinence").getAsInt() : 0;
                    lblCvAnalyse.setText("Profil : " + profil + "\nScore de pertinence : " + score + "/100");
                } catch (Exception e) {
                    lblCvAnalyse.setText(currentDemande.getCvAnalyse());
                }
                if (vboxCvAnalyse != null) {
                    vboxCvAnalyse.setVisible(true);
                    vboxCvAnalyse.setManaged(true);
                }
            }
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

        if (!newStatus.equals(oldStatus)) {
            int etudiantId = currentDemande.getEtudiant_id();
            String studentEmail = uService.getEmailParId(etudiantId);
            String studentNom = uService.getNomParId(etudiantId);
            bourses bourse = bService.getById(currentDemande.getBourse_id());

            if (studentEmail != null && bourse != null) {
                new Thread(() -> {
                    try {
                        BoursesEmailService.sendStatusUpdateEmail(studentEmail, studentNom, bourse, newStatus, currentDemande.getNote());
                        System.out.println("Email envoye avec succes");
                    } catch (Exception e) {
                        System.err.println("Erreur envoi email: " + e.getMessage());
                    }
                }).start();

                String notifMsg;
                if (newStatus.toLowerCase().contains("accept")) {
                    notifMsg = "Votre demande pour la bourse \"" + bourse.getTitre() + "\" a ete ACCEPTEE ! Vous pouvez passer l'interview IA.";
                } else if (newStatus.toLowerCase().contains("refus")) {
                    notifMsg = "Votre demande pour la bourse \"" + bourse.getTitre() + "\" a ete refusee.";
                } else {
                    notifMsg = "Le statut de votre demande pour \"" + bourse.getTitre() + "\" a ete mis a jour : " + newStatus;
                }
                notifService.creer(currentDemande.getId(), notifMsg);
                System.out.println("Notification creee");
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

    @FXML
    private void genererPDF(ActionEvent event) {
        if (currentDemande == null) return;

        int etudiantId = currentDemande.getEtudiant_id();
        String nomEtudiant = uService.getNomParId(etudiantId);
        String emailEtudiant = uService.getEmailParId(etudiantId);
        bourses bourse = bService.getById(currentDemande.getBourse_id());

        if (nomEtudiant == null || bourse == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Impossible de recuperer les informations de l'etudiant ou de la bourse.");
            alert.showAndWait();
            return;
        }

        String bureau = System.getProperty("user.home") + File.separator + "Desktop";
        String nomFichier = "Attestation_Demande_" + currentDemande.getId() + ".pdf";
        String cheminComplet = bureau + File.separator + nomFichier;

        try {
            PdfService.genererAttestationDemande(currentDemande, bourse, nomEtudiant, emailEtudiant, cheminComplet);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("PDF Genere");
            alert.setHeaderText(null);
            alert.setContentText("L'attestation a ete generee avec succes !\nFichier : " + cheminComplet);
            alert.showAndWait();

        } catch (IOException e) {
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
        if (lm.isEmpty() || lm.length() < 5) {
            lblErreur.setText("La lettre de motivation est obligatoire (> 5 caractères)");
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
    private void telechargerCV(ActionEvent event) {
        if (currentDemande == null || currentDemande.getCvPath() == null || currentDemande.getCvPath().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("CV indisponible");
            alert.setHeaderText(null);
            alert.setContentText("Aucun CV n'a ete joint a cette demande.");
            alert.showAndWait();
            return;
        }

        java.io.File cvFile = new java.io.File(currentDemande.getCvPath());
        if (!cvFile.exists()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Fichier introuvable");
            alert.setHeaderText(null);
            alert.setContentText("Le fichier CV n'existe plus sur le disque.");
            alert.showAndWait();
            return;
        }

        try {
            java.awt.Desktop.getDesktop().open(cvFile);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Impossible d'ouvrir le CV : " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void ouvrirInterviewIA(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demandes/InterviewIA.fxml"));
            Parent root = loader.load();
            InterviewIAController ctrl = loader.getController();
            ctrl.setDemande(currentDemande);
            tfNiveauEtudes.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void retourListe(ActionEvent event) { naviguerVers("/org/example/demandes/ListeDemandes.fxml"); }
    @FXML
    private void allerBourses(ActionEvent event) { naviguerVers("/org/example/bourses/ListeBourses.fxml"); }
    @FXML
    private void allerStatistiques(ActionEvent event) { naviguerVers("/org/example/bourses/Statistiques.fxml"); }
    @FXML
    private void allerInterviewIA(ActionEvent event) { naviguerVers("/org/example/demandes/ListeInterviews.fxml"); }
    @FXML
    private void allerAccueil(ActionEvent event) {
        try {
            Session.logout();
        } catch (Exception e) {
            return;
        }
    }

    private void naviguerVers(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            tfNiveauEtudes.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
