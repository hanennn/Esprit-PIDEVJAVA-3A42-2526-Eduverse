package org.example.controllers;

import org.example.entities.Session;
import org.example.entities.Student;
import org.example.entities.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.entities.cours;
import org.example.services.InscriptionService;


import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;

public class StudentController {

    @FXML private Label heroNameLabel;

    @FXML private Label fullNameLabel;
    @FXML private Label emailLabel;
    @FXML private Label usernameLabel;
    @FXML private Label lastLoginLabel;
    @FXML private Label inscriptionLabel;

    @FXML private Label avatarInitialsLabel;
    @FXML private Label sidebarNameLabel;
    @FXML private Label sidebarEmailLabel;

    @FXML private VBox calendarContainer;
    @FXML private VBox mesCoursContainer;
    @FXML private Label coursInscritsLabel;

    @FXML private Button logoutBtn;


    private final InscriptionService inscriptionService = new InscriptionService();
    private static final Set<String> CERT_DATES = new HashSet<>(Arrays.asList(
            "2026-02-14", "2026-02-18", "2026-02-25", "2026-03-04"
    ));

    @FXML
    public void initialize() {
        User user = Session.getCurrentUser();

        if (user instanceof Student student) {
            String fullName = student.getFirstName() + " " + student.getLastName();

            heroNameLabel.setText(fullName);

            fullNameLabel.setText(fullName);
            emailLabel.setText(student.getEmail());
            usernameLabel.setText(student.getUserName());
            lastLoginLabel.setText(
                    student.getDateLastConnexion() != null && !student.getDateLastConnexion().isBlank()
                            ? student.getDateLastConnexion()
                            : "Première connexion"
            );
            inscriptionLabel.setText(
                    student.getDateInscription() != null && !student.getDateInscription().isBlank()
                            ? student.getDateInscription()
                            : "—"
            );

            avatarInitialsLabel.setText(buildInitials(student.getFirstName(), student.getLastName()));
            sidebarNameLabel.setText(fullName);
            sidebarEmailLabel.setText(student.getEmail());
        }

        applyLogoutHover();
        buildMiniCalendar(YearMonth.now());
        chargerMesCours();
    }

    @FXML
    void logout(ActionEvent event) throws SQLException {
        try {
            Session.logout();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainView.fxml"));
            Parent root = loader.load();
            logoutBtn.getScene().setRoot(root);
        } catch (IOException e) {
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
                        "• Tous vos cours seront perdus\n" +
                        "• Tous vos badges seront supprimés\n" +
                        "• Votre historique de donations sera effacé"
        );

        DialogPane dp = confirm.getDialogPane();
        dp.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #dc3545; -fx-border-width: 2;"
        );

