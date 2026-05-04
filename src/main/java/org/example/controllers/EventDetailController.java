package org.example.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.example.entities.Event;

public class EventDetailController {

    @FXML private Label lblTitre;
    @FXML private Label lblDesc;
    @FXML private Label lblTypeTag;
    @FXML private Label lblNiveauTag;
    @FXML private Label lblDate;
    @FXML private Label lblHoraire;
    @FXML private Label lblNiveau;
    @FXML private Label lblLien;

    public void setEventData(Event e) {
        lblTitre.setText(e.getTitre());
        lblDesc.setText(e.getDescription());
        lblTypeTag.setText(e.getType());
        lblNiveauTag.setText(e.getNiveau());
        lblNiveau.setText(e.getNiveau());
        lblDate.setText(e.getDate() != null ? e.getDate().toString() : "N/A");
        
        String hDeb = (e.getHeureDeb() != null) ? e.getHeureDeb().toString() : "??";
        String hFin = (e.getHeureFin() != null) ? e.getHeureFin().toString() : "??";
        lblHoraire.setText(hDeb + " - " + hFin);
        
        lblLien.setText(e.getLienWebinaire() != null && !e.getLienWebinaire().isEmpty() 
                        ? e.getLienWebinaire() : "Aucun lien");
    }

    @FXML
    private void handleClose(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}
