package org.example.controllers;

import org.example.entities.Admin;
import org.example.entities.Message;
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
import java.util.List;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class MessageController {
    private static final int MIN_MESSAGE_LENGTH = 3;
    private static final int MAX_MESSAGE_LENGTH = 2000;

    @FXML private VBox subjectCardContainer;
    @FXML private VBox messagesBox;
    @FXML private TextArea replyArea;
    @FXML private ProgressIndicator translateIndicator;
    @FXML private Label infoLabel;
    @FXML private Label validationLabel;
    @FXML private Label selectedGifLabel;
    @FXML private Button clearGifButton;

    private final MessageService messageService = new MessageService();
    private final SujetService sujetService = new SujetService();
    private final BadwordLogService badwordLogService = new BadwordLogService();
    private Sujet sujet;
    private String selectedGifUrl;

    public void setSujet(Sujet sujet) {
        this.sujet = sujet;
        refreshView();
    }

    @FXML
    public void initialize() {
        replyArea.setTextFormatter(new TextFormatter<>(change -> change.getControlNewText().length() <= MAX_MESSAGE_LENGTH ? change : null));
        if (sujet != null) {
            refreshView();
        }
    }

    private void refreshView() {
        if (sujet == null) {
            return;
        }
        try {
            subjectCardContainer.getChildren().setAll(createSubjectCard());
            loadMessages();
        } catch (RuntimeException e) {
            infoLabel.setText("Impossible de charger les messages.");
            error("Erreur de chargement", e.getMessage());
        }
    }

    private VBox createSubjectCard() {
        Label title = new Label(sujet.getTitre());
        title.getStyleClass().add("sujet-titre");

        String date = sujet.getDateCreation() == null ? "" : sujet.getDateCreation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        String author = sujet.getAuteur() == null ? "Auteur inconnu" : sujet.getAuteur().getUserName();
        Label meta = new Label(author + " • " + date);

        Label content = new Label(sujet.getContenu());
        content.setWrapText(true);

        // Calculate once for the subject
        String repText = ReputationUtil.calculerReputation(sujet.getAuteurId(), sujetService, messageService);
        Label reputation = new Label(repText);
        reputation.getStyleClass().add("badge-neutral");

        Button translateButton = new Button("Traduire ce sujet 🌐");
        translateButton.getStyleClass().add("btn-primary");
        translateButton.setOnAction(event -> translateSubject());

        VBox card = new VBox(8, title, meta, content, reputation, translateButton);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(14));
        card.setMaxHeight(250); // Capping the subject card height
        return card;
    }

    private void loadMessages() {
        ProgressIndicator loading = new ProgressIndicator();
        loading.setMaxSize(40, 40);
        messagesBox.getChildren().setAll(new VBox(loading));
        messagesBox.setAlignment(Pos.CENTER);

        javafx.concurrent.Task<List<Message>> task = new javafx.concurrent.Task<>() {
            @Override
            protected List<Message> call() throws Exception {
                return messageService.findBySujetId(sujet.getId());
            }
        };

        task.setOnSucceeded(event -> {
            List<Message> messages = task.getValue();
            messagesBox.setAlignment(Pos.TOP_LEFT);
            if (messages.isEmpty()) {
                messagesBox.getChildren().setAll(new Label("Aucun message pour le moment."));
            } else {
                List<VBox> cards = new java.util.ArrayList<>();
                // Cache reputation per author ID
                java.util.Map<Integer, String> reputationCache = new java.util.HashMap<>();
                for (Message message : messages) {
                    String rep = reputationCache.computeIfAbsent(message.getAuteurId(), 
                        id -> ReputationUtil.calculerReputation(id, sujetService, messageService));
                    cards.add(createMessageCard(message, rep));
                }
                messagesBox.getChildren().setAll(cards);
            }
        });

        task.setOnFailed(event -> {
            messagesBox.setAlignment(Pos.CENTER);
            messagesBox.getChildren().setAll(new Label("Impossible de charger les messages."));
            if (task.getException() != null) task.getException().printStackTrace();
        });

        new Thread(task).start();
    }

    private VBox createMessageCard(Message message, String reputationText) {
        User currentUser = AppContext.getCurrentUser();
        boolean canEdit = currentUser instanceof Admin || (currentUser != null && currentUser.getId() == message.getAuteurId());

        // 1. CONTENT
        Label contentLabel = new Label(message.getContenu());
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #1a1f3c;");

        // 2. HEADER (Author + Date)
        String authorName = (message.getAuteur() != null) ? message.getAuteur().getUserName() : "Auteur inconnu";
        String dateStr = (message.getDatePublication() != null) 
            ? message.getDatePublication().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) 
            : "";
        Label metaLabel = new Label(authorName + " • " + dateStr);
        metaLabel.getStyleClass().add("text-muted");

        // 3. REPUTATION BADGE
        Label reputationLabel = new Label(reputationText);
        reputationLabel.getStyleClass().add("badge-neutral");

        // 4. ACTIONS
        Button editButton = new Button("Modifier");
        editButton.getStyleClass().add("btn-secondary");
        editButton.setVisible(canEdit);
        editButton.setManaged(canEdit);
        editButton.setOnAction(event -> {
            replyArea.setText(message.getContenu());
            infoLabel.setText("La modification directe des messages peut être ajoutée dans une boîte de dialogue dédiée.");
        });

        Button deleteButton = new Button("Supprimer");
        deleteButton.getStyleClass().add("btn-danger");
        deleteButton.setVisible(canEdit);
        deleteButton.setManaged(canEdit);
        deleteButton.setOnAction(event -> {
            if (confirm("Supprimer ce message ?")) {
                messageService.delete(message.getId());
                loadMessages();
                info("Message supprimé");
            }
        });

        HBox actions = new HBox(10, reputationLabel, editButton, deleteButton);
        actions.setAlignment(Pos.CENTER_LEFT);

        // ASSEMBLY
        VBox card = new VBox(12);
        card.getStyleClass().add("message-card");
        card.setPadding(new Insets(18));
        
        card.getChildren().addAll(metaLabel, contentLabel);

        if (message.getGifUrl() != null && !message.getGifUrl().isEmpty()) {
            javafx.scene.image.Image img = new javafx.scene.image.Image(message.getGifUrl(), true);
            ImageView gifView = new ImageView(img);
            gifView.setFitWidth(280); // Limit width to save space
            gifView.setPreserveRatio(true);
            gifView.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 4); -fx-background-radius: 8;");
            card.getChildren().add(gifView);
        }

        card.getChildren().add(actions);

        return card;
    }

    @FXML
    private void handleBack() {
        AppContext.getMainController().showSubjectsView();
    }

    private void translateSubject() {
        translateIndicator.setVisible(true);
        javafx.concurrent.Task<String> task = new javafx.concurrent.Task<>() {
            @Override
            protected String call() {
                return org.example.utils.TranslateUtil.translate(sujet.getContenu());
            }
        };
        task.setOnSucceeded(event -> {
            infoLabel.setText(task.getValue());
            translateIndicator.setVisible(false);
        });
        task.setOnFailed(event -> {
            infoLabel.setText("Erreur de traduction");
            translateIndicator.setVisible(false);
        });
        new Thread(task, "translate-subject-task").start();
    }

    @FXML
    private void handlePublish() {
        String text = InputValidationUtil.normalize(replyArea.getText());
        User currentUser = AppContext.getCurrentUser();

        String validationError = validateMessageInput(text, currentUser);
        if (validationError != null) {
            validationLabel.setText(validationError);
            return;
        }
        validationLabel.setText("");

        BadwordFilterUtil.FilterResult filter = BadwordFilterUtil.filter(text);

        if (filter.hasViolation) {
            badwordLogService.logViolation(currentUser.getId(), filter.violatedWord, filter.action, text);

            if ("BLOCK".equals(filter.action)) {
                error("Contenu refusé", "Mot interdit détecté (" + filter.violatedWord + "). Message non publié.");
                return;
            } else if ("ALERT".equals(filter.action)) {
                info("⚠️ Attention: mot sensible détecté. Modérateurs notifiés.");
                text = filter.filtered;
            } else if ("MASK".equals(filter.action)) {
                text = filter.filtered;
                info("Contenu filtré: mots sensibles masqués.");
            }
        }

        try {
            Message message = new Message();
            message.setContenu(text);
            message.setGifUrl(selectedGifUrl);
            message.setAuteurId(currentUser.getId());
            message.setSujetId(sujet.getId());
            messageService.save(message);
            replyArea.clear();
            handleClearGif();
            loadMessages();
            info("Réponse publiée");
        } catch (RuntimeException e) {
            error("Erreur SQL", e.getMessage());
        }
    }

    @FXML
    private void handleShowGifPicker() {
        javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog();
        dialog.setTitle("Rechercher un GIF (Klipy)");
        dialog.setHeaderText("Entrez un mot-clé pour trouver un GIF");
        dialog.setContentText("Recherche :");
        
        java.util.Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().isBlank()) {
            List<String> gifs = org.example.utils.KlipyUtil.searchGifs(result.get());
            if (gifs.isEmpty()) {
                info("Aucun GIF trouvé.");
            } else {
                // Pour simplifier, on prend le premier résultat
                selectedGifUrl = gifs.get(0);
                selectedGifLabel.setText("GIF sélectionné : " + result.get());
                clearGifButton.setVisible(true);
            }
        }
    }

    @FXML
    private void handleClearGif() {
        selectedGifUrl = null;
        selectedGifLabel.setText("Aucun GIF");
        clearGifButton.setVisible(false);
    }

    private String validateMessageInput(String text, User currentUser) {
        if (sujet == null || sujet.getId() <= 0) {
            return "Aucun sujet sélectionné pour publier la réponse.";
        }
        if (currentUser == null) {
            return "Veuillez sélectionner un utilisateur.";
        }
        if (text == null || text.isBlank()) {
            return "La réponse est obligatoire.";
        }
        if (text.length() < MIN_MESSAGE_LENGTH) {
            return "La réponse doit contenir au moins " + MIN_MESSAGE_LENGTH + " caractères.";
        }
        if (text.length() > MAX_MESSAGE_LENGTH) {
            return "La réponse ne doit pas dépasser " + MAX_MESSAGE_LENGTH + " caractères.";
        }
        if (!InputValidationUtil.isLengthBetween(text, MIN_MESSAGE_LENGTH, MAX_MESSAGE_LENGTH)) {
            return "La réponse doit contenir entre " + MIN_MESSAGE_LENGTH + " et " + MAX_MESSAGE_LENGTH + " caractères.";
        }
        return null;
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

    private void error(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Eduverse Forum");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}