package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.entities.CertificationFinale;
import org.example.entities.Session;
import org.example.services.CertificationFinaleService;

import java.io.File;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

public class CertificationsEtudiantController {

    // ══════════════════════════════════════════════
    //  INJECTION FXML
    // ══════════════════════════════════════════════
    @FXML private VBox      certifContainer;
    @FXML private Label     messageLabel;
    @FXML private Label     moisAnneeLabel;
    @FXML private GridPane  calendrierGrid;
    @FXML private GridPane  joursSemaine;
    @FXML private VBox      detailJourBox;
    @FXML private Label     detailJourTitre;
    @FXML private VBox      detailJourContenu;

    // ══════════════════════════════════════════════
    //  SERVICE & ÉTAT
    // ══════════════════════════════════════════════
    private final CertificationFinaleService service = new CertificationFinaleService();


    private YearMonth moisCourant = YearMonth.now();
    private List<CertificationFinale> mesCertifs = new ArrayList<>();
    private Map<LocalDate, List<CertificationFinale>> certifParDate = new HashMap<>();

    // ══════════════════════════════════════════════
    //  INITIALISATION
    // ══════════════════════════════════════════════
    @FXML
    public void initialize() {
        chargerCertifications();
        construireEnTeteSemaine();
        construireCalendrier();
    }

    // ══════════════════════════════════════════════
    //  CHARGER LES CERTIFICATIONS
    // ══════════════════════════════════════════════
    @FXML
    public void chargerCertifications() {
        certifContainer.getChildren().clear();
        certifParDate.clear();

        try {
            mesCertifs = service.afficher().stream()
                    .filter(c -> c.getUserId() == Session.getCurrentUser().getId())
                    .collect(Collectors.toList());

            if (mesCertifs.isEmpty()) {
                messageLabel.setText("Aucune certification finale disponible pour le moment.");
                messageLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 13;");
            } else {
                messageLabel.setText(mesCertifs.size() + " certification(s) trouvée(s)");
                messageLabel.setStyle(
                        "-fx-text-fill: #2ecc71; -fx-font-weight: bold; -fx-font-size: 13;");
            }

            for (CertificationFinale cf : mesCertifs) {
                if (cf.getDateEmission() != null) {
                    LocalDate date = cf.getDateEmission().toLocalDateTime().toLocalDate();
                    certifParDate.computeIfAbsent(date, k -> new ArrayList<>()).add(cf);
                }
                certifContainer.getChildren().add(creerCarteCertifFinale(cf));
            }

            construireCalendrier();

        } catch (Exception e) {
            messageLabel.setText("❌ Erreur : " + e.getMessage());
            messageLabel.setStyle("-fx-text-fill: #e74c3c;");
        }
    }

    // ══════════════════════════════════════════════
    //  EN-TÊTE JOURS DE LA SEMAINE
    // ══════════════════════════════════════════════
    private void construireEnTeteSemaine() {
        joursSemaine.getChildren().clear();
        String[] jours = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"};
        for (int i = 0; i < 7; i++) {
            Label lbl = new Label(jours[i]);
            lbl.setStyle(
                    "-fx-text-fill: #a0a8c0; -fx-font-size: 11;" +
                            "-fx-font-weight: bold; -fx-alignment: center;");
            lbl.setMaxWidth(Double.MAX_VALUE);
            lbl.setAlignment(Pos.CENTER);
            GridPane.setColumnIndex(lbl, i);
            joursSemaine.getChildren().add(lbl);
        }
    }

