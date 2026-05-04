package utils;

import models.User;
import models.bourses;
import models.demande;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PdfService {

    public static void genererAttestationDemande(demande d, bourses bourse, User etudiant, String cheminFichier) throws IOException {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        PDPageContentStream content = new PDPageContentStream(document, page);

        float largeurPage = page.getMediaBox().getWidth();
        float y = 750;

        // En-tete
        content.setFont(PDType1Font.HELVETICA_BOLD, 22);
        String titre = "ATTESTATION DE DEMANDE DE BOURSE";
        float largeurTitre = PDType1Font.HELVETICA_BOLD.getStringWidth(titre) / 1000 * 22;
        content.beginText();
        content.newLineAtOffset((largeurPage - largeurTitre) / 2, y);
        content.showText(titre);
        content.endText();

        y -= 30;
        content.setFont(PDType1Font.HELVETICA, 14);
        String sousTitre = "Plateforme Eduverse - Gestion des Bourses";
        float largeurSousTitre = PDType1Font.HELVETICA.getStringWidth(sousTitre) / 1000 * 14;
        content.beginText();
        content.newLineAtOffset((largeurPage - largeurSousTitre) / 2, y);
        content.showText(sousTitre);
        content.endText();

        y -= 20;
        content.setLineWidth(1.5f);
        content.moveTo(50, y);
        content.lineTo(largeurPage - 50, y);
        content.stroke();

        // Date de generation
        y -= 30;
        content.setFont(PDType1Font.HELVETICA, 10);
        String dateGeneration = "Document genere le : " + new SimpleDateFormat("dd/MM/yyyy a HH:mm").format(new Date());
        content.beginText();
        content.newLineAtOffset(50, y);
        content.showText(dateGeneration);
        content.endText();

        // Section etudiant
        y -= 40;
        content.setFont(PDType1Font.HELVETICA_BOLD, 14);
        content.beginText();
        content.newLineAtOffset(50, y);
        content.showText("Informations de l'etudiant");
        content.endText();

        y -= 5;
        content.moveTo(50, y);
        content.lineTo(250, y);
        content.stroke();

        y -= 22;
        content.setFont(PDType1Font.HELVETICA, 11);
        content.beginText();
        content.newLineAtOffset(70, y);
        content.showText("Nom complet : " + etudiant.getPrenom() + " " + etudiant.getNom());
        content.endText();

        y -= 18;
        content.beginText();
        content.newLineAtOffset(70, y);
        content.showText("Email : " + etudiant.getEmail());
        content.endText();

        // Section bourse
        y -= 35;
        content.setFont(PDType1Font.HELVETICA_BOLD, 14);
        content.beginText();
        content.newLineAtOffset(50, y);
        content.showText("Informations de la bourse");
        content.endText();

        y -= 5;
        content.moveTo(50, y);
        content.lineTo(250, y);
        content.stroke();

        y -= 22;
        content.setFont(PDType1Font.HELVETICA, 11);
        content.beginText();
        content.newLineAtOffset(70, y);
        content.showText("Bourse : " + bourse.getTitre());
        content.endText();

        y -= 18;
        content.beginText();
        content.newLineAtOffset(70, y);
        content.showText("Montant : " + String.format("%.2f", bourse.getMontant()) + " DT");
        content.endText();

        y -= 18;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String dateAttr = bourse.getDate_attribution() != null ? sdf.format(bourse.getDate_attribution()) : "N/A";
        content.beginText();
        content.newLineAtOffset(70, y);
        content.showText("Date d'attribution : " + dateAttr);
        content.endText();

        y -= 18;
        String dateFin = bourse.getDate_fin() != null ? sdf.format(bourse.getDate_fin()) : "N/A";
        content.beginText();
        content.newLineAtOffset(70, y);
        content.showText("Date de fin : " + dateFin);
        content.endText();

        // Section demande
        y -= 35;
        content.setFont(PDType1Font.HELVETICA_BOLD, 14);
        content.beginText();
        content.newLineAtOffset(50, y);
        content.showText("Details de la demande");
        content.endText();

        y -= 5;
        content.moveTo(50, y);
        content.lineTo(250, y);
        content.stroke();

        y -= 22;
        content.setFont(PDType1Font.HELVETICA, 11);
        String dateDemande = d.getDate_demande() != null ? sdf.format(d.getDate_demande()) : "N/A";
        content.beginText();
        content.newLineAtOffset(70, y);
        content.showText("Date de demande : " + dateDemande);
        content.endText();

        y -= 18;
        content.beginText();
        content.newLineAtOffset(70, y);
        content.showText("Niveau d'etudes : " + d.getNiveau_etudes());
        content.endText();

        y -= 18;
        content.setFont(PDType1Font.HELVETICA_BOLD, 11);
        content.beginText();
        content.newLineAtOffset(70, y);
        content.showText("Statut : " + d.getStatut());
        content.endText();

        y -= 18;
        content.setFont(PDType1Font.HELVETICA, 11);
        String note = (d.getNote() != null && !d.getNote().isEmpty()) ? d.getNote() : "Non attribuee";
        content.beginText();
        content.newLineAtOffset(70, y);
        content.showText("Note : " + note);
        content.endText();

        // Lettre de motivation
        y -= 35;
        content.setFont(PDType1Font.HELVETICA_BOLD, 14);
        content.beginText();
        content.newLineAtOffset(50, y);
        content.showText("Lettre de motivation");
        content.endText();

        y -= 5;
        content.moveTo(50, y);
        content.lineTo(250, y);
        content.stroke();

        y -= 20;
        content.setFont(PDType1Font.HELVETICA, 10);
        String lettreMotivation = d.getLettre_motivation() != null ? d.getLettre_motivation() : "";
        int maxCaracteresParLigne = 90;
        int index = 0;
        while (index < lettreMotivation.length() && y > 80) {
            int fin = Math.min(index + maxCaracteresParLigne, lettreMotivation.length());
            // ne pas couper un mot en plein milieu
            if (fin < lettreMotivation.length() && lettreMotivation.charAt(fin) != ' ') {
                int dernierEspace = lettreMotivation.lastIndexOf(' ', fin);
                if (dernierEspace > index) {
                    fin = dernierEspace;
                }
            }
            String ligne = lettreMotivation.substring(index, fin).trim();
            content.beginText();
            content.newLineAtOffset(70, y);
            content.showText(ligne);
            content.endText();
            y -= 15;
            index = fin;
        }

        // Pied de page
        content.moveTo(50, 70);
        content.lineTo(largeurPage - 50, 70);
        content.stroke();

        content.setFont(PDType1Font.HELVETICA_OBLIQUE, 9);
        String piedDePage = "Ce document a ete genere automatiquement par la plateforme Eduverse.";
        float largeurPied = PDType1Font.HELVETICA_OBLIQUE.getStringWidth(piedDePage) / 1000 * 9;
        content.beginText();
        content.newLineAtOffset((largeurPage - largeurPied) / 2, 55);
        content.showText(piedDePage);
        content.endText();

        content.close();
        document.save(new File(cheminFichier));
        document.close();

        System.out.println("PDF genere avec succes : " + cheminFichier);
    }
}
