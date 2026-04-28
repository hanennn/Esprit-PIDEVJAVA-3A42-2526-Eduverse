package com.eduverse.forum.controllers;

import com.eduverse.forum.models.Badword;
import com.eduverse.forum.services.BadwordService;
import com.eduverse.forum.utils.InputValidationUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.cell.PropertyValueFactory;

public class BadwordController {
    @FXML private TableView<Badword> badwordsTable;
    @FXML private TextField wordField;
    @FXML private ComboBox<String> actionCombo;
    @FXML private Label validationLabel;

    private final BadwordService badwordService = new BadwordService();

    @FXML
    public void initialize() {
        actionCombo.setItems(FXCollections.observableArrayList("MASK", "BLOCK", "ALERT"));
        actionCombo.getSelectionModel().selectFirst();
        wordField.setTextFormatter(new TextFormatter<>(change -> change.getControlNewText().length() <= 50 ? change : null));
        badwordsTable.getStyleClass().add("modern-table");
        badwordsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        setupTable();
        refreshTable();
    }

    private void setupTable() {
        TableColumn<Badword, String> wordCol = new TableColumn<>("Mot");
        wordCol.setCellValueFactory(new PropertyValueFactory<>("word"));
        TableColumn<Badword, String> actionCol = new TableColumn<>("Action");
        actionCol.setCellValueFactory(new PropertyValueFactory<>("action"));
        TableColumn<Badword, String> activeCol = new TableColumn<>("Statut");
        activeCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(Boolean.TRUE.equals(cell.getValue().isActive()) ? "Actif" : "Inactif"));
        TableColumn<Badword, Void> deleteCol = new TableColumn<>("Supprimer");
        deleteCol.setCellFactory(column -> new javafx.scene.control.TableCell<>() {
            private final Button delete = new Button("🗑");
            {
                delete.getStyleClass().add("btn-danger");
                delete.setOnAction(event -> {
                    Badword item = getTableView().getItems().get(getIndex());
                    if (confirm("Supprimer ce badword?")) {
                        badwordService.delete(item.getId());
                        refreshTable();
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : delete);
            }
        });

        badwordsTable.getColumns().setAll(java.util.List.of(wordCol, actionCol, activeCol, deleteCol));
    }

    @FXML
    private void handleAdd() {
        String word = InputValidationUtil.normalize(wordField.getText());
        String action = actionCombo.getValue();

        if (!InputValidationUtil.isLengthBetween(word, 2, 50)) {
            validationLabel.setText("Le mot doit contenir entre 2 et 50 caractères.");
            return;
        }
        if (!InputValidationUtil.isValidBadword(word)) {
            validationLabel.setText("Le mot ne doit contenir que des lettres, chiffres, tirets ou underscores.");
            return;
        }
        if (action == null) {
            validationLabel.setText("L'action est obligatoire.");
            return;
        }
        validationLabel.setText("");

        try {
            Badword badword = new Badword();
            badword.setWord(word);
            badword.setAction(action);
            badword.setActive(true);
            badwordService.save(badword);
            wordField.clear();
            info("Badword ajouté.");
            refreshTable();
        } catch (RuntimeException e) {
            error("Erreur", e.getMessage());
        }
    }

    private void refreshTable() {
        try {
            badwordsTable.getItems().setAll(badwordService.findAll());
        } catch (RuntimeException e) {
            error("Erreur", "Impossible de charger les badwords");
        }
    }

    private void info(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Eduverse Forum");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void error(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Eduverse Forum");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean confirm(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message);
        return alert.showAndWait().filter(buttonType -> buttonType.getText().equals("OK")).isPresent();
    }
}
