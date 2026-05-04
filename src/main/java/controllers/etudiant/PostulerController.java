package controllers.etudiant;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import models.bourses;
import models.demande;
import services.InterviewIAService;
import services.demandeService;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.util.UUID;

public class PostulerController {

    @FXML private Label lblBourseTitre;
    @FXML private TextField tfNiveauEtudes;
    @FXML private TextArea taLettreMotivation;
    @FXML private Label lblErreur;
    @FXML private Button btnCV;
    @FXML private Label lblCVStatus;

    private bourses bourseConcernee;
    private demandeService service = new demandeService();
    private File cvFile;
    private String cvAnalyseResult;

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
        d.setStatut("En attente");
        d.setBourse_id(bourseConcernee.getId());
        d.setDate_demande(new Timestamp(System.currentTimeMillis()));
        d.setEtudiant_id(1);
        if (cvAnalyseResult != null) {
            d.setCvAnalyse(cvAnalyseResult);
        }

        if (cvFile != null) {
            try {
                File uploadsDir = new File("uploads/cv");
                if (!uploadsDir.exists()) uploadsDir.mkdirs();
                String destName = "cv_demande_" + System.currentTimeMillis() + ".pdf";
                File dest = new File(uploadsDir, destName);
                Files.copy(cvFile.toPath(), dest.toPath());
                d.setCvPath(dest.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("Erreur copie CV: " + e.getMessage());
            }
        }

        service.add(d);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Candidature envoyee");
        alert.setHeaderText(null);
        alert.setContentText("Votre dossier a bien ete soumis ! Vous serez notifie(e) lorsque votre demande sera traitee.");
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

        // Verifier que l'etudiant n'a pas deja postule a cette bourse
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
    private void choisirCV(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choisir votre CV (PDF)");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        cvFile = fc.showOpenDialog(tfNiveauEtudes.getScene().getWindow());

        if (cvFile != null) {
            lblCVStatus.setText("CV joint : " + cvFile.getName());
            btnCV.setDisable(true);
            btnCV.setText("CV joint");

            // Envoyer le CV au microservice pour analyse
            new Thread(() -> {
                try {
                    String boundary = UUID.randomUUID().toString();
                    byte[] fileBytes = Files.readAllBytes(cvFile.toPath());

                    String CRLF = "\r\n";
                    StringBuilder sb = new StringBuilder();
                    sb.append("--").append(boundary).append(CRLF);
                    sb.append("Content-Disposition: form-data; name=\"cv\"; filename=\"").append(cvFile.getName()).append("\"").append(CRLF);
                    sb.append("Content-Type: application/pdf").append(CRLF).append(CRLF);
                    byte[] header = sb.toString().getBytes();
                    byte[] footer = (CRLF + "--" + boundary + "--" + CRLF).getBytes();
                    byte[] body = new byte[header.length + fileBytes.length + footer.length];
                    System.arraycopy(header, 0, body, 0, header.length);
                    System.arraycopy(fileBytes, 0, body, header.length, fileBytes.length);
                    System.arraycopy(footer, 0, body, header.length + fileBytes.length, footer.length);

                    HttpClient client = HttpClient.newHttpClient();
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("http://localhost:8001/analyser-cv"))
                            .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                            .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                            .timeout(java.time.Duration.ofSeconds(30))
                            .build();

                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() == 200) {
                        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                        String profil = json.get("profil").getAsString();
                        int score = json.get("score_pertinence").getAsInt();
                        cvAnalyseResult = response.body();
                        System.out.println("CV analyse : " + profil + " (score " + score + ")");
                    }
                } catch (Exception e) {
                    System.err.println("Analyse CV indisponible: " + e.getMessage());
                }
            }).start();
        }
    }

    @FXML
    private void annuler(ActionEvent event) {
        retourListe();
    }

    private void retourListe() {
        naviguerVers("/etudiant/EtudiantBourses.fxml");
    }

    @FXML
    private void allerCalendrier(ActionEvent event) { naviguerVers("/etudiant/CalendrierBourses.fxml"); }
    @FXML
    private void allerAccueil(ActionEvent event) { naviguerVers("/accueil/Accueil.fxml"); }
    @FXML
    private void allerInterviewIA(ActionEvent event) { naviguerVers("/etudiant/InterviewEtudiant.fxml"); }

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
