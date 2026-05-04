package org.example.controllers;

import org.example.services.*;
import org.example.entities.Professor;
import org.example.entities.Student;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.example.utils.EmailService;
import org.example.utils.Helpers;

import java.io.IOException;
import java.sql.SQLException;

public class RegisterController {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

    @FXML private TextField specialtyField;
    @FXML private TextField experienceField;
    @FXML private VBox teacherFieldsBox;
    @FXML private Button backBtn;

    private String selectedRole = "Student";
    private String pendingCode = null;

    @FXML
    public void initialize() {
        toggleProfessorFields(false);
    }

    private void toggleProfessorFields(boolean visible) {
        teacherFieldsBox.setVisible(visible);
        teacherFieldsBox.setManaged(visible);
    }

    @FXML
    void selectStudent() {
        selectedRole = "Student";
        toggleProfessorFields(false);
    }

    @FXML
    void selectProfessor() {
        selectedRole = "Professor";
        toggleProfessorFields(true);
    }

    @FXML
    void register(ActionEvent event) {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        try {
            if (!validateCommonFields(firstName, lastName, username, email)) return;

            if (!Helpers.checkPassword(password)) {
                Helpers.showAlert("Erreur",
                        "Le mot de passe doit contenir au moins :\n" +
                                "• une lettre minuscule\n• une lettre majuscule\n" +
                                "• un chiffre\n• un caractère spécial\n• 8 caractères au total");
                return;
            }

            pendingCode = EmailService.generateCode();

            try {
                EmailService.sendVerificationEmail(email, firstName, pendingCode);
            } catch (Exception e) {
                Helpers.showAlert("Erreur", "Impossible d'envoyer l'e-mail de vérification.\n" + e.getMessage());
                return;
            }

            if (!askForCode(email)) return;

            saveAccount(firstName, lastName, username, email, password, null);
            showSuccessAndGoBack(event);

        } catch (Exception e) {
            e.printStackTrace();
            Helpers.showAlert("Erreur", "Une erreur inattendue s'est produite.");
        }
    }

    @FXML
    void registerWithGoogle(ActionEvent event) throws SQLException {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String username = usernameField.getText().trim();

        if (firstName.length() < 5) {
            Helpers.showAlert("Erreur", "Le prénom doit comporter au moins 5 lettres.");
            return;
        }
        if (lastName.length() < 5) {
            Helpers.showAlert("Erreur", "Le nom doit comporter au moins 5 lettres.");
            return;
        }
        if (username.length() < 3) {
            Helpers.showAlert("Erreur", "Le nom d'utilisateur doit comporter au moins 3 caractères.");
            return;
        }
        if (new AdminService().userNameExists(username)) {
            Helpers.showAlert("Erreur", "Ce nom d'utilisateur est déjà utilisé.");
            return;
        }

        if (selectedRole.equals("Professor")) {
            if (specialtyField.getText().trim().isEmpty()) {
                Helpers.showAlert("Erreur", "Veuillez entrer votre spécialité.");
                return;
            }
            if (experienceField.getText().trim().isEmpty()) {
                Helpers.showAlert("Erreur", "Veuillez entrer votre expérience.");
                return;
            }
        }

        GoogleOAuthService oauthService = new GoogleOAuthService();

        new Thread(() -> {
            try {
                GoogleUserInfo userInfo = oauthService.startOAuthFlow().get();

                javafx.application.Platform.runLater(() -> {
                    try {
                        if (new AdminService().EmailExists(userInfo.email)) {
                            org.example.entities.Session.loginWithGoogle(userInfo);
                            navigateAfterLogin(event);
                            return;
                        }

                        saveAccount(firstName, lastName, username, userInfo.email, "", userInfo.googleId);
                        showSuccessAndGoBack(event);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Helpers.showAlert("Erreur", "Inscription Google échouée : " + e.getMessage());
                    }
                });

            } catch (Exception e) {
                javafx.application.Platform.runLater(() ->
                        Helpers.showAlert("Erreur", "OAuth échoué : " + e.getMessage())
                );
            }
        }).start();
    }

    private boolean validateCommonFields(String firstName, String lastName, String username, String email) throws SQLException {
        if (firstName.length() < 5) {
            Helpers.showAlert("Erreur", "Le prénom doit comporter au moins 5 lettres.");
            return false;
        }
        if (lastName.length() < 5) {
            Helpers.showAlert("Erreur", "Le nom doit comporter au moins 5 lettres.");
            return false;
        }
        if (username.length() < 3) {
            Helpers.showAlert("Erreur", "Le nom d'utilisateur doit comporter au moins 3 caractères.");
            return false;
        }
        if (new AdminService().userNameExists(username)) {
            Helpers.showAlert("Erreur", "Ce nom d'utilisateur est déjà utilisé.");
            return false;
        }
        if (!Helpers.checkEmail(email)) {
            Helpers.showAlert("Erreur", "L'adresse e-mail n'est pas valide.");
            return false;
        }
        if (new AdminService().EmailExists(email)) {
            Helpers.showAlert("Erreur", "Cette adresse e-mail est déjà utilisée.");
            return false;
        }

        return true;
    }

    private void saveAccount(String firstName, String lastName, String username,
                             String email, String password, String googleId) {
        if (selectedRole.equals("Professor")) {
            new ProfessorService().AjouterProfessor(new Professor(
                    firstName, lastName, username, email,
                    password, false, googleId,
                    specialtyField.getText().trim(),
                    experienceField.getText().trim(),
                    null
            ));
        } else {
            new StudentService().AjouterStudent(new Student(
                    firstName, lastName, username, email,
                    password, false, googleId, null
            ));
        }
    }

    private void showSuccessAndGoBack(ActionEvent event) throws IOException {
        Alert success = new Alert(Alert.AlertType.INFORMATION);
        success.setTitle("Compte créé");
        success.setContentText("Votre compte a été créé avec succès.\nEn attente de validation par un administrateur.");
        success.showAndWait();
        goBack(event);
    }

    private void navigateAfterLogin(ActionEvent event) throws IOException {
        org.example.entities.User user = org.example.entities.Session.getCurrentUser();

        if (user == null) {
            Helpers.showAlert("Erreur", "Connexion échouée.");
            return;
        }

        String fxml = user instanceof org.example.entities.Professor ? "/ProfessorView.fxml" : "/StudentView.fxml";
        Parent root = new FXMLLoader(getClass().getResource(fxml)).load();
        backBtn.getScene().setRoot(root);
    }

    private boolean askForCode(String email) {
        for (int attempt = 1; attempt <= 3; attempt++) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Vérification de l'e-mail");
            dialog.setHeaderText(attempt == 1
                    ? "Un code de vérification a été envoyé à :\n" + email
                    : "Code incorrect. Tentative " + attempt + "/3\nCode envoyé à : " + email);
            dialog.setContentText("Entrez le code à 6 chiffres :");
            dialog.getDialogPane().setStyle("-fx-font-size: 13px;");

            var result = dialog.showAndWait();
            if (result.isEmpty()) return false;
            if (result.get().trim().equals(pendingCode)) return true;
        }

        Helpers.showAlert("Erreur", "Trop de tentatives incorrectes. Veuillez réessayer.");
        return false;
    }

    @FXML
    void goBack(ActionEvent event) throws IOException {
        Parent root = new FXMLLoader(getClass().getResource("/MainView.fxml")).load();
        backBtn.getScene().setRoot(root);
    }
}