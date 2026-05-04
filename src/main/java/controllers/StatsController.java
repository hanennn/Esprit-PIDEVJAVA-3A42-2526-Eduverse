package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.embed.swing.SwingNode;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import services.EventService;
import services.EventInscriptionService;
import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.util.Map;

public class StatsController {

    @FXML private Label totalEventsLabel;
    @FXML private Label totalInscriptionsLabel;
    @FXML private StackPane barChartPane;
    @FXML private StackPane pieChartPane;

    private final EventService eventService = new EventService();
    private final EventInscriptionService inscriptionService = new EventInscriptionService();

    @FXML
    public void initialize() {
        loadKeyFigures();
        loadBarChart();
        loadPieChart();
    }

    private void loadKeyFigures() {
        totalEventsLabel.setText(String.valueOf(eventService.getTotalEvents()));
        totalInscriptionsLabel.setText(String.valueOf(inscriptionService.getTotalInscriptions()));
    }

    private void loadBarChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Map<String, Integer> data = eventService.getParticipantCountPerEvent();
        
        data.forEach((titre, count) -> {
            dataset.addValue(count, "Participants", titre);
        });

        JFreeChart chart = ChartFactory.createBarChart(
                null,                   // Title (we have our own label in FXML)
                "Événements",           // Category axis label
                "Nombre",               // Value axis label
                dataset,                // Data
                PlotOrientation.VERTICAL,
                false,                   // Legend
                true,                   // Tooltips
                false                   // URLs
        );

        displayChart(chart, barChartPane);
    }

    private void loadPieChart() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        Map<String, Integer> data = inscriptionService.getInscriptionCountByStatus();

        if (data.isEmpty()) {
            dataset.setValue("Aucune donnée", 1);
        } else {
            data.forEach(dataset::setValue);
        }

        JFreeChart chart = ChartFactory.createPieChart(
                null,                   // Title
                dataset,                // Data
                true,                   // Legend
                true,                   // Tooltips
                false                   // URLs
        );

        displayChart(chart, pieChartPane);
    }

    private void displayChart(JFreeChart chart, StackPane pane) {
        SwingNode swingNode = new SwingNode();
        SwingUtilities.invokeLater(() -> {
            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new Dimension(500, 400));
            swingNode.setContent(chartPanel);
        });
        pane.getChildren().add(swingNode);
    }
}
