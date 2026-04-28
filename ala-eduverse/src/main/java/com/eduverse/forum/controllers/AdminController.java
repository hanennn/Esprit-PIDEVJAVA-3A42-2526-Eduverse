package com.eduverse.forum.controllers;

import com.eduverse.forum.models.Message;
import com.eduverse.forum.models.Sujet;
import com.eduverse.forum.services.MessageService;
import com.eduverse.forum.services.SujetService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

public class AdminController {
    @FXML private TableView<Sujet> sujetsTable;
    @FXML private TableView<Message> messagesTable;

    private final SujetService sujetService = new SujetService();
    private final MessageService messageService = new MessageService();

    @FXML
    public void initialize() {
        try {
            sujetsTable.getStyleClass().add("modern-table");
            messagesTable.getStyleClass().add("modern-table");
            sujetsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
            messagesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
            setupTables();
            sujetsTable.getItems().setAll(sujetService.findAll());
            messagesTable.getItems().setAll(messageService.findAll());
        } catch (RuntimeException e) {
            info("Impossible de charger les données d'administration: " + e.getMessage());
        }
    }

    private void setupTables() {
        TableColumn<Sujet, String> titreCol = new TableColumn<>("Titre");
        titreCol.setCellValueFactory(new PropertyValueFactory<>("titre"));
        TableColumn<Sujet, Object> auteurCol = new TableColumn<>("Auteur");
        auteurCol.setCellValueFactory(new PropertyValueFactory<>("auteur"));
        TableColumn<Sujet, Object> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));
        TableColumn<Sujet, Integer> nbMessagesCol = new TableColumn<>("Nb Messages");
        nbMessagesCol.setCellValueFactory(new PropertyValueFactory<>("nbMessages"));
        TableColumn<Sujet, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setCellFactory(column -> new javafx.scene.control.TableCell<>() {
            private final Button edit = new Button("✏️ Modifier");
            private final Button delete = new Button("🗑 Supprimer");
            private final HBox box = new HBox(6, edit, delete);
            {
                edit.getStyleClass().add("btn-secondary");
                delete.getStyleClass().add("btn-danger");
                edit.setOnAction(event -> info("Modifier sujet à implémenter selon le flux de navigation."));
                delete.setOnAction(event -> {
                    Sujet item = getTableView().getItems().get(getIndex());
                    if (confirm("Supprimer ce sujet ?")) {
                        sujetService.delete(item.getId());
                        getTableView().getItems().remove(item);
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
        TableColumn<Sujet, String> imgCol = new TableColumn<>("Image");
        imgCol.setCellValueFactory(new PropertyValueFactory<>("imageUrl"));
        imgCol.setCellFactory(column -> new javafx.scene.control.TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setGraphic(null);
                else {
                    javafx.scene.image.ImageView iv = new javafx.scene.image.ImageView(item);
                    iv.setFitHeight(40); iv.setPreserveRatio(true); setGraphic(iv);
                }
            }
        });

        sujetsTable.getColumns().setAll(java.util.List.of(titreCol, imgCol, auteurCol, dateCol, nbMessagesCol, actionCol));

        TableColumn<Message, String> contenuCol = new TableColumn<>("Contenu");
        contenuCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(truncate(cell.getValue().getContenu())));
        TableColumn<Message, Object> auteurMsgCol = new TableColumn<>("Auteur");
        auteurMsgCol.setCellValueFactory(new PropertyValueFactory<>("auteur"));
        TableColumn<Message, Object> sujetMsgCol = new TableColumn<>("Sujet");
        sujetMsgCol.setCellValueFactory(new PropertyValueFactory<>("sujet"));
        TableColumn<Message, Object> dateMsgCol = new TableColumn<>("Date");
        dateMsgCol.setCellValueFactory(new PropertyValueFactory<>("datePublication"));
        TableColumn<Message, Void> actionMsgCol = new TableColumn<>("Actions");
        actionMsgCol.setCellFactory(column -> new javafx.scene.control.TableCell<>() {
            private final Button delete = new Button("🗑 Supprimer");
            private final HBox box = new HBox(delete);
            {
                delete.getStyleClass().add("btn-danger");
                delete.setOnAction(event -> {
                    Message item = getTableView().getItems().get(getIndex());
                    if (confirm("Supprimer ce message ?")) {
                        messageService.delete(item.getId());
                        getTableView().getItems().remove(item);
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
        TableColumn<Message, String> gifCol = new TableColumn<>("GIF");
        gifCol.setCellValueFactory(new PropertyValueFactory<>("gifUrl"));
        gifCol.setCellFactory(column -> new javafx.scene.control.TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setGraphic(null);
                else {
                    javafx.scene.image.ImageView iv = new javafx.scene.image.ImageView(item);
                    iv.setFitHeight(40); iv.setPreserveRatio(true); setGraphic(iv);
                }
            }
        });

        messagesTable.getColumns().setAll(java.util.List.of(contenuCol, gifCol, auteurMsgCol, sujetMsgCol, dateMsgCol, actionMsgCol));
    }

    private String truncate(String text) {
        if (text == null) {
            return "";
        }
        return text.length() > 60 ? text.substring(0, 60) + "..." : text;
    }

    private void info(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Eduverse Forum");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean confirm(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message);
        return alert.showAndWait().filter(buttonType -> buttonType.getText().equals("OK")).isPresent();
    }
}