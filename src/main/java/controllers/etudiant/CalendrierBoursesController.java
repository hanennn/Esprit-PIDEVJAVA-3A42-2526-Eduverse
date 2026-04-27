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

// Controleur du calendrier mensuel des bourses
// Affiche une grille 7 colonnes (Lun-Dim) avec les jours du mois
// Les jours sont colores selon les bourses :
//   - Vert : date d'attribution d'une bourse
//   - Rouge : date de fin d'une bourse
//   - Bleu : bourse en cours (entre attribution et fin)
public class CalendrierBoursesController implements Initializable {

    @FXML
    private GridPane gridCalendrier;  // Grille du calendrier (7 colonnes x 7 lignes)
    @FXML
    private Label lblMoisAnnee;       // Affiche "Avril 2026" par exemple

    private boursesService service = new boursesService();
    private List<bourses> toutesLesBourses;

    // Mois actuellement affiche dans le calendrier
    private YearMonth moisCourant;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Charger toutes les bourses depuis la base de donnees
        toutesLesBourses = service.getAll();
        // Commencer par le mois actuel
        moisCourant = YearMonth.now();
        // Construire le calendrier
        afficherCalendrier();
    }

    // Naviguer vers le mois precedent
    @FXML
    private void moisPrecedent(ActionEvent event) {
        moisCourant = moisCourant.minusMonths(1);
        afficherCalendrier();
    }

    // Naviguer vers le mois suivant
    @FXML
    private void moisSuivant(ActionEvent event) {
        moisCourant = moisCourant.plusMonths(1);
        afficherCalendrier();
    }

    // Construire et afficher la grille du calendrier pour le mois courant
    private void afficherCalendrier() {
        // Vider la grille avant de la reconstruire
        gridCalendrier.getChildren().clear();

        // Mettre a jour le label du mois et de l'annee (ex: "Avril 2026")
        String nomMois = moisCourant.getMonth().getDisplayName(TextStyle.FULL, Locale.FRANCE);
        // Mettre la premiere lettre en majuscule
        nomMois = nomMois.substring(0, 1).toUpperCase() + nomMois.substring(1);
        lblMoisAnnee.setText(nomMois + " " + moisCourant.getYear());

        // --- Ligne d'en-tete : jours de la semaine ---
        String[] joursNoms = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"};
        for (int col = 0; col < 7; col++) {
            Label lblJour = new Label(joursNoms[col]);
            lblJour.setStyle("-fx-font-weight: bold; -fx-font-size: 13; -fx-text-fill: #2c3e50;");
            lblJour.setPrefWidth(108);
            lblJour.setAlignment(Pos.CENTER);
            gridCalendrier.add(lblJour, col, 0);
        }

        // --- Remplir les jours du mois ---
        LocalDate premierJour = moisCourant.atDay(1);
        int nbJours = moisCourant.lengthOfMonth();
        // Decalage pour que Lundi = colonne 0 (getDayOfWeek() : MONDAY=1 ... SUNDAY=7)
        int decalage = premierJour.getDayOfWeek().getValue() - 1;

        for (int jour = 1; jour <= nbJours; jour++) {
            LocalDate dateJour = moisCourant.atDay(jour);
            int col = (decalage + jour - 1) % 7;
            int row = (decalage + jour - 1) / 7 + 1; // +1 car la ligne 0 = en-tete

            // Creer la cellule du jour
            VBox cellule = creerCelluleJour(jour, dateJour);
            gridCalendrier.add(cellule, col, row);
        }
    }

    // Creer une cellule pour un jour donne avec les couleurs des bourses
    private VBox creerCelluleJour(int jour, LocalDate dateJour) {
        VBox cellule = new VBox(2);
        cellule.setPrefWidth(108);
        cellule.setPrefHeight(70);
        cellule.setAlignment(Pos.TOP_CENTER);

        // Style de base de la cellule
        String styleBase = "-fx-background-color: white; -fx-background-radius: 8; "
                + "-fx-border-color: #ddd; -fx-border-radius: 8; -fx-padding: 3;";

        // Numero du jour
        Label lblNumero = new Label(String.valueOf(jour));
        lblNumero.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Mettre en evidence le jour actuel
        if (dateJour.equals(LocalDate.now())) {
            styleBase = "-fx-background-color: #ffeaa7; -fx-background-radius: 8; "
                    + "-fx-border-color: #f39c12; -fx-border-radius: 8; -fx-padding: 3;";
        }

        cellule.setStyle(styleBase);
        cellule.getChildren().add(lblNumero);

        // Chercher les bourses associees a ce jour
        StringBuilder tooltipText = new StringBuilder();

        for (bourses b : toutesLesBourses) {
            LocalDate dateAttr = b.getDate_attribution() != null
                    ? b.getDate_attribution().toLocalDateTime().toLocalDate() : null;
            LocalDate dateFin = b.getDate_fin() != null
                    ? b.getDate_fin().toLocalDateTime().toLocalDate() : null;

            // Jour = date d'attribution -> etiquette verte
            if (dateAttr != null && dateAttr.equals(dateJour)) {
                Label tag = creerEtiquette(tronquer(b.getTitre(), 12), "#2ecc71");
                cellule.getChildren().add(tag);
                tooltipText.append("Debut : ").append(b.getTitre())
                        .append(" (").append(b.getMontant()).append(" DT)\n");
            }

            // Jour = date de fin -> etiquette rouge
            if (dateFin != null && dateFin.equals(dateJour)) {
                Label tag = creerEtiquette(tronquer(b.getTitre(), 12), "#e74c3c");
                cellule.getChildren().add(tag);
                tooltipText.append("Fin : ").append(b.getTitre())
                        .append(" (").append(b.getMontant()).append(" DT)\n");
            }

            // Jour entre attribution et fin (bourse en cours) -> etiquette bleue
            if (dateAttr != null && dateFin != null
                    && dateJour.isAfter(dateAttr) && dateJour.isBefore(dateFin)) {
                // Pour eviter de surcharger le calendrier, afficher seulement le lundi de chaque semaine
                if (dateJour.getDayOfWeek().getValue() == 1) {
                    Label tag = creerEtiquette(tronquer(b.getTitre(), 12), "#3498db");
                    cellule.getChildren().add(tag);
                }
                tooltipText.append("En cours : ").append(b.getTitre())
                        .append(" (").append(b.getMontant()).append(" DT)\n");
            }
        }

        // Ajouter un tooltip avec les details si des bourses sont presentes ce jour
        if (tooltipText.length() > 0) {
            Tooltip tooltip = new Tooltip(tooltipText.toString().trim());
            tooltip.setStyle("-fx-font-size: 12;");
            Tooltip.install(cellule, tooltip);
            // Changer le curseur pour indiquer qu'il y a du contenu
            cellule.setStyle(cellule.getStyle() + " -fx-cursor: hand;");
        }

        return cellule;
    }

    // Creer une petite etiquette coloree avec le nom de la bourse
    private Label creerEtiquette(String texte, String couleur) {
        Label tag = new Label(texte);
        tag.setStyle("-fx-background-color: " + couleur + "; -fx-text-fill: white; "
                + "-fx-padding: 1 4; -fx-background-radius: 5; -fx-font-size: 9;");
        tag.setMaxWidth(100);
        return tag;
    }

    // Tronquer un texte trop long pour l'affichage dans une cellule
    private String tronquer(String texte, int maxLength) {
        if (texte == null) return "";
        if (texte.length() <= maxLength) return texte;
        return texte.substring(0, maxLength - 2) + "..";
    }

    // Retour a la liste des bourses etudiant
    @FXML
    private void retourBourses(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/etudiant/EtudiantBourses.fxml"));
            Parent root = loader.load();
            gridCalendrier.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
