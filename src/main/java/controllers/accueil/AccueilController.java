package controllers.accueil;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class AccueilController {

    @FXML
    private void espaceAdmin(ActionEvent event) {
        naviguer(event, "/bourses/ListeBourses.fxml");
    }

    @FXML
    private void espaceEtudiant(ActionEvent event) {
        naviguer(event, "/etudiant/EtudiantBourses.fxml");
    }

    private void naviguer(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Button source = (Button) event.getSource();
            source.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
