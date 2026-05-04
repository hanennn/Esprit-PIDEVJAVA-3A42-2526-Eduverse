package org.example.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import org.example.entities.Quiz;
import org.example.entities.Session;
import org.example.services.QuizService;
import org.example.services.coursservices;

public class AdminQuizController {

    @FXML private Label                      lblNbQuiz;
    @FXML private Label                      lblTotalQuiz;
    @FXML private Label                      messageQuiz;

    @FXML private TableView<Quiz>            tableQuiz;
    @FXML private TableColumn<Quiz, String>  colTitre;
    @FXML private TableColumn<Quiz, String>  colType;
    @FXML private TableColumn<Quiz, Integer> colDuree;
    @FXML private TableColumn<Quiz, Float>   colScore;
    @FXML private TableColumn<Quiz, String>  colCours;

    private final QuizService   quizService  = new QuizService();
    private final coursservices coursService = new coursservices();

    // ─────────── INIT ───────────
    @FXML
    public void initialize() {

        // Cell value factories
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colType.setCellValueFactory(new PropertyValueFactory<>("typeQuiz"));
        colDuree.setCellValueFactory(new PropertyValueFactory<>("duree"));
        colScore.setCellValueFactory(new PropertyValueFactory<>("scoreMinimum"));
        colCours.setCellValueFactory(cell ->
                new SimpleStringProperty(
                        quizService.getNomCours(cell.getValue().getCoursAssocieId())));

        // Type badge
        colType.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); setStyle(""); }
                else {
                    Label badge = new Label(item);
                    badge.setStyle(item.equals("Final")
                            ? "-fx-background-color: #fff3e0; -fx-text-fill: #f5a623; -fx-font-weight: bold; -fx-font-size: 11px; -fx-background-radius: 20; -fx-padding: 4 10;"
                            : "-fx-background-color: #e3f2fd; -fx-text-fill: #1976d2; -fx-font-weight: bold; -fx-font-size: 11px; -fx-background-radius: 20; -fx-padding: 4 10;");
                    setGraphic(badge);
                    setText(null);
                    setAlignment(Pos.CENTER_LEFT);
                    setStyle("-fx-background-color: transparent;");
                }
            }
        });

        // Score coloré
        colScore.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Float item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); }
                else {
                    setText(String.format("%.1f%%", item));
                    setStyle("-fx-font-weight: bold;" +
                            (item >= 70 ? "-fx-text-fill: #2ecc71;" : "-fx-text-fill: #f5a623;"));
                }
            }
        });

        // Durée
        colDuree.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); }
                else { setText(item + " min"); setStyle("-fx-text-fill: #555;"); }
            }
        });

        // Titre gras
        colTitre.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); }
                else { setText(item); setStyle("-fx-font-weight: bold; -fx-text-fill: #1a1a2e; -fx-font-size: 13px;"); }
            }
        });

        // Cours badge violet
        colCours.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); setStyle(""); }
                else {
                    Label badge = new Label(item);
                    badge.setStyle("-fx-background-color: #f3e5f5; -fx-text-fill: #7b1fa2; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 4 10;");
                    setGraphic(badge);
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                }
            }
        });

        chargerQuiz();
    }

    // ─────────── CHARGER ───────────
    @FXML
    public void chargerQuiz() {
        try {
            ObservableList<Quiz> list = FXCollections.observableArrayList(quizService.afficher());
            tableQuiz.setItems(list);
            int nb = list.size();
            if (lblNbQuiz    != null) lblNbQuiz.setText(nb + " quiz au total");
            if (lblTotalQuiz != null) lblTotalQuiz.setText(String.valueOf(nb));
            messageQuiz.setText("");
        } catch (Exception e) {
            showMsg("Erreur : " + e.getMessage(), false);
        }
    }

    // ─────────── SUPPRIMER ───────────
    @FXML
    public void supprimerQuiz() {
        Quiz selected = tableQuiz.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMsg("⚠ Sélectionnez un quiz à supprimer !", false);
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer le quiz ?");
        alert.setContentText("Voulez-vous vraiment supprimer \"" + selected.getTitre() + "\" ?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    quizService.supprimer(selected.getId());
                    showMsg("✅ Quiz supprimé avec succès !", true);
                    chargerQuiz();
                } catch (Exception e) {
                    showMsg("Erreur : " + e.getMessage(), false);
                }
            }
        });
    }

    // ─────────── NAVIGATION SIDEBAR ───────────
    @FXML
    public void ouvrirCours() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/admin.fxml"));
            tableQuiz.getScene().setRoot(root);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    public void ouvrirCertifications() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/CertifAdmin.fxml"));
            tableQuiz.getScene().setRoot(root);
        } catch (Exception e) { e.printStackTrace(); }
    }
    @FXML
    public void ouvrirUser() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AdminView.fxml"));
            tableQuiz.getScene().setRoot(root);
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ─────────── UTILITAIRE ───────────
    private void showMsg(String msg, boolean success) {
        messageQuiz.setStyle(success
                ? "-fx-text-fill: #2ecc71; -fx-font-weight: bold; -fx-font-size: 12px;"
                : "-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 12px;");
        messageQuiz.setText(msg);
    }

    @FXML
    void logout(ActionEvent event) {
        try {
            Session.logout();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainView.fxml"));
            Parent root = loader.load();
            messageQuiz.getScene().setRoot(root);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}