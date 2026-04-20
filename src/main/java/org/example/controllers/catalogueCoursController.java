package org.example.controllers;

import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.entities.chapitres;
import org.example.entities.cours;
import org.example.services.chapitresservices;
import org.example.services.coursservices;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

public class catalogueCoursController {

    @FXML private FlowPane listContainer;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> critereRecherche;
    @FXML private ComboBox<String> trierPar;
    @FXML private Button btnMesFavoris;

    // Persistance via Java Preferences (survit aux redémarrages)
    private final Preferences prefs = Preferences.userNodeForPackage(catalogueCoursController.class);
    private Set<Integer> favoris = new HashSet<>();
    private boolean afficherFavoris = false;

    private List<cours> tousLesCours;

    @FXML
    public void initialize() throws SQLException {
        tousLesCours = new coursservices().afficher();

        // Charger favoris persistés
        String saved = prefs.get("favoris", "");
        if (!saved.isEmpty()) {
            for (String id : saved.split(",")) {
                try { favoris.add(Integer.parseInt(id.trim())); }
                catch (NumberFormatException ignored) {}
            }
        }

        critereRecherche.getItems().addAll("Titre", "Matière", "Niveau", "Langue");
        critereRecherche.setValue("Titre");
        trierPar.getItems().addAll("Titre", "Niveau", "Langue", "Matière");
        trierPar.setValue("Titre");
        afficherCours(tousLesCours);
        mettreAJourBtnFavoris();
    }

    // Sauvegarde les favoris dans les préférences système
    private void sauvegarderFavoris() {
        String val = favoris.stream().map(String::valueOf).collect(Collectors.joining(","));
        prefs.put("favoris", val);
    }

    // Met à jour le style du bouton "Mes Favoris" selon l'état actif/inactif
    private void mettreAJourBtnFavoris() {
        int nb = favoris.size();
        if (afficherFavoris) {
            btnMesFavoris.setText("★ Mes Favoris (" + nb + ")  ✕");
            btnMesFavoris.setStyle(
                    "-fx-background-color: #1a1a2e; -fx-text-fill: #f5a623;" +
                            "-fx-font-weight: bold; -fx-font-size: 11px;" +
                            "-fx-background-radius: 20; -fx-padding: 6 14;" +
                            "-fx-border-color: #f5a623; -fx-border-radius: 20; -fx-border-width: 1.5;"
            );
        } else {
            btnMesFavoris.setText("★ Mes Favoris (" + nb + ")");
            btnMesFavoris.setStyle(
                    "-fx-background-color: transparent; -fx-text-fill: white;" +
                            "-fx-font-weight: bold; -fx-font-size: 11px;" +
                            "-fx-background-radius: 20; -fx-padding: 6 14;" +
                            "-fx-border-color: white; -fx-border-radius: 20; -fx-border-width: 1.5;"
            );
        }
    }

    @FXML
    void toggleFavoris() {
        afficherFavoris = !afficherFavoris;
        mettreAJourBtnFavoris();
        filtrerCours();
    }

    @FXML
    void filtrerCours() {
        String recherche = searchField.getText().toLowerCase().trim();
        String critere = critereRecherche.getValue();
        String tri = trierPar.getValue();

        List<cours> filtre = tousLesCours.stream()
                .filter(c -> {
                    // Filtre favoris
                    if (afficherFavoris && !favoris.contains(c.getId())) return false;
                    if (recherche.isEmpty()) return true;
                    switch (critere) {
                        case "Matière": return c.getMatiere_cours() != null &&
                                c.getMatiere_cours().toLowerCase().contains(recherche);
                        case "Niveau":  return c.getNiv_cours() != null &&
                                c.getNiv_cours().toLowerCase().contains(recherche);
                        case "Langue":  return c.getLangue_cours() != null &&
                                c.getLangue_cours().toLowerCase().contains(recherche);
                        default:        return c.getTitre_cours() != null &&
                                c.getTitre_cours().toLowerCase().contains(recherche);
                    }
                })
                .collect(Collectors.toList());

        filtre.sort((a, b) -> {
            switch (tri) {
                case "Niveau":  return safe(a.getNiv_cours()).compareTo(safe(b.getNiv_cours()));
                case "Langue":  return safe(a.getLangue_cours()).compareTo(safe(b.getLangue_cours()));
                case "Matière": return safe(a.getMatiere_cours()).compareTo(safe(b.getMatiere_cours()));
                default:        return safe(a.getTitre_cours()).compareTo(safe(b.getTitre_cours()));
            }
        });

        afficherCours(filtre);
    }

    private String safe(String s) { return s != null ? s : ""; }

    private void afficherCours(List<cours> liste) {
        listContainer.getChildren().clear();

        if (liste.isEmpty() && afficherFavoris) {
            Label vide = new Label("Aucun cours en favori pour l'instant ★");
            vide.setStyle("-fx-font-size: 15px; -fx-text-fill: #aaa; -fx-padding: 40;");
            listContainer.getChildren().add(vide);
            return;
        }

        for (cours c : liste)
            listContainer.getChildren().add(creerCarte(c));
    }

