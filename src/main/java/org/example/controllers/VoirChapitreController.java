package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.*;
import javafx.scene.layout.VBox;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.example.entities.chapitres;
import org.example.entities.cours;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class VoirChapitreController {

    @FXML private Label     labelTitre;
    @FXML private Label     labelDesc;
    @FXML private Label     labelOrdre;
    @FXML private Label     labelDuree;
    @FXML private Label     labelStatut;
    @FXML private Label     labelType;
    @FXML private Label     labelPageInfo;
    @FXML private ImageView imageViewPdf;
    @FXML private Button    btnPrev;
    @FXML private Button    btnNext;
    @FXML private VBox      pdfContainer;

    private chapitres       chapitreActuel;
    private cours           coursActuel;
    private List<Image>     pages = new ArrayList<>();
    private int             pageActuelle = 0;

    public void setChapitre(chapitres ch, cours c) {
        this.chapitreActuel = ch;
        this.coursActuel    = c;

        labelTitre.setText(ch.getTitre_chap());
        labelDesc.setText(ch.getDesc_chap());
        labelOrdre.setText("ORDRE: " + ch.getOrdre_chap());
        labelDuree.setText("DURÉE: " + ch.getDuree_chap());
        labelStatut.setText(ch.getStatut_chap());
        labelType.setText("TYPE: " + ch.getType_contenu().toUpperCase());

        chargerPDF(ch.getContenu_chap());
    }

    private void chargerPDF(String chemin) {
        File f = new File(chemin);
        if (!f.exists()) {
            labelPageInfo.setText("Fichier introuvable : " + chemin);
            return;
        }

        try {
            PDDocument doc = Loader.loadPDF(f);
            PDFRenderer renderer = new PDFRenderer(doc);
            pages.clear();

            for (int i = 0; i < doc.getNumberOfPages(); i++) {
                BufferedImage bImg = renderer.renderImageWithDPI(i, 150);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bImg, "PNG", baos);
                byte[] data = baos.toByteArray();
                Image fxImage = new Image(new ByteArrayInputStream(data));
                pages.add(fxImage);
            }
            doc.close();

            pageActuelle = 0;
            afficherPage();

        } catch (Exception e) {
            e.printStackTrace();
            labelPageInfo.setText("Erreur : " + e.getMessage());
        }
    }

    private void afficherPage() {
        if (pages.isEmpty()) return;
        imageViewPdf.setImage(pages.get(pageActuelle));
        labelPageInfo.setText("Page " + (pageActuelle + 1) + " / " + pages.size());
        btnPrev.setDisable(pageActuelle == 0);
        btnNext.setDisable(pageActuelle == pages.size() - 1);
    }

    @FXML void pagePrecedente() { if (pageActuelle > 0) { pageActuelle--; afficherPage(); } }
    @FXML void pageSuivante()   { if (pageActuelle < pages.size() - 1) { pageActuelle++; afficherPage(); } }
    @FXML void zoomIn()  { imageViewPdf.setFitWidth(imageViewPdf.getFitWidth() + 80); }
    @FXML void zoomOut() { imageViewPdf.setFitWidth(Math.max(300, imageViewPdf.getFitWidth() - 80)); }

    @FXML
    void retour() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/catalogueChapitres.fxml"));
            Parent root = loader.load();
            catalogueChapitresController ctrl = loader.getController();
            ctrl.setCours(coursActuel);
            labelTitre.getScene().setRoot(root);
        } catch (Exception e) { e.printStackTrace(); }
    }
}