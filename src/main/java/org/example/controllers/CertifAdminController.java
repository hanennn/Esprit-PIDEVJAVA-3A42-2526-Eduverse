package org.example.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Font;
import javafx.scene.SnapshotParameters;
import javafx.embed.swing.SwingFXUtils;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.entities.Certification;
import org.example.entities.CertificationFinale;
import org.example.entities.Session;
import org.example.services.CertificationFinaleService;
import org.example.services.CertificationService;
import org.example.services.EmailService;
import org.example.services.UserService;

import javax.imageio.ImageIO;
import java.io.File;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

public class CertifAdminController {

    @FXML private VBox   certifContainer;
    @FXML private Label  lblTotalCertif;
    @FXML private Label  lblTotalReussi;
    @FXML private Label  lblTotalFinales;

    private final CertificationService       certifService       = new CertificationService();
    private final CertificationFinaleService certifFinaleService = new CertificationFinaleService();
    private final UserService                userService         = new UserService();

    // ─────────── INIT ───────────
    @FXML
    public void initialize() {
        chargerCertifications();
    }

    // ─────────── CHARGER CERTIFICATIONS ───────────
    @FXML
    public void chargerCertifications() {
        certifContainer.getChildren().clear();
        try {
            List<Certification> list = certifService.afficher();

            // Stats
            long nbReussi  = list.stream().filter(c -> "Réussi".equalsIgnoreCase(c.getStatut())).count();
            long nbFinales = certifFinaleService.afficher().size();
            if (lblTotalCertif  != null) lblTotalCertif.setText(String.valueOf(list.size()));
            if (lblTotalReussi  != null) lblTotalReussi.setText(String.valueOf(nbReussi));
            if (lblTotalFinales != null) lblTotalFinales.setText(String.valueOf(nbFinales));

            if (list.isEmpty()) {
                Label vide = new Label("Aucune certification trouvée.");
                vide.setStyle("-fx-text-fill: #888; -fx-font-size: 13px; -fx-padding: 20;");
                certifContainer.getChildren().add(vide);
                return;
            }

            for (Certification c : list) {
                VBox carte = creerCarteTentative(c);
                certifContainer.getChildren().add(carte);
                chargerCertifFinalesLiees(c.getId(), carte);
            }

        } catch (Exception e) {
            Label err = new Label("Erreur : " + e.getMessage());
            err.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px;");
            certifContainer.getChildren().add(err);
        }
    }

    // ─────────── CARTE TENTATIVE ───────────
    private VBox creerCarteTentative(Certification c) {
        boolean reussi = c.getStatut() != null && c.getStatut().equalsIgnoreCase("Réussi");

        VBox card = new VBox(10);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #e8e8e8;" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-width: 1;" +
                        "-fx-padding: 20;"
        );

        // ── Header ──
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label statutIcon = new Label(reussi ? "✅" : "❌");
        statutIcon.setStyle("-fx-font-size: 18px;");

        Label idLabel = new Label("Tentative #" + c.getId());
        idLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1a1a2e;");

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label statutBadge = new Label(c.getStatut() != null ? c.getStatut() : "—");
        statutBadge.setStyle(reussi
                ? "-fx-background-color: #e8f5e9; -fx-text-fill: #2ecc71; -fx-font-weight: bold; -fx-font-size: 11px; -fx-background-radius: 20; -fx-padding: 4 12;"
                : "-fx-background-color: #fdecea; -fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 11px; -fx-background-radius: 20; -fx-padding: 4 12;");

        header.getChildren().addAll(statutIcon, idLabel, spacer, statutBadge);

        // ── Barre colorée ──
        HBox barre = new HBox();
        barre.setPrefHeight(2);
        barre.setStyle("-fx-background-color: " + (reussi ? "#2ecc71" : "#e74c3c") + "; -fx-background-radius: 2;");

        // ── Infos score / badge ──
        HBox infos = new HBox(20);
        infos.setAlignment(Pos.CENTER_LEFT);

        Label scoreLabel = new Label("🎯 Score : " + String.format("%.1f%%", c.getScoreObtenu()));
        scoreLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #f5a623; -fx-font-weight: bold;");

        Label badgeLabel = new Label("🏅 " + (c.getBadge() != null ? c.getBadge() : "—"));
        badgeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");

        infos.getChildren().addAll(scoreLabel, badgeLabel);

