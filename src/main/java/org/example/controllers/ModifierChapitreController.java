package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import org.example.entities.chapitres;
import org.example.entities.cours;
import org.example.services.chapitresservices;

public class ModifierChapitreController {

    @FXML private TextField    titreChap;
    @FXML private TextArea     descChap;
    @FXML private TextField    ordreChap;
    @FXML private TextField    dureeChap;
    @FXML private ComboBox<String> statutChap;
    @FXML private TextField    contenuChap;
    @FXML private ComboBox<String> typeContenu;
    @FXML private Label        titreCours;

    private chapitres chapitreAModifier;
    private cours     coursSelectionne;

    @FXML
    public void initialize() {
        statutChap.getItems().addAll("OUVERT", "NON OUVERT");
        typeContenu.getItems().addAll("pdf", "video", "image", "texte");
    }

    public void setChapitre(chapitres ch, cours c) {
        this.chapitreAModifier = ch;
        this.coursSelectionne  = c;
//remplir champ
        titreCours.setText("Modifier le chapitre — " + c.getTitre_cours());
        titreChap.setText(ch.getTitre_chap());
        descChap.setText(ch.getDesc_chap());
        ordreChap.setText(String.valueOf(ch.getOrdre_chap()));
        dureeChap.setText(ch.getDuree_chap());
        statutChap.setValue(ch.getStatut_chap());
        contenuChap.setText(ch.getContenu_chap());
        typeContenu.setValue(ch.getType_contenu());
    }

    @FXML
    void modifierChapitre() {
        if (titreChap.getText().isEmpty()) {
            showError("Le titre est obligatoire !");
            titreChap.setStyle("-fx-border-color: red;");
            return;
        }

        chapitreAModifier.setTitre_chap(titreChap.getText());
        chapitreAModifier.setDesc_chap(descChap.getText());
        chapitreAModifier.setOrdre_chap(Integer.parseInt(ordreChap.getText()));
        chapitreAModifier.setDuree_chap(dureeChap.getText());
        chapitreAModifier.setStatut_chap(statutChap.getValue());
        chapitreAModifier.setContenu_chap(contenuChap.getText());
        chapitreAModifier.setType_contenu(typeContenu.getValue());

        try {
            new chapitresservices().modifier(chapitreAModifier.getId(), chapitreAModifier);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setContentText("Chapitre modifié avec succès !");
            alert.show();

            retour();
        } catch (Exception e) {
            showError("Erreur lors de la modification !");
        }
    }

    @FXML
    void annuler() {
        setChapitre(chapitreAModifier, coursSelectionne);
        titreChap.setStyle("");
    }

    @FXML
    void retour() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/ListeChapitres.fxml"));
            Parent root = loader.load();
            ListeChapitresController controller = loader.getController();
            controller.setCours(coursSelectionne);
            titreChap.getScene().setRoot(root);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
//alert error
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setContentText(message);
        alert.show();
    }
}