    private VBox creerCarte(cours c) {
        VBox card = new VBox(0);
        card.setPrefWidth(360);
        card.setMaxWidth(360);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10;" +
                "-fx-border-color: #e0e0e0; -fx-border-radius: 10;");

        boolean estFavori = favoris.contains(c.getId());
        Button btnFavori = new Button(estFavori ? "♥" : "♡");
        btnFavori.setStyle(buildFavoriStyle(estFavori));
        btnFavori.setOnAction(e -> {
            boolean nowFavori;
            if (favoris.contains(c.getId())) {
                favoris.remove(c.getId());
                nowFavori = false;
            } else {
                favoris.add(c.getId());
                nowFavori = true;
            }
            btnFavori.setText(nowFavori ? "♥" : "♡");
            btnFavori.setStyle(buildFavoriStyle(nowFavori));
            ScaleTransition st = new ScaleTransition(Duration.millis(150), btnFavori);
            st.setFromX(1.0); st.setFromY(1.0);
            st.setToX(1.4);   st.setToY(1.4);
            st.setAutoReverse(true);
            st.setCycleCount(2);
            st.play();
            sauvegarderFavoris();
            mettreAJourBtnFavoris();
            if (afficherFavoris) filtrerCours();
        });

        Label titre = new Label(c.getTitre_cours());
        titre.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1a1a2e;");
        titre.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(titre, Priority.ALWAYS);

        HBox header = new HBox(titre, btnFavori);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: #f5a623; " +
                "-fx-background-radius: 10 10 0 0; -fx-padding: 12 16;");

        VBox body = new VBox(8);
        body.setStyle("-fx-padding: 14 16;");
        VBox.setVgrow(body, Priority.ALWAYS);  // ← LA SEULE LIGNE AJOUTÉE

        Label desc = new Label(c.getDescription());
        desc.setWrapText(true);
        desc.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");
        body.getChildren().addAll(desc,
                badge("Niveau :",  c.getNiv_cours(),    "#1abc9c"),
                badge("Matière :", c.getMatiere_cours(), "#2c3e50"),
                badge("Langue :",  c.getLangue_cours(),  "#f5a623"),
                chapitresBox(c));

        Button btn = new Button("Accéder Au Cours");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-color: #f5a623; -fx-text-fill: white;" +
                "-fx-font-weight: bold; -fx-background-radius: 6;" +
                "-fx-font-size: 13px; -fx-padding: 10;");
        btn.setOnAction(e -> voirChapitres(c));

        VBox footer = new VBox(btn);
        footer.setStyle("-fx-padding: 10 16 12 16;");

        card.getChildren().addAll(header, body, footer);
        return card;
    }
    // Style dynamique du bouton favori
    private String buildFavoriStyle(boolean actif) {
        return actif
                ? "-fx-background-color: #ff4d6d; -fx-text-fill: white;" +
                "-fx-background-radius: 20; -fx-font-size: 16px;" +
                "-fx-padding: 4 10; -fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(255,77,109,0.5), 6, 0, 0, 2);"
                : "-fx-background-color: rgba(255,255,255,0.3); -fx-text-fill: white;" +
                "-fx-background-radius: 20; -fx-font-size: 16px;" +
                "-fx-padding: 4 10; -fx-cursor: hand;";
    }

    private void voirChapitres(cours c) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/catalogueChapitres.fxml"));
            Parent root = loader.load();
            catalogueChapitresController controller = loader.getController();
            controller.setCours(c);
            Stage stage = (Stage) listContainer.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private VBox chapitresBox(cours c) {
        VBox box = new VBox(6);
        try {
            List<chapitres> chaps = new chapitresservices().afficher()
                    .stream().filter(ch -> ch.getCours_id() == c.getId())
                    .collect(Collectors.toList());

            Label nbChap = new Label("Chapitres : " + chaps.size());
            nbChap.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;");
            box.getChildren().add(nbChap);

            if (chaps.isEmpty()) {
                Label noChap = new Label("Aucun chapitre disponible");
                noChap.setStyle("-fx-font-style: italic; -fx-text-fill: #aaa; -fx-font-size: 13px;");
                box.getChildren().add(noChap);
            } else {
                Label contenu = new Label("Contenu du cours :");
                contenu.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 4 0 0 0;");
                box.getChildren().add(contenu);
                int i = 1;
                for (chapitres ch : chaps) {
                    Label l = new Label(i + ". " + ch.getTitre_chap()
                            + "\n" + ch.getDuree_chap() + " | " + ch.getType_contenu());
                    l.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");
                    box.getChildren().add(l);
                    i++;
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return box;
    }

    private HBox badge(String label, String valeur, String couleur) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 13px;");
        Label bdg = new Label(valeur != null ? valeur.toUpperCase() : "-");
        bdg.setStyle("-fx-background-color: " + couleur + "; -fx-text-fill: white;" +
                "-fx-background-radius: 20; -fx-padding: 3 12;" +
                "-fx-font-size: 11px; -fx-font-weight: bold;");
        row.getChildren().addAll(lbl, bdg);
        return row;
    }

    @FXML
    void versEnseignant() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/listeCours.fxml"));
            listContainer.getScene().setRoot(root);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}