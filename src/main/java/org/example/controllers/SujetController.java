package org.example.controllers;

import org.example.entities.Admin;
import org.example.entities.Professor;
import org.example.entities.Sujet;
import org.example.entities.User;
import org.example.services.BadwordLogService;
import org.example.services.MessageService;
import org.example.services.SujetService;
import org.example.utils.AppContext;
import org.example.utils.BadwordFilterUtil;
import org.example.utils.InputValidationUtil;
import org.example.utils.ReputationUtil;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class SujetController {
    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortComboBox;
    @FXML private Button newSujetButton;
    @FXML private VBox subjectsBox;
    @FXML private VBox formPane;
    @FXML private VBox listPane;
    @FXML private Label formTitle;
    @FXML private TextField titreField;
    @FXML private TextArea contenuField;
    @FXML private TextArea translatedField;
    @FXML private Label validationLabel;
    @FXML private ProgressIndicator translationIndicator;
    @FXML private Label imageNameLabel;

    private final SujetService sujetService = new SujetService();
    private final MessageService messageService = new MessageService();
    private final BadwordLogService badwordLogService = new BadwordLogService();
    private Sujet editingSujet;
    private java.io.File selectedImageFile;

    @FXML
    public void initialize() {
        sortComboBox.setItems(FXCollections.observableArrayList("Plus Récent", "Plus Ancien", "Plus de Messages"));
        sortComboBox.getSelectionModel().selectFirst();
        titreField.setTextFormatter(new TextFormatter<>(change -> change.getControlNewText().length() <= 2000 ? change : null));
        contenuField.setTextFormatter(new TextFormatter<>(change -> change.getControlNewText().length() <= 2000 ? change : null));
        searchField.textProperty().addListener((observable, oldValue, newValue) -> refreshList());
        sortComboBox.setOnAction(event -> refreshList());
        refreshList();
        hideForm();
    }

    @FXML
    private void handleExitForum(ActionEvent event) {
        try {
            User user = AppContext.getCurrentUser();
            String fxmlPath = "/org/example/AccueilEtudiant.fxml"; // Par défaut Étudiant
            
            if (user instanceof Professor) {
                fxmlPath = "/org/example/AccueilFormateur.fxml";
            } else if (user instanceof Admin) {
                fxmlPath = "/AdminView.fxml";
            }
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
            info("Erreur lors du retour au tableau de bord : " + e.getMessage());
        }
    }

    private void refreshList() {
        ProgressIndicator loading = new ProgressIndicator();
        loading.setMaxSize(50, 50);
        subjectsBox.getChildren().setAll(new VBox(loading));
        subjectsBox.setAlignment(Pos.CENTER);

        Task<List<Sujet>> task = new Task<>() {
            @Override
            protected List<Sujet> call() throws Exception {
                User currentUser = AppContext.getCurrentUser();
                Integer currentUserId = currentUser == null ? null : currentUser.getId();
                List<Sujet> sujets = sujetService.findAll(currentUserId);
                
                String query = searchField.getText() == null ? "" : searchField.getText().toLowerCase(Locale.ROOT).trim();
                if (!query.isEmpty()) {
                    sujets = sujets.stream()
                            .filter(sujet -> (sujet.getTitre() != null && sujet.getTitre().toLowerCase(Locale.ROOT).contains(query)) || (sujet.getContenu() != null && sujet.getContenu().toLowerCase(Locale.ROOT).contains(query)))
                            .collect(Collectors.toList());
                }

                String sort = sortComboBox.getValue();
                if ("Plus Ancien".equals(sort)) {
                    sujets = sujets.stream().sorted(Comparator.comparing(Sujet::getDateCreation, Comparator.nullsLast(Comparator.naturalOrder()))).collect(Collectors.toList());
                } else if ("Plus de Messages".equals(sort)) {
                    sujets = sujets.stream().sorted(Comparator.comparingInt(Sujet::getNbMessages).reversed()).collect(Collectors.toList());
                } else {
                    sujets = sujets.stream().sorted(Comparator.comparing(Sujet::getDateCreation, Comparator.nullsLast(Comparator.reverseOrder()))).collect(Collectors.toList());
                }
                
                // Pre-fetch author info is already done via JOIN in SujetService
                return sujets;
            }
        };

        task.setOnSucceeded(event -> {
            List<Sujet> sujets = task.getValue();
            subjectsBox.setAlignment(Pos.TOP_LEFT);
            if (sujets.isEmpty()) {
                subjectsBox.getChildren().setAll(emptyState("Aucun sujet trouvé."));
            } else {
                List<VBox> cards = new java.util.ArrayList<>();
                // Cache reputation per author ID to avoid redundant DB calls (done on UI thread but very fast due to caching)
                java.util.Map<Integer, String> reputationCache = new java.util.HashMap<>();
                for (Sujet sujet : sujets) {
                    String rep = reputationCache.computeIfAbsent(sujet.getAuteurId(), 
                        id -> ReputationUtil.calculerReputation(id, sujetService, messageService));
                    cards.add(createSubjectCard(sujet, rep));
                }
                subjectsBox.getChildren().setAll(cards);
            }
        });

        task.setOnFailed(event -> {
            subjectsBox.setAlignment(Pos.CENTER);
            subjectsBox.getChildren().setAll(emptyState("Impossible de charger les sujets."));
            Throwable e = task.getException();
            if (e != null) e.printStackTrace();
        });

        new Thread(task).start();
    }

    private VBox createSubjectCard(Sujet sujet, String reputationText) {
        User currentUser = AppContext.getCurrentUser();
        boolean canEdit = currentUser instanceof Admin || (currentUser != null && currentUser.getId() == sujet.getAuteurId());

        Label title = new Label(sujet.getTitre());
        title.getStyleClass().add("sujet-titre");
        title.setOnMouseClicked(event -> AppContext.getMainController().showMessagesView(sujet));

        Label meta = new Label(formatAuthorAndDate(sujet));
        meta.getStyleClass().add("text-muted");

        String excerpt = sujet.getContenu() == null ? "" : (sujet.getContenu().length() > 50 ? sujet.getContenu().substring(0, 50) + "..." : sujet.getContenu());
        Label content = new Label(excerpt);
        content.setWrapText(true);

        Label messagesBadge = new Label(sujet.getNbMessages() + " messages");
        messagesBadge.getStyleClass().add("badge-orange");

        Label likesBadge = new Label("👍 " + sujet.getLikesCount());
        likesBadge.getStyleClass().add("badge-neutral");

        Label dislikesBadge = new Label("👎 " + sujet.getDislikesCount());
        dislikesBadge.getStyleClass().add("badge-neutral");

        Label reputation = new Label(reputationText);
        reputation.getStyleClass().add("badge-neutral");

        Button likeButton = new Button("👍 Like");
        likeButton.getStyleClass().add(sujet.getUserRating() == 1 ? "btn-like-active" : "btn-secondary");
        likeButton.setDisable(currentUser == null);
        likeButton.setOnAction(event -> handleRateSujet(sujet, 1));

        Button dislikeButton = new Button("👎 Dislike");
        dislikeButton.getStyleClass().add(sujet.getUserRating() == -1 ? "btn-dislike-active" : "btn-secondary");
        dislikeButton.setDisable(currentUser == null);
        dislikeButton.setOnAction(event -> handleRateSujet(sujet, -1));

        Button viewButton = new Button("Voir Messages");
        viewButton.getStyleClass().add("btn-primary");
        viewButton.setOnAction(event -> AppContext.getMainController().showMessagesView(sujet));

        Button editButton = new Button("Modifier");
        editButton.getStyleClass().add("btn-secondary");
        editButton.setVisible(canEdit);
        editButton.setManaged(canEdit);
        editButton.setOnAction(event -> openForm(sujet));

        Button deleteButton = new Button("Supprimer");
        deleteButton.getStyleClass().add("btn-danger");
        deleteButton.setVisible(canEdit);
        deleteButton.setManaged(canEdit);
        deleteButton.setOnAction(event -> {
            if (confirm("Supprimer ce sujet ?")) {
                sujetService.delete(sujet.getId());
                info("Sujet supprimé");
                refreshList();
            }
        });

        HBox actions = new HBox(8, likeButton, dislikeButton, viewButton, editButton, deleteButton);
        actions.setAlignment(Pos.CENTER_LEFT);

        HBox badges = new HBox(8, messagesBadge, likesBadge, dislikesBadge, reputation);
        badges.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(10, title, meta);
        
        if (sujet.getImageUrl() != null && !sujet.getImageUrl().isEmpty()) {
            javafx.scene.image.Image img = new javafx.scene.image.Image(sujet.getImageUrl(), true);
            javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView(img);
            imageView.setFitWidth(300);
            imageView.setPreserveRatio(true);
            card.getChildren().add(imageView);
        }
        
        card.getChildren().addAll(content, badges, actions);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(16));
        return card;
    }

    private void handleRateSujet(Sujet sujet, int rating) {
        User currentUser = AppContext.getCurrentUser();
        if (currentUser == null) {
            error("Utilisateur requis", "Veuillez sélectionner un utilisateur pour voter.");
            return;
        }
        try {
            sujetService.rateSujet(sujet.getId(), currentUser.getId(), rating);
            refreshList();
        } catch (RuntimeException e) {
            error("Erreur de vote", e.getMessage());
        }
    }

    private String formatAuthorAndDate(Sujet sujet) {
        String author = sujet.getAuteur() == null ? "Auteur inconnu" : sujet.getAuteur().getUserName();
        String date = sujet.getDateCreation() == null ? "" : sujet.getDateCreation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        return author + " • " + date;
    }

    @FXML
    private void handleNewSujet() {
        openForm(null);
    }

    private void openForm(Sujet sujet) {
        editingSujet = sujet;
        formTitle.setText(sujet == null ? "Créer un nouveau sujet" : "Modifier le sujet");
        titreField.setText(sujet == null ? "" : sujet.getTitre());
        contenuField.setText(sujet == null ? "" : sujet.getContenu());
        selectedImageFile = null;
        imageNameLabel.setText(sujet != null && sujet.getImageUrl() != null ? "Image actuelle conservée" : "Aucune image sélectionnée");
        translatedField.clear();
        validationLabel.setText("");
        listPane.setVisible(false);
        listPane.setManaged(false);
        formPane.setVisible(true);
        formPane.setManaged(true);
    }

    private void hideForm() {
        formPane.setVisible(false);
        formPane.setManaged(false);
        listPane.setVisible(true);
        listPane.setManaged(true);
    }

    @FXML
    private void handleSave() {
        String titre = InputValidationUtil.normalize(titreField.getText());
        String contenu = InputValidationUtil.normalize(contenuField.getText());

        if (!InputValidationUtil.isLengthBetween(titre, 10, 2000)) {
            validationLabel.setText("Le titre doit contenir entre 10 et 2000 caractères.");
            return;
        }
        if (!InputValidationUtil.isLengthBetween(contenu, 10, 2000)) {
            validationLabel.setText("Le contenu doit contenir entre 10 et 2000 caractères.");
            return;
        }

        User currentUser = AppContext.getCurrentUser();
        if (currentUser == null) {
            validationLabel.setText("Veuillez sélectionner un utilisateur.");
            return;
        }

        BadwordFilterUtil.FilterResult filterTitle = BadwordFilterUtil.filter(titre);
        BadwordFilterUtil.FilterResult filterContent = BadwordFilterUtil.filter(contenu);

        if (filterTitle.hasViolation || filterContent.hasViolation) {
            BadwordFilterUtil.FilterResult violation = filterTitle.hasViolation ? filterTitle : filterContent;
            badwordLogService.logViolation(currentUser.getId(), violation.violatedWord, violation.action, titre + " | " + contenu);

            if ("BLOCK".equals(violation.action)) {
                validationLabel.setText("❌ Contenu refusé: mot interdit détecté (" + violation.violatedWord + ")");
                return;
            } else if ("ALERT".equals(violation.action)) {
                validationLabel.setText("⚠️ Attention: mot sensible détecté. Modérateurs notifiés.");
            } else if ("MASK".equals(violation.action)) {
                titre = filterTitle.filtered;
                contenu = filterContent.filtered;
                info("Contenu filtré: mots sensibles masqués.");
            }
        }

        String imageUrl = editingSujet != null ? editingSujet.getImageUrl() : null;
        if (selectedImageFile != null) {
            validationLabel.setText("⏳ Upload de l'image en cours...");
            imageUrl = org.example.utils.ImageKitUtil.uploadImage(selectedImageFile);
            if (imageUrl == null) {
                validationLabel.setText("❌ Erreur lors de l'upload de l'image.");
                return;
            }
        }

        try {
            if (editingSujet == null) {
                Sujet sujet = new Sujet();
                sujet.setTitre(titre);
                sujet.setContenu(contenu);
                sujet.setImageUrl(imageUrl);
                sujet.setAuteurId(currentUser.getId());
                sujetService.save(sujet);
                info("Sujet créé avec succès");
            } else {
                editingSujet.setTitre(titre);
                editingSujet.setContenu(contenu);
                editingSujet.setImageUrl(imageUrl);
                sujetService.update(editingSujet);
                info("Sujet modifié avec succès");
            }

            hideForm();
            refreshList();
        } catch (RuntimeException e) {
            error("Erreur SQL", e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        hideForm();
    }

    @FXML
    private void handleChooseImage() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new javafx.stage.FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        java.io.File file = fileChooser.showOpenDialog(titreField.getScene().getWindow());
        if (file != null) {
            selectedImageFile = file;
            imageNameLabel.setText(file.getName());
        }
    }

    @FXML
    private void handleTranslate() {
        String text = contenuField.getText();
        if (text == null || text.isBlank()) {
            validationLabel.setText("Saisis d'abord un contenu à traduire.");
            return;
        }
        if (text.trim().length() < 10) {
            validationLabel.setText("Le contenu est trop court pour être traduit.");
            return;
        }
        validationLabel.setText("");
        translationIndicator.setVisible(true);
        Task<String> task = new Task<>() {
            @Override
            protected String call() {
                return org.example.utils.TranslateUtil.translate(text);
            }
        };
        task.setOnSucceeded(event -> {
            translatedField.setText(task.getValue());
            translationIndicator.setVisible(false);
        });
        task.setOnFailed(event -> {
            translatedField.setText("Erreur de traduction");
            translationIndicator.setVisible(false);
        });
        new Thread(task, "translate-task").start();
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

    private VBox emptyState(String text) {
        Label label = new Label(text);
        VBox box = new VBox(label);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(24));
        return box;
    }

    private void error(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Eduverse Forum");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}