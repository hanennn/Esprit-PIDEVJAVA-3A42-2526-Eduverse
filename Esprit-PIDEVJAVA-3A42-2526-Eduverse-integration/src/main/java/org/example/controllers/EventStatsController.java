package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.example.services.EventInscriptionService;
import org.example.services.EventService;

import java.util.Map;

public class EventStatsController {

    @FXML private Label totalEventsLabel;
    @FXML private Label totalInscriptionsLabel;
    @FXML private StackPane pieChartPane;
    @FXML private StackPane barChartPane;

    private EventService eventService;
    private EventInscriptionService inscriptionService;

    @FXML
    public void initialize() {
        eventService = new EventService();
        inscriptionService = new EventInscriptionService();

        loadStats();
    }

    private void loadStats() {
        int totalE = eventService.getAll().size();
        int totalI = inscriptionService.getTotalInscriptions();

        totalEventsLabel.setText(String.valueOf(totalE));
        totalInscriptionsLabel.setText(String.valueOf(totalI));

        // PieChart Status
        Map<String, Integer> statusData = inscriptionService.getInscriptionCountByStatus();
        PieChart pieChart = new PieChart();
        statusData.forEach((statut, count) -> {
            pieChart.getData().add(new PieChart.Data(statut, count));
        });
        pieChartPane.getChildren().add(pieChart);

        // BarChart Top Events
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Top 5 Participation");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Inscriptions");

        eventService.getHistoricalData().stream()
                .sorted((a, b) -> Integer.compare((int) b.get("nbInscrits"), (int) a.get("nbInscrits")))
                .limit(5)
                .forEach(row -> {
                    series.getData().add(new XYChart.Data<>((String) row.get("titre"), (Integer) row.get("nbInscrits")));
                });

        barChart.getData().add(series);
        barChartPane.getChildren().add(barChart);
    }
}
