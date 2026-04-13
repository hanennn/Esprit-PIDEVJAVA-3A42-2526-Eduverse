package org.example.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.entities.Certification;
import org.example.entities.CertificationFinale;
import org.example.entities.Quiz;
import org.example.services.CertificationFinaleService;
import org.example.services.CertificationService;
import org.example.services.QuizService;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

public class AdminQuizController {

    @FXML private Label  breadcrumb;
    @FXML private Label  titreSection;
    @FXML private Label  quizCountLabel;
    @FXML private HBox   menuQuiz;
    @FXML private HBox   menuCertif;
    @FXML private VBox   sectionQuiz;
    @FXML private VBox   sectionCertif;
    @FXML private VBox   certifContainer;
    @FXML private Label  messageQuiz;

    @FXML private TableView<Quiz>            quizTable;
    @FXML private TableColumn<Quiz, Integer> idCol;
    @FXML private TableColumn<Quiz, String>  titreCol;
    @FXML private TableColumn<Quiz, String>  typeCol;
    @FXML private TableColumn<Quiz, Integer> dureeCol;
    @FXML private TableColumn<Quiz, Float>   scoreCol;
    @FXML private TableColumn<Quiz, String>  coursCol;

    private final QuizService                quizService         = new QuizService();
    private final CertificationService       certifService       = new CertificationService();
    private final CertificationFinaleService certifFinaleService =
            new CertificationFinaleService();

