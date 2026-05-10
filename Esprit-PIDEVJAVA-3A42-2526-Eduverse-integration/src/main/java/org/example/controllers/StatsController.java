package org.example.controllers;

import org.example.entities.Sujet;
import org.example.services.MessageService;
import org.example.services.SujetService;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class StatsController {
    @FXML private VBox cardsBox;

    private final SujetService sujetService = new SujetService();
    private final MessageService messageService = new MessageService();

    @FXML
    public void initialize() {
        try {
            cardsBox.getChildren().setAll(
                    card("Total sujets", String.valueOf(sujetService.countAll())),
                    card("Total messages", String.valueOf(messageService.countAll())),
                    topAuthorsCard(),
                    subjectCard("Sujet le plus discuté", sujetService.getMostDiscussed()),
                    subjectCard("Dernier sujet créé", sujetService.getLatest())
            );
        } catch (RuntimeException e) {
            cardsBox.getChildren().setAll(card("Statistiques", "Impossible de charger les statistiques"));
        }
    }

    private VBox card(String title, String value) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("card-title");
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("stat-value");
        VBox card = new VBox(10, titleLabel, valueLabel);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(16));
        return card;
    }

    private VBox topAuthorsCard() {
        List<Map<String, Object>> authors = sujetService.topAuthors();
        StringBuilder builder = new StringBuilder();
        for (Map<String, Object> author : authors) {
            builder.append(author.get("username")).append(" — ")
                    .append(author.get("total")).append(" activités\n");
        }
        return card("Top 3 auteurs", builder.toString().trim().isEmpty() ? "Aucune donnée" : builder.toString().trim());
    }

    private VBox subjectCard(String title, Sujet sujet) {
        if (sujet == null) {
            return card(title, "Aucune donnée");
        }
        String date = sujet.getDateCreation() == null ? "" : sujet.getDateCreation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        String value = sujet.getTitre() + "\n" + date;
        return card(title, value);
    }
}