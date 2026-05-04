package org.example.controllers;

import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.entities.User;
import org.example.entities.Session;
import org.example.entities.chapitres;
import org.example.entities.Session;
import org.example.utils.AppContext;
import org.example.services.chapitresservices;
import org.example.utils.MyConnection;

import java.sql.*;
import java.util.*;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

public class AccueilEtudiantController {

    // ══════════════════════════════════════════════
    //  INJECTION FXML
    // ══════════════════════════════════════════════
    @FXML private FlowPane coursContainer;
    @FXML private HBox     paginationBox;
    @FXML private HBox     searchBarContainer;
    @FXML private Button   btnMesFavoris;

    // ══════════════════════════════════════════════
    //  SERVICES & PREFS
    // ══════════════════════════════════════════════
    private final chapitresservices chapitresService = new chapitresservices();
    private final Preferences prefs =
            Preferences.userNodeForPackage(AccueilEtudiantController.class);

    // ══════════════════════════════════════════════
    //  ÉTAT
    // ══════════════════════════════════════════════
    private Set<Integer>    favoris         = new HashSet<>();
    private boolean         afficherFavoris = false;
    private List<CoursData> tousLesCours    = new ArrayList<>();
    private List<CoursData> coursFiltrés    = new ArrayList<>();

    private String critereRecherche = "Titre";
    private String critereTri       = "Titre";
    private String texteRecherche   = "";

    // ← Mettez 2 pour tester la pagination, 6 en production
    private static final int PAR_PAGE = 2;
    private int pageActuelle = 0;

    // ══════════════════════════════════════════════
    //  INITIALISATION
    // ══════════════════════════════════════════════
    @FXML
    public void initialize() {
        String saved = prefs.get("favoris_etudiant", "");
        if (!saved.isEmpty()) {
            for (String id : saved.split(",")) {
                try { favoris.add(Integer.parseInt(id.trim())); }
                catch (NumberFormatException ignored) {}
            }
        }
        chargerCours();
        construireBarreRecherche();
        filtrerEtTrier(texteRecherche, critereRecherche);
        mettreAJourBtnFavoris();
    }

