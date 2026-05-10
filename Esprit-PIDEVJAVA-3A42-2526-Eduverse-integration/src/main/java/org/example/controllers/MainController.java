package org.example.controllers;

import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;
import org.example.entities.Session;
import org.example.entities.Sujet;
import org.example.entities.User;
import org.example.services.MessageService;
import org.example.services.SujetService;
import org.example.utils.AppContext;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;

public class MainController {
    @FXML private StackPane contentHolder;

    private final SujetService sujetService = new SujetService();
    private final MessageService messageService = new MessageService();
    private User currentUser;

    @FXML
    public void initialize() {
        AppContext.setMainController(this);
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
        AppContext.setCurrentUser(currentUser);
        showSubjectsView();
    }

    private void loadIntoCenter(String resource) {
        try {
            System.out.println("Chargement de la vue : " + resource);
            FXMLLoader loader = new FXMLLoader(getClass().getResource(resource));
            Parent root = loader.load();
            contentHolder.getChildren().setAll(root);
            System.out.println("Vue chargée avec succès : " + resource);
        } catch (Throwable t) {
            t.printStackTrace();
            System.err.println("LOAD ERROR (" + resource + "): " + t.getMessage());
            showError("Impossible de charger la vue", resource + "\n" + t.getMessage());
        }
    }

    public void showSubjectsView() {
        loadIntoCenter("/sujet-view.fxml");
    }

    public void showMessagesView(Sujet sujet) {
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

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Eduverse Forum");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    void goToAdminUser(MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdminView.fxml"));
        Parent root = loader.load();
        ((javafx.scene.Node) event.getSource()).getScene().setRoot(root);
    }

    @FXML
    void goToAdminCours(MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin.fxml"));
        Parent root = loader.load();
        ((javafx.scene.Node) event.getSource()).getScene().setRoot(root);
    }

    @FXML
    void goToAdminCertification(MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/CertifAdmin.fxml"));
        Parent root = loader.load();
        ((javafx.scene.Node) event.getSource()).getScene().setRoot(root);
    }

    @FXML
    void goToAdminQuiz(MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/AdminQuiz.fxml"));
        Parent root = loader.load();
        ((javafx.scene.Node) event.getSource()).getScene().setRoot(root);
    }

    @FXML
    void goToAdminForum(MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/main.fxml"));
        Parent root = loader.load();

        MainController mainController = loader.getController();
        mainController.setCurrentUser(AppContext.getCurrentUser());

        ((javafx.scene.Node) event.getSource()).getScene().setRoot(root);
    }

    @FXML
    void goToAdminBadwords(MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/badword-view.fxml"));
        Parent root = loader.load();
        ((javafx.scene.Node) event.getSource()).getScene().setRoot(root);
    }


    @FXML
    void NavigateToBourses(MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/bourses/ListeBourses.fxml"));
        Parent root = loader.load();
        ((javafx.scene.Node) event.getSource()).getScene().setRoot(root);
    }
    @FXML
    void goToAdminEvents(MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Event.fxml"));
        Parent root = loader.load();
        ((javafx.scene.Node) event.getSource()).getScene().setRoot(root);
    }

    @FXML
    void NavigateToDemandes(MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demandes/ListeDemandes.fxml"));
        Parent root = loader.load();
        ((javafx.scene.Node) event.getSource()).getScene().setRoot(root);
    }
    @FXML
    void logout(ActionEvent event) {
        try {
            Session.logout();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainView.fxml"));
            Parent root = loader.load();
            ((javafx.scene.Node) event.getSource()).getScene().setRoot(root);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

}