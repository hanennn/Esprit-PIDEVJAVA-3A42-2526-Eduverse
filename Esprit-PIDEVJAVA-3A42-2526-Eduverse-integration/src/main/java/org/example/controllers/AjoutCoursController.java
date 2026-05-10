package org.example.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.entities.Session;
import org.example.entities.cours;
import org.example.services.EmailService;
import org.example.services.coursservices;

import java.sql.SQLException;
import java.util.List;

public class AjoutCoursController {

    // ── Formulaire ──
    @FXML private TextField        titreCours;
    @FXML private TextArea         descCours;
    @FXML private TextField        niveauCours;
    @FXML private TextField        matiereCours;
    @FXML private ComboBox<String> langueCours;
    @FXML private Label            messageForm;

    // ── Liste ──
    @FXML private VBox             coursListContainer;
    @FXML private TextField        searchField;
    @FXML private ComboBox<String> filterNiveauBox;
    @FXML private ComboBox<String> filterLangueBox;
    @FXML private Label            countLabel;

    private final coursservices coursService = new coursservices();
    private List<cours> tousLesCours;

    // Pour savoir si on est en mode modification
    private cours coursEnCoursDeModification = null;

    @FXML
    public void initialize() {
        langueCours.getItems().addAll("Français", "Anglais");
        filterNiveauBox.getItems().addAll("Tous", "Débutant", "Intermédiaire", "Avancé");
        filterNiveauBox.setValue("Tous");
        filterLangueBox.getItems().addAll("Tous", "Français", "Anglais");
        filterLangueBox.setValue("Tous");

        chargerCours();

        // Filtres en temps réel
        searchField.textProperty().addListener((obs, o, n) -> appliquerFiltres());
        filterNiveauBox.valueProperty().addListener((obs, o, n) -> appliquerFiltres());
        filterLangueBox.valueProperty().addListener((obs, o, n) -> appliquerFiltres());
    }

