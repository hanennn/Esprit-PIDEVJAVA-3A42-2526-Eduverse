package controllers;

import entities.Session;

import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.awt.*;
import java.io.IOException;
import java.sql.SQLException;

public class ProfessorController {

    @FXML private Button logoutBtn;

    @FXML
    void logout(ActionEvent event) throws SQLException {
        try {
            Session.logout();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainView.fxml"));
            Parent root = loader.load();
            logoutBtn.getScene().setRoot(root);
        }catch (IOException e){
            System.err.println(e.getMessage());
        }
    }
}
