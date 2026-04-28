package com.eduverse.forum.controllers;

import com.eduverse.forum.models.Sujet;
import com.eduverse.forum.models.User;
import com.eduverse.forum.services.MessageService;
import com.eduverse.forum.services.SujetService;
import com.eduverse.forum.services.UserService;
import com.eduverse.forum.utils.AppContext;
import com.eduverse.forum.utils.ReputationUtil;
import java.io.IOException;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class MainController {
    @FXML private StackPane contentHolder;
    @FXML private ComboBox<User> userComboBox;
    @FXML private Label reputationLabel;
    @FXML private Button adminButton;
    @FXML private Button statsButton;
    @FXML private Button badwordButton;

    private final UserService userService = new UserService();
    private final SujetService sujetService = new SujetService();
    private final MessageService messageService = new MessageService();
    private String currentView = "subjects";

    @FXML
    public void initialize() {
        AppContext.setMainController(this);
        loadUsers();
        showSubjectsView();
    }

    private void loadUsers() {
        try {
            List<User> users = userService.findActiveUsers();
            userComboBox.setItems(FXCollections.observableArrayList(users));
            userComboBox.setOnAction(event -> {
                User user = userComboBox.getValue();
                AppContext.setCurrentUser(user);
                refreshSidebar();
                reloadCurrentView();
            });
            if (!users.isEmpty()) {
                userComboBox.getSelectionModel().selectFirst();
                AppContext.setCurrentUser(users.get(0));
            }
            refreshSidebar();
        } catch (RuntimeException e) {
            AppContext.setCurrentUser(null);
            showError("Impossible de charger les utilisateurs", e.getMessage());
            refreshSidebar();
        }
    }

    private void refreshSidebar() {
        User currentUser = AppContext.getCurrentUser();
        boolean isAdmin = currentUser != null && currentUser.hasRole("ROLE_ADMIN");
        adminButton.setVisible(isAdmin);
        adminButton.setManaged(isAdmin);
        statsButton.setVisible(isAdmin);
        statsButton.setManaged(isAdmin);
        if (currentUser != null) {
            reputationLabel.setText("Réputation: " + ReputationUtil.calculerReputation(currentUser.getId(), sujetService, messageService));
        } else {
            reputationLabel.setText("Réputation: -");
        }
    }

    private void reloadCurrentView() {
        if ("messages".equals(currentView)) {
            showSubjectsView();
        } else if ("admin".equals(currentView)) {
            showAdminView();
        } else if ("stats".equals(currentView)) {
            showStatsView();
        } else {
            showSubjectsView();
        }
    }

    private void loadIntoCenter(String resource) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(resource));
            Parent root = loader.load();
            contentHolder.getChildren().setAll(root);
        } catch (IOException | RuntimeException e) {
            showError("Impossible de charger la vue", resource + "\n" + e.getMessage());
        }
    }

    public void showSubjectsView() {
        currentView = "subjects";
        loadIntoCenter("/sujet-view.fxml");
    }

    public void showMessagesView(Sujet sujet) {
        currentView = "messages";
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/message-view.fxml"));
            Parent root = loader.load();
            MessageController controller = loader.getController();
            controller.setSujet(sujet);
            contentHolder.getChildren().setAll(root);
        } catch (IOException e) {
            throw new RuntimeException("Impossible de charger la vue messages", e);
        }
    }

    public void showAdminView() {
        currentView = "admin";
        loadIntoCenter("/admin-view.fxml");
    }

    public void showStatsView() {
        currentView = "stats";
        loadIntoCenter("/stats-view.fxml");
    }

    public void showBadwordView() {
        currentView = "badword";
        loadIntoCenter("/badword-view.fxml");
    }

    @FXML
    private void handleSubjectsButton() {
        showSubjectsView();
    }

    @FXML
    private void handleNewSujetButton() {
        showSubjectsView();
    }

    @FXML
    private void handleAdminButton() {
        showAdminView();
    }

    @FXML
    private void handleStatsButton() {
        showStatsView();
    }

    @FXML
    private void handleBadwordButton() {
        showBadwordView();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Eduverse Forum");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}