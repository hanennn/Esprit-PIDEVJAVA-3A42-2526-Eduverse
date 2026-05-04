package controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Event;

public class EventDetailHelper {

    public static void openDetail(Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(EventDetailHelper.class.getResource("/EventDetail.fxml"));
            Parent root = loader.load();
            EventDetailController controller = loader.getController();
            controller.setEvent(event);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Détail de l'événement : " + event.getTitre());
            stage.setScene(new Scene(root, 700, 550));
            stage.setResizable(false);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
