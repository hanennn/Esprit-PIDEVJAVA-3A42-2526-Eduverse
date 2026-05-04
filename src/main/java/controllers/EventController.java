package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import models.Event;
import services.EventService;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import javafx.concurrent.Task;
import org.json.JSONObject;
import org.json.JSONArray;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.image.ImageView;
import javafx.application.Platform;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import netscape.javascript.JSObject;
import javafx.concurrent.Worker;

public class EventController {

    @FXML
    private TableView<Event> tvEvents;
    @FXML
    private TableColumn<Event, Integer> colId;
    @FXML
    private TableColumn<Event, String> colTitre;
    @FXML
    private TableColumn<Event, String> colDesc;
    @FXML
    private TableColumn<Event, String> colType;
    @FXML
    private TableColumn<Event, String> colLien;
    @FXML
    private TableColumn<Event, String> colLieu;
    @FXML
    private TableColumn<Event, String> colNiveau;
    @FXML
    private TableColumn<Event, Date> colDate;
    @FXML
    private TableColumn<Event, Time> colHeureDeb;
    @FXML
    private TableColumn<Event, Time> colHeureFin;

    @FXML
    private TextField tfId;
    @FXML
    private TextField tfTitre;
    @FXML
    private TextField tfDesc;
    @FXML
    private TextField tfType;
    @FXML
    private TextField tfLien;
    @FXML
    private TextField tfLieu;
    @FXML
    private TextField tfNiveau;
    @FXML
    private DatePicker dpDate;
    @FXML
    private TextField tfHeureDeb;
    @FXML
    private TextField tfHeureFin;
    @FXML
    private TextField tfImage;
    @FXML
    private TextField tfSearch;
    @FXML
    private ComboBox<String> cbSort;
    @FXML
    private Label lblError;
    @FXML
    private WebView mapWebView;

    // Weather UI components
    @FXML
    private VBox boxWeather;
    @FXML
    private Label lblWeatherCity;
    @FXML
    private Label lblWeatherTemp;
    @FXML
    private Label lblWeatherDesc;
    @FXML
    private ImageView ivWeatherIcon;
    @FXML
    private Label lblWeatherWarning;
    @FXML
    private ProgressIndicator piWeather;

   's/private static final String OPENWEATHER_API_KEY = ".*"/private static final String OPENWEATHER_API_KEY = System.getenv("OPENWEATHER_API_KEY")/'

    private EventService eventService;
    private ObservableList<Event> eventList;