        ButtonType deleteBtn = new ButtonType("Oui, Supprimer Mon Compte", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(deleteBtn, cancelBtn);

        confirm.showAndWait().ifPresent(response -> {
            if (response == deleteBtn) {
                try {
                    Session.logout();
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainView.fxml"));
                    Parent root = loader.load();
                    logoutBtn.getScene().setRoot(root);
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        });
    }

    private void buildMiniCalendar(YearMonth ym) {
        calendarContainer.getChildren().clear();

        String monthName = ym.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH);
        monthName = monthName.substring(0, 1).toUpperCase() + monthName.substring(1);

        Label header = new Label(monthName + " " + ym.getYear());
        header.setStyle(
                "-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1a2035;" +
                        "-fx-padding: 0 0 10 0;"
        );
        calendarContainer.getChildren().add(header);

        String[] days = {"Lu", "Ma", "Me", "Je", "Ve", "Sa", "Di"};
        GridPane grid = new GridPane();
        grid.setHgap(4);
        grid.setVgap(4);

        for (int i = 0; i < 7; i++) {
            Label d = new Label(days[i]);
            d.setStyle("-fx-font-size: 10px; -fx-text-fill: #8a9bb5; -fx-font-weight: bold;");
            d.setMinWidth(28);
            d.setAlignment(Pos.CENTER);
            grid.add(d, i, 0);
        }

        LocalDate first = ym.atDay(1);
        int startCol = first.getDayOfWeek().getValue() - 1;
        int day = 1;
        int row = 1;
        int col = startCol;

        while (day <= ym.lengthOfMonth()) {
            LocalDate date = ym.atDay(day);
            String iso = date.toString();

            boolean hasCert = CERT_DATES.contains(iso);
            boolean isToday = date.equals(LocalDate.now());

            Label cell = new Label(String.valueOf(day));
            cell.setMinWidth(28);
            cell.setMinHeight(26);
            cell.setAlignment(Pos.CENTER);

            String cellStyle;
            if (hasCert) {
                cellStyle = "-fx-background-color: #f5a623; -fx-text-fill: white;" +
                        "-fx-font-weight: bold; -fx-font-size: 11px;" +
                        "-fx-background-radius: 4;";
            } else if (isToday) {
                cellStyle = "-fx-background-color: #1a2035; -fx-text-fill: white;" +
                        "-fx-font-size: 11px; -fx-background-radius: 4;";
            } else {
                cellStyle = "-fx-font-size: 11px; -fx-text-fill: #333;";
            }

            cell.setStyle(cellStyle);
            grid.add(cell, col, row);

            col++;
            if (col == 7) {
                col = 0;
                row++;
            }
            day++;
        }

        calendarContainer.getChildren().add(grid);

        HBox legend = new HBox(8);
        legend.setStyle("-fx-padding: 10 0 0 0;");
        legend.setAlignment(Pos.CENTER_LEFT);

        Label certDot = new Label("■ Certification");
        certDot.setStyle("-fx-font-size: 10px; -fx-text-fill: #f5a623; -fx-font-weight: bold;");

        Label todayDot = new Label("■ Aujourd'hui");
        todayDot.setStyle("-fx-font-size: 10px; -fx-text-fill: #1a2035;");

        legend.getChildren().addAll(certDot, todayDot);
        calendarContainer.getChildren().add(legend);
    }

    private String buildInitials(String first, String last) {
        String f = (first != null && !first.isEmpty()) ? String.valueOf(first.charAt(0)).toUpperCase() : "";
        String l = (last != null && !last.isEmpty()) ? String.valueOf(last.charAt(0)).toUpperCase() : "";
        return f + l;
    }

    private void applyLogoutHover() {
        if (logoutBtn == null) return;

        logoutBtn.setOnMouseEntered(e ->
                logoutBtn.setStyle(
                        "-fx-background-color: #dc3545;" +
                                "-fx-text-fill: white;" +
                                "-fx-font-weight: bold; -fx-font-size: 13px;" +
                                "-fx-background-radius: 0; -fx-border-radius: 0;" +
                                "-fx-cursor: hand;"
                )
        );

        logoutBtn.setOnMouseExited(e ->
                logoutBtn.setStyle(
                        "-fx-background-color: transparent;" +
                                "-fx-border-color: #dc3545; -fx-border-width: 1.5;" +
                                "-fx-text-fill: #dc3545;" +
                                "-fx-font-weight: bold; -fx-font-size: 13px;" +
                                "-fx-background-radius: 0; -fx-border-radius: 0;" +
                                "-fx-cursor: hand;"
                )
        );
    }



    @FXML
    private void navigateToAccueil(javafx.scene.input.MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/AccueilEtudiant.fxml"));
        Parent root = loader.load();
        heroNameLabel.getScene().setRoot(root);
    }

    @FXML
    private void navigateToCours(javafx.event.ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/AccueilEtudiant.fxml"));
        Parent root = loader.load();
        heroNameLabel.getScene().setRoot(root);
    }

    @FXML
    private void navigateToCours(javafx.scene.input.MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/AccueilEtudiant.fxml"));
        Parent root = loader.load();
        heroNameLabel.getScene().setRoot(root);
    }

    @FXML
    private void navigateToCertifications(javafx.scene.input.MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/CertificationsEtudiant.fxml"));
        Parent root = loader.load();
        heroNameLabel.getScene().setRoot(root);
    }

    @FXML
    private void retourChoixEspace(javafx.scene.input.MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainView.fxml"));
        Parent root = loader.load();
        heroNameLabel.getScene().setRoot(root);
    }

    @FXML
    private void navigateToEvents(javafx.scene.input.MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Student.fxml"));
        Parent root = loader.load();
        heroNameLabel.getScene().setRoot(root);
    }


    private void chargerMesCours() {
        mesCoursContainer.getChildren().clear();
        try {
            int userId = Session.getCurrentUser().getId();
            List<cours> mesCours = inscriptionService.getCoursForUser(userId);

            // update sidebar counter
            if (coursInscritsLabel != null)
                coursInscritsLabel.setText(String.valueOf(mesCours.size()));

            if (mesCours.isEmpty()) {
                VBox vide = new VBox(8);
                vide.setAlignment(javafx.geometry.Pos.CENTER);
                vide.setStyle("-fx-padding: 20;");
                Label icon = new Label("📚");
                icon.setStyle("-fx-font-size: 36px;");
                Label msg = new Label("Vous n'êtes inscrit à aucun cours pour le moment.");
                msg.setStyle("-fx-font-size: 14px; -fx-text-fill: #8a9bb5;");
                vide.getChildren().addAll(icon, msg);
                mesCoursContainer.getChildren().add(vide);
                return;
            }

            for (cours c : mesCours) {
                HBox card = new HBox(15);
                card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                card.setStyle("-fx-background-color: #f7f9ff; -fx-padding: 14 18;" +
                        "-fx-border-color: #dee8ff; -fx-border-width: 0 0 1 0;");

                Label icon = new Label("📚");
                icon.setStyle("-fx-font-size: 20px;");

                VBox infos = new VBox(4);
                HBox.setHgrow(infos, javafx.scene.layout.Priority.ALWAYS);

                Label titre = new Label(c.getTitre_cours());
                titre.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1a1f3c;");

                StringBuilder details = new StringBuilder();
                if (c.getNiv_cours() != null && !c.getNiv_cours().isEmpty())
                    details.append("📊 ").append(c.getNiv_cours()).append("   ");
                if (c.getMatiere_cours() != null && !c.getMatiere_cours().isEmpty())
                    details.append("🔬 ").append(c.getMatiere_cours()).append("   ");
                if (c.getLangue_cours() != null && !c.getLangue_cours().isEmpty())
                    details.append("🌍 ").append(c.getLangue_cours());

                Label detailsLabel = new Label(details.toString().trim());
                detailsLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #8a9bb5;");

                infos.getChildren().addAll(titre, detailsLabel);
                card.getChildren().addAll(icon, infos);
                mesCoursContainer.getChildren().add(card);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}