    // ─────────── INIT ───────────
    @FXML
    public void initialize() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        titreCol.setCellValueFactory(new PropertyValueFactory<>("titre"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("typeQuiz"));
        dureeCol.setCellValueFactory(new PropertyValueFactory<>("duree"));
        scoreCol.setCellValueFactory(new PropertyValueFactory<>("scoreMinimum"));
        coursCol.setCellValueFactory(cell ->
                new SimpleStringProperty(
                        quizService.getNomCours(cell.getValue().getCoursAssocieId())
                )
        );

        idCol.setStyle("-fx-alignment: CENTER;");
        dureeCol.setStyle("-fx-alignment: CENTER;");

        // ── Badge coloré Type ──
        typeCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("");
                } else {
                    Label badge = new Label(item);
                    badge.setStyle(item.equals("Final")
                            ? "-fx-background-color: #fff3e0; -fx-text-fill: #f5a623;" +
                            "-fx-font-weight: bold; -fx-font-size: 11;" +
                            "-fx-background-radius: 4; -fx-padding: 3 10;"
                            : "-fx-background-color: #e3f2fd; -fx-text-fill: #1976d2;" +
                            "-fx-font-weight: bold; -fx-font-size: 11;" +
                            "-fx-background-radius: 4; -fx-padding: 3 10;"
                    );
                    setGraphic(badge);
                    setText(null);
                    setAlignment(Pos.CENTER);
                    // ← FIX: pas de background sur la cellule
                    setStyle("-fx-background-color: transparent;");
                }
            }
        });

        // ── Score coloré ──
        scoreCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Float item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%.1f%%", item));
                    setStyle(
                            "-fx-alignment: CENTER; -fx-font-weight: bold;" +
                                    (item >= 70
                                            ? "-fx-text-fill: #2ecc71;"
                                            : "-fx-text-fill: #f5a623;")
                    );
                }
            }
        });

        // ── Durée ──
        dureeCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item + " min");
                    setStyle("-fx-alignment: CENTER; -fx-text-fill: #555;");
                }
            }
        });

        // ── ID ──
        idCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.valueOf(item));
                    setStyle(
                            "-fx-alignment: CENTER; -fx-text-fill: #888;" +
                                    "-fx-font-size: 11;");
                }
            }
        });

        // ── Titre ──
        titreCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle(
                            "-fx-font-weight: bold; -fx-text-fill: #1a1f3c;" +
                                    "-fx-font-size: 13;");
                }
            }
        });

        // ── Cours ──
        coursCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    Label badge = new Label(item);
                    badge.setStyle(
                            "-fx-background-color: #f3e5f5;" +
                                    "-fx-text-fill: #7b1fa2;" +
                                    "-fx-font-size: 11; -fx-font-weight: bold;" +
                                    "-fx-background-radius: 4; -fx-padding: 3 10;"
                    );
                    setGraphic(badge);
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                }
            }
        });

        // ── Style lignes ──
        quizTable.setRowFactory(tv -> {
            TableRow<Quiz> row = new TableRow<>() {
                @Override
                protected void updateItem(Quiz item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setStyle("-fx-background-color: transparent;");
                    } else {
                        // Alterner couleurs
                        setStyle(getIndex() % 2 == 0
                                ? "-fx-background-color: white;"
                                : "-fx-background-color: #fafafa;");
                    }
                }
            };

            // Hover
            row.setOnMouseEntered(e -> {
                if (!row.isEmpty()) {
                    row.setStyle(
                            "-fx-background-color: #fff8ee; -fx-cursor: hand;");
                }
            });
            row.setOnMouseExited(e -> {
                if (!row.isEmpty()) {
                    row.setStyle(row.getIndex() % 2 == 0
                            ? "-fx-background-color: white;"
                            : "-fx-background-color: #fafafa;");
                }
            });

            // Sélection
            row.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                if (!row.isEmpty()) {
                    if (isSelected) {
                        row.setStyle(
                                "-fx-background-color: #fff3e0;");
                    } else {
                        row.setStyle(row.getIndex() % 2 == 0
                                ? "-fx-background-color: white;"
                                : "-fx-background-color: #fafafa;");
                    }
                }
            });

            return row;
        });

        // ── Style TableView ──
        quizTable.setStyle(
                "-fx-background-color: white;" +
                        "-fx-table-header-background: #f8f9fa;" +
                        "-fx-border-color: transparent;"
        );

        chargerQuiz();
    }

    // ─────────── SIDEBAR ───────────
    @FXML
    public void afficherQuiz() {
        titreSection.setText("📝 Gestion des Quiz");
        breadcrumb.setText("🏠 Admin  ›  Quiz");
        menuQuiz.setStyle(
                "-fx-padding: 12 20; -fx-background-color: #f5a623; -fx-cursor: hand;");
        menuCertif.setStyle("-fx-padding: 12 20; -fx-cursor: hand;");
        sectionQuiz.setVisible(true);
        sectionQuiz.setManaged(true);
        sectionCertif.setVisible(false);
        sectionCertif.setManaged(false);
        chargerQuiz();
    }

    @FXML
    public void afficherCertifications() {
        titreSection.setText("🏆 Gestion des Certifications");
        breadcrumb.setText("🏠 Admin  ›  Certifications");
        menuCertif.setStyle(
                "-fx-padding: 12 20; -fx-background-color: #f5a623; -fx-cursor: hand;");
        menuQuiz.setStyle("-fx-padding: 12 20; -fx-cursor: hand;");
        sectionQuiz.setVisible(false);
        sectionQuiz.setManaged(false);
        sectionCertif.setVisible(true);
        sectionCertif.setManaged(true);
        chargerCertifications();
    }

    // ─────────── QUIZ ───────────
    @FXML
    public void chargerQuiz() {
        try {
            ObservableList<Quiz> list =
                    FXCollections.observableArrayList(quizService.afficher());
            quizTable.setItems(list);
            if (quizCountLabel != null) {
                quizCountLabel.setText(list.size() + " quiz");
            }
            messageQuiz.setText("");
        } catch (Exception e) {
            showMsg(" Erreur : " + e.getMessage(), false);
        }
    }

    @FXML
    private void supprimerQuiz() {
        Quiz selected = quizTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMsg(" Sélectionnez un quiz à supprimer !", false);
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer le quiz ?");
        alert.setContentText(
                "Voulez-vous vraiment supprimer \""
                        + selected.getTitre() + "\" ?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    quizService.supprimer(selected.getId());
                    showMsg(" Quiz supprimé avec succès !", true);
                    chargerQuiz();
                } catch (Exception e) {
                    showMsg(" Erreur : " + e.getMessage(), false);
                }
            }
        });
    }

    // ─────────── CERTIFICATIONS ───────────
    @FXML
    public void chargerCertifications() {
        certifContainer.getChildren().clear();
        try {
            List<Certification> list = certifService.afficher();
            if (list.isEmpty()) {
                Label vide = new Label(
                        "Aucune certification / tentative trouvée.");
                vide.setStyle(
                        "-fx-text-fill: #888; -fx-font-size: 13; -fx-padding: 20;");
                certifContainer.getChildren().add(vide);
                return;
            }
            for (Certification c : list) {
                VBox carteTentative = creerCarteTentative(c);
                certifContainer.getChildren().add(carteTentative);
                chargerCertifFinalesLiees(c.getId(), carteTentative);
            }
        } catch (Exception e) {
            Label err = new Label(" Erreur : " + e.getMessage());
            err.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12;");
            certifContainer.getChildren().add(err);
        }
    }

    private void chargerCertifFinalesLiees(int tentativeId,
                                           VBox carteTentative) {
        try {
            for (CertificationFinale cf : certifFinaleService.afficher()) {
                if (cf.getTentativeId() == tentativeId) {
                    carteTentative.getChildren().add(
                            creerCarteFinaleExistante(cf));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ─────────── CARTE TENTATIVE ───────────
    private VBox creerCarteTentative(Certification c) {
        VBox card = new VBox(10);
        card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 12;" +
                        "-fx-padding: 20;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);"
        );

        boolean reussi = c.getStatut() != null &&
                c.getStatut().equalsIgnoreCase("Réussi");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label statutIcon = new Label(reussi ? "✅" : "❌");
        statutIcon.setStyle("-fx-font-size: 18;");
        Label idLabel = new Label("Tentative #" + c.getId());
        idLabel.setStyle(
                "-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #1a1f3c;");
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label statutBadge = new Label(
                c.getStatut() != null ? c.getStatut() : "—");
        statutBadge.setStyle(reussi
                ? "-fx-background-color: #e8f5e9; -fx-text-fill: #2ecc71;" +
                "-fx-font-weight: bold; -fx-font-size: 11;" +
                "-fx-background-radius: 4; -fx-padding: 4 10;"
                : "-fx-background-color: #fdecea; -fx-text-fill: #e74c3c;" +
                "-fx-font-weight: bold; -fx-font-size: 11;" +
                "-fx-background-radius: 4; -fx-padding: 4 10;"
        );
        header.getChildren().addAll(statutIcon, idLabel, spacer, statutBadge);

        HBox barre = new HBox();
        barre.setPrefHeight(2);
        barre.setStyle("-fx-background-color: "
                + (reussi ? "#2ecc71" : "#e74c3c")
                + "; -fx-background-radius: 2;");

        HBox infos = new HBox(20);
        infos.setAlignment(Pos.CENTER_LEFT);
        Label scoreLabel = new Label(
                "🎯 Score : " + String.format("%.1f%%", c.getScoreObtenu()));
        scoreLabel.setStyle(
                "-fx-font-size: 13; -fx-text-fill: #f5a623; -fx-font-weight: bold;");
        Label badgeLabel = new Label(
                "🏅 " + (c.getBadge() != null ? c.getBadge() : "—"));
        badgeLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #555;");
        infos.getChildren().addAll(scoreLabel, badgeLabel);

        HBox details = new HBox(20);
        Label quizLabel = new Label("📝 Quiz ID : " + c.getQuizId());
        quizLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #888;");
        Label userLabel = new Label("👤 User ID : " + c.getUserId());
        userLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #888;");
        Label dateLabel = new Label("📅 " + c.getDateAttribution());
        dateLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #888;");
        details.getChildren().addAll(quizLabel, userLabel, dateLabel);

        card.getChildren().addAll(header, barre, infos, details);

        if (reussi) {
            Button btnCertifFinale =
                    new Button("🏆  Ajouter Certification Finale");
            btnCertifFinale.setStyle(
                    "-fx-background-color: #f5a623; -fx-text-fill: white;" +
                            "-fx-font-weight: bold; -fx-font-size: 12;" +
                            "-fx-background-radius: 8; -fx-padding: 9 20; -fx-cursor: hand;"
            );
            btnCertifFinale.setOnMouseEntered(e -> btnCertifFinale.setStyle(
                    "-fx-background-color: #e8960f; -fx-text-fill: white;" +
                            "-fx-font-weight: bold; -fx-font-size: 12;" +
                            "-fx-background-radius: 8; -fx-padding: 9 20; -fx-cursor: hand;"
            ));
            btnCertifFinale.setOnMouseExited(e -> btnCertifFinale.setStyle(
                    "-fx-background-color: #f5a623; -fx-text-fill: white;" +
                            "-fx-font-weight: bold; -fx-font-size: 12;" +
                            "-fx-background-radius: 8; -fx-padding: 9 20; -fx-cursor: hand;"
            ));
            btnCertifFinale.setOnAction(e ->
                    afficherPopupCertifFinale(c, card));
            card.getChildren().add(btnCertifFinale);
        }

        return card;
    }

    // ─────────── CARTE CERTIF FINALE ───────────
    private VBox creerCarteFinaleExistante(CertificationFinale cf) {
        VBox cardFinale = new VBox(8);
        cardFinale.setStyle(
                "-fx-background-color: #fff8ee; -fx-background-radius: 10;" +
                        "-fx-padding: 15; -fx-border-color: #f5a623;" +
                        "-fx-border-radius: 10; -fx-border-width: 2;"
        );
        VBox.setMargin(cardFinale, new Insets(5, 0, 0, 20));

        Label titre = new Label("🏆 Certification Finale #" + cf.getId());
        titre.setStyle(
                "-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #f5a623;");

        HBox infos = new HBox(20);
        Label badgeL = new Label(
                "🏅 " + (cf.getBadge() != null ? cf.getBadge() : "—"));
        badgeL.setStyle("-fx-font-size: 12; -fx-text-fill: #555;");
        Label dateL = new Label("📅 " + cf.getDateEmission());
        dateL.setStyle("-fx-font-size: 11; -fx-text-fill: #888;");
        Label userL = new Label("👤 User ID : " + cf.getUserId());
        userL.setStyle("-fx-font-size: 11; -fx-text-fill: #888;");
        Label quizL = new Label("📝 Quiz ID : " + cf.getQuizId());
        quizL.setStyle("-fx-font-size: 11; -fx-text-fill: #888;");
        infos.getChildren().addAll(badgeL, dateL, userL, quizL);

        cardFinale.getChildren().addAll(titre, infos);
        return cardFinale;
    }

    // ─────────── POPUP CERTIFICATION FINALE ───────────
    private void afficherPopupCertifFinale(Certification certifOrigine,
                                           VBox card) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Certification Finale");
        popup.setResizable(false);

        VBox root = new VBox(12);
        root.setStyle("-fx-background-color: #f4f6f9; -fx-padding: 30;");
        root.setPrefWidth(430);

        Label titre = new Label("🏆 Ajouter Certification Finale");
        titre.setStyle(
                "-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #1a1f3c;");

        HBox barre = new HBox();
        barre.setPrefHeight(3);
        barre.setStyle(
                "-fx-background-color: #f5a623; -fx-background-radius: 2;");

        VBox infoBox = new VBox(4);
        infoBox.setStyle(
                "-fx-background-color: #f0f4ff; -fx-background-radius: 8;" +
                        "-fx-padding: 10;");
        Label infoTentative = new Label(
                "Tentative #" + certifOrigine.getId() +
                        "  |  User " + certifOrigine.getUserId() +
                        "  |  Score : " +
                        String.format("%.1f%%", certifOrigine.getScoreObtenu()));
        infoTentative.setStyle(
                "-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #1a1f3c;");
        infoBox.getChildren().add(infoTentative);

        Label lblBadge = new Label("Badge :");
        lblBadge.setStyle("-fx-font-weight: bold; -fx-text-fill: #555;");
        ComboBox<String> badgeBox = new ComboBox<>();
        badgeBox.getItems().addAll("Or", "Argent", "Bronze");
        badgeBox.setValue(certifOrigine.getBadge() != null
                && !certifOrigine.getBadge().isEmpty()
                ? certifOrigine.getBadge() : "Or");
        badgeBox.setPrefWidth(370);
        badgeBox.setStyle(
                "-fx-background-color: white; -fx-border-color: #dde1e7;" +
                        "-fx-border-radius: 8; -fx-background-radius: 8;");

        Label msgLabel = new Label("");
        msgLabel.setStyle("-fx-font-size: 12; -fx-font-weight: bold;");
        msgLabel.setWrapText(true);

        HBox btnBox = new HBox(10);
        btnBox.setAlignment(Pos.CENTER);

        Button btnConfirmer = new Button(" Confirmer");
        btnConfirmer.setPrefWidth(180);
        btnConfirmer.setStyle(
                "-fx-background-color: #f5a623; -fx-text-fill: white;" +
                        "-fx-font-weight: bold; -fx-background-radius: 8;" +
                        "-fx-padding: 11; -fx-cursor: hand;");

        Button btnAnnuler = new Button("Annuler");
        btnAnnuler.setPrefWidth(180);
        btnAnnuler.setStyle(
                "-fx-background-color: #e74c3c; -fx-text-fill: white;" +
                        "-fx-font-weight: bold; -fx-background-radius: 8;" +
                        "-fx-padding: 11; -fx-cursor: hand;");
        btnAnnuler.setOnAction(e -> popup.close());

        btnConfirmer.setOnAction(e -> {
            try {
                String badge = badgeBox.getValue();
                if (badge == null) {
                    msgLabel.setText("⚠ Choisissez un badge !");
                    msgLabel.setStyle(
                            "-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    return;
                }

                CertificationFinale certifFinale = new CertificationFinale(
                        Timestamp.valueOf(LocalDateTime.now()),
                        badge,
                        certifOrigine.getUserId(),
                        certifOrigine.getQuizId(),
                        certifOrigine.getId()
                );
                certifFinaleService.ajouter(certifFinale);

                CertificationFinale inserted =
                        trouverDerniereCertifFinale(certifOrigine.getId());

                if (inserted != null) {
                    VBox carteFinale = creerCarteFinaleExistante(inserted);
                    int index =
                            certifContainer.getChildren().indexOf(card);
                    if (index >= 0) {
                        certifContainer.getChildren()
                                .add(index + 1, carteFinale);
                    } else {
                        certifContainer.getChildren().add(carteFinale);
                    }
                }

                msgLabel.setText(" Certification finale ajoutée !");
                msgLabel.setStyle(
                        "-fx-text-fill: #2ecc71; -fx-font-weight: bold;");

                javafx.animation.PauseTransition pause =
                        new javafx.animation.PauseTransition(
                                javafx.util.Duration.seconds(2));
                pause.setOnFinished(ev -> popup.close());
                pause.play();

            } catch (Exception ex) {
                msgLabel.setText(" Erreur : " + ex.getMessage());
                msgLabel.setStyle(
                        "-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                ex.printStackTrace();
            }
        });

        btnBox.getChildren().addAll(btnConfirmer, btnAnnuler);

        root.getChildren().addAll(
                titre, barre, infoBox,
                lblBadge, badgeBox,
                msgLabel, btnBox
        );

        popup.setScene(new Scene(root));
        popup.showAndWait();
    }

    // ─────────── HELPER ───────────
    private CertificationFinale trouverDerniereCertifFinale(
            int tentativeId) {
        try {
            CertificationFinale derniere = null;
            for (CertificationFinale cf : certifFinaleService.afficher()) {
                if (cf.getTentativeId() == tentativeId) derniere = cf;
            }
            return derniere;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ─────────── RETOUR ───────────
    @FXML
    private void retourAccueil() {
        try {
            Stage stage = (Stage) quizTable.getScene().getWindow();
            new org.example.MainFX().start(stage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ─────────── HELPERS ───────────
    private void showMsg(String msg, boolean success) {
        messageQuiz.setStyle(success
                ? "-fx-text-fill: #2ecc71; -fx-font-weight: bold;"
                : "-fx-text-fill: #e74c3c; -fx-font-weight: bold;"
        );
        messageQuiz.setText(msg);
    }
}