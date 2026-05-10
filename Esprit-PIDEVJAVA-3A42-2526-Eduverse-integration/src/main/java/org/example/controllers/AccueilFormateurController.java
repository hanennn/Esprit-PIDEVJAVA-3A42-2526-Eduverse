package org.example.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.entities.Professor;
import org.example.entities.Question;
import org.example.entities.Quiz;
import org.example.entities.Session;
import org.example.entities.User;
import org.example.entities.cours;
import org.example.utils.AppContext;
import org.example.services.QuestionService;
import org.example.services.QuizService;
import org.example.services.coursservices;
import org.example.utils.MyConnection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccueilFormateurController {

    // ─── Dashboard FXML nodes ───────────────────────────────────────────────
    @FXML private TextField        searchTitreField;
    @FXML private ComboBox<String> filterTypeBox;
    @FXML private ComboBox<String> filterCoursBox;
    @FXML private ComboBox<String> sortBox;
    @FXML private VBox             quizListContainer;
    @FXML private VBox             coursListContainer;
    @FXML private HBox             statsBox;
    @FXML private PieChart         typeQuizChart;
    @FXML private BarChart<String, Number> questionsParQuizChart;

    // Global stats labels (dashboard right column)
    @FXML private Label statCoursActifsLabel;
    @FXML private Label statTotalEtudiantsLabel;
    @FXML private Label statEvaluationsLabel;
    @FXML private Label statNoteMoyenneLabel;
    @FXML private Label statTauxReussiteLabel;

    // ─── Profile FXML nodes ─────────────────────────────────────────────────
    @FXML private Label heroNameLabel;
    @FXML private Label heroSpecialtyLabel;
    @FXML private Label fullNameLabel;
    @FXML private Label emailLabel;
    @FXML private Label usernameLabel;
    @FXML private Label specialtyLabel;
    @FXML private Label experienceLabel;
    @FXML private Label lastLoginLabel;
    @FXML private Label inscriptionLabel;
    @FXML private Label avatarInitialsLabel;
    @FXML private Label sidebarNameLabel;
    @FXML private Label sidebarEmailLabel;
    @FXML private Label sidebarSpecialtyLabel;
    @FXML private Label statExperienceLabel;
    @FXML private Button logoutBtn;

    // Profile course/quiz summary labels
    @FXML private Label totalCoursLabel;
    @FXML private Label coursPubliesLabel;
    @FXML private Label etudiantsInscritsLabel;
    @FXML private Label totalQuizLabel;
    @FXML private Label quizActifsLabel;
    @FXML private Label tentativesTotalesLabel;

    // Profile right column stat labels
    @FXML private Label statCoursLabel;
    @FXML private Label statQuizLabel;
    @FXML private Label statEtudiantsLabel;

    // ─── Tab containers ─────────────────────────────────────────────────────
    @FXML private HBox       tabDashboard;
    @FXML private ScrollPane tabProfil;

    // ─── Navbar labels ──────────────────────────────────────────────────────
    @FXML private Label navDashboard;
    @FXML private Label navProfil;
    @FXML private Label breadcrumbLabel;

    // ─── Services ───────────────────────────────────────────────────────────
    private final QuizService     quizService     = new QuizService();
    private final QuestionService questionService = new QuestionService();
    private final coursservices   coursService    = new coursservices();

    private ObservableList<Quiz> masterList   = FXCollections.observableArrayList();
    private FilteredList<Quiz>   filteredList;
    private final Map<String, Integer> coursMap = new HashMap<>();

    // ═══════════════════════════════════════════════════════════════════════
    // INITIALIZE
    // ═══════════════════════════════════════════════════════════════════════
    @FXML
    public void initialize() {
        // Dashboard setup
        filterTypeBox.getItems().addAll("Tous", "Intermédiaire", "Final");
        filterTypeBox.setValue("Tous");
        sortBox.getItems().addAll(
                "Titre A→Z", "Titre Z→A",
                "Durée croissante", "Durée décroissante",
                "Score croissant", "Score décroissant");
        chargerCours();
        chargerDonnees();
        chargerStatistiques();
        configurerFiltres();
        chargerCoursList();

        // Profile setup
        chargerProfil();
        applyLogoutHover();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // TAB SWITCHING
    // ═══════════════════════════════════════════════════════════════════════
    private static final String STYLE_NAV_ACTIVE =
            "-fx-background-color: rgba(245,166,35,0.12); -fx-text-fill: #f5a623;" +
                    "-fx-font-size: 11; -fx-font-weight: bold; -fx-cursor: hand;" +
                    "-fx-padding: 7 14; -fx-background-radius: 8;";

    private static final String STYLE_NAV_INACTIVE =
            "-fx-text-fill: #8892b0; -fx-font-size: 11; -fx-font-weight: bold;" +
                    "-fx-cursor: hand; -fx-padding: 7 14; -fx-background-radius: 8;";

    @FXML
    private void showTabDashboard(javafx.scene.input.MouseEvent event) {
        switchToDashboard();
    }

    @FXML
    private void showTabProfil(javafx.scene.input.MouseEvent event) {
        switchToProfil();
    }

    // Called from the ActionEvent button ("Mon Profil") in the dashboard sidebar
    @FXML
    private void showTabProfilAction(ActionEvent event) {
        switchToProfil();
    }

    private void switchToDashboard() {
        tabDashboard.setVisible(true);
        tabDashboard.setManaged(true);
        tabProfil.setVisible(false);
        tabProfil.setManaged(false);
        navDashboard.setStyle(STYLE_NAV_ACTIVE);
        navProfil.setStyle(STYLE_NAV_INACTIVE);
        if (breadcrumbLabel != null)
            breadcrumbLabel.setText("Espace Formateur — Tableau de Bord");
    }

    private void switchToProfil() {
        tabDashboard.setVisible(false);
        tabDashboard.setManaged(false);
        tabProfil.setVisible(true);
        tabProfil.setManaged(true);
        navDashboard.setStyle(STYLE_NAV_INACTIVE);
        navProfil.setStyle(STYLE_NAV_ACTIVE);
        if (breadcrumbLabel != null)
            breadcrumbLabel.setText("Espace Formateur — Mon Profil");
    }

    @FXML
    public void handleForumButton(ActionEvent event) {
        try {
            System.out.println("Ouverture du Forum (Formateur)...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main.fxml"));
            Parent root = loader.load();
            
            MainController controller = loader.getController();
            User currentUser = Session.getCurrentUser();
            controller.setCurrentUser(currentUser);
            AppContext.setCurrentUser(currentUser);
            
            ((javafx.scene.Node) event.getSource()).getScene().setRoot(root);
        } catch (Throwable t) {
            t.printStackTrace();
            System.err.println("FORUM ERROR: " + t.getMessage());
        }
    }

    @FXML
    public void navigateToBourses(javafx.scene.input.MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/bourses/ListeBourses.fxml"));
            Parent root = loader.load();
            ((javafx.scene.Node) event.getSource()).getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // PROFILE LOGIC  (from ProfessorController)
    // ═══════════════════════════════════════════════════════════════════════
    private void chargerProfil() {
        User user = Session.getCurrentUser();
        if (!(user instanceof Professor professor)) return;

        String fullName = professor.getFirstName() + " " + professor.getLastName();

        heroNameLabel.setText(fullName);
        heroSpecialtyLabel.setText(
                professor.getSpecialty() != null && !professor.getSpecialty().isBlank()
                        ? "Spécialité : " + professor.getSpecialty()
                        : "");

        fullNameLabel.setText(fullName);
        emailLabel.setText(professor.getEmail());
        usernameLabel.setText(professor.getUserName());
        specialtyLabel.setText(nvl(professor.getSpecialty()));
        experienceLabel.setText(nvl(professor.getExperience()));
        lastLoginLabel.setText(
                professor.getDateLastConnexion() != null && !professor.getDateLastConnexion().isBlank()
                        ? professor.getDateLastConnexion()
                        : "Première connexion");
        inscriptionLabel.setText(nvl(professor.getDateInscription()));

        avatarInitialsLabel.setText(buildInitials(professor.getFirstName(), professor.getLastName()));
        sidebarNameLabel.setText(fullName);
        sidebarEmailLabel.setText(professor.getEmail());
        sidebarSpecialtyLabel.setText(nvl(professor.getSpecialty()));
        statExperienceLabel.setText(nvl(professor.getExperience()));
    }

    @FXML
    void logout(ActionEvent event) {
        try {
            Session.logout();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainView.fxml"));
            // Use quizListContainer or logoutBtn, whichever is available
            javafx.scene.Node anchor = (logoutBtn != null) ? logoutBtn : quizListContainer;
            anchor.getScene().setRoot(loader.load());
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @FXML
    void deleteAccount(ActionEvent event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer la Suppression");
        confirm.setHeaderText("⚠  Êtes-vous absolument sûr ?");
        confirm.setContentText(
                "Cette action est irréversible !\n\n" +
                        "• Tous vos cours seront supprimés\n" +
                        "• Tous vos quiz seront supprimés\n" +
                        "• Votre compte sera définitivement effacé");

        ButtonType deleteBtn = new ButtonType("Oui, Supprimer Mon Compte", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(deleteBtn, cancelBtn);

        confirm.showAndWait().ifPresent(response -> {
            if (response == deleteBtn) {
                try {
                    Session.logout();
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainView.fxml"));
                    quizListContainer.getScene().setRoot(loader.load());
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        });
    }

    private void applyLogoutHover() {
        if (logoutBtn == null) return;
        logoutBtn.setOnMouseEntered(e ->
                logoutBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;" +
                        "-fx-font-weight: bold; -fx-font-size: 13px;" +
                        "-fx-background-radius: 0; -fx-border-radius: 0; -fx-cursor: hand;"));
        logoutBtn.setOnMouseExited(e ->
                logoutBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #dc3545;" +
                        "-fx-border-width: 1.5; -fx-text-fill: #dc3545; -fx-font-weight: bold;" +
                        "-fx-font-size: 13px; -fx-background-radius: 0; -fx-border-radius: 0; -fx-cursor: hand;"));
    }

    // ═══════════════════════════════════════════════════════════════════════
    // DASHBOARD — COURS LIST
    // ═══════════════════════════════════════════════════════════════════════
    private void chargerCours() {
        try {
            Connection cnx = MyConnection.getInstance().getCnx();
            ResultSet rs = cnx.createStatement()
                    .executeQuery("SELECT id, titre_cours FROM cours");
            filterCoursBox.getItems().add("Tous");
            while (rs.next()) {
                String nom = rs.getString("titre_cours");
                coursMap.put(nom, rs.getInt("id"));
                filterCoursBox.getItems().add(nom);
            }
            filterCoursBox.setValue("Tous");
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void chargerCoursList() {
        if (coursListContainer == null) return;
        coursListContainer.getChildren().clear();
        try {
            List<cours> listeCours = coursService.afficher();

            // Update both dashboard stat and profile summary
            int nbCours = listeCours.size();
            if (statCoursActifsLabel != null) statCoursActifsLabel.setText(String.valueOf(nbCours));
            if (totalCoursLabel      != null) totalCoursLabel.setText(String.valueOf(nbCours));
            if (coursPubliesLabel    != null) coursPubliesLabel.setText(String.valueOf(nbCours));
            if (statCoursLabel       != null) statCoursLabel.setText(String.valueOf(nbCours));

            if (listeCours.isEmpty()) {
                Label vide = new Label("Aucun cours créé. Cliquez sur '+ Créer Un Cours'");
                vide.setStyle("-fx-text-fill: #888; -fx-font-size: 13; -fx-padding: 20;");
                coursListContainer.getChildren().add(vide);
                return;
            }
            for (cours c : listeCours)
                coursListContainer.getChildren().add(creerCarteCours(c));

        } catch (Exception e) { e.printStackTrace(); }
    }

    private VBox creerCarteCours(cours c) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12;" +
                "-fx-padding: 20; -fx-border-color: #f0f0f0;" +
                "-fx-border-radius: 12; -fx-border-width: 1;" +
                "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),8,0,0,2);");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        HBox barre = new HBox();
        barre.setPrefWidth(4); barre.setPrefHeight(36);
        barre.setStyle("-fx-background-color: #f5a623; -fx-background-radius: 2;");
        VBox titreBox = new VBox(2);
        Label labelTitre = new Label("TITRE DU COURS");
        labelTitre.setStyle("-fx-font-size: 9; -fx-font-weight: bold; -fx-text-fill: #aaa;");
        Label titreLabel = new Label(c.getTitre_cours());
        titreLabel.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: #1a1f3c;");
        titreBox.getChildren().addAll(labelTitre, titreLabel);
        HBox spacer = new HBox(); HBox.setHgrow(spacer, Priority.ALWAYS);
        Label dateLabel = new Label("📅 Créé: 30 April 2026");
        dateLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #888;");
        header.getChildren().addAll(barre, titreBox, spacer, dateLabel);

        HBox infos = new HBox(10);
        infos.setStyle("-fx-padding: 5 0;");
        infos.getChildren().addAll(
                creerInfoCoursBox("NIVEAU",   c.getNiv_cours()),
                creerInfoCoursBox("MATIÈRE",  c.getMatiere_cours()),
                creerInfoCoursBox("LANGUE",   c.getLangue_cours())
        );

        VBox descBox = new VBox(4);
        descBox.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 8;" +
                "-fx-padding: 10 14; -fx-border-color: #f5a623;" +
                "-fx-border-radius: 8; -fx-border-width: 0 0 0 3;");
        Label descKey = new Label("DESCRIPTION");
        descKey.setStyle("-fx-font-size: 9; -fx-font-weight: bold; -fx-text-fill: #aaa;");
        Label descVal = new Label(c.getDescription() != null ? c.getDescription() : "Aucune description");
        descVal.setStyle("-fx-font-size: 12; -fx-text-fill: #555;");
        descVal.setWrapText(true);
        descBox.getChildren().addAll(descKey, descVal);

        HBox statsRow = new HBox(15);
        statsRow.setAlignment(Pos.CENTER_LEFT);
        statsRow.getChildren().addAll(
                creerStatCoursBox("0", "Étudiants Inscrits"),
                creerStatCoursBox("0%", "Taux d'Achèvement"),
                creerStatCoursBox("0/5", "Note Moyenne")
        );

        HBox boutons = new HBox(10);
        boutons.setAlignment(Pos.CENTER_LEFT);

        Button btnSupprimer = new Button("🗑 Supprimer Ce Cours");
        btnSupprimer.setStyle("-fx-background-color: #fef2f2; -fx-text-fill: #e74c3c;" +
                "-fx-font-weight: bold; -fx-background-radius: 8;" +
                "-fx-border-color: #fecaca; -fx-border-radius: 8; -fx-border-width: 1;" +
                "-fx-padding: 8 16; -fx-cursor: hand;");
        btnSupprimer.setOnAction(e -> supprimerCours(c));

        Button btnChapitres = new Button("📚 Chapitres");
        btnChapitres.setStyle("-fx-background-color: #1a1f3c; -fx-text-fill: white;" +
                "-fx-font-weight: bold; -fx-background-radius: 8;" +
                "-fx-padding: 8 16; -fx-cursor: hand;");
        btnChapitres.setOnAction(e -> allerVersChapitres(c));

        Button btnModifier = new Button("✏️ Modifier");
        btnModifier.setStyle("-fx-background-color: #eff6ff; -fx-text-fill: #3b82f6;" +
                "-fx-font-weight: bold; -fx-background-radius: 8;" +
                "-fx-border-color: #bfdbfe; -fx-border-radius: 8; -fx-border-width: 1;" +
                "-fx-padding: 8 16; -fx-cursor: hand;");
        btnModifier.setOnAction(e -> allerVersModificationCours(c));

        boutons.getChildren().addAll(btnSupprimer, btnChapitres, btnModifier);
        card.getChildren().addAll(header, infos, descBox, statsRow, boutons);
        return card;
    }

    private VBox creerInfoCoursBox(String label, String valeur) {
        VBox box = new VBox(3);
        box.setPrefWidth(180);
        box.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 8;" +
                "-fx-padding: 10 14; -fx-border-color: #e0e0e0;" +
                "-fx-border-radius: 8; -fx-border-width: 1;");
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 9; -fx-font-weight: bold; -fx-text-fill: #888;");
        Label val = new Label(valeur != null ? valeur : "-");
        val.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #1a1f3c;");
        box.getChildren().addAll(lbl, val);
        return box;
    }

    private VBox creerStatCoursBox(String valeur, String label) {
        VBox box = new VBox(4);
        box.setAlignment(Pos.CENTER);
        box.setPrefWidth(150);
        box.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 8;" +
                "-fx-padding: 12 14; -fx-border-color: #e0e0e0;" +
                "-fx-border-radius: 8; -fx-border-width: 1;");
        Label val = new Label(valeur);
        val.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #f5a623;");
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 10; -fx-text-fill: #888;");
        box.getChildren().addAll(val, lbl);
        return box;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // DASHBOARD — COURS ACTIONS
    // ═══════════════════════════════════════════════════════════════════════
    private void supprimerCours(cours c) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer \"" + c.getTitre_cours() + "\" ?",
                ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle("Confirmation");
        alert.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                try {
                    coursService.supprimer(c.getId());
                    chargerCoursList();
                } catch (Exception e) { e.printStackTrace(); }
            }
        });
    }

    private void allerVersChapitres(cours c) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/listeChapitres.fxml"));
            Stage stage = (Stage) quizListContainer.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Chapitres — " + c.getTitre_cours());
            ListeChapitresController ctrl = loader.getController();
            ctrl.setCours(c);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void allerVersModificationCours(cours c) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/modifCours.fxml"));
            Stage stage = (Stage) quizListContainer.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Modifier Cours");
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void allerVersAjoutCours() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajoutCours.fxml"));
            Stage stage = (Stage) quizListContainer.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Créer Un Cours");
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // DASHBOARD — QUIZ DATA
    // ═══════════════════════════════════════════════════════════════════════
    @FXML
    public void chargerDonnees() {
        try {
            List<Quiz> tousQuiz = quizService.afficher();
            masterList   = FXCollections.observableArrayList(tousQuiz);
            filteredList = new FilteredList<>(masterList, p -> true);
            afficherQuiz(filteredList);
            chargerStatistiques();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void chargerStatistiques() {
        try {
            List<Quiz>     quizList     = quizService.afficher();
            List<Question> questionList = questionService.afficher();

            int    totalQuiz      = quizList.size();
            long   nbFinal        = quizList.stream().filter(q -> "Final".equals(q.getTypeQuiz())).count();
            long   nbInter        = quizList.stream().filter(q -> "Intermédiaire".equals(q.getTypeQuiz())).count();
            int    totalQuestions = questionList.size();
            double dureeMoy       = quizList.stream().mapToInt(Quiz::getDuree).average().orElse(0);
            double scoreMoy       = quizList.stream().mapToDouble(Quiz::getScoreMinimum).average().orElse(0);

            // Update profile tab summary labels
            if (totalQuizLabel       != null) totalQuizLabel.setText(String.valueOf(totalQuiz));
            if (quizActifsLabel      != null) quizActifsLabel.setText(String.valueOf(totalQuiz));
            if (statQuizLabel        != null) statQuizLabel.setText(String.valueOf(totalQuiz));
            if (tentativesTotalesLabel != null) tentativesTotalesLabel.setText("0");

            statsBox.getChildren().clear();
            statsBox.getChildren().addAll(
                    creerStatCard("Total quiz",    String.valueOf(totalQuiz),                "#4361ee", "#eef2ff", "#4361ee"),
                    creerStatCard("Quiz finaux",    String.valueOf(nbFinal),                  "#f5a623", "#fff8ee", "#f5a623"),
                    creerStatCard("Intermediaires", String.valueOf(nbInter),                  "#2196F3", "#e3f2fd", "#2196F3"),
                    creerStatCard("Questions",       String.valueOf(totalQuestions),           "#2ecc71", "#e8f5e9", "#2ecc71"),
                    creerStatCard("Duree moyenne",  String.format("%.1f min", dureeMoy),      "#9b59b6", "#f3e5f5", "#9b59b6"),
                    creerStatCard("Score moyen",    String.format("%.1f%%", scoreMoy),        "#e74c3c", "#fdecea", "#e74c3c")
            );

            // Pie chart
            typeQuizChart.setTitle("");
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                    new PieChart.Data("Final (" + nbFinal + ")", nbFinal),
                    new PieChart.Data("Intermediaire (" + nbInter + ")", nbInter)
            );
            typeQuizChart.setData(pieData);
            typeQuizChart.setLabelsVisible(true);
            typeQuizChart.setLegendVisible(true);
            typeQuizChart.setStartAngle(90);
            javafx.application.Platform.runLater(() -> {
                ObservableList<PieChart.Data> data = typeQuizChart.getData();
                if (data.size() >= 1) data.get(0).getNode().setStyle("-fx-pie-color: #f5a623;");
                if (data.size() >= 2) data.get(1).getNode().setStyle("-fx-pie-color: #4361ee;");
            });

            // Bar chart
            questionsParQuizChart.setTitle("");
            questionsParQuizChart.getData().clear();
            questionsParQuizChart.setLegendVisible(false);
            questionsParQuizChart.setAnimated(true);
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Questions");
            String[] barColors = {"#4361ee","#f5a623","#2ecc71","#e74c3c","#9b59b6","#2196F3"};
            int colorIdx = 0;
            for (Quiz quiz : quizList) {
                long nb = questionList.stream().filter(q -> q.getQuizId() == quiz.getId()).count();
                XYChart.Data<String, Number> barData = new XYChart.Data<>(quiz.getTitre(), nb);
                series.getData().add(barData);
                final String color = barColors[colorIdx++ % barColors.length];
                barData.nodeProperty().addListener((obs, oldNode, newNode) -> {
                    if (newNode != null) newNode.setStyle("-fx-bar-fill: " + color + ";");
                });
            }
            questionsParQuizChart.getData().add(series);
            javafx.application.Platform.runLater(() -> {
                int idx = 0;
                for (XYChart.Data<String, Number> d : series.getData()) {
                    if (d.getNode() != null)
                        d.getNode().setStyle("-fx-bar-fill: " + barColors[idx % barColors.length] +
                                "; -fx-background-radius: 6 6 0 0;");
                    idx++;
                }
            });

        } catch (Exception e) { e.printStackTrace(); }
    }

    private VBox creerStatCard(String titre, String valeur,
                               String colorText, String colorBg, String colorBorder) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPrefWidth(160);
        card.setStyle("-fx-background-color: " + colorBg + "; -fx-background-radius: 12;" +
                "-fx-padding: 16 18; -fx-border-color: " + colorBorder + ";" +
                "-fx-border-radius: 12; -fx-border-width: 0 0 0 4;" +
                "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),8,0,0,2);");
        Label titreLabel = new Label(titre.toUpperCase());
        titreLabel.setStyle("-fx-text-fill: " + colorText + "; -fx-font-size: 9;" +
                "-fx-font-weight: bold; -fx-opacity: 0.8;");
        Label valeurLabel = new Label(valeur);
        valeurLabel.setStyle("-fx-text-fill: #1a1f3c; -fx-font-size: 22; -fx-font-weight: bold;");
        HBox barre = new HBox();
        barre.setPrefHeight(3);
        barre.setMaxWidth(Double.MAX_VALUE);
        barre.setStyle("-fx-background-color: " + colorBorder + ";" +
                "-fx-background-radius: 2; -fx-opacity: 0.3;");
        card.getChildren().addAll(titreLabel, valeurLabel, barre);
        return card;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // DASHBOARD — QUIZ DISPLAY & FILTERS
    // ═══════════════════════════════════════════════════════════════════════
    private void configurerFiltres() {
        searchTitreField.textProperty().addListener((obs, o, n) -> appliquerFiltres());
        filterTypeBox.valueProperty().addListener((obs, o, n) -> appliquerFiltres());
        filterCoursBox.valueProperty().addListener((obs, o, n) -> appliquerFiltres());
        sortBox.valueProperty().addListener((obs, o, n) -> appliquerFiltres());
    }

    private void appliquerFiltres() {
        if (filteredList == null) return;
        filteredList.setPredicate(quiz -> {
            String titre = searchTitreField.getText().trim().toLowerCase();
            if (!titre.isEmpty() && !quiz.getTitre().toLowerCase().contains(titre)) return false;
            String type = filterTypeBox.getValue();
            if (type != null && !type.equals("Tous") && !quiz.getTypeQuiz().equals(type)) return false;
            String cours = filterCoursBox.getValue();
            if (cours != null && !cours.equals("Tous")
                    && !quizService.getNomCours(quiz.getCoursAssocieId()).equals(cours)) return false;
            return true;
        });

        String tri = sortBox.getValue();
        if (tri != null) {
            ObservableList<Quiz> sorted = FXCollections.observableArrayList(filteredList);
            switch (tri) {
                case "Titre A→Z"          -> sorted.sort(Comparator.comparing(q -> q.getTitre().toLowerCase()));
                case "Titre Z→A"          -> sorted.sort(Comparator.comparing((Quiz q) -> q.getTitre().toLowerCase()).reversed());
                case "Durée croissante"   -> sorted.sort(Comparator.comparingInt(Quiz::getDuree));
                case "Durée décroissante" -> sorted.sort(Comparator.comparingInt(Quiz::getDuree).reversed());
                case "Score croissant"    -> sorted.sort(Comparator.comparingDouble(Quiz::getScoreMinimum));
                case "Score décroissant"  -> sorted.sort(Comparator.comparingDouble(Quiz::getScoreMinimum).reversed());
            }
            afficherQuiz(sorted);
        } else {
            afficherQuiz(filteredList);
        }
    }

    @FXML
    private void reinitialiserFiltres() {
        searchTitreField.clear();
        filterTypeBox.setValue("Tous");
        filterCoursBox.setValue("Tous");
        sortBox.setValue(null);
        if (filteredList != null) filteredList.setPredicate(p -> true);
        afficherQuiz(filteredList != null ? filteredList : masterList);
    }

    private void afficherQuiz(Iterable<Quiz> quizList) {
        quizListContainer.getChildren().clear();
        boolean hasQuiz = false;
        for (Quiz q : quizList) {
            quizListContainer.getChildren().add(creerCarteQuiz(q));
            hasQuiz = true;
        }
        if (!hasQuiz) {
            VBox vide = new VBox(8);
            vide.setAlignment(Pos.CENTER);
            vide.setStyle("-fx-padding: 40;");
            Label l1 = new Label("Aucun quiz trouvé");
            l1.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 13; -fx-font-weight: bold;");
            Label l2 = new Label("Créez votre premier quiz");
            l2.setStyle("-fx-text-fill: #d1d5db; -fx-font-size: 11;");
            vide.getChildren().addAll(l1, l2);
            quizListContainer.getChildren().add(vide);
        }
    }

    private VBox creerCarteQuiz(Quiz quiz) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12;" +
                "-fx-padding: 18; -fx-border-color: #f0f0f0;" +
                "-fx-border-radius: 12; -fx-border-width: 1;" +
                "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),8,0,0,2);");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        HBox barre = new HBox();
        barre.setPrefWidth(4); barre.setPrefHeight(36);
        barre.setStyle("-fx-background-color: #f5a623; -fx-background-radius: 2;");
        VBox titreBox = new VBox(2);
        Label labelTitre = new Label("TITRE DU QUIZ");
        labelTitre.setStyle("-fx-font-size: 9; -fx-font-weight: bold; -fx-text-fill: #aaa;");
        Label titreLabel = new Label(quiz.getTitre());
        titreLabel.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: #1a1f3c;");
        titreBox.getChildren().addAll(labelTitre, titreLabel);
        HBox spacer = new HBox(); HBox.setHgrow(spacer, Priority.ALWAYS);
        boolean isFinal = "Final".equals(quiz.getTypeQuiz());
        Label typeBadge = new Label(quiz.getTypeQuiz());
        typeBadge.setStyle(isFinal
                ? "-fx-background-color: #fff3e0; -fx-text-fill: #f5a623; -fx-font-weight: bold; -fx-font-size: 11; -fx-background-radius: 20; -fx-padding: 4 12;"
                : "-fx-background-color: #e3f2fd; -fx-text-fill: #1976d2; -fx-font-weight: bold; -fx-font-size: 11; -fx-background-radius: 20; -fx-padding: 4 12;");
        header.getChildren().addAll(barre, titreBox, spacer, typeBadge);

        int nbQ = 0;
        try {
            nbQ = (int) questionService.afficher().stream()
                    .filter(q -> q.getQuizId() == quiz.getId()).count();
        } catch (Exception e) { e.printStackTrace(); }

        HBox infos = new HBox(16);
        infos.setAlignment(Pos.CENTER_LEFT);
        infos.getChildren().addAll(
                creerInfoBox("QUESTIONS", String.valueOf(nbQ)),
                creerInfoBox("DUREE", quiz.getDuree() + " min"),
                creerInfoBox("SCORE MIN", quiz.getScoreMinimum() + "%"),
                creerStatutBox()
        );

        HBox boutons = new HBox(10);
        boutons.setAlignment(Pos.CENTER_LEFT);
        Button btnQ = new Button("Questions");
        btnQ.setStyle("-fx-background-color: #eff6ff; -fx-text-fill: #3b82f6; -fx-font-weight: bold; -fx-background-radius: 8; -fx-border-color: #bfdbfe; -fx-border-radius: 8; -fx-border-width: 1; -fx-padding: 8 16; -fx-cursor: hand;");
        btnQ.setOnAction(e -> allerVersQuestionsQuiz(quiz));
        Button btnM = new Button("Modifier");
        btnM.setStyle("-fx-background-color: #1a1f3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;");
        btnM.setOnAction(e -> allerVersModificationQuiz(quiz));
        Button btnS = new Button("Supprimer");
        btnS.setStyle("-fx-background-color: #fef2f2; -fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-background-radius: 8; -fx-border-color: #fecaca; -fx-border-radius: 8; -fx-border-width: 1; -fx-padding: 8 16; -fx-cursor: hand;");
        btnS.setOnAction(e -> supprimerQuiz(quiz));
        boutons.getChildren().addAll(btnQ, btnM, btnS);

        card.getChildren().addAll(header, infos, boutons);
        return card;
    }

    private VBox creerInfoBox(String label, String valeur) {
        VBox box = new VBox(3);
        box.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 8; -fx-padding: 10 14; -fx-border-color: #f5a623; -fx-border-radius: 8; -fx-border-width: 0 0 0 3;");
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 9; -fx-font-weight: bold; -fx-text-fill: #888;");
        Label val = new Label(valeur);
        val.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #1a1f3c;");
        box.getChildren().addAll(lbl, val);
        return box;
    }

    private VBox creerStatutBox() {
        VBox box = new VBox(3);
        box.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 8; -fx-padding: 10 14; -fx-border-color: #2ecc71; -fx-border-radius: 8; -fx-border-width: 0 0 0 3;");
        Label lbl = new Label("STATUT");
        lbl.setStyle("-fx-font-size: 9; -fx-font-weight: bold; -fx-text-fill: #888;");
        Label val = new Label("ACTIF");
        val.setStyle("-fx-background-color: #d1fae5; -fx-text-fill: #065f46; -fx-font-size: 11; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 3 10;");
        box.getChildren().addAll(lbl, val);
        return box;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // DASHBOARD — QUIZ ACTIONS
    // ═══════════════════════════════════════════════════════════════════════
    private void allerVersQuestionsQuiz(Quiz quiz) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/AjouterQuestion.fxml"));
            Stage stage = (Stage) quizListContainer.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Questions — " + quiz.getTitre());
            AjouterQuestionController ctrl = loader.getController();
            ctrl.setQuiz(quiz.getId(), quiz.getTitre());
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void allerVersModificationQuiz(Quiz quiz) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/AjouterQuiz.fxml"));
            Stage stage = (Stage) quizListContainer.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Modifier Quiz");
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void supprimerQuiz(Quiz quiz) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer \"" + quiz.getTitre() + "\" ?", ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle("Confirmation");
        alert.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                try { quizService.supprimer(quiz.getId()); chargerDonnees(); }
                catch (Exception e) { e.printStackTrace(); }
            }
        });
    }

    @FXML
    private void allerVersAjoutQuiz() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/AjouterQuiz.fxml"));
            Stage stage = (Stage) quizListContainer.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Formateur — Gestion Quiz");
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void retourAccueil() {
        try {
            Stage stage = (Stage) quizListContainer.getScene().getWindow();
            new org.example.MainFX().start(stage);
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════════════════════════
    private String buildInitials(String first, String last) {
        String f = (first != null && !first.isEmpty()) ? String.valueOf(first.charAt(0)).toUpperCase() : "";
        String l = (last  != null && !last.isEmpty())  ? String.valueOf(last.charAt(0)).toUpperCase()  : "";
        return f + l;
    }

    private String nvl(String value) {
        return (value != null && !value.isBlank()) ? value : "—";
    }

}