package org.example.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.example.entities.Event;
import org.example.services.EventService;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

public class EventCalendarController {

    @FXML private Label lblMonthYear;
    @FXML private GridPane calendarGrid;

    private EventService eventService;
    private YearMonth currentYearMonth;

    @FXML
    public void initialize() {
        eventService = new EventService();
        currentYearMonth = YearMonth.now();
        drawCalendar();
    }

    private void drawCalendar() {
        calendarGrid.getChildren().clear();
        lblMonthYear.setText(currentYearMonth.getMonth().toString() + " " + currentYearMonth.getYear());

        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue(); // 1=Mon, 7=Sun
        int daysInMonth = currentYearMonth.lengthOfMonth();

        List<Event> allEvents = eventService.getAll();

        // Offset for the first day
        int col = dayOfWeek - 1;
        int row = 0;

        for (int day = 1; day <= daysInMonth; day++) {
            VBox dayBox = new VBox(5);
            dayBox.setAlignment(Pos.TOP_CENTER);
            dayBox.setStyle("-fx-border-color: #ddd; -fx-padding: 5; -fx-min-width: 100; -fx-min-height: 80;");

            Label lblDay = new Label(String.valueOf(day));
            lblDay.setStyle("-fx-font-weight: bold;");
            dayBox.getChildren().add(lblDay);

            LocalDate currentLocalDate = currentYearMonth.atDay(day);
            List<Event> eventsOnDay = allEvents.stream()
                    .filter(e -> e.getDate() != null && e.getDate().toLocalDate().equals(currentLocalDate))
                    .collect(Collectors.toList());

            for (Event e : eventsOnDay) {
                Label lblEvent = new Label(e.getTitre());
                lblEvent.setStyle("-fx-background-color: #e3f2fd; -fx-font-size: 10px; -fx-text-fill: #1976d2;");
                lblEvent.setWrapText(true);
                dayBox.getChildren().add(lblEvent);
            }

            calendarGrid.add(dayBox, col, row);

            col++;
            if (col > 6) {
                col = 0;
                row++;
            }
        }
    }

    @FXML
    private void prevMonth() {
        currentYearMonth = currentYearMonth.minusMonths(1);
        drawCalendar();
    }

    @FXML
    private void nextMonth() {
        currentYearMonth = currentYearMonth.plusMonths(1);
        drawCalendar();
    }
}
