package org.example.controllers;

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
import org.example.entities.bourses;
import org.example.entities.demande;
import org.example.services.boursesService;
import org.example.services.demandeService;
import org.example.entities.Session;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class StatistiquesController implements Initializable {

    @FXML private Label lblTotalBourses;
    @FXML private Label lblTotalDemandes;
    @FXML private Label lblTauxAcceptation;
    @FXML private Label lblMontantMoyen;
    @FXML private Label lblMontantTotal;

    @FXML private PieChart pieStatut;
    @FXML private BarChart<String, Number> barMontants;
    @FXML private PieChart pieNiveau;
    @FXML private BarChart<String, Number> barDemandesParBourse;

    private boursesService bService = new boursesService();
    private demandeService dService = new demandeService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        List<bourses> listeBourses = bService.getAll();
        List<demande> listeDemandes = dService.getAll();

        calculerCartes(listeBourses, listeDemandes);
        remplirPieStatut(listeDemandes);
        remplirBarMontants(listeBourses);
        remplirPieNiveau(listeDemandes);
        remplirBarDemandesParBourse(listeDemandes, listeBourses);
    }

    private void calculerCartes(List<bourses> listeBourses, List<demande> listeDemandes) {
        int totalBourses = listeBourses.size();
        int totalDemandes = listeDemandes.size();

        double montantTotal = 0;
        for (bourses b : listeBourses) {
            montantTotal += b.getMontant();
        }
        double montantMoyen = totalBourses > 0 ? montantTotal / totalBourses : 0;

        long nbAcceptees = 0;
        for (demande d : listeDemandes) {
            if ("Acceptee".equalsIgnoreCase(d.getStatut())) {
                nbAcceptees++;
            }
        }
        double tauxAcceptation = totalDemandes > 0 ? (nbAcceptees * 100.0 / totalDemandes) : 0;

        lblTotalBourses.setText(String.valueOf(totalBourses));
        lblTotalDemandes.setText(String.valueOf(totalDemandes));
        lblTauxAcceptation.setText(String.format("%.0f%%", tauxAcceptation));
        lblMontantMoyen.setText(String.format("%.0f DT", montantMoyen));
        lblMontantTotal.setText(String.format("%.0f DT", montantTotal));
    }

    private void remplirPieStatut(List<demande> listeDemandes) {
        Map<String, Integer> statutCount = new HashMap<>();
        for (demande d : listeDemandes) {
            String statut = d.getStatut() != null ? d.getStatut() : "Inconnu";
            statutCount.put(statut, statutCount.getOrDefault(statut, 0) + 1);
        }

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        for (Map.Entry<String, Integer> entry : statutCount.entrySet()) {
            pieData.add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
        }

        pieStatut.setData(pieData);
    }

    private void remplirBarMontants(List<bourses> listeBourses) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Montant");

        for (bourses b : listeBourses) {
            String titre = b.getTitre().length() > 15
                    ? b.getTitre().substring(0, 13) + ".."
                    : b.getTitre();
            series.getData().add(new XYChart.Data<>(titre, b.getMontant()));
        }

        barMontants.getData().add(series);
    }

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

    private void remplirBarDemandesParBourse(List<demande> listeDemandes, List<bourses> listeBourses) {
        Map<Integer, String> bourseTitres = new HashMap<>();
        for (bourses b : listeBourses) {
            bourseTitres.put(b.getId(), b.getTitre());
        }

        Map<Integer, Integer> demandeCount = new HashMap<>();
        for (demande d : listeDemandes) {
            demandeCount.put(d.getBourse_id(), demandeCount.getOrDefault(d.getBourse_id(), 0) + 1);
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Demandes");

        for (Map.Entry<Integer, Integer> entry : demandeCount.entrySet()) {
            String titre = bourseTitres.getOrDefault(entry.getKey(), "Bourse #" + entry.getKey());
            if (titre.length() > 15) {
                titre = titre.substring(0, 13) + "..";
            }
            series.getData().add(new XYChart.Data<>(titre, entry.getValue()));
        }

        barDemandesParBourse.getData().add(series);
    }

    @FXML
    private void retourBourses(ActionEvent event) { naviguerVers("/org/example/bourses/ListeBourses.fxml"); }
    @FXML
    private void allerDemandes(ActionEvent event) { naviguerVers("/org/example/demandes/ListeDemandes.fxml"); }
    @FXML
    private void allerInterviewIA(ActionEvent event) { naviguerVers("/org/example/demandes/ListeInterviews.fxml"); }
    @FXML
    private void allerAccueil(ActionEvent event) {
        try {
            Session.logout();
        } catch (Exception e) {
            {
                return;
            }
        }
    }
    private void naviguerVers(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            pieStatut.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
