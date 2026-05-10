package org.example.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import java.io.IOException;

public class EventDashboardController {

    @FXML private Button btnEvent;
    @FXML private Button btnInscri;
    @FXML private Button btnStats;
    @FXML private Button btnPdf;
    @FXML private Button btnCal;
    @FXML private Button btnIa;

    @FXML private VBox viewEvent;
    @FXML private VBox viewInscri;
    @FXML private VBox viewStats;
    @FXML private VBox viewPdf;
    @FXML private VBox viewCal;
    @FXML private VBox viewIa;

    @FXML private Label lblPagePath;

    @FXML
    public void initialize() {
        switchView("Event");
    }

    private void resetSidebar() {
        btnEvent.getStyleClass().setAll("button", "sidebar-btn");
        btnInscri.getStyleClass().setAll("button", "sidebar-btn");
        btnStats.getStyleClass().setAll("button", "sidebar-btn");
        btnPdf.getStyleClass().setAll("button", "sidebar-btn");
        btnCal.getStyleClass().setAll("button", "sidebar-btn");
        btnIa.getStyleClass().setAll("button", "sidebar-btn");
        btnIa.setStyle("-fx-text-fill: #efa515;");

        viewEvent.setVisible(false);
        viewInscri.setVisible(false);
        viewStats.setVisible(false);
        viewPdf.setVisible(false);
        viewCal.setVisible(false);
        viewIa.setVisible(false);
    }

    private void switchView(String viewName) {
        resetSidebar();
        switch(viewName) {
            case "Event":
                btnEvent.getStyleClass().setAll("button", "sidebar-btn-active");
                viewEvent.setVisible(true);
                lblPagePath.setText("🏠 Admin > Événements");
                break;
            case "Inscri":
                btnInscri.getStyleClass().setAll("button", "sidebar-btn-active");
                viewInscri.setVisible(true);
                lblPagePath.setText("🏠 Admin > Inscriptions");
                break;
            case "Stats":
                btnStats.getStyleClass().setAll("button", "sidebar-btn-active");
                viewStats.setVisible(true);
                lblPagePath.setText("🏠 Admin > Statistiques");
                break;
            case "Pdf":
                btnPdf.getStyleClass().setAll("button", "sidebar-btn-active");
                viewPdf.setVisible(true);
                lblPagePath.setText("🏠 Admin > Export PDF");
                break;
            case "Cal":
                btnCal.getStyleClass().setAll("button", "sidebar-btn-active");
                viewCal.setVisible(true);
                lblPagePath.setText("🏠 Admin > Calendrier");
                break;
            case "Ia":
                btnIa.getStyleClass().setAll("button", "sidebar-btn-active");
                btnIa.setStyle("-fx-text-fill: white;");
                viewIa.setVisible(true);
                lblPagePath.setText("🏠 Admin > Assistant IA");
                break;
        }
    }

    @FXML private void showEvents(ActionEvent e) { switchView("Event"); }
    @FXML private void showInscriptions(ActionEvent e) { switchView("Inscri"); }
    @FXML private void showStats(ActionEvent e) { switchView("Stats"); }
    @FXML private void showPdf(ActionEvent e) { switchView("Pdf"); }
    @FXML private void showCalendar(ActionEvent e) { switchView("Cal"); }
    @FXML private void showIa(ActionEvent e) { switchView("Ia"); }

    @FXML
    private void goHome(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Home.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Eduverse - Accueil");
            stage.setScene(new Scene(root, 1050, 700));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
