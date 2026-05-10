package org.example;

import org.example.services.BadwordDatabaseBootstrap;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class EduverseForumApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        BadwordDatabaseBootstrap.ensureInitialized();
        Parent root = FXMLLoader.load(getClass().getResource("/main.fxml"));

        // Évite une fenêtre trop grande sur les écrans de petite résolution
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        double desiredWidth = 1400;
        double desiredHeight = 900;
        double width = Math.min(desiredWidth, bounds.getWidth() * 0.9);
        double height = Math.min(desiredHeight, bounds.getHeight() * 0.9);

        Scene scene = new Scene(root, width, height);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        stage.setTitle("Eduverse Forum");
        stage.setScene(scene);
        stage.setFullScreen(false);
        stage.setMaximized(false);
        stage.setMinWidth(1000);
        stage.setMinHeight(700);
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}