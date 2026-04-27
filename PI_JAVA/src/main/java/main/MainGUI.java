package main;

import entities.Admin;
import entities.Professor;
import entities.User;
import entities.Session;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utils.RememberMeManager;

public class MainGUI extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        if (RememberMeManager.hasCredentials()) {
            try {
                Session.login(RememberMeManager.fetchEmail(), RememberMeManager.fetchPassword());
                User user = Session.getCurrentUser();
                if (user != null) {


                    String fxml;
                    if (user instanceof Admin)
                        fxml = "/AdminView.fxml";
                    else if (user instanceof Professor)
                        fxml = "/ProfessorView.fxml";
                    else
                        fxml = "/StudentView.fxml";

                    Parent root = FXMLLoader.load(getClass().getResource(fxml));
                    stage.setScene(new Scene(root));
                    stage.setTitle("PI JAVA");
                    stage.show();
                    return;
                }
            } catch (Exception e) {
                RememberMeManager.clearCredentials();
            }
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainView.fxml"));
        stage.setScene(new Scene(loader.load()));
        stage.setTitle("PI JAVA");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}