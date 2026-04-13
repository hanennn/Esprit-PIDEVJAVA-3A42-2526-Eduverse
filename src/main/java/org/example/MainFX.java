package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainFX extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        VBox root = new VBox(25);
        root.setAlignment(javafx.geometry.Pos.CENTER);
        root.setStyle("-fx-background-color: #1a1f3c; -fx-padding: 60;");

        // Logo
        HBox logo = new HBox(6);
        logo.setAlignment(javafx.geometry.Pos.CENTER);
        Label icon  = new Label("📚");
        icon.setStyle("-fx-font-size: 36;");
        Label edu   = new Label("edu");
        edu.setStyle("-fx-font-size: 36; -fx-font-weight: bold; -fx-text-fill: white;");
        Label verse = new Label("verse");
        verse.setStyle("-fx-font-size: 36; -fx-font-weight: bold; -fx-text-fill: #f5a623;");
        logo.getChildren().addAll(icon, edu, verse);

        Label subtitle = new Label("Choisissez votre espace");
        subtitle.setStyle("-fx-font-size: 16; -fx-text-fill: #a0a8c0;");

        HBox sep = new HBox();
        sep.setPrefHeight(1);
        sep.setPrefWidth(280);
        sep.setStyle("-fx-background-color: #2d3561;");

        // Bouton Formateur
        Button btnFormateur = new Button("🎓  Espace Formateur");
        btnFormateur.setPrefWidth(280);
        btnFormateur.setPrefHeight(55);
        btnFormateur.setStyle(
                "-fx-background-color: #f5a623; -fx-text-fill: white;" +
                        "-fx-font-size: 15; -fx-font-weight: bold;" +
                        "-fx-background-radius: 10; -fx-cursor: hand;"
        );
        btnFormateur.setOnMouseEntered(e -> btnFormateur.setStyle(
                "-fx-background-color: #e8960f; -fx-text-fill: white;" +
                        "-fx-font-size: 15; -fx-font-weight: bold;" +
                        "-fx-background-radius: 10; -fx-cursor: hand;"
        ));
        btnFormateur.setOnMouseExited(e -> btnFormateur.setStyle(
                "-fx-background-color: #f5a623; -fx-text-fill: white;" +
                        "-fx-font-size: 15; -fx-font-weight: bold;" +
                        "-fx-background-radius: 10; -fx-cursor: hand;"
        ));
        btnFormateur.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/org/example/AccueilFormateur.fxml"));
                stage.setScene(new Scene(loader.load(), 1200, 750));
                stage.setTitle("Formateur — Tableau de bord");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Bouton Étudiant
        Button btnEtudiant = new Button("👨‍🎓  Espace Étudiant");
        btnEtudiant.setPrefWidth(280);
        btnEtudiant.setPrefHeight(55);
        btnEtudiant.setStyle(
                "-fx-background-color: #2d3561; -fx-text-fill: white;" +
                        "-fx-font-size: 15; -fx-font-weight: bold;" +
                        "-fx-background-radius: 10; -fx-cursor: hand;"
        );
        btnEtudiant.setOnMouseEntered(e -> btnEtudiant.setStyle(
                "-fx-background-color: #3d4571; -fx-text-fill: white;" +
                        "-fx-font-size: 15; -fx-font-weight: bold;" +
                        "-fx-background-radius: 10; -fx-cursor: hand;"
        ));
        btnEtudiant.setOnMouseExited(e -> btnEtudiant.setStyle(
                "-fx-background-color: #2d3561; -fx-text-fill: white;" +
                        "-fx-font-size: 15; -fx-font-weight: bold;" +
                        "-fx-background-radius: 10; -fx-cursor: hand;"
        ));
        btnEtudiant.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/org/example/AccueilEtudiant.fxml"));
                stage.setScene(new Scene(loader.load(), 1100, 700));
                stage.setTitle("Étudiant — Espace Cours");
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        // Bouton Admin
        Button btnAdmin = new Button("🔧  Espace Admin");
        btnAdmin.setPrefWidth(280);
        btnAdmin.setPrefHeight(55);
        btnAdmin.setStyle(
                "-fx-background-color: #c0392b; -fx-text-fill: white;" +
                        "-fx-font-size: 15; -fx-font-weight: bold;" +
                        "-fx-background-radius: 10; -fx-cursor: hand;"
        );
        btnAdmin.setOnMouseEntered(e -> btnAdmin.setStyle(
                "-fx-background-color: #a93226; -fx-text-fill: white;" +
                        "-fx-font-size: 15; -fx-font-weight: bold;" +
                        "-fx-background-radius: 10; -fx-cursor: hand;"
        ));
        btnAdmin.setOnMouseExited(e -> btnAdmin.setStyle(
                "-fx-background-color: #c0392b; -fx-text-fill: white;" +
                        "-fx-font-size: 15; -fx-font-weight: bold;" +
                        "-fx-background-radius: 10; -fx-cursor: hand;"
        ));
        btnAdmin.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/org/example/AdminQuiz.fxml"));
                stage.setScene(new Scene(loader.load(), 1100, 700));
                stage.setTitle("Admin — Gestion");
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        root.getChildren().addAll(logo, subtitle, sep,
                btnFormateur, btnEtudiant, btnAdmin);

        Scene scene = new Scene(root, 500, 500);
        stage.setTitle("eduverse — Accueil");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}