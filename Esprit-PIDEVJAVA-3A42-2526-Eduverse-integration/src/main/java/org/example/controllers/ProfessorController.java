package org.example.controllers;

import org.example.entities.Professor;
import org.example.entities.Session;
import org.example.entities.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;

import java.io.IOException;
import java.sql.SQLException;

public class ProfessorController {

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

    @FXML private Label totalCoursLabel;
    @FXML private Label coursPubliesLabel;
    @FXML private Label etudiantsInscritsLabel;
    @FXML private Label statCoursLabel;
    @FXML private Label statEtudiantsLabel;

    @FXML private Label totalQuizLabel;
    @FXML private Label quizActifsLabel;
    @FXML private Label tentativesTotalesLabel;
    @FXML private Label statQuizLabel;

    @FXML private Button logoutBtn;

    @FXML
    public void initialize() {
        User user = Session.getCurrentUser();

        if (user instanceof Professor professor) {
            String fullName = professor.getFirstName() + " " + professor.getLastName();

            heroNameLabel.setText(fullName);
            heroSpecialtyLabel.setText(
                    professor.getSpecialty() != null && !professor.getSpecialty().isBlank()
                            ? "Spécialité : " + professor.getSpecialty()
                            : ""
            );

            fullNameLabel.setText(fullName);
            emailLabel.setText(professor.getEmail());
            usernameLabel.setText(professor.getUserName());
            specialtyLabel.setText(nvl(professor.getSpecialty()));
            experienceLabel.setText(nvl(professor.getExperience()));
            lastLoginLabel.setText(
                    professor.getDateLastConnexion() != null && !professor.getDateLastConnexion().isBlank()
                            ? professor.getDateLastConnexion()
                            : "Première connexion"
            );
            inscriptionLabel.setText(nvl(professor.getDateInscription()));

            avatarInitialsLabel.setText(buildInitials(professor.getFirstName(), professor.getLastName()));
            sidebarNameLabel.setText(fullName);
            sidebarEmailLabel.setText(professor.getEmail());
            sidebarSpecialtyLabel.setText(nvl(professor.getSpecialty()));
            statExperienceLabel.setText(nvl(professor.getExperience()));
        }

        applyLogoutHover();
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
                        "• Tous vos cours seront supprimés\n" +
                        "• Tous vos quiz seront supprimés\n" +
                        "• Votre compte sera définitivement effacé"
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

    private String buildInitials(String first, String last) {
        String f = (first != null && !first.isEmpty()) ? String.valueOf(first.charAt(0)).toUpperCase() : "";
        String l = (last != null && !last.isEmpty()) ? String.valueOf(last.charAt(0)).toUpperCase() : "";
        return f + l;
    }

    private String nvl(String value) {
        return (value != null && !value.isBlank()) ? value : "—";
    }

    private void applyLogoutHover() {
        if (logoutBtn == null) return;

        logoutBtn.setOnMouseEntered(e ->
                logoutBtn.setStyle(
                        "-fx-background-color: #dc3545; -fx-text-fill: white;" +
                                "-fx-font-weight: bold; -fx-font-size: 13px;" +
                                "-fx-background-radius: 0; -fx-border-radius: 0; -fx-cursor: hand;"
                )
        );

        logoutBtn.setOnMouseExited(e ->
                logoutBtn.setStyle(
                        "-fx-background-color: transparent; -fx-border-color: #dc3545; -fx-border-width: 1.5;" +
                                "-fx-text-fill: #dc3545; -fx-font-weight: bold; -fx-font-size: 13px;" +
                                "-fx-background-radius: 0; -fx-border-radius: 0; -fx-cursor: hand;"
                )
        );
    }
}