        // ── Détails ──
        HBox details = new HBox(20);
        Label quizLabel = new Label("📝 Quiz ID : " + c.getQuizId());
        quizLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");
        Label userLabel = new Label("👤 User ID : " + c.getUserId());
        userLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");
        Label dateLabel = new Label("📅 " + c.getDateAttribution());
        dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");
        details.getChildren().addAll(quizLabel, userLabel, dateLabel);

        card.getChildren().addAll(header, barre, infos, details);

        // ── Bouton certification finale (si réussi) ──
        if (reussi) {
            Button btnCertifFinale = new Button("🏆  Ajouter Certification Finale");
            btnCertifFinale.setStyle(
                    "-fx-background-color: #f5a623; -fx-text-fill: white;" +
                            "-fx-font-weight: bold; -fx-font-size: 12px;" +
                            "-fx-background-radius: 8; -fx-padding: 9 20; -fx-cursor: hand;");
            btnCertifFinale.setOnAction(e -> afficherPopupCertifFinale(c, card));
            card.getChildren().add(btnCertifFinale);
        }

        return card;
    }

    // ─────────── CERTIF FINALES LIÉES ───────────
    private void chargerCertifFinalesLiees(int tentativeId, VBox carte) {
        try {
            for (CertificationFinale cf : certifFinaleService.afficher()) {
                if (cf.getTentativeId() == tentativeId)
                    carte.getChildren().add(creerCarteFinaleExistante(cf));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private VBox creerCarteFinaleExistante(CertificationFinale cf) {
        VBox cardFinale = new VBox(8);
        cardFinale.setStyle(
                "-fx-background-color: #fff8ee;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 15;" +
                        "-fx-border-color: #f5a623;" +
                        "-fx-border-radius: 10;" +
                        "-fx-border-width: 2;");
        VBox.setMargin(cardFinale, new Insets(5, 0, 0, 20));

        Label titre = new Label("🏆 Certification Finale #" + cf.getId());
        titre.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #f5a623;");

        HBox infos = new HBox(20);
        Label badgeL = new Label("🏅 " + (cf.getBadge() != null ? cf.getBadge() : "—"));
        badgeL.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");
        Label dateL = new Label("📅 " + cf.getDateEmission());
        dateL.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");
        Label userL = new Label("👤 User ID : " + cf.getUserId());
        userL.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");
        Label quizL = new Label("📝 Quiz ID : " + cf.getQuizId());
        quizL.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");
        infos.getChildren().addAll(badgeL, dateL, userL, quizL);

        cardFinale.getChildren().addAll(titre, infos);
        return cardFinale;
    }

    // ─────────── POPUP CERTIFICATION FINALE ───────────
    private void afficherPopupCertifFinale(Certification certifOrigine, VBox card) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Certification Finale");
        popup.setResizable(false);

        VBox root = new VBox(12);
        root.setStyle("-fx-background-color: #f4f6f8; -fx-padding: 30;");
        root.setPrefWidth(500);

        Label titre = new Label("🏆 Ajouter Certification Finale");
        titre.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1a1a2e;");

        HBox barre = new HBox();
        barre.setPrefHeight(3);
        barre.setStyle("-fx-background-color: #f5a623; -fx-background-radius: 2;");

        // Info tentative
        VBox infoBox = new VBox(4);
        infoBox.setStyle("-fx-background-color: #f0f4ff; -fx-background-radius: 8; -fx-padding: 10;");
        Label infoTentative = new Label(
                "Tentative #" + certifOrigine.getId() +
                        "  |  User " + certifOrigine.getUserId() +
                        "  |  Score : " + String.format("%.1f%%", certifOrigine.getScoreObtenu()));
        infoTentative.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #1a1a2e;");
        infoBox.getChildren().add(infoTentative);

        // Info email
        VBox emailInfoBox = new VBox(4);
        emailInfoBox.setStyle("-fx-background-color: #e8f5e9; -fx-background-radius: 8; -fx-padding: 10;");
        Label emailIcon = new Label("📧 Un email sera envoyé automatiquement à l'étudiant");
        emailIcon.setStyle("-fx-font-size: 11px; -fx-text-fill: #2ecc71; -fx-font-weight: bold;");
        emailInfoBox.getChildren().add(emailIcon);

        // Badge
        Label lblBadge = new Label("Badge :");
        lblBadge.setStyle("-fx-font-weight: bold; -fx-text-fill: #555;");
        ComboBox<String> badgeBox = new ComboBox<>();
        badgeBox.getItems().addAll("Or", "Argent", "Bronze");
        badgeBox.setValue(certifOrigine.getBadge() != null && !certifOrigine.getBadge().isEmpty()
                ? certifOrigine.getBadge() : "Or");
        badgeBox.setPrefWidth(440);

        // Signature canvas
        Label lblSig = new Label("✍️ Signature de l'admin :");
        lblSig.setStyle("-fx-font-weight: bold; -fx-text-fill: #555;");

        Canvas canvas = new Canvas(440, 160);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, 440, 160);
        gc.setStroke(Color.web("#dde1e7"));
        gc.setLineWidth(1);
        gc.strokeLine(20, 130, 420, 130);
        gc.setFill(Color.web("#aaa"));
        gc.setFont(Font.font(10));
        gc.fillText("Signez ici", 190, 148);

        gc.setStroke(Color.web("#1a1a2e"));
        gc.setLineWidth(2.5);
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineJoin(StrokeLineJoin.ROUND);

        final boolean[] enTrain = {false};
        canvas.setOnMousePressed(e -> { enTrain[0] = true; gc.beginPath(); gc.moveTo(e.getX(), e.getY()); });
        canvas.setOnMouseDragged(e -> {
            if (enTrain[0]) {
                gc.setStroke(Color.web("#1a1a2e"));
                gc.setLineWidth(2.5);
                gc.lineTo(e.getX(), e.getY());
                gc.stroke();
                gc.beginPath();
                gc.moveTo(e.getX(), e.getY());
            }
        });
        canvas.setOnMouseReleased(e -> enTrain[0] = false);

        StackPane canvasWrapper = new StackPane(canvas);
        canvasWrapper.setStyle(
                "-fx-border-color: #1a1a2e; -fx-border-width: 2;" +
                        "-fx-border-radius: 8; -fx-background-color: white;" +
                        "-fx-background-radius: 8;");

        Button btnEffacer = new Button("🗑️ Effacer signature");
        btnEffacer.setStyle(
                "-fx-background-color: #e9ecef; -fx-text-fill: #555;" +
                        "-fx-font-size: 11px; -fx-background-radius: 6;" +
                        "-fx-padding: 6 12; -fx-cursor: hand;");
        btnEffacer.setOnAction(e -> {
            gc.setFill(Color.WHITE);
            gc.fillRect(0, 0, 440, 160);
            gc.setStroke(Color.web("#dde1e7"));
            gc.setLineWidth(1);
            gc.strokeLine(20, 130, 420, 130);
            gc.setFill(Color.web("#aaa"));
            gc.setFont(Font.font(10));
            gc.fillText("Signez ici", 190, 148);
        });

        Label msgLabel = new Label("");
        msgLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        msgLabel.setWrapText(true);

        // Boutons confirmer / annuler
        HBox btnBox = new HBox(10);
        btnBox.setAlignment(Pos.CENTER);

        Button btnConfirmer = new Button("✅  Confirmer et signer");
        btnConfirmer.setPrefWidth(200);
        btnConfirmer.setStyle(
                "-fx-background-color: #f5a623; -fx-text-fill: white;" +
                        "-fx-font-weight: bold; -fx-background-radius: 8;" +
                        "-fx-padding: 11; -fx-cursor: hand;");

        Button btnAnnuler = new Button("❌  Annuler");
        btnAnnuler.setPrefWidth(160);
        btnAnnuler.setStyle(
                "-fx-background-color: #e74c3c; -fx-text-fill: white;" +
                        "-fx-font-weight: bold; -fx-background-radius: 8;" +
                        "-fx-padding: 11; -fx-cursor: hand;");
        btnAnnuler.setOnAction(e -> popup.close());

        btnConfirmer.setOnAction(e -> {
            try {
                String badge = badgeBox.getValue();
                if (badge == null) { msgLabel.setText("⚠ Choisissez un badge !"); return; }

                CertificationFinale certifFinale = new CertificationFinale(
                        Timestamp.valueOf(LocalDateTime.now()), badge,
                        certifOrigine.getUserId(), certifOrigine.getQuizId(), certifOrigine.getId());
                certifFinaleService.ajouter(certifFinale);

                CertificationFinale inserted = trouverDerniereCertifFinale(certifOrigine.getId());
                if (inserted == null) { msgLabel.setText("❌ Impossible de récupérer la certification."); return; }

                // Sauvegarder signature
                SnapshotParameters params = new SnapshotParameters();
                params.setFill(Color.WHITE);
                WritableImage snapshot = canvas.snapshot(params, null);
                sauvegarderSignature(snapshot, inserted.getId());

                // Ajouter carte dans la vue
                VBox carteFinale = creerCarteFinaleExistante(inserted);
                int index = certifContainer.getChildren().indexOf(card);
                if (index >= 0) certifContainer.getChildren().add(index + 1, carteFinale);
                else            certifContainer.getChildren().add(carteFinale);

                btnConfirmer.setDisable(true);
                msgLabel.setText("📧 Certif créée. Envoi email en cours...");
                msgLabel.setStyle("-fx-text-fill: #f5a623; -fx-font-weight: bold;");

                final CertificationFinale insertedFinal = inserted;
                final String badgeFinal = badge;

                new Thread(() -> {
                    try {
                        String email = userService.getEmailParId(certifOrigine.getUserId());
                        String nom   = userService.getNomParId(certifOrigine.getUserId());
                        if (email != null && !email.isEmpty()) {
                            String dateStr = insertedFinal.getDateEmission() != null
                                    ? insertedFinal.getDateEmission().toString().substring(0, 16)
                                    : LocalDateTime.now().toString().substring(0, 16);
                            EmailService.envoyerEmailCertification(
                                    email, nom, badgeFinal,
                                    insertedFinal.getId(), dateStr,
                                    certifOrigine.getQuizId());
                            javafx.application.Platform.runLater(() -> {
                                msgLabel.setText("✅ Certif signée ! Email envoyé à " + email);
                                msgLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
                            });
                        } else {
                            javafx.application.Platform.runLater(() -> {
                                msgLabel.setText("✅ Certif signée ! Email introuvable.");
                                msgLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                            });
                        }
                    } catch (Exception ex) {
                        javafx.application.Platform.runLater(() -> {
                            msgLabel.setText("✅ Certif signée ! Erreur email.");
                            msgLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                        });
                        ex.printStackTrace();
                    }
                    javafx.application.Platform.runLater(() -> {
                        javafx.animation.PauseTransition pause =
                                new javafx.animation.PauseTransition(javafx.util.Duration.seconds(3));
                        pause.setOnFinished(ev -> popup.close());
                        pause.play();
                    });
                }).start();

            } catch (Exception ex) {
                msgLabel.setText("❌ Erreur : " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        btnBox.getChildren().addAll(btnConfirmer, btnAnnuler);
        root.getChildren().addAll(
                titre, barre, infoBox, emailInfoBox,
                lblBadge, badgeBox,
                lblSig, canvasWrapper, btnEffacer,
                msgLabel, btnBox);

        popup.setScene(new Scene(root));
        popup.showAndWait();
    }

    // ─────────── UTILITAIRES ───────────
    private CertificationFinale trouverDerniereCertifFinale(int tentativeId) {
        try {
            CertificationFinale derniere = null;
            for (CertificationFinale cf : certifFinaleService.afficher())
                if (cf.getTentativeId() == tentativeId) derniere = cf;
            return derniere;
        } catch (Exception e) { e.printStackTrace(); return null; }
    }

    private void sauvegarderSignature(WritableImage image, int certifId) {
        try {
            File dossier = new File("signatures");
            if (!dossier.exists()) dossier.mkdirs();
            File file = new File(dossier, "certif_" + certifId + ".png");
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
            System.out.println("✅ Signature sauvegardée : " + file.getAbsolutePath());
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ─────────── NAVIGATION SIDEBAR ───────────
    @FXML
    public void ouvrirQuiz() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/org/example/AdminQuiz.fxml"));
            certifContainer.getScene().setRoot(root);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    public void ouvrirCours() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/admin.fxml"));
            certifContainer.getScene().setRoot(root);
        } catch (Exception e) { e.printStackTrace(); }
    }
    @FXML
    public void ouvrirUser() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AdminView.fxml"));
            certifContainer.getScene().setRoot(root);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    void logout(ActionEvent event) {
        try {
            Session.logout();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainView.fxml"));
            Parent root = loader.load();
            certifContainer.getScene().setRoot(root);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}