    @FXML
    public void initialize() {
        eventService = new EventService();

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colLien.setCellValueFactory(new PropertyValueFactory<>("lienWebinaire"));
        colLieu.setCellValueFactory(new PropertyValueFactory<>("lieu"));
        colNiveau.setCellValueFactory(new PropertyValueFactory<>("niveau"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colHeureDeb.setCellValueFactory(new PropertyValueFactory<>("heureDeb"));
        colHeureFin.setCellValueFactory(new PropertyValueFactory<>("heureFin"));

        cbSort.setItems(FXCollections.observableArrayList(
                "Plus récent", "Plus ancien", "Titre (A-Z)", "Titre (Z-A)"));

        initMap();
        loadData();
    }

    private void initMap() {
        WebEngine engine = mapWebView.getEngine();
        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) engine.executeScript("window");
                window.setMember("javaConnector", this);
            }
        });
        // Charger la carte (utiliser URL local)
        try {
            String url = getClass().getResource("/map.html").toExternalForm();
            engine.load(url);
        } catch (Exception e) {
            System.err.println("Map introuvable: " + e.getMessage());
        }
    }

    private void loadData() {
        List<Event> events = eventService.getAll();
        eventList = FXCollections.observableArrayList(events);

        // Wrap the ObservableList in a FilteredList
        FilteredList<Event> filteredData = new FilteredList<>(eventList, p -> true);

        // Set the filter Predicate whenever the filter changes.
        tfSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(event -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                if (event.getTitre().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (event.getType().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (event.getDescription() != null
                        && event.getDescription().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
        });

        // Wrap the FilteredList in a SortedList.
        SortedList<Event> sortedData = new SortedList<>(filteredData);

        // Bind the SortedList comparator to the TableView comparator.
        sortedData.comparatorProperty().bind(tvEvents.comparatorProperty());

        // Custom sorting based on ComboBox
        cbSort.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                switch (newVal) {
                    case "Plus récent":
                        tvEvents.getSortOrder().clear();
                        colDate.setSortType(TableColumn.SortType.DESCENDING);
                        tvEvents.getSortOrder().add(colDate);
                        break;
                    case "Plus ancien":
                        tvEvents.getSortOrder().clear();
                        colDate.setSortType(TableColumn.SortType.ASCENDING);
                        tvEvents.getSortOrder().add(colDate);
                        break;
                    case "Titre (A-Z)":
                        tvEvents.getSortOrder().clear();
                        colTitre.setSortType(TableColumn.SortType.ASCENDING);
                        tvEvents.getSortOrder().add(colTitre);
                        break;
                    case "Titre (Z-A)":
                        tvEvents.getSortOrder().clear();
                        colTitre.setSortType(TableColumn.SortType.DESCENDING);
                        tvEvents.getSortOrder().add(colTitre);
                        break;
                }
            }
        });

        tvEvents.setItems(sortedData);
    }

    @FXML
    private void handleAdd(ActionEvent event) {
        Event e = buildEventFromForm();
        if (e != null) {
            e.setDateCreation(new Timestamp(System.currentTimeMillis()));
            eventService.add(e);
            loadData();
            handleClear();
        }
    }

    @FXML
    private void handleUpdate(ActionEvent event) {
        if (tfId.getText().isEmpty())
            return;
        Event e = buildEventFromForm();
        if (e != null) {
            e.setId(Integer.parseInt(tfId.getText()));
            e.setDateCreation(new Timestamp(System.currentTimeMillis()));
            eventService.update(e);
            loadData();
            handleClear();
        }
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        if (!tfId.getText().isEmpty()) {
            eventService.delete(Integer.parseInt(tfId.getText()));
            loadData();
            handleClear();
        }
    }

    @FXML
    private void handleClear() {
        lblError.setVisible(false);
        tfId.clear();
        tfTitre.clear();
        tfDesc.clear();
        tfType.clear();
        tfLien.clear();
        tfLieu.clear();
        tfNiveau.clear();
        dpDate.setValue(null);
        tfHeureDeb.clear();
        tfHeureFin.clear();
        tfImage.clear();
        boxWeather.setVisible(false);
        boxWeather.setManaged(false);
    }

    @FXML
    private void handleCheckWeather(ActionEvent event) {
        String city = tfLieu.getText().trim();
        if (city.isEmpty()) {
            showError("Veuillez saisir une ville dans le champ 'Lieu' pour vérifier la météo.");
            return;
        }

        boxWeather.setVisible(false);
        boxWeather.setManaged(false);
        lblWeatherWarning.setVisible(false);
        lblWeatherWarning.setManaged(false);
        piWeather.setVisible(true);
        piWeather.setManaged(true);

        Task<JSONObject> weatherTask = new Task<JSONObject>() {
            @Override
            protected JSONObject call() throws Exception {
                String url = String.format(
                        "https://api.openweathermap.org/data/2.5/forecast?q=%s&appid=%s&units=metric&lang=fr",
                        city.replace(" ", "%20"), OPENWEATHER_API_KEY);

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    return new JSONObject(response.body());
                } else if (response.statusCode() == 404) {
                    throw new Exception("Ville introuvable. Veuillez vérifier l'orthographe.");
                } else {
                    throw new Exception("Erreur de l'API (Code: " + response.statusCode() + ").");
                }
            }
        };

        weatherTask.setOnSucceeded(e -> {
            piWeather.setVisible(false);
            piWeather.setManaged(false);
            JSONObject json = weatherTask.getValue();
            updateWeatherUI(json);
        });

        weatherTask.setOnFailed(e -> {
            piWeather.setVisible(false);
            piWeather.setManaged(false);
            Throwable ex = weatherTask.getException();
            showError("Erreur météo: " + ex.getMessage());
        });

        new Thread(weatherTask).start();
    }

    private void updateWeatherUI(JSONObject json) {
        try {
            JSONObject cityObj = json.getJSONObject("city");
            String cityName = cityObj.getString("name");
            String country = cityObj.getString("country");

            JSONArray list = json.getJSONArray("list");
            if (list.length() > 0) {
                // On prend la première prévision disponible
                JSONObject forecast = list.getJSONObject(0);
                JSONObject main = forecast.getJSONObject("main");
                double temp = main.getDouble("temp");

                JSONArray weatherArray = forecast.getJSONArray("weather");
                JSONObject weather = weatherArray.getJSONObject(0);
                String description = weather.getString("description");
                String iconCode = weather.getString("icon");

                lblWeatherCity.setText(cityName + ", " + country);
                lblWeatherTemp.setText(String.format("%.1f°C", temp));
                lblWeatherDesc.setText(description.substring(0, 1).toUpperCase() + description.substring(1));

                String iconUrl = "https://openweathermap.org/img/wn/" + iconCode + "@2x.png";
                ivWeatherIcon.setImage(new Image(iconUrl, true));

                // Vérifier si des conditions défavorables sont présentes
                String descLower = description.toLowerCase();
                if (descLower.contains("pluie") || descLower.contains("orage") || descLower.contains("neige")
                        || descLower.contains("tempête")) {
                    lblWeatherWarning.setText("⚠️ Attention : Mauvaises conditions météo prévues (" + description
                            + "). L'événement pourrait être impacté.");
                    lblWeatherWarning.setVisible(true);
                    lblWeatherWarning.setManaged(true);
                }

                boxWeather.setVisible(true);
                boxWeather.setManaged(true);
            }
        } catch (Exception ex) {
            showError("Erreur lors de l'analyse des données météo : " + ex.getMessage());
        }
    }

    /**
     * Pré-remplit le formulaire de création avec la suggestion de l'IA.
     * Appelé par AIAssistantController via le bouton "Créer cet événement".
     */
    public void prefillFromAISuggestion(String titre, String description,
            String lieu, String type, String niveau) {
        handleClear();
        if (titre != null)
            tfTitre.setText(titre);
        if (description != null)
            tfDesc.setText(description);
        if (lieu != null)
            tfLieu.setText(lieu);
        if (type != null)
            tfType.setText(type);
        if (niveau != null)
            tfNiveau.setText(niveau);
    }

    @FXML
    private void handleTableClick() {
        Event selected = tvEvents.getSelectionModel().getSelectedItem();
        if (selected != null) {
            tfId.setText(String.valueOf(selected.getId()));
            tfTitre.setText(selected.getTitre());
            tfDesc.setText(selected.getDescription());
            tfType.setText(selected.getType());
            tfLien.setText(selected.getLienWebinaire());
            tfLieu.setText(selected.getLieu() != null ? selected.getLieu() : "");
            tfNiveau.setText(selected.getNiveau());
            if (selected.getDate() != null) {
                dpDate.setValue(selected.getDate().toLocalDate());
            } else {
                dpDate.setValue(null);
            }
            tfHeureDeb.setText(selected.getHeureDeb() != null ? selected.getHeureDeb().toString() : "");
            tfHeureFin.setText(selected.getHeureFin() != null ? selected.getHeureFin().toString() : "");
            tfImage.setText(selected.getImage());
        }
    }

    @FXML
    private void handleViewDetail(ActionEvent event) {
        Event selected = tvEvents.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Aucune sélection");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez sélectionner un événement dans le tableau.");
            alert.showAndWait();
            return;
        }
        EventDetailHelper.openDetail(selected);
    }

    @FXML
    private void handleSearchMap(ActionEvent event) {
        String address = tfLieu.getText().trim();
        if (address.isEmpty()) {
            showError("Veuillez saisir une adresse à rechercher.");
            return;
        }

        Task<JSONObject> searchTask = new Task<JSONObject>() {
            @Override
            protected JSONObject call() throws Exception {
                String url = String.format("https://nominatim.openstreetmap.org/search?format=json&q=%s",
                        address.replace(" ", "%20"));

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("User-Agent", "EduverseApp/1.0 (Contact: admin@eduverse.com)") // Requis par Nominatim
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    JSONArray jsonArray = new JSONArray(response.body());
                    if (jsonArray.length() > 0) {
                        return jsonArray.getJSONObject(0);
                    } else {
                        throw new Exception("Lieu non trouvé.");
                    }
                } else {
                    throw new Exception("Erreur de l'API (Code: " + response.statusCode() + ").");
                }
            }
        };

        searchTask.setOnSucceeded(e -> {
            JSONObject json = searchTask.getValue();
            double lat = json.getDouble("lat");
            double lon = json.getDouble("lon");
            // Mettre à jour la carte en exécutant du JS
            Platform.runLater(() -> {
                mapWebView.getEngine().executeScript("updateMap(" + lat + ", " + lon + ")");
            });
        });

        searchTask.setOnFailed(e -> {
            Throwable ex = searchTask.getException();
            showError("Erreur recherche: " + ex.getMessage());
        });

        new Thread(searchTask).start();
    }

    // Méthode appelée depuis JavaScript
    public void setCoordinates(double lat, double lng) {
        Task<String> reverseGeoTask = new Task<String>() {
            @Override
            protected String call() throws Exception {
                String url = String.format("https://nominatim.openstreetmap.org/reverse?format=json&lat=%s&lon=%s", lat, lng);

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("User-Agent", "EduverseApp/1.0 (Contact: admin@eduverse.com)")
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    JSONObject json = new JSONObject(response.body());
                    return json.optString("display_name", "Lieu inconnu");
                } else {
                    throw new Exception("Erreur Reverse Geo");
                }
            }
        };

        reverseGeoTask.setOnSucceeded(e -> {
            String address = reverseGeoTask.getValue();
            Platform.runLater(() -> {
                tfLieu.setText(address);
            });
        });

        new Thread(reverseGeoTask).start();
    }

    private Event buildEventFromForm() {
        lblError.setVisible(false);
        try {
            if (tfTitre.getText().isEmpty() || tfType.getText().isEmpty() || tfNiveau.getText().isEmpty()) {
                showError("Veuillez remplir tous les champs obligatoires (Titre, Type, Niveau).");
                return null;
            }

            Event e = new Event();
            e.setTitre(tfTitre.getText());
            e.setDescription(tfDesc.getText());
            e.setType(tfType.getText());
            e.setLienWebinaire(tfLien.getText());
            e.setLieu(tfLieu.getText());
            e.setNiveau(tfNiveau.getText());

            if (dpDate.getValue() != null) {
                e.setDate(Date.valueOf(dpDate.getValue()));
            } else {
                showError("La date est obligatoire.");
                return null;
            }

            if (tfHeureDeb.getText() != null && !tfHeureDeb.getText().trim().isEmpty()) {
                e.setHeureDeb(Time.valueOf(tfHeureDeb.getText()));
            }
            if (tfHeureFin.getText() != null && !tfHeureFin.getText().trim().isEmpty()) {
                e.setHeureFin(Time.valueOf(tfHeureFin.getText()));
            }

            // Validation de la cohérence temporelle
            if (e.getHeureDeb() != null && e.getHeureFin() != null) {
                if (!e.getHeureDeb().before(e.getHeureFin())) {
                    showError("L'heure de début doit être strictement avant l'heure de fin.");
                    return null;
                }
            } else if (e.getHeureDeb() == null || e.getHeureFin() == null) {
                showError("Veuillez saisir à la fois l'heure de début et l'heure de fin.");
                return null;
            }

            e.setImage(tfImage.getText());
            return e;
        } catch (Exception ex) {
            showError("Format invalide (Heure: HH:mm:ss). " + ex.getMessage());
            return null;
        }
    }

    private void showError(String message) {
        lblError.setText(message);
        lblError.setVisible(true);
        lblError.setManaged(true);
    }
}
