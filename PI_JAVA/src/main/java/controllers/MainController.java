package controllers;

import entities.*;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button ;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import utils.Helpers;

import java.io.IOException;
import java.sql.SQLException;

public class MainController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;


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

            Helpers.showAlert("Error", "mot de pass ou email incorrect");
            return;
        }

        if (user instanceof Admin) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdminView.fxml"));
            Parent root = loader.load();
            emailField.getScene().setRoot(root);
        } else if (user instanceof Professor) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ProfessorView.fxml"));
            Parent root = loader.load();
            emailField.getScene().setRoot(root);
        } else if (user instanceof Student) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/StudentView.fxml"));
            Parent root = loader.load();
            emailField.getScene().setRoot(root);
        }
    }

}
