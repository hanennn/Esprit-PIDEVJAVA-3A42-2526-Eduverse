package controllers.etudiant;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import models.bourses;
import services.boursesService;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class CalendrierBoursesController implements Initializable {

    @FXML
    private GridPane gridCalendrier;
    @FXML
    private Label lblMoisAnnee;

    private boursesService service = new boursesService();
    private List<bourses> toutesLesBourses;
    private YearMonth moisCourant;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        toutesLesBourses = service.getAll();
        moisCourant = YearMonth.now();
        afficherCalendrier();
    }

    @FXML
    private void moisPrecedent(ActionEvent event) {
        moisCourant = moisCourant.minusMonths(1);
        afficherCalendrier();
    }

    @FXML
    private void moisSuivant(ActionEvent event) {
        moisCourant = moisCourant.plusMonths(1);
        afficherCalendrier();
    }

    private void afficherCalendrier() {
        gridCalendrier.getChildren().clear();

        String nomMois = moisCourant.getMonth().getDisplayName(TextStyle.FULL, Locale.FRANCE);
        nomMois = nomMois.substring(0, 1).toUpperCase() + nomMois.substring(1);
        lblMoisAnnee.setText(nomMois + " " + moisCourant.getYear());

        // En-tete des jours de la semaine
        String[] joursNoms = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"};
        for (int col = 0; col < 7; col++) {
            Label lblJour = new Label(joursNoms[col]);
            lblJour.setStyle("-fx-font-weight: bold; -fx-font-size: 13; -fx-text-fill: #2c3e50;");
            lblJour.setPrefWidth(108);
            lblJour.setAlignment(Pos.CENTER);
            gridCalendrier.add(lblJour, col, 0);
        }

        LocalDate premierJour = moisCourant.atDay(1);
        int nbJours = moisCourant.lengthOfMonth();
        // MONDAY=1 ... SUNDAY=7, on veut Lundi = colonne 0
        int decalage = premierJour.getDayOfWeek().getValue() - 1;

        for (int jour = 1; jour <= nbJours; jour++) {
            LocalDate dateJour = moisCourant.atDay(jour);
            int col = (decalage + jour - 1) % 7;
            int row = (decalage + jour - 1) / 7 + 1;

            VBox cellule = creerCelluleJour(jour, dateJour);
            gridCalendrier.add(cellule, col, row);
        }
    }

    private VBox creerCelluleJour(int jour, LocalDate dateJour) {
        VBox cellule = new VBox(2);
        cellule.setPrefWidth(108);
        cellule.setPrefHeight(70);
        cellule.setAlignment(Pos.TOP_CENTER);

        String styleBase = "-fx-background-color: white; -fx-background-radius: 8; "
                + "-fx-border-color: #ddd; -fx-border-radius: 8; -fx-padding: 3;";

        Label lblNumero = new Label(String.valueOf(jour));
        lblNumero.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        if (dateJour.equals(LocalDate.now())) {
            styleBase = "-fx-background-color: #ffeaa7; -fx-background-radius: 8; "
                    + "-fx-border-color: #f39c12; -fx-border-radius: 8; -fx-padding: 3;";
        }

        cellule.setStyle(styleBase);
        cellule.getChildren().add(lblNumero);

        StringBuilder tooltipText = new StringBuilder();

        for (bourses b : toutesLesBourses) {
            LocalDate dateAttr = b.getDate_attribution() != null
                    ? b.getDate_attribution().toLocalDateTime().toLocalDate() : null;
            LocalDate dateFin = b.getDate_fin() != null
                    ? b.getDate_fin().toLocalDateTime().toLocalDate() : null;

            // Vert = debut, Rouge = fin, Bleu = en cours
            if (dateAttr != null && dateAttr.equals(dateJour)) {
                Label tag = creerEtiquette(tronquer(b.getTitre(), 12), "#2ecc71");
                cellule.getChildren().add(tag);
                tooltipText.append("Debut : ").append(b.getTitre())
                        .append(" (").append(b.getMontant()).append(" DT)\n");
            }

            if (dateFin != null && dateFin.equals(dateJour)) {
                Label tag = creerEtiquette(tronquer(b.getTitre(), 12), "#e74c3c");
                cellule.getChildren().add(tag);
                tooltipText.append("Fin : ").append(b.getTitre())
                        .append(" (").append(b.getMontant()).append(" DT)\n");
            }

            if (dateAttr != null && dateFin != null
                    && dateJour.isAfter(dateAttr) && dateJour.isBefore(dateFin)) {
                // afficher seulement le lundi pour ne pas surcharger
                if (dateJour.getDayOfWeek().getValue() == 1) {
                    Label tag = creerEtiquette(tronquer(b.getTitre(), 12), "#3498db");
                    cellule.getChildren().add(tag);
                }
                tooltipText.append("En cours : ").append(b.getTitre())
                        .append(" (").append(b.getMontant()).append(" DT)\n");
            }
        }

        if (tooltipText.length() > 0) {
            Tooltip tooltip = new Tooltip(tooltipText.toString().trim());
            tooltip.setStyle("-fx-font-size: 12;");
            Tooltip.install(cellule, tooltip);
            cellule.setStyle(cellule.getStyle() + " -fx-cursor: hand;");
        }

        return cellule;
    }

    private Label creerEtiquette(String texte, String couleur) {
        Label tag = new Label(texte);
        tag.setStyle("-fx-background-color: " + couleur + "; -fx-text-fill: white; "
                + "-fx-padding: 1 4; -fx-background-radius: 5; -fx-font-size: 9;");
        tag.setMaxWidth(100);
        return tag;
    }

    private String tronquer(String texte, int maxLength) {
        if (texte == null) return "";
        if (texte.length() <= maxLength) return texte;
        return texte.substring(0, maxLength - 2) + "..";
    }

    @FXML
    private void retourBourses(ActionEvent event) { naviguerVers("/etudiant/EtudiantBourses.fxml"); }
    @FXML
    private void allerInterviewIA(ActionEvent event) { naviguerVers("/etudiant/InterviewEtudiant.fxml"); }
    @FXML
    private void allerAccueil(ActionEvent event) { naviguerVers("/accueil/Accueil.fxml"); }

    private void naviguerVers(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            gridCalendrier.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
