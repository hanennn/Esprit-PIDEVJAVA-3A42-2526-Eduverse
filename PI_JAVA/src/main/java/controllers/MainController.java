package controllers;

import Services.GoogleOAuthService;
import entities.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import utils.EmailService;
import utils.Helpers;
import utils.IpService;
import utils.RememberMeManager;

import java.io.IOException;
import java.sql.SQLException;

public class MainController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox rememberMeCheckbox;

    @FXML
    void login(ActionEvent event) throws IOException {
        try {
            Session.login(emailField.getText(), passwordField.getText());
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return;
        }

        User user = Session.getCurrentUser();

        if (user == null) {
            Helpers.showAlert("Erreur", "Email ou mot de passe incorrect");
            return;
        }

        try {
            String currentIp = IpService.getCurrentIp();
            String savedIp = Session.getLastKnownIp(user.getId());

            if (savedIp == null) {
                Session.updateLastKnownIp(user.getId(), currentIp);
            } else if (!savedIp.equals(currentIp)) {
                String code = EmailService.generateCode();
                EmailService.sendIpAlertEmail(user.getEmail(), user.getFirstName(), code);

                if (!askForCode(code)) {
                    Session.logout();
                    Helpers.showAlert("Erreur", "Vérification échouée. Connexion annulée.");
                    return;
                }

                Session.updateLastKnownIp(user.getId(), currentIp);
            }
        } catch (Exception e) {
            Helpers.showAlert("Erreur", e.getMessage());
        }

        if (rememberMeCheckbox.isSelected()) {
            RememberMeManager.saveCredentials(user.getEmail(), user.getPassword());
        }

        navigateTo(user);
    }

    @FXML
    void loginWithGoogle(ActionEvent event) {
        GoogleOAuthService oauthService = new GoogleOAuthService();

        new Thread(() -> {
            try {
                Services.GoogleUserInfo userInfo = oauthService.startOAuthFlow().get();

                javafx.application.Platform.runLater(() -> {
                    try {
                        Session.loginWithGoogle(userInfo);
                        User user = Session.getCurrentUser();

                        if (user == null) {
                            Helpers.showAlert("Erreur", "Compte introuvable.");
                            return;
                        }

                        navigateTo(user);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Helpers.showAlert("Erreur", "Google login error: " + e.getMessage());
                    }
                });

            } catch (Exception e) {
                javafx.application.Platform.runLater(() ->
                        Helpers.showAlert("Erreur", "OAuth failed: " + e.getMessage())
                );
            }
        }).start();
    }

    @FXML
    void goToRegister(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/RegisterView.fxml"));
        Parent root = loader.load();
        emailField.getScene().setRoot(root);
    }

    private void navigateTo(User user) throws IOException {
        String fxml;

        if (user instanceof Admin) fxml = "/AdminView.fxml";
        else if (user instanceof Professor) fxml = "/ProfessorView.fxml";
        else fxml = "/StudentView.fxml";

        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
        Parent root = loader.load();
        emailField.getScene().setRoot(root);
    }

    private boolean askForCode(String expectedCode) {
        for (int attempt = 1; attempt <= 3; attempt++) {
            javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog();
            dialog.setTitle("Vérification de sécurité");
            dialog.setHeaderText(attempt == 1
                    ? "Une nouvelle connexion a été détectée depuis un réseau inconnu.\nUn code a été envoyé à votre adresse e-mail."
                    : "Code incorrect. Tentative " + attempt + "/3");
            dialog.setContentText("Entrez le code à 6 chiffres :");

            var result = dialog.showAndWait();
            if (result.isEmpty()) return false;
            if (result.get().trim().equals(expectedCode)) return true;
        }

        return false;
    }
}