    // ══════════════════════════════════════════════
    //  CHARGEMENT BDD
    // ══════════════════════════════════════════════
    private void chargerCours() {
        tousLesCours.clear();
        try {
            Connection cnx = MyConnection.getInstance().getCnx();
            String sql = "SELECT id, titre_cours, niv_cours, matiere_cours, " +
                    "langue_cours, description FROM cours";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                tousLesCours.add(new CoursData(
                        rs.getInt("id"),
                        rs.getString("titre_cours"),
                        rs.getString("niv_cours"),
                        rs.getString("matiere_cours"),
                        rs.getString("langue_cours"),
                        rs.getString("description")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ══════════════════════════════════════════════
    //  FAVORIS
    // ══════════════════════════════════════════════
    private void sauvegarderFavoris() {
        prefs.put("favoris_etudiant",
                favoris.stream().map(String::valueOf).collect(Collectors.joining(",")));
    }

    private void mettreAJourBtnFavoris() {
        int nb = favoris.size();
        if (afficherFavoris) {
            btnMesFavoris.setText("★  Mes Favoris (" + nb + ")  ✕");
            btnMesFavoris.setStyle(
                    "-fx-background-color: #1a1f3c; -fx-text-fill: white;" +
                            "-fx-font-weight: bold; -fx-font-size: 12px;" +
                            "-fx-background-radius: 50; -fx-padding: 11 22;" +
                            "-fx-cursor: hand;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 10, 0, 0, 4);");
        } else {
            btnMesFavoris.setText("★  Mes Favoris (" + nb + ")");
            btnMesFavoris.setStyle(
                    "-fx-background-color: white; -fx-text-fill: #1a1f3c;" +
                            "-fx-font-weight: bold; -fx-font-size: 12px;" +
                            "-fx-background-radius: 50; -fx-padding: 11 22;" +
                            "-fx-cursor: hand;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 10, 0, 0, 4);");
        }
    }

    @FXML
    void toggleFavoris() {
        afficherFavoris = !afficherFavoris;
        mettreAJourBtnFavoris();
        pageActuelle = 0;
        filtrerEtTrier(texteRecherche, critereRecherche);
    }

    // ══════════════════════════════════════════════
    //  FILTRAGE + TRI
    // ══════════════════════════════════════════════
    private void filtrerEtTrier(String recherche, String critere) {
        texteRecherche   = recherche != null ? recherche.toLowerCase().trim() : "";
        critereRecherche = critere   != null ? critere : "Titre";

        coursFiltrés = tousLesCours.stream()
                .filter(c -> {
                    if (afficherFavoris && !favoris.contains(c.id)) return false;
                    if (texteRecherche.isEmpty()) return true;
                    String val = switch (critereRecherche) {
                        case "Niveau"  -> safe(c.niveau);
                        case "Matière" -> safe(c.matiere);
                        case "Langue"  -> safe(c.langue);
                        default        -> safe(c.titre);
                    };
                    return val.toLowerCase().contains(texteRecherche);
                })
                .sorted((a, b) -> {
                    String va = switch (critereTri) {
                        case "Niveau"  -> safe(a.niveau);
                        case "Matière" -> safe(a.matiere);
                        case "Langue"  -> safe(a.langue);
                        default        -> safe(a.titre);
                    };
                    String vb = switch (critereTri) {
                        case "Niveau"  -> safe(b.niveau);
                        case "Matière" -> safe(b.matiere);
                        case "Langue"  -> safe(b.langue);
                        default        -> safe(b.titre);
                    };
                    return va.compareToIgnoreCase(vb);
                })
                .collect(Collectors.toList());

        pageActuelle = 0;
        afficherPage();
    }

    private String safe(String s) { return s != null ? s : ""; }

    // ══════════════════════════════════════════════
    //  AFFICHAGE PAGE
    // ══════════════════════════════════════════════
    private void afficherPage() {
        coursContainer.getChildren().clear();

        if (coursFiltrés.isEmpty()) {
            Label vide = new Label(afficherFavoris
                    ? "Aucun cours en favori pour l'instant ★"
                    : "Aucun cours trouvé.");
            vide.setStyle("-fx-font-size: 15px; -fx-text-fill: #aaa; -fx-padding: 50;");
            coursContainer.getChildren().add(vide);
            mettreAJourPagination();
            return;
        }

        int debut = pageActuelle * PAR_PAGE;
        int fin   = Math.min(debut + PAR_PAGE, coursFiltrés.size());
        for (int i = debut; i < fin; i++) {
            coursContainer.getChildren().add(creerCarte(coursFiltrés.get(i)));
        }
        mettreAJourPagination();
    }

    // ══════════════════════════════════════════════
    //  BARRE DE RECHERCHE
    // ══════════════════════════════════════════════
    private void construireBarreRecherche() {
        searchBarContainer.getChildren().clear();
        searchBarContainer.setAlignment(Pos.CENTER_LEFT);
        searchBarContainer.setSpacing(12);

        Label critLabel = new Label("Critère :");
        critLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7a849e; -fx-font-weight: 500;");

        ComboBox<String> critereBox = new ComboBox<>();
        critereBox.getItems().addAll("Titre", "Matière", "Niveau", "Langue");
        critereBox.setValue(critereRecherche);
        critereBox.setPrefWidth(120);
        critereBox.setStyle(
                "-fx-font-size: 12; -fx-background-color: white;" +
                        "-fx-border-color: #e4e8f0; -fx-border-radius: 8;" +
                        "-fx-background-radius: 8;");

        TextField searchField = new TextField();
        searchField.setPromptText("Rechercher un cours...");
        HBox.setHgrow(searchField, Priority.ALWAYS);
        searchField.setStyle(
                "-fx-font-size: 13; -fx-background-radius: 8;" +
                        "-fx-border-color: #e4e8f0; -fx-border-radius: 8;" +
                        "-fx-padding: 8 14; -fx-background-color: white;");

        Label triLabel = new Label("Trier par :");
        triLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7a849e; -fx-font-weight: 500;");

        ComboBox<String> triBox = new ComboBox<>();
        triBox.getItems().addAll("Titre", "Niveau", "Langue", "Matière");
        triBox.setValue(critereTri);
        triBox.setPrefWidth(120);
        triBox.setStyle(
                "-fx-font-size: 12; -fx-background-color: white;" +
                        "-fx-border-color: #e4e8f0; -fx-border-radius: 8;" +
                        "-fx-background-radius: 8;");

        Button btnRecherche = new Button("🔍");
        btnRecherche.setStyle(
                "-fx-background-color: #f5a623; -fx-text-fill: white;" +
                        "-fx-font-size: 14; -fx-background-radius: 8;" +
                        "-fx-min-width: 36; -fx-min-height: 36; -fx-cursor: hand;");

        critereBox.setOnAction(e -> critereRecherche = critereBox.getValue());

        triBox.setOnAction(e -> {
            critereTri = triBox.getValue();
            pageActuelle = 0;
            filtrerEtTrier(searchField.getText(), critereBox.getValue());
        });

        btnRecherche.setOnAction(e -> {
            pageActuelle = 0;
            filtrerEtTrier(searchField.getText(), critereBox.getValue());
        });

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            pageActuelle = 0;
            filtrerEtTrier(newVal, critereBox.getValue());
        });

        searchBarContainer.getChildren().addAll(
                critLabel, critereBox, searchField, triLabel, triBox, btnRecherche);
    }

    // ══════════════════════════════════════════════
    //  PAGINATION — corrigée
    // ══════════════════════════════════════════════
    private void mettreAJourPagination() {
        paginationBox.getChildren().clear();

        int totalPages = (int) Math.ceil((double) coursFiltrés.size() / PAR_PAGE);

        // Toujours afficher le compteur de résultats
        Label info = new Label(coursFiltrés.size() + " cours  •  page "
                + (pageActuelle + 1) + " / " + Math.max(1, totalPages));
        info.setStyle("-fx-font-size: 12px; -fx-text-fill: #7a849e; -fx-padding: 0 0 0 8;");

        // Si une seule page : juste le compteur
        if (totalPages <= 1) {
            paginationBox.getChildren().add(info);
            return;
        }

        // ── Bouton Précédent ──
        Button prev = new Button("← Précédent");
        prev.setDisable(pageActuelle == 0);
        prev.setStyle(styleBtnNav(pageActuelle == 0));
        prev.setOnAction(e -> { pageActuelle--; afficherPage(); });

        // ── Numéros avec fenêtre glissante ──
        HBox numeros = new HBox(6);
        numeros.setAlignment(Pos.CENTER);

        int fenetreDebut = Math.max(0, Math.min(pageActuelle - 3, totalPages - 7));
        int fenetreFin   = Math.min(totalPages, fenetreDebut + 7);

        // Bouton "1" + "…" si on est loin du début
        if (fenetreDebut > 0) {
            Button b = new Button("1");
            b.setStyle(styleBtnPage());
            b.setOnAction(e -> { pageActuelle = 0; afficherPage(); });
            numeros.getChildren().add(b);
            if (fenetreDebut > 1) {
                Label dots = new Label("…");
                dots.setStyle("-fx-font-size: 13px; -fx-text-fill: #aaa; -fx-padding: 0 4;");
                numeros.getChildren().add(dots);
            }
        }

        // Numéros centraux
        for (int i = fenetreDebut; i < fenetreFin; i++) {
            final int idx = i;
            Button btn = new Button(String.valueOf(i + 1));
            btn.setStyle(i == pageActuelle ? styleBtnPageActif() : styleBtnPage());
            btn.setOnAction(e -> { pageActuelle = idx; afficherPage(); });
            numeros.getChildren().add(btn);
        }

        // "…" + dernière page si on est loin de la fin
        if (fenetreFin < totalPages) {
            if (fenetreFin < totalPages - 1) {
                Label dots = new Label("…");
                dots.setStyle("-fx-font-size: 13px; -fx-text-fill: #aaa; -fx-padding: 0 4;");
                numeros.getChildren().add(dots);
            }
            Button b = new Button(String.valueOf(totalPages));
            b.setStyle(styleBtnPage());
            b.setOnAction(e -> { pageActuelle = totalPages - 1; afficherPage(); });
            numeros.getChildren().add(b);
        }

        // ── Bouton Suivant ──
        Button next = new Button("Suivant →");
        next.setDisable(pageActuelle >= totalPages - 1);
        next.setStyle(styleBtnNav(pageActuelle >= totalPages - 1));
        next.setOnAction(e -> { pageActuelle++; afficherPage(); });

        paginationBox.getChildren().addAll(prev, numeros, next, info);
    }

    private String styleBtnNav(boolean disabled) {
        String bg     = disabled ? "#f0f2f8" : "white";
        String fg     = disabled ? "#bbb"    : "#1a1f3c";
        String border = disabled ? "#e4e8f0" : "#c8cdd8";
        return "-fx-background-color: " + bg + ";" +
                "-fx-text-fill: " + fg + ";" +
                "-fx-border-color: " + border + ";" +
                "-fx-border-width: 1.5; -fx-border-radius: 10; -fx-background-radius: 10;" +
                "-fx-font-size: 12px; -fx-font-weight: bold;" +
                "-fx-padding: 8 18; -fx-cursor: hand;";
    }

    private String styleBtnPage() {
        return "-fx-background-color: white; -fx-text-fill: #1a1f3c;" +
                "-fx-border-color: #c8cdd8; -fx-border-width: 1.5;" +
                "-fx-border-radius: 50; -fx-background-radius: 50;" +
                "-fx-font-size: 12px; -fx-font-weight: 600;" +
                "-fx-min-width: 36; -fx-min-height: 36; -fx-cursor: hand;";
    }

    private String styleBtnPageActif() {
        return "-fx-background-color: #f5a623; -fx-text-fill: white; -fx-font-weight: bold;" +
                "-fx-border-color: #f5a623; -fx-border-width: 1.5;" +
                "-fx-border-radius: 50; -fx-background-radius: 50;" +
                "-fx-font-size: 12px;" +
                "-fx-min-width: 36; -fx-min-height: 36; -fx-cursor: hand;";
    }

    // ══════════════════════════════════════════════
    //  BARRE DE PROGRESSION
    // ══════════════════════════════════════════════
    private double calculerProgression(int coursId) {
        try {
            List<chapitres> chaps = chapitresService.afficher()
                    .stream().filter(ch -> ch.getCours_id() == coursId)
                    .collect(Collectors.toList());
            if (chaps.isEmpty()) return 0;
            long nbOuverts = chaps.stream()
                    .filter(ch -> "OUVERT".equalsIgnoreCase(ch.getStatut_chap()))
                    .count();
            return (double) nbOuverts / chaps.size();
        } catch (Exception e) { return 0; }
    }

    private VBox barreProgression(int coursId) {
        double progression = calculerProgression(coursId);
        int pourcentage = (int) (progression * 100);

        Label labelProg = new Label("Progression");
        labelProg.setStyle(
                "-fx-font-size: 10px; -fx-text-fill: rgba(26,31,60,0.65);" +
                        "-fx-font-weight: 500;");

        Label pctLabel = new Label(pourcentage + "%");
        pctLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #1a1f3c; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox labelRow = new HBox(labelProg, spacer, pctLabel);
        labelRow.setAlignment(Pos.CENTER_LEFT);

        HBox fond = new HBox();
        fond.setPrefHeight(6);
        fond.setMaxWidth(Double.MAX_VALUE);
        fond.setStyle("-fx-background-color: rgba(255,255,255,0.35); -fx-background-radius: 10;");

        HBox barre = new HBox();
        barre.setPrefHeight(6);
        barre.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        HBox reste = new HBox();
        reste.setPrefHeight(6);
        HBox.setHgrow(reste, Priority.ALWAYS);
        reste.setStyle("-fx-background-color: transparent;");

        fond.widthProperty().addListener((obs, oldW, newW) ->
                barre.setPrefWidth(newW.doubleValue() * progression));

        fond.getChildren().addAll(barre, reste);

        VBox box = new VBox(5, labelRow, fond);
        box.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    // ══════════════════════════════════════════════
    //  CARTE COURS
    // ══════════════════════════════════════════════
    private VBox creerCarte(CoursData c) {
        VBox card = new VBox(0);
        card.setPrefWidth(360);
        card.setMaxWidth(360);
        card.setStyle(styleCarteNormal());

        // Bouton Favori
        boolean estFavori = favoris.contains(c.id);
        Button btnFavori = new Button(estFavori ? "♥" : "♡");
        btnFavori.setStyle(buildFavoriStyle(estFavori));
        btnFavori.setOnAction(e -> {
            boolean nowFavori;
            if (favoris.contains(c.id)) { favoris.remove(c.id); nowFavori = false; }
            else                        { favoris.add(c.id);    nowFavori = true;  }
            btnFavori.setText(nowFavori ? "♥" : "♡");
            btnFavori.setStyle(buildFavoriStyle(nowFavori));
            ScaleTransition st = new ScaleTransition(Duration.millis(150), btnFavori);
            st.setFromX(1.0); st.setFromY(1.0);
            st.setToX(1.4);   st.setToY(1.4);
            st.setAutoReverse(true); st.setCycleCount(2); st.play();
            sauvegarderFavoris();
            mettreAJourBtnFavoris();
            if (afficherFavoris) {
                pageActuelle = 0;
                filtrerEtTrier(texteRecherche, critereRecherche);
            }
        });

        // Header
        Label titreLabel = new Label(c.titre != null ? c.titre : "");
        titreLabel.setStyle(
                "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1a1f3c;");
        titreLabel.setMaxWidth(Double.MAX_VALUE);
        titreLabel.setWrapText(true);
        HBox.setHgrow(titreLabel, Priority.ALWAYS);

        HBox titreEtFavori = new HBox(titreLabel, btnFavori);
        titreEtFavori.setAlignment(Pos.CENTER_LEFT);

        VBox headerContent = new VBox(10, titreEtFavori, barreProgression(c.id));
        headerContent.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(headerContent, Priority.ALWAYS);

        HBox header = new HBox(headerContent);
        header.setMaxWidth(Double.MAX_VALUE);
        header.setStyle(
                "-fx-background-color: #f5a623;" +
                        "-fx-background-radius: 14 14 0 0;" +
                        "-fx-padding: 16 18 14 18;");

        // Corps
        VBox body = new VBox(10);
        body.setStyle("-fx-padding: 16 18 10 18;");
        VBox.setVgrow(body, Priority.ALWAYS);

        Label desc = new Label(c.description != null ? c.description : "");
        desc.setWrapText(true);
        desc.setStyle("-fx-font-size: 13px; -fx-text-fill: #7a849e;");

        HBox badgesRow = new HBox(6);
        badgesRow.getChildren().addAll(
                badge(c.niveau,  "rgba(26,188,156,0.12)", "#0d8c71"),
                badge(c.matiere, "rgba(26,31,60,0.08)",   "#1a1f3c"),
                badge(c.langue,  "rgba(245,166,35,0.12)", "#c47d00")
        );

        body.getChildren().addAll(desc, badgesRow, chapitresBox(c.id));

        // Footer
        Button btn = new Button("Accéder Au Cours  →");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle(styleBtnAccederNormal());
        btn.setOnMouseEntered(e -> btn.setStyle(styleBtnAccederHover()));
        btn.setOnMouseExited(e  -> btn.setStyle(styleBtnAccederNormal()));
        btn.setOnAction(e -> ouvrirCours(
                c.id, c.titre, c.niveau, c.matiere, c.langue, c.description));

        VBox footer = new VBox(btn);
        footer.setStyle("-fx-padding: 10 18 16 18;");

        card.setOnMouseEntered(e -> card.setStyle(styleCarteHover()));
        card.setOnMouseExited(e  -> card.setStyle(styleCarteNormal()));

        card.getChildren().addAll(header, body, footer);
        return card;
    }

    private String styleCarteNormal() {
        return "-fx-background-color: white; -fx-background-radius: 14;" +
                "-fx-border-color: #e4e8f0; -fx-border-radius: 14;" +
                "-fx-effect: dropshadow(gaussian, rgba(26,31,60,0.07), 8, 0, 0, 2);";
    }

    private String styleCarteHover() {
        return "-fx-background-color: white; -fx-background-radius: 14;" +
                "-fx-border-color: #f5a623; -fx-border-radius: 14; -fx-border-width: 1.5;" +
                "-fx-effect: dropshadow(gaussian, rgba(245,166,35,0.25), 20, 0, 0, 6);";
    }

    private String styleBtnAccederNormal() {
        return "-fx-background-color: #1a1f3c; -fx-text-fill: white;" +
                "-fx-font-weight: bold; -fx-background-radius: 10;" +
                "-fx-font-size: 13px; -fx-padding: 11; -fx-cursor: hand;";
    }

    private String styleBtnAccederHover() {
        return "-fx-background-color: #252c55; -fx-text-fill: white;" +
                "-fx-font-weight: bold; -fx-background-radius: 10;" +
                "-fx-font-size: 13px; -fx-padding: 11; -fx-cursor: hand;";
    }

    private String buildFavoriStyle(boolean actif) {
        return actif
                ? "-fx-background-color: #ff4d6d; -fx-text-fill: white;" +
                "-fx-background-radius: 50; -fx-font-size: 15px;" +
                "-fx-padding: 4 10; -fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(255,77,109,0.45), 6, 0, 0, 2);"
                : "-fx-background-color: rgba(255,255,255,0.3); -fx-text-fill: #1a1f3c;" +
                "-fx-background-radius: 50; -fx-font-size: 15px;" +
                "-fx-padding: 4 10; -fx-cursor: hand;";
    }

    // ══════════════════════════════════════════════
    //  BADGE
    // ══════════════════════════════════════════════
    private Label badge(String valeur, String bg, String fg) {
        Label bdg = new Label(valeur != null ? valeur.toUpperCase() : "-");
        bdg.setStyle(
                "-fx-background-color: " + bg + ";" +
                        "-fx-text-fill: " + fg + ";" +
                        "-fx-background-radius: 20; -fx-padding: 4 12;" +
                        "-fx-font-size: 10px; -fx-font-weight: bold;");
        return bdg;
    }

    // ══════════════════════════════════════════════
    //  CHAPITRES
    // ══════════════════════════════════════════════
    private VBox chapitresBox(int coursId) {
        VBox box = new VBox(6);
        try {
            List<chapitres> chaps = chapitresService.afficher()
                    .stream().filter(ch -> ch.getCours_id() == coursId)
                    .collect(Collectors.toList());

            HBox rowChap = new HBox(8);
            rowChap.setAlignment(Pos.CENTER_LEFT);
            Label keyChap = new Label("Chapitres :");
            keyChap.setStyle("-fx-font-size: 12px; -fx-text-fill: #7a849e;");
            Label valChap = new Label(String.valueOf(chaps.size()));
            valChap.setStyle(
                    "-fx-background-color: " + (chaps.isEmpty() ? "#e74c3c" : "#1abc9c") + ";" +
                            "-fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold;" +
                            "-fx-background-radius: 12; -fx-padding: 2 10;");
            rowChap.getChildren().addAll(keyChap, valChap);
            box.getChildren().add(rowChap);

            if (chaps.isEmpty()) {
                Label noChap = new Label("Aucun chapitre disponible");
                noChap.setStyle(
                        "-fx-font-style: italic; -fx-text-fill: #ccc; -fx-font-size: 12px;");
                box.getChildren().add(noChap);
            } else {
                Label contenu = new Label("Contenu du cours");
                contenu.setStyle(
                        "-fx-font-weight: bold; -fx-font-size: 12px;" +
                                "-fx-text-fill: #1a1f3c; -fx-padding: 4 0 2 0;");
                box.getChildren().add(contenu);

                for (int i = 0; i < Math.min(chaps.size(), 3); i++) {
                    chapitres ch = chaps.get(i);

                    Label numLabel = new Label(String.valueOf(i + 1));
                    numLabel.setStyle(
                            "-fx-background-color: #fff7e6; -fx-text-fill: #c47d00;" +
                                    "-fx-background-radius: 6; -fx-font-size: 9px;" +
                                    "-fx-font-weight: bold; -fx-padding: 3 7;" +
                                    "-fx-min-width: 22; -fx-alignment: center;");

                    Label lTitre = new Label(ch.getTitre_chap());
                    lTitre.setStyle(
                            "-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #1a1f3c;");
                    lTitre.setWrapText(true);

                    Label lInfo = new Label(
                            ch.getDuree_chap() + "  •  " + ch.getType_contenu().toUpperCase());
                    lInfo.setStyle("-fx-font-size: 10px; -fx-text-fill: #aaa;");

                    VBox chapInfo = new VBox(1, lTitre, lInfo);
                    HBox.setHgrow(chapInfo, Priority.ALWAYS);

                    HBox chapRow = new HBox(8, numLabel, chapInfo);
                    chapRow.setAlignment(Pos.CENTER_LEFT);
                    chapRow.setStyle(
                            "-fx-background-color: #f0f2f8;" +
                                    "-fx-background-radius: 8; -fx-padding: 7 10;");
                    box.getChildren().add(chapRow);
                }

                if (chaps.size() > 3) {
                    Label plus = new Label("+ " + (chaps.size() - 3) + " autre(s) chapitre(s)...");
                    plus.setStyle(
                            "-fx-font-size: 10px; -fx-text-fill: #f5a623;" +
                                    "-fx-font-weight: 600; -fx-padding: 2 0 0 4;");
                    box.getChildren().add(plus);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return box;
    }

    // ══════════════════════════════════════════════
    //  DONNÉES COURS
    // ══════════════════════════════════════════════
    private static class CoursData {
        int id;
        String titre, niveau, matiere, langue, description;

        CoursData(int id, String titre, String niveau,
                  String matiere, String langue, String description) {
            this.id          = id;
            this.titre       = titre;
            this.niveau      = niveau;
            this.matiere     = matiere;
            this.langue      = langue;
            this.description = description;
        }
    }

    // ══════════════════════════════════════════════
    //  NAVIGATION
    // ══════════════════════════════════════════════



    @FXML
    public void navigateToProfile(javafx.scene.input.MouseEvent event) {
        try {
            System.out.println("Navigation vers Profil...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/StudentView.fxml"));
            Parent root = loader.load();
            ((javafx.scene.Node) event.getSource()).getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void ouvrirCours(int coursId, String titre, String niveau,
                             String matiere, String langue, String description) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/CoursDetailEtudiant.fxml"));
            Stage stage = (Stage) coursContainer.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle(titre);
            CoursDetailEtudiantController ctrl = loader.getController();
            ctrl.setCours(coursId, titre, niveau, matiere, langue, description);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    public void ouvrirCertifications(javafx.scene.input.MouseEvent event) {
        try {
            System.out.println("Navigation vers Certifications...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/CertificationsEtudiant.fxml"));
            Parent root = loader.load();
            ((javafx.scene.Node) event.getSource()).getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleForumButton(ActionEvent event) {
        try {
            System.out.println("Ouverture du Forum...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main.fxml"));
            Parent root = loader.load();
            
            MainController controller = loader.getController();
            User currentUser = Session.getCurrentUser();
            controller.setCurrentUser(currentUser);
            AppContext.setCurrentUser(currentUser);
            
            ((javafx.scene.Node) event.getSource()).getScene().setRoot(root);
        } catch (Throwable t) {
            t.printStackTrace();
            System.err.println("FORUM ERROR: " + t.getMessage());
        }
    }

    @FXML
    public void retourChoixEspace(javafx.scene.input.MouseEvent event) {
        try {
            System.out.println("Retour à l'accueil...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainView.fxml"));
            Parent root = loader.load();
            ((javafx.scene.Node) event.getSource()).getScene().setRoot(root);
        } catch (Exception e) {
            try {
                Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
                new org.example.MainFX().start(stage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @FXML
    public void navigateToEvents(javafx.scene.input.MouseEvent event) {
        try {
            System.out.println("Navigation vers Événements...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Student.fxml"));
            Parent root = loader.load();
            ((javafx.scene.Node) event.getSource()).getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}