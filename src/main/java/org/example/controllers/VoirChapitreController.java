package org.example.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;

import javafx.scene.layout.VBox;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.example.entities.chapitres;
import org.example.entities.cours;

import org.example.services.ChatbotService;
import org.example.services.ResumeService;

//lire résume
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.example.services.TextToSpeechService;

//pdf
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class VoirChapitreController {

    @FXML
    private Label labelTitre;
    @FXML
    private Label labelDesc;
    @FXML
    private Label labelOrdre;
    @FXML
    private Label labelDuree;
    @FXML
    private Label labelStatut;
    @FXML
    private Label labelType;
    @FXML
    private Label labelPageInfo;
    @FXML
    private Button btnResume;
    @FXML
    private ImageView imageViewPdf;
    @FXML
    private Button btnPrev;
    @FXML
    private Button btnNext;
    @FXML
    private Button btnLire;
    @FXML
    private VBox pdfContainer;

    private String dernierResume = "";
    private MediaPlayer mediaPlayer;

    private chapitres chapitreActuel;
    private cours coursActuel;
    private List<Image> pages = new ArrayList<>();
    private int pageActuelle = 0;

    public void setChapitre(chapitres ch, cours c) {
        this.chapitreActuel = ch;
        this.coursActuel = c;

        labelTitre.setText(ch.getTitre_chap());
        labelDesc.setText(ch.getDesc_chap());
        labelOrdre.setText("ORDRE: " + ch.getOrdre_chap());
        labelDuree.setText("DURÉE: " + ch.getDuree_chap());
        labelStatut.setText(ch.getStatut_chap());
        labelType.setText("TYPE: " + ch.getType_contenu().toUpperCase());

        chargerPDF(ch.getContenu_chap());
    }

    private void chargerPDF(String chemin) {
        File f = new File(chemin);
        if (!f.exists()) {
            labelPageInfo.setText("⚠ Fichier introuvable : " + chemin);
            return;
        }
        try {
            PDDocument doc = Loader.loadPDF(f);
            PDFRenderer renderer = new PDFRenderer(doc);
            pages.clear();

            for (int i = 0; i < doc.getNumberOfPages(); i++) {
                BufferedImage bImg = renderer.renderImageWithDPI(i, 120);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bImg, "PNG", baos);
                pages.add(new Image(new ByteArrayInputStream(baos.toByteArray())));
            }
            doc.close();
            pageActuelle = 0;
            afficherPage();

        } catch (Exception e) {
            labelPageInfo.setText("⚠ Fichier PDF invalide ou corrompu.");
            System.err.println("PDF error: " + e.getMessage());
        }
    }

    private void afficherPage() {
        if (pages.isEmpty()) return;
        imageViewPdf.setImage(pages.get(pageActuelle));
        labelPageInfo.setText("Page " + (pageActuelle + 1) + " / " + pages.size());
        btnPrev.setDisable(pageActuelle == 0);
        btnNext.setDisable(pageActuelle == pages.size() - 1);
    }

    @FXML
    void pagePrecedente() {
        if (pageActuelle > 0) {
            pageActuelle--;
            afficherPage();
        }
    }

    @FXML
    void pageSuivante() {
        if (pageActuelle < pages.size() - 1) {
            pageActuelle++;
            afficherPage();
        }
    }

    @FXML
    void zoomIn() {
        imageViewPdf.setFitWidth(imageViewPdf.getFitWidth() + 80);
    }

    @FXML
    void zoomOut() {
        imageViewPdf.setFitWidth(Math.max(300, imageViewPdf.getFitWidth() - 80));
    }


    @FXML
    void genererResume() {
        btnResume.setDisable(true);
        btnResume.setText("⏳ Génération...");
        btnLire.setDisable(true);

        new Thread(() -> {
            String texte = "Titre : " + chapitreActuel.getTitre_chap()
                    + ". Description : " + chapitreActuel.getDesc_chap();
            String resume = ResumeService.genererResume(texte);

            // audio en //
            File audio = TextToSpeechService.genererAudio(resume);

            Platform.runLater(() -> {
                dernierResume = resume;
                btnResume.setDisable(false);
                btnResume.setText("✨ Générer le résumé IA");

                // Aff résumé
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Résumé IA — " + chapitreActuel.getTitre_chap());
                alert.setHeaderText("📝 Résumé généré par IA");
                alert.setContentText(resume);
                alert.getDialogPane().setMinWidth(600);
                alert.getDialogPane().setMinHeight(600);
                alert.getDialogPane().setStyle(
                        "-fx-font-size: 14px;" +
                                "-fx-background-color: #fffbe6;" +
                                "-fx-border-color: #f5a623;" +
                                "-fx-border-width: 2;"
                );

                // Lancer la lecture
                if (audio != null) {
                    if (mediaPlayer != null) mediaPlayer.stop();
                    mediaPlayer = new MediaPlayer(new Media(audio.toURI().toString()));
                    mediaPlayer.play();
                    btnLire.setText("🔊 Lecture en cours...");
                    mediaPlayer.setOnEndOfMedia(() ->
                            Platform.runLater(() -> btnLire.setText("🔊 Lire le résumé")));
                }
                btnLire.setDisable(false);

                alert.showAndWait();
            });
        }).start();
    }


    @FXML
    void lireResume() {
        // Vérifier que le résumé a été généré
        String texteALire = dernierResume; // variable à stocker

        if (texteALire == null || texteALire.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Résumé manquant");
            alert.setContentText("Veuillez d'abord générer le résumé IA avant de le lire !");
            alert.show();
            return;
        }

        btnLire.setText("⏳ Génération audio...");
        btnLire.setDisable(true);

        new Thread(() -> {
            File audio = TextToSpeechService.genererAudio(texteALire);
            Platform.runLater(() -> {
                btnLire.setDisable(false);
                if (audio != null) {
                    if (mediaPlayer != null) mediaPlayer.stop();
                    mediaPlayer = new MediaPlayer(new Media(audio.toURI().toString()));
                    mediaPlayer.play();
                    btnLire.setText("🔊 Lecture en cours...");
                    mediaPlayer.setOnEndOfMedia(() -> btnLire.setText("🔊 Lire le résumé"));
                } else {
                    btnLire.setText("🔊 Lire le résumé");
                }
            });
        }).start();
    }


    @FXML
    private Button btnChatbot;

    @FXML
    void ouvrirChatbot() {
        String question = demanderQuestion();
        if (question == null || question.trim().isEmpty()) return;

        btnChatbot.setDisable(true);
        btnChatbot.setText("⏳ Réflexion...");

        new Thread(() -> {
            String reponse = ChatbotService.poserQuestion(
                    chapitreActuel.getTitre_chap(),
                    chapitreActuel.getDesc_chap(),
                    question);
            Platform.runLater(() -> {
                btnChatbot.setDisable(false);
                btnChatbot.setText("💬 Poser une question");

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("💬 Assistant IA");
                alert.setHeaderText("Question : " + question);
                alert.setContentText(reponse);
                alert.getDialogPane().setMinWidth(550);
                alert.getDialogPane().setMinHeight(250);
                alert.getDialogPane().setStyle(
                        "-fx-font-size: 13px;" +
                                "-fx-background-color: #f4f6f8;" +
                                "-fx-border-color: #1a1a2e; -fx-border-width: 2;");
                alert.show();
            });
        }).start();
    }

    private String demanderQuestion() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("💬 Poser une question");
        dialog.setHeaderText("Chapitre : " + chapitreActuel.getTitre_chap());
        dialog.setContentText("Votre question :");
        dialog.getDialogPane().setMinWidth(450);
        return dialog.showAndWait().orElse(null);
    }

    private void ajouterMessage(VBox messagesBox, String texte, boolean estUtilisateur) {
        Label msg = new Label(texte);
        msg.setWrapText(true);
        msg.setMaxWidth(380);
        msg.setStyle(estUtilisateur
                ? "-fx-background-color: #1a1a2e; -fx-text-fill: white;" +
                "-fx-background-radius: 10; -fx-padding: 8 12;"
                : "-fx-background-color: white; -fx-text-fill: #333;" +
                "-fx-background-radius: 10; -fx-padding: 8 12;" +
                "-fx-border-color: #e0e0e0; -fx-border-radius: 10;");

        HBox row = new HBox(msg);
        row.setAlignment(estUtilisateur ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        messagesBox.getChildren().add(row);
    }


    @FXML
    void retour() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/CoursDetailEtudiant.fxml"));
            Stage stage = (Stage) labelTitre.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle(coursActuel.getTitre_cours());
            CoursDetailEtudiantController ctrl = loader.getController();
            ctrl.setCours(
                    coursActuel.getId(),
                    coursActuel.getTitre_cours(),
                    coursActuel.getNiv_cours(),
                    coursActuel.getMatiere_cours(),
                    coursActuel.getLangue_cours(),
                    coursActuel.getDescription()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}