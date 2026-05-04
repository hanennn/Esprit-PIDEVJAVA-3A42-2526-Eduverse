package controllers;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.Event;
import models.EventInscription;
import services.EventService;
import services.EventInscriptionService;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ExportPDFController {

    @FXML private Label statusLabel;

    private final EventService eventService = new EventService();
    private final EventInscriptionService inscriptionService = new EventInscriptionService();

    @FXML
    private void generatePDF() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le rapport PDF");
        fileChooser.setInitialFileName("Eduverse_Rapport_Admin_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));

        File file = fileChooser.showSaveDialog(new Stage());

        if (file != null) {
            try {
                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();

                // Fonts
                Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
                Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);
                Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);

                // Title
                Paragraph title = new Paragraph("Rapport Administratif Eduverse", titleFont);
                title.setAlignment(Element.ALIGN_CENTER);
                document.add(title);
                
                document.add(new Paragraph("Généré le : " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date())));
                document.add(new Paragraph(" ")); // Spacer

                // 1. Events Table
                document.add(new Paragraph("Liste des Événements", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
                document.add(new Paragraph(" "));
                PdfPTable eventTable = new PdfPTable(4);
                eventTable.setWidthPercentage(100);
                addTableHeader(eventTable, new String[]{"Titre", "Type", "Date", "Niveau"}, headerFont);
                
                List<Event> events = eventService.getAll();
                for (Event e : events) {
                    eventTable.addCell(new Phrase(e.getTitre(), normalFont));
                    eventTable.addCell(new Phrase(e.getType(), normalFont));
                    eventTable.addCell(new Phrase(e.getDate().toString(), normalFont));
                    eventTable.addCell(new Phrase(e.getNiveau(), normalFont));
                }
                document.add(eventTable);
                document.add(new Paragraph(" "));

                // 2. Inscriptions Table
                document.add(new Paragraph("Liste des Inscriptions", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
                document.add(new Paragraph(" "));
                PdfPTable insTable = new PdfPTable(3);
                insTable.setWidthPercentage(100);
                addTableHeader(insTable, new String[]{"ID Participant", "ID Événement", "Statut"}, headerFont);
                
                List<EventInscription> inscriptions = inscriptionService.getAll();
                for (EventInscription i : inscriptions) {
                    insTable.addCell(new Phrase(String.valueOf(i.getParticipantId()), normalFont));
                    insTable.addCell(new Phrase(String.valueOf(i.getEventId()), normalFont));
                    insTable.addCell(new Phrase(i.getStatut(), normalFont));
                }
                document.add(insTable);
                document.add(new Paragraph(" "));

                // 3. Summary
                document.add(new Paragraph("Résumé des Statistiques", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
                document.add(new Paragraph("Nombre total d'événements : " + eventService.getTotalEvents()));
                document.add(new Paragraph("Nombre total d'inscriptions : " + inscriptionService.getTotalInscriptions()));

                document.close();
                statusLabel.setText("✅ PDF généré avec succès !");
                statusLabel.setStyle("-fx-text-fill: #27ae60;");

            } catch (Exception e) {
                e.printStackTrace();
                statusLabel.setText("❌ Erreur lors de la génération du PDF.");
                statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            }
        }
    }

    private void addTableHeader(PdfPTable table, String[] headers, Font font) {
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, font));
            cell.setBackgroundColor(new BaseColor(41, 128, 185)); // Eduverse Blue
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            table.addCell(cell);
        }
    }
}
