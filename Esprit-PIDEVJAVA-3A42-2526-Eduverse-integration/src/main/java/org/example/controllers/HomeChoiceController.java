package org.example.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class HomeChoiceController {

    @FXML
    private void openAdmin(ActionEvent event) {
        loadScene(event, "/AdminView.fxml", "Eduverse - Administration");
    }

    @FXML
    private void openStudent(ActionEvent event) {
        loadScene(event, "/Student.fxml", "Eduverse - Espace Étudiant");
    }

    private void loadScene(ActionEvent event, String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(new Scene(root, 1050, 700));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