    // ══════════════════════════════════════════════
    //  CONSTRUIRE LE CALENDRIER
    // ══════════════════════════════════════════════
    private void construireCalendrier() {
        calendrierGrid.getChildren().clear();
        calendrierGrid.getRowConstraints().clear();

        // Nom du mois en français, capitalisé
        String nomMois = moisCourant.getMonth()
                .getDisplayName(TextStyle.FULL, Locale.FRENCH);
        String nomMoisCap = nomMois.substring(0, 1).toUpperCase() + nomMois.substring(1);
        moisAnneeLabel.setText(nomMoisCap + " " + moisCourant.getYear());

        LocalDate premierJour   = moisCourant.atDay(1);
        int       debutSemaine  = premierJour.getDayOfWeek().getValue() - 1;
        int       nbJours       = moisCourant.lengthOfMonth();
        LocalDate aujourdhui    = LocalDate.now();

        int totalCases = debutSemaine + nbJours;
        int nbLignes   = (int) Math.ceil(totalCases / 7.0);

        for (int i = 0; i < nbLignes; i++) {
            RowConstraints rc = new RowConstraints();
            rc.setPrefHeight(65);
            rc.setMinHeight(55);
            calendrierGrid.getRowConstraints().add(rc);
        }

        int colonne = debutSemaine;
        int ligne   = 0;

        for (int jour = 1; jour <= nbJours; jour++) {
            LocalDate date = moisCourant.atDay(jour);
            List<CertificationFinale> certifsDuJour =
                    certifParDate.getOrDefault(date, new ArrayList<>());

            VBox cellule = creerCellule(jour, date, certifsDuJour, aujourdhui);
            GridPane.setColumnIndex(cellule, colonne);
            GridPane.setRowIndex(cellule, ligne);
            calendrierGrid.getChildren().add(cellule);

            colonne++;
            if (colonne == 7) { colonne = 0; ligne++; }
        }
    }

    // ══════════════════════════════════════════════
    //  CELLULE JOUR DU CALENDRIER
    // ══════════════════════════════════════════════
    private VBox creerCellule(int jour, LocalDate date,
                              List<CertificationFinale> certifs,
                              LocalDate aujourdhui) {
        VBox cell = new VBox(2);
        cell.setAlignment(Pos.TOP_CENTER);
        cell.setPadding(new Insets(4));
        cell.setMaxWidth(Double.MAX_VALUE);
        cell.setMaxHeight(Double.MAX_VALUE);

        boolean estAujourdhui = date.equals(aujourdhui);
        boolean aCertif       = !certifs.isEmpty();

        String bgColor = estAujourdhui ? "#fff3e0" : aCertif ? "#f0fff4" : "transparent";

        cell.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-color: " + (estAujourdhui ? "#f5a623" : "#f0f0f0") + ";" +
                        "-fx-border-radius: 8;" +
                        "-fx-border-width: " + (estAujourdhui ? "2" : "0.5") + ";" +
                        "-fx-cursor: " + (aCertif ? "hand" : "default") + ";");

        Label numLabel = new Label(String.valueOf(jour));
        numLabel.setStyle(
                "-fx-font-size: 12; -fx-font-weight: " + (estAujourdhui ? "bold" : "normal") + ";" +
                        "-fx-text-fill: " + (estAujourdhui ? "#f5a623"
                        : date.getDayOfWeek().getValue() >= 6 ? "#e74c3c" : "#333") + ";");
        cell.getChildren().add(numLabel);

        if (aCertif) {
            // Points verts
            HBox points = new HBox(3);
            points.setAlignment(Pos.CENTER);
            int max = Math.min(certifs.size(), 3);
            for (int i = 0; i < max; i++) {
                Label pt = new Label("●");
                pt.setStyle("-fx-text-fill: #2ecc71; -fx-font-size: 8;");
                points.getChildren().add(pt);
            }
            if (certifs.size() > 3) {
                Label plus = new Label("+" + (certifs.size() - 3));
                plus.setStyle("-fx-text-fill: #f5a623; -fx-font-size: 7; -fx-font-weight: bold;");
                points.getChildren().add(plus);
            }
            cell.getChildren().add(points);

            // Badge compteur
            Label countLabel = new Label(certifs.size() + " 🏆");
            countLabel.setStyle(
                    "-fx-background-color: #2ecc71; -fx-text-fill: white;" +
                            "-fx-font-size: 8; -fx-font-weight: bold;" +
                            "-fx-background-radius: 10; -fx-padding: 1 5;");
            cell.getChildren().add(countLabel);

            // Hover et clic
            cell.setOnMouseClicked(e -> afficherDetailJour(date, certifs));
            cell.setOnMouseEntered(ev -> cell.setStyle(
                    "-fx-background-color: #e8f5e9; -fx-background-radius: 8;" +
                            "-fx-border-color: #2ecc71; -fx-border-radius: 8;" +
                            "-fx-border-width: 1.5; -fx-cursor: hand;"));
            cell.setOnMouseExited(ev -> cell.setStyle(
                    "-fx-background-color: #f0fff4; -fx-background-radius: 8;" +
                            "-fx-border-color: #f0f0f0; -fx-border-radius: 8;" +
                            "-fx-border-width: 0.5; -fx-cursor: hand;"));
        }

