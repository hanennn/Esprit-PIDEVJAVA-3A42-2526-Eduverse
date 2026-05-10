package org.example.controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.entities.Event;

import java.io.IOException;

public class EventDetailHelper {

    public static void openDetail(Event e) {
        try {
            FXMLLoader loader = new FXMLLoader(EventDetailHelper.class.getResource("/EventDetail.fxml"));
            Parent root = loader.load();

            EventDetailController controller = loader.getController();
            controller.setEventData(e);

            Stage stage = new Stage();
            stage.setTitle("Détails de l'événement : " + e.getTitre());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
