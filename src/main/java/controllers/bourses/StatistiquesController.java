package controllers.bourses;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import models.bourses;
import models.demande;
import services.boursesService;
import services.demandeService;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

// Controleur du dashboard statistiques admin
// Affiche des cartes chiffrees + 4 graphiques :
// 1. PieChart : demandes par statut (En attente / Acceptee / Refusee)
// 2. BarChart : montant de chaque bourse
// 3. PieChart : demandes par niveau d'etudes
// 4. BarChart : nombre de demandes par bourse
public class StatistiquesController implements Initializable {

    // --- Cartes chiffrees ---
    @FXML
    private Label lblTotalBourses;      // Nombre total de bourses
    @FXML
    private Label lblTotalDemandes;     // Nombre total de demandes
    @FXML
    private Label lblTauxAcceptation;   // Pourcentage de demandes acceptees
    @FXML
    private Label lblMontantMoyen;      // Montant moyen des bourses
    @FXML
    private Label lblMontantTotal;      // Somme de tous les montants

    // --- Graphiques ---
    @FXML
    private PieChart pieStatut;          // Repartition des demandes par statut
    @FXML
    private BarChart<String, Number> barMontants;  // Montant de chaque bourse
    @FXML
    private PieChart pieNiveau;          // Repartition des demandes par niveau d'etudes
    @FXML
    private BarChart<String, Number> barDemandesParBourse;  // Nombre de demandes par bourse

    private boursesService bService = new boursesService();
    private demandeService dService = new demandeService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Charger les donnees depuis la base
        List<bourses> listeBourses = bService.getAll();
        List<demande> listeDemandes = dService.getAll();

        // Remplir les cartes chiffrees
        calculerCartes(listeBourses, listeDemandes);

        // Remplir les graphiques
        remplirPieStatut(listeDemandes);
        remplirBarMontants(listeBourses);
        remplirPieNiveau(listeDemandes);
        remplirBarDemandesParBourse(listeDemandes, listeBourses);
    }

    // Calculer et afficher les chiffres dans les cartes du haut
    private void calculerCartes(List<bourses> listeBourses, List<demande> listeDemandes) {
        int totalBourses = listeBourses.size();
        int totalDemandes = listeDemandes.size();

        // Calculer le montant total et moyenne des bourses
        double montantTotal = 0;
        for (bourses b : listeBourses) {
            montantTotal += b.getMontant();
        }
        double montantMoyen = totalBourses > 0 ? montantTotal / totalBourses : 0;

        // Compter les demandes acceptees pour le taux d'acceptation
        long nbAcceptees = 0;
        for (demande d : listeDemandes) {
            if ("Acceptee".equalsIgnoreCase(d.getStatut())) {
                nbAcceptees++;
            }
        }
        double tauxAcceptation = totalDemandes > 0 ? (nbAcceptees * 100.0 / totalDemandes) : 0;

        // Afficher dans les labels
        lblTotalBourses.setText(String.valueOf(totalBourses));
        lblTotalDemandes.setText(String.valueOf(totalDemandes));
        lblTauxAcceptation.setText(String.format("%.0f%%", tauxAcceptation));
        lblMontantMoyen.setText(String.format("%.0f DT", montantMoyen));
        lblMontantTotal.setText(String.format("%.0f DT", montantTotal));
    }

    // PieChart : repartition des demandes par statut
    // Compte combien de demandes ont chaque statut (En attente, Acceptee, Refusee)
    private void remplirPieStatut(List<demande> listeDemandes) {
        // Compter les demandes par statut
        Map<String, Integer> statutCount = new HashMap<>();
        for (demande d : listeDemandes) {
            String statut = d.getStatut() != null ? d.getStatut() : "Inconnu";
            statutCount.put(statut, statutCount.getOrDefault(statut, 0) + 1);
        }

        // Creer les portions du PieChart
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        for (Map.Entry<String, Integer> entry : statutCount.entrySet()) {
            pieData.add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
        }

        pieStatut.setData(pieData);
    }

    // BarChart : afficher le montant de chaque bourse sous forme de barres
    private void remplirBarMontants(List<bourses> listeBourses) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Montant");

        for (bourses b : listeBourses) {
            // Tronquer le titre si trop long pour l'axe X
            String titre = b.getTitre().length() > 15
                    ? b.getTitre().substring(0, 13) + ".."
                    : b.getTitre();
            series.getData().add(new XYChart.Data<>(titre, b.getMontant()));
        }

        barMontants.getData().add(series);
    }

    // PieChart : repartition des demandes par niveau d'etudes
    // Compte combien de demandes proviennent de chaque niveau (Licence, Master, etc.)
    private void remplirPieNiveau(List<demande> listeDemandes) {
        Map<String, Integer> niveauCount = new HashMap<>();
        for (demande d : listeDemandes) {
            String niveau = d.getNiveau_etudes() != null ? d.getNiveau_etudes() : "Inconnu";
            niveauCount.put(niveau, niveauCount.getOrDefault(niveau, 0) + 1);
        }

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        for (Map.Entry<String, Integer> entry : niveauCount.entrySet()) {
            pieData.add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
        }

        pieNiveau.setData(pieData);
    }

    // BarChart : nombre de demandes recues pour chaque bourse
    // Permet de voir quelles bourses sont les plus demandees
    private void remplirBarDemandesParBourse(List<demande> listeDemandes, List<bourses> listeBourses) {
        // Creer un index id -> titre pour retrouver le nom de la bourse
        Map<Integer, String> bourseTitres = new HashMap<>();
        for (bourses b : listeBourses) {
            bourseTitres.put(b.getId(), b.getTitre());
        }

        // Compter les demandes par bourse_id
        Map<Integer, Integer> demandeCount = new HashMap<>();
        for (demande d : listeDemandes) {
            demandeCount.put(d.getBourse_id(), demandeCount.getOrDefault(d.getBourse_id(), 0) + 1);
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Demandes");

        for (Map.Entry<Integer, Integer> entry : demandeCount.entrySet()) {
            String titre = bourseTitres.getOrDefault(entry.getKey(), "Bourse #" + entry.getKey());
            // Tronquer le titre si trop long
            if (titre.length() > 15) {
                titre = titre.substring(0, 13) + "..";
            }
            series.getData().add(new XYChart.Data<>(titre, entry.getValue()));
        }

        barDemandesParBourse.getData().add(series);
    }

    // Retour a la liste des bourses
    @FXML
    private void retourBourses(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/bourses/ListeBourses.fxml"));
            Parent root = loader.load();
            pieStatut.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
