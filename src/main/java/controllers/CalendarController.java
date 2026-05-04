package controllers;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import models.Event;
import services.EventService;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class CalendarController {

    @FXML private Label monthYearLabel;
    @FXML private GridPane calendarGrid;
    
    // Side Panel elements
    @FXML private VBox eventDetailsPane;
    @FXML private VBox emptyDetailsPane;
    @FXML private Label detailTitre;
    @FXML private Label detailDate;
    @FXML private Label detailHeure;
    @FXML private Label detailType;
    @FXML private Label detailNiveau;
    @FXML private Label detailDescription;

    private final EventService eventService = new EventService();
    private YearMonth currentYearMonth;
    private List<Event> allEvents;

    @FXML
    public void initialize() {
        currentYearMonth = YearMonth.now();
        allEvents = eventService.getAll();
        drawCalendar();
    }

    private void drawCalendar() {
        calendarGrid.getChildren().clear();
        
        // Month Year header
        String monthName = currentYearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH);
        monthYearLabel.setText(monthName.substring(0, 1).toUpperCase() + monthName.substring(1) + " " + currentYearMonth.getYear());

        // Day names header
        String[] dayNames = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"};
        for (int i = 0; i < 7; i++) {
            Label dayLabel = new Label(dayNames[i]);
            dayLabel.getStyleClass().add("calendar-header-day");
            calendarGrid.add(dayLabel, i, 0);
            GridPane.setHalignment(dayLabel, javafx.geometry.HPos.CENTER);
        }

        LocalDate calendarDate = currentYearMonth.atDay(1);
        int dayOfWeek = calendarDate.getDayOfWeek().getValue(); // 1 = Mon, 7 = Sun
        int daysInMonth = currentYearMonth.lengthOfMonth();

        int row = 1;
        int col = dayOfWeek - 1;

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentYearMonth.atDay(day);
            VBox dayBox = createDayBox(date);
            calendarGrid.add(dayBox, col, row);
            
            col++;
            if (col > 6) {
                col = 0;
                row++;
            }
        }
    }

    private VBox createDayBox(LocalDate date) {
        VBox box = new VBox(5);
        box.getStyleClass().add("calendar-day-box");
        box.setAlignment(Pos.TOP_LEFT);
        box.setPadding(new javafx.geometry.Insets(5));

        Label dayLabel = new Label(String.valueOf(date.getDayOfMonth()));
        dayLabel.getStyleClass().add("calendar-day-number");
        box.getChildren().add(dayLabel);

        // Find events for this date
        List<Event> eventsOnDate = allEvents.stream()
                .filter(e -> {
                    LocalDate eventDate = e.getDate().toLocalDate();
                    return eventDate.equals(date);
                })
                .collect(Collectors.toList());

        if (!eventsOnDate.isEmpty()) {
            box.getStyleClass().add("calendar-day-has-event");
            for (Event e : eventsOnDate) {
                Label eventTitle = new Label(e.getTitre());
                eventTitle.getStyleClass().add("calendar-event-mini-title");
                eventTitle.setEllipsisString("...");
                eventTitle.setMaxWidth(100);
                box.getChildren().add(eventTitle);
            }
            
            box.setOnMouseClicked(event -> showEventDetails(eventsOnDate.get(0)));
        }

        return box;
    }

    private void showEventDetails(Event event) {
        emptyDetailsPane.setVisible(false);
        emptyDetailsPane.setManaged(false);
        eventDetailsPane.setVisible(true);
        eventDetailsPane.setManaged(true);

        detailTitre.setText(event.getTitre());
        detailDate.setText("📅 " + event.getDate().toString());
        detailHeure.setText("🕒 " + event.getHeureDeb() + " - " + event.getHeureFin());
        detailType.setText("🏷 Type : " + event.getType());
        detailNiveau.setText("🎓 Niveau : " + event.getNiveau());
        detailDescription.setText(event.getDescription());
    }

    @FXML
    private void previousMonth() {
        currentYearMonth = currentYearMonth.minusMonths(1);
        drawCalendar();
    }

    @FXML
    private void nextMonth() {
        currentYearMonth = currentYearMonth.plusMonths(1);
        drawCalendar();
    }
}