    // ─────────── CHARGER COURS ───────────
    @FXML
    private void chargerCours() {
        try {
            // afficher() retourne uniquement les cours du formateur connecté
            tousLesCours = coursService.afficher();
            afficherCours(tousLesCours);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void afficherCours(List<cours> liste) {
        coursListContainer.getChildren().clear();
        if (countLabel != null)
            countLabel.setText(liste.size() + " cours au total");

        if (liste.isEmpty()) {
            Label vide = new Label("Aucun cours disponible.");
            vide.setStyle("-fx-text-fill: #888; -fx-font-size: 13; -fx-padding: 20;");
            coursListContainer.getChildren().add(vide);
            return;
        }

        for (cours c : liste) {
            coursListContainer.getChildren().add(creerLigneCours(c));
        }
    }

    // ─────────── LIGNE COURS ───────────
    private HBox creerLigneCours(cours c) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 10 15; -fx-border-color: transparent transparent #f0f0f0 transparent;");

        // Titre
        Label titreLabel = new Label(c.getTitre_cours());
        titreLabel.setPrefWidth(180);
        titreLabel.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #1a1f3c;");
        titreLabel.setWrapText(true);

        // Niveau
        Label niveauLabel = new Label(c.getNiv_cours() != null ? c.getNiv_cours().trim() : "-");
        niveauLabel.setPrefWidth(130);
        niveauLabel.setStyle(
                "-fx-background-color: #e3f2fd; -fx-text-fill: #1976d2;" +
                        "-fx-font-size: 11; -fx-font-weight: bold;" +
                        "-fx-background-radius: 4; -fx-padding: 3 10;"
        );

        // Matière
        Label matiereLabel = new Label(c.getMatiere_cours() != null ? c.getMatiere_cours().trim() : "-");
        matiereLabel.setPrefWidth(130);
        matiereLabel.setStyle(
                "-fx-background-color: #f3e5f5; -fx-text-fill: #7b1fa2;" +
                        "-fx-font-size: 11; -fx-font-weight: bold;" +
                        "-fx-background-radius: 4; -fx-padding: 3 10;"
        );

        // Langue
        Label langueLabel = new Label(c.getLangue_cours() != null ? c.getLangue_cours().trim() : "-");
        langueLabel.setPrefWidth(100);
        langueLabel.setStyle(
                "-fx-background-color: #e8f5e9; -fx-text-fill: #388e3c;" +
                        "-fx-font-size: 11; -fx-font-weight: bold;" +
                        "-fx-background-radius: 4; -fx-padding: 3 10;"
        );

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Bouton Modifier
        Button btnModif = new Button("✏");
        btnModif.setStyle(
                "-fx-background-color: #fffbeb; -fx-text-fill: #f5a623;" +
                        "-fx-font-size: 12; -fx-background-radius: 6;" +
                        "-fx-border-color: #fde68a; -fx-border-radius: 6; -fx-border-width: 1;" +
                        "-fx-padding: 4 10; -fx-cursor: hand;"
        );
        btnModif.setOnMouseEntered(e -> btnModif.setStyle(
                "-fx-background-color: #f5a623; -fx-text-fill: white;" +
                        "-fx-font-size: 12; -fx-background-radius: 6;" +
                        "-fx-border-color: #f5a623; -fx-border-radius: 6; -fx-border-width: 1;" +
                        "-fx-padding: 4 10; -fx-cursor: hand;"
        ));
        btnModif.setOnMouseExited(e -> btnModif.setStyle(
                "-fx-background-color: #fffbeb; -fx-text-fill: #f5a623;" +
                        "-fx-font-size: 12; -fx-background-radius: 6;" +
                        "-fx-border-color: #fde68a; -fx-border-radius: 6; -fx-border-width: 1;" +
                        "-fx-padding: 4 10; -fx-cursor: hand;"
        ));
        btnModif.setOnAction(e -> ouvrirModification(c));

        // Bouton Supprimer
        Button btnSupp = new Button("🗑");
        btnSupp.setStyle(
                "-fx-background-color: #fef2f2; -fx-text-fill: #e74c3c;" +
                        "-fx-font-size: 12; -fx-background-radius: 6;" +
                        "-fx-border-color: #fecaca; -fx-border-radius: 6; -fx-border-width: 1;" +
                        "-fx-padding: 4 10; -fx-cursor: hand;"
        );
        btnSupp.setOnMouseEntered(e -> btnSupp.setStyle(
                "-fx-background-color: #e74c3c; -fx-text-fill: white;" +
                        "-fx-font-size: 12; -fx-background-radius: 6;" +
                        "-fx-border-color: #e74c3c; -fx-border-radius: 6; -fx-border-width: 1;" +
                        "-fx-padding: 4 10; -fx-cursor: hand;"
        ));
        btnSupp.setOnMouseExited(e -> btnSupp.setStyle(
                "-fx-background-color: #fef2f2; -fx-text-fill: #e74c3c;" +
                        "-fx-font-size: 12; -fx-background-radius: 6;" +
                        "-fx-border-color: #fecaca; -fx-border-radius: 6; -fx-border-width: 1;" +
                        "-fx-padding: 4 10; -fx-cursor: hand;"
        ));
        btnSupp.setOnAction(e -> supprimerCours(c));

        row.getChildren().addAll(titreLabel, niveauLabel, matiereLabel, langueLabel, spacer, btnModif, btnSupp);

        // Hover
        row.setOnMouseEntered(e -> row.setStyle(
                "-fx-padding: 10 15; -fx-background-color: #fff8ee;" +
                        "-fx-border-color: transparent transparent #f0f0f0 transparent;"));
        row.setOnMouseExited(e -> row.setStyle(
                "-fx-padding: 10 15; -fx-border-color: transparent transparent #f0f0f0 transparent;"));

        return row;
    }

    // ─────────── FILTRES ───────────
    @FXML
    private void appliquerFiltres() {
        if (tousLesCours == null) return;
        String recherche = searchField.getText().trim().toLowerCase();
        String niveau    = filterNiveauBox.getValue();
        String langue    = filterLangueBox.getValue();

        List<cours> filtre = tousLesCours.stream()
                .filter(c -> recherche.isEmpty() ||
                        c.getTitre_cours().toLowerCase().contains(recherche))
                .filter(c -> niveau == null || niveau.equals("Tous") ||
                        (c.getNiv_cours() != null && c.getNiv_cours().toLowerCase().contains(niveau.toLowerCase())))
                .filter(c -> langue == null || langue.equals("Tous") ||
                        (c.getLangue_cours() != null && c.getLangue_cours().toLowerCase().contains(langue.toLowerCase())))
                .collect(java.util.stream.Collectors.toList());

        afficherCours(filtre);
    }

    @FXML
    private void reinitialiserFiltres() {
        searchField.clear();
        filterNiveauBox.setValue("Tous");
        filterLangueBox.setValue("Tous");
        afficherCours(tousLesCours);
    }

    // ─────────── AJOUTER COURS ───────────
    @FXML
    void ajouterCours() {
        resetStyles();

        String titre   = titreCours.getText().trim();
        String desc    = descCours.getText().trim();
        String niveau  = niveauCours.getText().trim();
        String matiere = matiereCours.getText().trim();
        String langue  = langueCours.getValue() != null ? langueCours.getValue().trim() : "";

        // Validations
        if (titre.isEmpty())        { showFieldError(titreCours,   "Le titre est obligatoire !"); return; }
        if (titre.length() < 3)     { showFieldError(titreCours,   "Le titre doit avoir au moins 3 caractères !"); return; }
        if (desc.isEmpty())         { showTextAreaError(           "La description est obligatoire !"); return; }
        if (desc.length() < 10)     { showTextAreaError(           "La description doit avoir au moins 10 caractères !"); return; }
        if (niveau.isEmpty())       { showFieldError(niveauCours,  "Le niveau est obligatoire !"); return; }
        if (matiere.isEmpty())      { showFieldError(matiereCours, "La matière est obligatoire !"); return; }
        if (langue.isEmpty())       { showComboError(              "La langue est obligatoire !"); return; }

        // Mode modification
        if (coursEnCoursDeModification != null) {
            coursEnCoursDeModification.setTitre_cours(titre);
            coursEnCoursDeModification.setDescription(desc);
            coursEnCoursDeModification.setNiv_cours(niveau);
            coursEnCoursDeModification.setMatiere_cours(matiere);
            coursEnCoursDeModification.setLangue_cours(langue);
            try {
                coursService.modifier(coursEnCoursDeModification.getId(), coursEnCoursDeModification);
                showSuccess("✅ Cours modifié avec succès !");
                coursEnCoursDeModification = null;
                annuler();
                chargerCours();
            } catch (SQLException e) {
                showFieldError(titreCours, e.getMessage());
            }
            return;
        }

        // Mode ajout
        // createur_id est injecté automatiquement dans coursService.ajouter()
        // via Session.getCurrentUser().getId()
        cours c = new cours(titre, desc, niveau, matiere, langue);
        try {
            coursService.ajouter(c);
            showSuccess("✅ Cours ajouté avec succès !");
            annuler();
            chargerCours();
            new Thread(() -> EmailService.envoyerNouveauCours(c.getTitre_cours())).start();
        } catch (SQLException e) {
            showFieldError(titreCours, e.getMessage());
        }
    }

    // ─────────── OUVRIR MODIFICATION ───────────
    private void ouvrirModification(cours c) {
        // Remplir le formulaire avec les données du cours
        titreCours.setText(c.getTitre_cours());
        descCours.setText(c.getDescription());
        niveauCours.setText(c.getNiv_cours());
        matiereCours.setText(c.getMatiere_cours());
        langueCours.setValue(c.getLangue_cours());

        // Mémoriser le cours en cours de modification
        coursEnCoursDeModification = c;

        // Message indicatif
        showSuccess("✏ Mode modification — \"" + c.getTitre_cours() + "\"");

        // Scroll vers le formulaire
        titreCours.requestFocus();
    }

    // ─────────── SUPPRIMER ───────────
    private void supprimerCours(cours c) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer \"" + c.getTitre_cours() + "\" ?",
                ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                try {
                    coursService.supprimer(c.getId());
                    showSuccess("🗑 Cours supprimé.");
                    chargerCours();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // ─────────── ANNULER ───────────
    @FXML
    void annuler() {
        titreCours.clear();
        descCours.clear();
        niveauCours.clear();
        matiereCours.clear();
        langueCours.setValue(null);
        coursEnCoursDeModification = null; // ← reset mode modification
        resetStyles();
        if (messageForm != null) messageForm.setText("");
    }

    // ─────────── RETOUR ───────────
    @FXML
    void retour() {
        try {
            Stage stage = (Stage) titreCours.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/AccueilFormateur.fxml"));
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Espace Formateur");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ─────────── HELPERS ───────────
    private void resetStyles() {
        titreCours.setStyle("");
        descCours.setStyle("");
        niveauCours.setStyle("");
        matiereCours.setStyle("");
        langueCours.setStyle("");
    }

    private void showFieldError(TextField field, String msg) {
        if (field != null)
            field.setStyle("-fx-border-color: #e74c3c; -fx-border-radius: 6; -fx-border-width: 1.5;");
        if (messageForm != null) {
            messageForm.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            messageForm.setText("⚠ " + msg);
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText(msg);
            alert.show();
        }
    }

    private void showTextAreaError(String msg) {
        descCours.setStyle("-fx-border-color: #e74c3c; -fx-border-radius: 6; -fx-border-width: 1.5;");
        if (messageForm != null) {
            messageForm.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            messageForm.setText("⚠ " + msg);
        }
    }

    private void showComboError(String msg) {
        langueCours.setStyle("-fx-border-color: #e74c3c; -fx-border-radius: 6; -fx-border-width: 1.5;");
        if (messageForm != null) {
            messageForm.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            messageForm.setText("⚠ " + msg);
        }
    }

    private void showSuccess(String msg) {
        if (messageForm != null) {
            messageForm.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
            messageForm.setText(msg);
        }
    }
}