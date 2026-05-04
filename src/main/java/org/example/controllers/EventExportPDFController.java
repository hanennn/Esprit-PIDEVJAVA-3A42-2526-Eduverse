package org.example.controllers;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import org.example.entities.Event;
import org.example.services.EventService;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class EventExportPDFController {

    private final EventService eventService = new EventService();

    @FXML
    private void generatePDF(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
        fileChooser.setInitialFileName("Liste_Evenements.pdf");

        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            exportToPDF(file.getAbsolutePath());
        }
    }

    private void exportToPDF(String filePath) {
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.ORANGE);
            Paragraph title = new Paragraph("Liste des Événements Eduverse", fontTitle);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            addTableHeader(table);

            List<Event> events = eventService.getAll();
            for (Event e : events) {
                table.addCell(e.getTitre());
                table.addCell(e.getType());
                table.addCell(e.getDate() != null ? e.getDate().toString() : "-");
                table.addCell(e.getLieu() != null ? e.getLieu() : "-");
            }

            document.add(table);
            document.close();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Export PDF");
            alert.setHeaderText(null);
            alert.setContentText("Le fichier PDF a été généré avec succès !");
            alert.showAndWait();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void addTableHeader(PdfPTable table) {
        Font fontHead = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);
        String[] headers = {"Titre", "Type", "Date", "Lieu"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, fontHead));
            cell.setBackgroundColor(BaseColor.GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            table.addCell(cell);
        }
    }
}
