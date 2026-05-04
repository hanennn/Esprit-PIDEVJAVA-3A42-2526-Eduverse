package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import models.Event;

public class EventDetailController {

    @FXML private Label lblTitre;
    @FXML private Label lblDesc;
    @FXML private Label lblDate;
    @FXML private Label lblHoraire;
    @FXML private Label lblNiveau;
    @FXML private Label lblLien;
    @FXML private Label lblTypeTag;
    @FXML private Label lblNiveauTag;

    public void setEvent(Event event) {
        lblTitre.setText(event.getTitre() != null ? event.getTitre() : "—");
        lblDesc.setText(event.getDescription() != null ? event.getDescription() : "Aucune description disponible.");
        lblDate.setText(event.getDate() != null ? event.getDate().toString() : "—");
        
        String debut = event.getHeureDeb() != null ? event.getHeureDeb().toString() : "—";
        String fin   = event.getHeureFin()  != null ? event.getHeureFin().toString()  : "—";
        lblHoraire.setText(debut + " → " + fin);
        
        lblNiveau.setText(event.getNiveau() != null ? event.getNiveau() : "—");
        lblLien.setText(event.getLienWebinaire() != null ? event.getLienWebinaire() : "Aucun lien disponible");
        lblTypeTag.setText(event.getType() != null ? event.getType() : "—");
        lblNiveauTag.setText(event.getNiveau() != null ? event.getNiveau() : "—");
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) lblTitre.getScene().getWindow();
        stage.close();
    }
}