        return cell;
    }

    // ══════════════════════════════════════════════
    //  AFFICHER DÉTAIL D'UN JOUR
    // ══════════════════════════════════════════════
    private void afficherDetailJour(LocalDate date, List<CertificationFinale> certifs) {
        detailJourBox.setVisible(true);
        detailJourBox.setManaged(true);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRENCH);
        detailJourTitre.setText("📅 Certifications du " + date.format(fmt));
        detailJourContenu.getChildren().clear();

        for (CertificationFinale cf : certifs) {
            HBox ligne = new HBox(10);
            ligne.setAlignment(Pos.CENTER_LEFT);
            ligne.setStyle(
                    "-fx-background-color: #f0fff4; -fx-background-radius: 6;" +
                            "-fx-padding: 8 12; -fx-border-color: #2ecc71;" +
                            "-fx-border-radius: 6; -fx-border-width: 1;");

            Label trophee = new Label("🏆");
            trophee.setStyle("-fx-font-size: 16;");

            VBox info = new VBox(2);
            Label badgeL = new Label("Badge : " + cf.getBadge());
            badgeL.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #f5a623;");
            Label idL = new Label("Certif #" + cf.getId() + " — Quiz ID : " + cf.getQuizId());
            idL.setStyle("-fx-font-size: 11; -fx-text-fill: #555;");
            String heure = cf.getDateEmission() != null
                    ? cf.getDateEmission().toString().substring(11, 16) : "—";
            Label heureL = new Label("⏰ " + heure);
            heureL.setStyle("-fx-font-size: 10; -fx-text-fill: #888;");
            info.getChildren().addAll(badgeL, idL, heureL);

            ligne.getChildren().addAll(trophee, info);
            detailJourContenu.getChildren().add(ligne);
        }
    }

    // ══════════════════════════════════════════════
    //  NAVIGATION CALENDRIER
    // ══════════════════════════════════════════════
    @FXML
    private void moisPrecedent() {
        moisCourant = moisCourant.minusMonths(1);
        masquerDetail();
        construireCalendrier();
    }

    @FXML
    private void moisSuivant() {
        moisCourant = moisCourant.plusMonths(1);
        masquerDetail();
        construireCalendrier();
    }

    @FXML
    private void allerAujourdhui() {
        moisCourant = YearMonth.now();
        masquerDetail();
        construireCalendrier();
    }

    private void masquerDetail() {
        detailJourBox.setVisible(false);
        detailJourBox.setManaged(false);
    }

    // ══════════════════════════════════════════════
    //  CARTE CERTIFICATION FINALE
    // ══════════════════════════════════════════════
    private VBox creerCarteCertifFinale(CertificationFinale cf) {
        VBox card = new VBox(12);
        card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 14;" +
                        "-fx-padding: 20;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 12, 0, 0, 2);");

        // Bandeau orange en haut
        HBox bandeau = new HBox();
        bandeau.setPrefHeight(5);
        bandeau.setStyle("-fx-background-color: #f5a623; -fx-background-radius: 4;");

        // Header : trophée + titre
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        Label trophee = new Label("🏆");
        trophee.setStyle("-fx-font-size: 28;");
        VBox titreBox = new VBox(3);
        Label titreCertif = new Label("CERTIFICATION FINALE");
        titreCertif.setStyle(
                "-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: #1a1f3c;");
        Label subtitre = new Label("educenter — Plateforme d'apprentissage");
        subtitre.setStyle("-fx-font-size: 10; -fx-text-fill: #888;");
        titreBox.getChildren().addAll(titreCertif, subtitre);
        header.getChildren().addAll(trophee, titreBox);

        // Séparateur
        HBox sep = new HBox();
        sep.setPrefHeight(1);
        sep.setStyle("-fx-background-color: #f0f0f0;");

        // Infos
        VBox infosBox = new VBox(8);

        HBox row1 = new HBox(20);
        row1.setAlignment(Pos.CENTER_LEFT);
        Label badgeLbl = new Label("🏅 Badge : ");
        badgeLbl.setStyle("-fx-font-size: 12; -fx-text-fill: #555; -fx-font-weight: bold;");
        Label badgeVal = new Label(cf.getBadge() != null ? cf.getBadge() : "—");
        badgeVal.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #f5a623;");
        Label idLbl = new Label("🆔 ID : ");
        idLbl.setStyle("-fx-font-size: 12; -fx-text-fill: #555; -fx-font-weight: bold;");
        Label idVal = new Label(String.valueOf(cf.getId()));
        idVal.setStyle("-fx-font-size: 12; -fx-text-fill: #333;");
        row1.getChildren().addAll(badgeLbl, badgeVal, idLbl, idVal);

        HBox row2 = new HBox(20);
        row2.setAlignment(Pos.CENTER_LEFT);
        Label dateLbl = new Label("📅 Date : ");
        dateLbl.setStyle("-fx-font-size: 12; -fx-text-fill: #555; -fx-font-weight: bold;");
        Label dateVal = new Label(cf.getDateEmission() != null
                ? cf.getDateEmission().toString().substring(0, 16) : "—");
        dateVal.setStyle("-fx-font-size: 12; -fx-text-fill: #333;");
        row2.getChildren().addAll(dateLbl, dateVal);

        HBox row3 = new HBox(20);
        row3.setAlignment(Pos.CENTER_LEFT);
        Label quizLbl = new Label("📝 Quiz ID : ");
        quizLbl.setStyle("-fx-font-size: 12; -fx-text-fill: #555; -fx-font-weight: bold;");
        Label quizVal = new Label(String.valueOf(cf.getQuizId()));
        quizVal.setStyle("-fx-font-size: 12; -fx-text-fill: #333;");
        Label userLbl = new Label("👤 Étudiant ID : ");
        userLbl.setStyle("-fx-font-size: 12; -fx-text-fill: #555; -fx-font-weight: bold;");
        Label userVal = new Label(String.valueOf(cf.getUserId()));
        userVal.setStyle("-fx-font-size: 12; -fx-text-fill: #333;");
        row3.getChildren().addAll(quizLbl, quizVal, userLbl, userVal);

        infosBox.getChildren().addAll(row1, row2, row3);

        Label felicit = new Label("🎓 Cette certification atteste de la réussite de l'étudiant.");
        felicit.setStyle("-fx-font-size: 11; -fx-text-fill: #666; -fx-font-style: italic;");

        // Bouton imprimer
        Button btnImprimer = new Button("🖨️  Imprimer / Exporter en PDF");
        btnImprimer.setPrefWidth(260);
        btnImprimer.setStyle(
                "-fx-background-color: #1a1f3c; -fx-text-fill: white;" +
                        "-fx-font-weight: bold; -fx-font-size: 12;" +
                        "-fx-background-radius: 8; -fx-padding: 10 18; -fx-cursor: hand;");
        btnImprimer.setOnMouseEntered(e -> btnImprimer.setStyle(
                "-fx-background-color: #f5a623; -fx-text-fill: white;" +
                        "-fx-font-weight: bold; -fx-font-size: 12;" +
                        "-fx-background-radius: 8; -fx-padding: 10 18; -fx-cursor: hand;"));
        btnImprimer.setOnMouseExited(e -> btnImprimer.setStyle(
                "-fx-background-color: #1a1f3c; -fx-text-fill: white;" +
                        "-fx-font-weight: bold; -fx-font-size: 12;" +
                        "-fx-background-radius: 8; -fx-padding: 10 18; -fx-cursor: hand;"));
        btnImprimer.setOnAction(e -> imprimerCertification(cf));

        card.getChildren().addAll(bandeau, header, sep, infosBox, felicit, btnImprimer);
        return card;
    }

    // ══════════════════════════════════════════════
    //  IMPRESSION / EXPORT PDF
    // ══════════════════════════════════════════════
    private void imprimerCertification(CertificationFinale cf) {
        VBox contenu = creerContenuImpression(cf);
        contenu.setPrefWidth(750);
        contenu.setPrefHeight(550);

        new Scene(contenu, 750, 550);
        contenu.applyCss();
        contenu.layout();

        javafx.print.PrinterJob job = javafx.print.PrinterJob.createPrinterJob();
        if (job == null) {
            messageLabel.setText("❌ Aucune imprimante disponible.");
            messageLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            return;
        }

        javafx.print.Printer printer = job.getPrinter();
        javafx.print.PageLayout pageLayout = printer.createPageLayout(
                javafx.print.Paper.A4,
                javafx.print.PageOrientation.LANDSCAPE,
                javafx.print.Printer.MarginType.DEFAULT);

        boolean proceed = job.showPrintDialog(certifContainer.getScene().getWindow());

        if (proceed) {
            double printableW = pageLayout.getPrintableWidth();
            double printableH = pageLayout.getPrintableHeight();
            double contentW   = contenu.prefWidth(-1);
            double contentH   = contenu.prefHeight(-1);
            double scale      = Math.min(printableW / contentW, printableH / contentH);

            contenu.setScaleX(scale);
            contenu.setScaleY(scale);
            contenu.setTranslateX((printableW - contentW * scale) / 2);
            contenu.setTranslateY((printableH - contentH * scale) / 2);

            boolean printed = job.printPage(pageLayout, contenu);
            if (printed) {
                job.endJob();
                messageLabel.setText("✅ PDF généré avec succès !");
                messageLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
            } else {
                messageLabel.setText("❌ Erreur lors de l'impression.");
                messageLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            }
        }
    }

    // ══════════════════════════════════════════════
    //  CONTENU IMPRESSION (mise en page certificat)
    // ══════════════════════════════════════════════
    private VBox creerContenuImpression(CertificationFinale cf) {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPrefWidth(750);
        root.setPrefHeight(530);
        root.setStyle("-fx-background-color: white; -fx-padding: 40;");

        VBox wrapper = new VBox();
        wrapper.setStyle("-fx-border-color: #f5a623; -fx-border-width: 4; -fx-padding: 0;");

        VBox inner = new VBox(18);
        inner.setAlignment(Pos.CENTER);
        inner.setStyle("-fx-background-color: white; -fx-padding: 30;");

        // Logo
        HBox logoBox = new HBox(8);
        logoBox.setAlignment(Pos.CENTER);
        Label logoIcon  = new Label("📚");
        logoIcon.setStyle("-fx-font-size: 28;");
        Label logoEdu   = new Label("edu");
        logoEdu.setStyle("-fx-font-size: 28; -fx-font-weight: bold; -fx-text-fill: #1a1f3c;");
        Label logoCenter = new Label("center");
        logoCenter.setStyle("-fx-font-size: 28; -fx-font-weight: bold; -fx-text-fill: #f5a623;");
        logoBox.getChildren().addAll(logoIcon, logoEdu, logoCenter);

        // Titre
        Label titre = new Label("CERTIFICAT DE RÉUSSITE");
        titre.setStyle("-fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: #1a1f3c;");

        Label sousTitre = new Label("Certification Finale — educenter");
        sousTitre.setStyle("-fx-font-size: 13; -fx-text-fill: #888;");

        // Séparateur orange
        HBox ligneSep = new HBox();
        ligneSep.setPrefHeight(3);
        ligneSep.setPrefWidth(350);
        ligneSep.setStyle("-fx-background-color: #f5a623;");
        HBox ligneSepW = new HBox(ligneSep);
        ligneSepW.setAlignment(Pos.CENTER);

        // Trophée + badge
        Label trophee = new Label("🏆");
        trophee.setStyle("-fx-font-size: 40;");

        Label badgeLabel = new Label(cf.getBadge() != null ? cf.getBadge() : "—");
        badgeLabel.setStyle("-fx-font-size: 22; -fx-font-weight: bold; -fx-text-fill: #f5a623;");

        // Infos certif
        String dateStr = cf.getDateEmission() != null
                ? cf.getDateEmission().toString().substring(0, 16) : "—";
        VBox infos = new VBox(8);
        infos.setAlignment(Pos.CENTER);
        Label idL   = new Label("N° Certification : " + cf.getId());
        idL.setStyle("-fx-font-size: 13; -fx-text-fill: #333;");
        Label quizL = new Label("Quiz ID : " + cf.getQuizId()
                + "     |     Étudiant ID : " + cf.getUserId());
        quizL.setStyle("-fx-font-size: 13; -fx-text-fill: #333;");
        Label dateL = new Label("Date d'émission : " + dateStr);
        dateL.setStyle("-fx-font-size: 13; -fx-text-fill: #333;");
        infos.getChildren().addAll(idL, quizL, dateL);

        // Message
        Label msg = new Label(
                "Ce certificat atteste que l'étudiant a réussi avec succès\n" +
                        "l'évaluation finale sur la plateforme educenter.");
        msg.setStyle("-fx-font-size: 12; -fx-text-fill: #555; -fx-font-style: italic;");
        msg.setAlignment(Pos.CENTER);
        msg.setWrapText(true);

        // Zone signature
        HBox signatureZone = new HBox(60);
        signatureZone.setAlignment(Pos.CENTER);
        signatureZone.setStyle("-fx-padding: 10 0 0 0;");

        // Tampon VALIDÉ
        VBox tamponBox = new VBox(5);
        tamponBox.setAlignment(Pos.CENTER);
        Label tamponLabel = new Label("VALIDÉ");
        tamponLabel.setStyle(
                "-fx-font-size: 18; -fx-font-weight: bold;" +
                        "-fx-text-fill: #2ecc71; -fx-border-color: #2ecc71;" +
                        "-fx-border-width: 3; -fx-padding: 8 15;" +
                        "-fx-border-radius: 4; -fx-rotate: -15;");
        Label tamponDate = new Label(dateStr);
        tamponDate.setStyle("-fx-font-size: 9; -fx-text-fill: #888;");
        tamponBox.getChildren().addAll(tamponLabel, tamponDate);

        // Signature
        VBox sigBox = new VBox(5);
        sigBox.setAlignment(Pos.CENTER);
        Image signatureImage = chargerSignature(cf.getId());
        if (signatureImage != null) {
            ImageView sigView = new ImageView(signatureImage);
            sigView.setFitWidth(180);
            sigView.setFitHeight(70);
            sigView.setPreserveRatio(true);
            sigBox.getChildren().add(sigView);
        } else {
            Label noSig = new Label("(Signature non disponible)");
            noSig.setStyle("-fx-font-size: 10; -fx-text-fill: #999;");
            sigBox.getChildren().add(noSig);
        }
        HBox ligneSig = new HBox();
        ligneSig.setPrefWidth(180);
        ligneSig.setPrefHeight(1);
        ligneSig.setStyle("-fx-background-color: #1a1f3c;");
        Label sigLabel = new Label("Signature de l'administrateur");
        sigLabel.setStyle("-fx-font-size: 9; -fx-text-fill: #888;");
        sigBox.getChildren().addAll(ligneSig, sigLabel);

        signatureZone.getChildren().addAll(tamponBox, sigBox);

        // Ligne bas
        HBox ligneBas = new HBox();
        ligneBas.setPrefHeight(1);
        ligneBas.setPrefWidth(500);
        ligneBas.setStyle("-fx-background-color: #dde1e7;");
        HBox ligneBasW = new HBox(ligneBas);
        ligneBasW.setAlignment(Pos.CENTER);

        Label footer = new Label("educenter © 2024 — Tous droits réservés");
        footer.setStyle("-fx-font-size: 10; -fx-text-fill: #aaa;");

        inner.getChildren().addAll(
                logoBox, titre, sousTitre,
                ligneSepW,
                trophee, badgeLabel, infos, msg,
                signatureZone,
                ligneBasW, footer);

        wrapper.getChildren().add(inner);
        root.getChildren().add(wrapper);
        return root;
    }

    // ══════════════════════════════════════════════
    //  UTILITAIRE : CHARGEMENT SIGNATURE
    // ══════════════════════════════════════════════
    private Image chargerSignature(int certifId) {
        try {
            File file = new File("signatures/certif_" + certifId + ".png");
            if (file.exists()) return new Image(file.toURI().toString());
        } catch (Exception e) {
            System.out.println("Erreur chargement signature : " + e.getMessage());
        }
        return null;
    }

    // ══════════════════════════════════════════════
    //  NAVIGATION
    // ══════════════════════════════════════════════
    @FXML
    private void retourAccueil() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/AccueilEtudiant.fxml"));
            Stage stage = (Stage) certifContainer.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Espace Étudiant");
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void retourChoixEspace() {
        try {
            Stage stage = (Stage) certifContainer.getScene().getWindow();
            new org.example.MainFX().start(stage);
        } catch (Exception e) { e.printStackTrace(); }
    }
}