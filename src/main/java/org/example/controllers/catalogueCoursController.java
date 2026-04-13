package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.entities.chapitres;
import org.example.entities.cours;
import org.example.services.chapitresservices;
import org.example.services.coursservices;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class catalogueCoursController {

    @FXML private VBox listContainer;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> critereRecherche;
    @FXML private ComboBox<String> trierPar;

    private List<cours> tousLesCours;

    @FXML
    //recup cours
    public void initialize() throws SQLException {
        tousLesCours = new coursservices().afficher();
        critereRecherche.getItems().addAll("Titre", "Matière", "Niveau", "Langue");
        critereRecherche.setValue("Titre");
        trierPar.getItems().addAll("Titre", "Niveau", "Langue", "Matière");
        trierPar.setValue("Titre");
        afficherCours(tousLesCours);
    }

    @FXML
    void filtrerCours() {
        String recherche = searchField.getText().toLowerCase().trim();
        String critere = critereRecherche.getValue();
        String tri = trierPar.getValue();

        List<cours> filtre = tousLesCours.stream()
                .filter(c -> {
                    if (recherche.isEmpty()) return true; //rien donc aff tous les cours
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

    private String safe(String s) { return s != null ? s : ""; } // pour val recherchée null

    private void afficherCours(List<cours> liste) {
        listContainer.getChildren().clear();
        for (cours c : liste)
            listContainer.getChildren().add(creerCarte(c));
    }

    private VBox creerCarte(cours c) {
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10;" +
                "-fx-border-color: #e0e0e0; -fx-border-radius: 10;");

        // Header orange
        Label titre = new Label(c.getTitre_cours());
        titre.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1a1a2e;");
        VBox header = new VBox(titre);
        header.setStyle("-fx-background-color: #f5a623; " +
                "-fx-background-radius: 10 10 0 0; -fx-padding: 12 16;");

        // Body
        VBox body = new VBox(8);
        body.setStyle("-fx-padding: 14 16;");
        Label desc = new Label(c.getDescription());
        desc.setWrapText(true);
        desc.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");
        body.getChildren().addAll(desc,
                badge("Niveau :",  c.getNiv_cours(),    "#1abc9c"),
                badge("Matière :", c.getMatiere_cours(), "#2c3e50"),
                badge("Langue :",  c.getLangue_cours(),  "#f5a623"),
                chapitresBox(c));

        // Footer bouton lié à voirChapitres
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
//list chaps liée aux cours
    private void voirChapitres(cours c) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/catalogueChapitres.fxml"));

            Parent root = loader.load();// charge chap

            catalogueChapitresController controller = loader.getController(); // ← recup controller

            controller.setCours(c); //passe cours

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
//badge pour matiere langue niveau
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