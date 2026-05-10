package org.example.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.entities.Session;
import org.example.entities.chapitres;
import org.example.entities.cours;
import org.example.services.chapitresservices;
import org.example.services.coursservices;
import org.example.utils.AppContext;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class AdminCoursController {

    // ── Stats ──
    @FXML private Label lblTotalCours;
    @FXML private Label lblTotalFormateurs;
    @FXML private Label lblTotalLangues;
    @FXML private Label lblTotalMatieres;
    @FXML private Label lblNbCours;

    // ── Table ──
    @FXML private TableView<cours>           tableCours;
    @FXML private TableColumn<cours, String> colTitre;
    @FXML private TableColumn<cours, String> colDesc;
    @FXML private TableColumn<cours, String> colNiveau;
    @FXML private TableColumn<cours, String> colMatiere;
    @FXML private TableColumn<cours, String> colLangue;
    @FXML private TableColumn<cours, Void>   colActions;

    private final coursservices    coursService    = new coursservices();
    private final chapitresservices chapService    = new chapitresservices();

    // ─────────── INIT ───────────
    @FXML
    public void initialize() throws SQLException {
        // Colonnes data
        colTitre.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getTitre_cours()));
        colDesc.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getDescription()));
        colNiveau.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getNiv_cours()));
        colMatiere.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getMatiere_cours()));
        colLangue.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getLangue_cours()));

        // Bouton Chapitres
        colActions.setCellFactory(col -> new TableCell<>() {
            final Button btn = new Button("Chapitres");
            {
                btn.setStyle(
                        "-fx-background-color: #1abc9c; -fx-text-fill: white;" +
                                "-fx-background-radius: 6; -fx-font-size: 11px; -fx-cursor: hand;");
                btn.setOnAction(e ->
                        ouvrirChapitres(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        chargerCours();
    }

    // ─────────── CHARGER COURS ───────────
    private void chargerCours() throws SQLException {
        List<cours> liste = coursService.afficherTous(); // ← ici le changement

        long nbLangues  = liste.stream()
                .map(cours::getLangue_cours)
                .filter(l -> l != null && !l.isEmpty())
                .distinct().count();
        long nbMatieres = liste.stream()
                .map(cours::getMatiere_cours)
                .filter(m -> m != null && !m.isEmpty())
                .distinct().count();

        if (lblTotalCours      != null) lblTotalCours.setText(String.valueOf(liste.size()));
        if (lblTotalFormateurs != null) lblTotalFormateurs.setText("—");
        if (lblTotalLangues    != null) lblTotalLangues.setText(String.valueOf(nbLangues));
        if (lblTotalMatieres   != null) lblTotalMatieres.setText(String.valueOf(nbMatieres));
        if (lblNbCours         != null) lblNbCours.setText(liste.size() + " cours au total");

        tableCours.setItems(FXCollections.observableArrayList(liste));
    }
    // ─────────── OUVRIR CHAPITRES ───────────
    private void ouvrirChapitres(cours c) {
        try {
            List<chapitres> chaps = chapService.getChapitresByCours(c.getId());

            TableView<chapitres> table = new TableView<>();

            TableColumn<chapitres, String> colT = new TableColumn<>("Titre");
            colT.setCellValueFactory(d ->
                    new SimpleStringProperty(d.getValue().getTitre_chap()));

            TableColumn<chapitres, String> colD = new TableColumn<>("Description");
            colD.setCellValueFactory(d ->
                    new SimpleStringProperty(d.getValue().getDesc_chap()));

            TableColumn<chapitres, String> colDuree = new TableColumn<>("Durée");
            colDuree.setCellValueFactory(d ->
                    new SimpleStringProperty(d.getValue().getDuree_chap()));

            TableColumn<chapitres, String> colStatut = new TableColumn<>("Statut");
            colStatut.setCellValueFactory(d ->
                    new SimpleStringProperty(d.getValue().getStatut_chap()));
            colStatut.setCellFactory(col -> new TableCell<>() {
                @Override protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setGraphic(null); return; }
                    Label lbl = new Label(item);
                    lbl.setStyle("OUVERT".equalsIgnoreCase(item)
                            ? "-fx-background-color: #2ecc71; -fx-text-fill: white;" +
                            "-fx-padding: 4 10; -fx-background-radius: 20;"
                            : "-fx-background-color: #e74c3c; -fx-text-fill: white;" +
                            "-fx-padding: 4 10; -fx-background-radius: 20;");
                    setGraphic(lbl);
                }
            });

            TableColumn<chapitres, String> colType = new TableColumn<>("Type");
            colType.setCellValueFactory(d ->
                    new SimpleStringProperty(d.getValue().getType_contenu()));
            colType.setCellFactory(col -> new TableCell<>() {
                @Override protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setGraphic(null); return; }
                    Label lbl = new Label(item);
                    lbl.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;" +
                            "-fx-padding: 4 10; -fx-background-radius: 20;");
                    setGraphic(lbl);
                }
            });

            table.getColumns().addAll(colT, colD, colDuree, colStatut, colType);
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            table.setItems(FXCollections.observableArrayList(chaps));

            // Bouton retour
            Button btnRetour = new Button("← Retour aux cours");
            btnRetour.setStyle(
                    "-fx-background-color: transparent; -fx-border-color: #f5a623;" +
                            "-fx-border-radius: 6; -fx-text-fill: #f5a623;" +
                            "-fx-cursor: hand; -fx-padding: 8 16;");
            btnRetour.setOnAction(ev -> {
                try {
                    Parent root = FXMLLoader.load(
                            getClass().getResource("/admin.fxml"));
                    table.getScene().setRoot(root);
                } catch (Exception ex) { ex.printStackTrace(); }
            });

            Label titre = new Label("Chapitres : " + c.getTitre_cours());
            titre.setStyle(
                    "-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1a1a2e;");

            HBox header = new HBox(20, titre, btnRetour);
            header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            header.setStyle("-fx-padding: 20 30;");

            VBox container = new VBox(16, header, table);
            container.setStyle(
                    "-fx-background-color: #f4f6f8; -fx-padding: 0 30 30 30;");
            VBox.setVgrow(table, javafx.scene.layout.Priority.ALWAYS);

            tableCours.getScene().setRoot(container);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ─────────── NAVIGATION SIDEBAR (identique à UserAdminController) ───────────
    @FXML void goToAdminUser(MouseEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/AdminView.fxml"));
        ((javafx.scene.Node) event.getSource()).getScene().setRoot(root);
    }

    @FXML void goToAdminCours(MouseEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/AdminCours.fxml"));
        ((javafx.scene.Node) event.getSource()).getScene().setRoot(root);
    }

    @FXML void goToAdminQuiz(MouseEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/org/example/AdminQuiz.fxml"));
        ((javafx.scene.Node) event.getSource()).getScene().setRoot(root);
    }

    @FXML void goToAdminCertification(MouseEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/CertifAdmin.fxml"));
        ((javafx.scene.Node) event.getSource()).getScene().setRoot(root);
    }

    @FXML void goToAdminForum(MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/main.fxml"));
        Parent root = loader.load();
        MainController ctrl = loader.getController();
        ctrl.setCurrentUser(AppContext.getCurrentUser());
        ((javafx.scene.Node) event.getSource()).getScene().setRoot(root);
    }

    @FXML void goToAdminBadwords(MouseEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/badword-view.fxml"));
        ((javafx.scene.Node) event.getSource()).getScene().setRoot(root);
    }

    @FXML void goToAdminEvents(MouseEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/Event.fxml"));
        ((javafx.scene.Node) event.getSource()).getScene().setRoot(root);
    }

    @FXML void NavigateToBourses(MouseEvent event) throws IOException {
        Parent root = FXMLLoader.load(
                getClass().getResource("/org/example/bourses/ListeBourses.fxml"));
        ((javafx.scene.Node) event.getSource()).getScene().setRoot(root);
    }

    @FXML void NavigateToDemandes(MouseEvent event) throws IOException {
        Parent root = FXMLLoader.load(
                getClass().getResource("/org/example/demandes/ListeDemandes.fxml"));
        ((javafx.scene.Node) event.getSource()).getScene().setRoot(root);
    }

    @FXML void logout(javafx.event.ActionEvent event) {
        try {
            Session.logout();
            Parent root = FXMLLoader.load(getClass().getResource("/MainView.fxml"));
            ((javafx.scene.Node) event.getSource()).getScene().setRoot(root);
        } catch (Exception e) { e.printStackTrace(); }
    }
}