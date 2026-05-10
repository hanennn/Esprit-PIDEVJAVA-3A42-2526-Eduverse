package org.example.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.entities.Admin;
import org.example.entities.Professor;
import org.example.entities.Session;
import org.example.entities.User;
import org.example.services.GoogleOAuthService;
import org.example.services.GoogleUserInfo;
import org.example.utils.RememberMeManager;

public class AuthController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox rememberMeCheckbox;

    @FXML
    private void login(ActionEvent event) {
        String email = emailField.getText() != null ? emailField.getText().trim() : "";
        String password = passwordField.getText() != null ? passwordField.getText() : "";

        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champs requis", "Veuillez saisir votre e-mail et mot de passe.");
            return;
        }

        try {
            Session.login(email, password);
            User user = Session.getCurrentUser();

            if (user == null) {
                showAlert(Alert.AlertType.ERROR, "Connexion échouée", "Email ou mot de passe incorrect.");
                return;
            }

            org.example.utils.AppContext.setCurrentUser(user);

            if (rememberMeCheckbox != null && rememberMeCheckbox.isSelected()) {
                RememberMeManager.saveCredentials(email, password);
            } else {
                RememberMeManager.clearCredentials();
            }

            navigateAfterLogin();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de se connecter: " + e.getMessage());
        }
    }

    @FXML
    private void loginWithGoogle(ActionEvent event) {
        GoogleOAuthService oauthService = new GoogleOAuthService();

        new Thread(() -> {
            try {
                GoogleUserInfo userInfo = oauthService.startOAuthFlow().get();
                Session.loginWithGoogle(userInfo);

                Platform.runLater(() -> {
                    try {
                        if (Session.getCurrentUser() == null) {
                            showAlert(Alert.AlertType.ERROR, "Connexion Google", "Connexion Google échouée.");
                            return;
                        }
                        org.example.utils.AppContext.setCurrentUser(Session.getCurrentUser());
                        navigateAfterLogin();
                    } catch (Exception e) {
                        showAlert(Alert.AlertType.ERROR, "Erreur", "Navigation impossible: " + e.getMessage());
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() ->
                        showAlert(Alert.AlertType.ERROR, "OAuth échoué", e.getMessage())
                );
            }
        }).start();
    }

    @FXML
    private void goToRegister(ActionEvent event) {
        switchScene("/RegisterView.fxml", "Inscription");
    }

    private void navigateAfterLogin() {
        User user = Session.getCurrentUser();
        if (user == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun utilisateur connecté.");
            return;
        }

        if (user instanceof Admin) {
            switchScene("/AdminView.fxml", "Admin");
        } else if (user instanceof Professor) {
            switchScene("/org/example/AccueilFormateur.fxml", "Espace Formateur");
        } else {
            switchScene("/org/example/AccueilEtudiant.fxml", "Espace Étudiant");
        }
    }

    private void switchScene(String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de navigation", e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle("Eduverse");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
