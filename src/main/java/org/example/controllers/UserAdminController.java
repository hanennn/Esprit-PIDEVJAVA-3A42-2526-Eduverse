package org.example.controllers;

import org.example.services.AdminService;
import org.example.services.ProfessorService;
import org.example.services.StudentService;
import org.example.entities.*;
import org.example.utils.AppContext;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UserAdminController {

    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Integer> idCol;
    @FXML private TableColumn<User, String> firstNameCol;
    @FXML private TableColumn<User, String> lastNameCol;
    @FXML private TableColumn<User, String> usernameCol;
    @FXML private TableColumn<User, String> emailCol;
    @FXML private TableColumn<User, String> activeCol;
    @FXML private TableColumn<User, String> dateInscriptionCol;
    @FXML private TableColumn<User, String> roleCol;
    @FXML private TableColumn<User, String> specialtyCol;
    @FXML private TableColumn<User, String> experienceCol;
    @FXML private TableColumn<User, String> googleIdCol;

    @FXML private Button addUserBtn;
    @FXML private Button deleteUserBtn;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortComboBox;

    @FXML private Label statTotalLabel;
    @FXML private Label statStudentsLabel;
    @FXML private Label statProfessorsLabel;
    @FXML private Label statActiveLabel;

    @FXML private PieChart userDistributionChart;
    @FXML private PieChart activeInactiveChart;
    @FXML private BarChart<String, Number> registrationsBarChart;
    @FXML private CategoryAxis barXAxis;
    @FXML private NumberAxis barYAxis;

    private final AdminService adminService = new AdminService();
    private final ProfessorService professorService = new ProfessorService();
    private final StudentService studentService = new StudentService();

    private ObservableList<User> masterList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        if (usersTable == null) return;

        usersTable.setEditable(true);

        sortComboBox.setItems(FXCollections.observableArrayList(
                "Professors", "Students", "Admins",
                "Nom alphabétique", "Date inscription"
        ));

        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        firstNameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        firstNameCol.setOnEditCommit(e -> {
            if (e.getNewValue().isBlank()) {
                showError("Ce champ ne peut pas être vide");
                usersTable.refresh();
            } else {
                e.getRowValue().setFirstName(e.getNewValue());
                updateUser(e.getRowValue());
            }
        });

        lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        lastNameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        lastNameCol.setOnEditCommit(e -> {
            if (e.getNewValue().isBlank()) {
                showError("Ce champ ne peut pas être vide");
                usersTable.refresh();
            } else {
                e.getRowValue().setLastName(e.getNewValue());
                updateUser(e.getRowValue());
            }
        });

        usernameCol.setCellValueFactory(new PropertyValueFactory<>("userName"));
        usernameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        usernameCol.setOnEditCommit(e -> {
            if (e.getNewValue().isBlank()) {
                showError("Ce champ ne peut pas être vide");
                usersTable.refresh();
            } else {
                e.getRowValue().setUserName(e.getNewValue());
                updateUser(e.getRowValue());
            }
        });

        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setCellFactory(TextFieldTableCell.forTableColumn());
        emailCol.setOnEditCommit(e -> {
            if (e.getNewValue().isBlank()) {
                showError("Ce champ ne peut pas être vide");
                usersTable.refresh();
            } else {
                e.getRowValue().setEmail(e.getNewValue());
                updateUser(e.getRowValue());
            }
        });

        activeCol.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getIsActive() ? "Actif" : "Inactif")
        );

        dateInscriptionCol.setCellValueFactory(new PropertyValueFactory<>("dateInscription"));

        roleCol.setCellValueFactory(cell -> {
            User u = cell.getValue();
            if (u instanceof Admin) return new SimpleStringProperty("Admin");
            if (u instanceof Professor) return new SimpleStringProperty("Formateur");
            if (u instanceof Student) return new SimpleStringProperty("Étudiant");
            return new SimpleStringProperty("—");
        });

        specialtyCol.setCellValueFactory(cell -> {
            User u = cell.getValue();
            if (u instanceof Professor p) return new SimpleStringProperty(p.getSpecialty() != null ? p.getSpecialty() : "—");
            return new SimpleStringProperty("N/A");
        });

        experienceCol.setCellValueFactory(cell -> {
            User u = cell.getValue();
            if (u instanceof Professor p) return new SimpleStringProperty(p.getExperience() != null ? p.getExperience() : "—");
            return new SimpleStringProperty("N/A");
        });

        googleIdCol.setCellValueFactory(cell -> {
            User u = cell.getValue();
            if (u instanceof Professor p) return new SimpleStringProperty(p.getGoogleId() != null ? p.getGoogleId() : "—");
            if (u instanceof Student s) return new SimpleStringProperty(s.getGoogleId() != null ? s.getGoogleId() : "—");
            return new SimpleStringProperty("N/A");
        });

        searchField.textProperty().addListener((obs, oldVal, newVal) -> applySearch(newVal));

        loadAllUsers();
    }

    private void loadAllUsers() {
        try {
            List<User> all = new ArrayList<>();
            all.addAll(adminService.AfficherAdmin());
            all.addAll(professorService.AfficherProfessors());
            all.addAll(studentService.AfficherStudents());

            masterList = FXCollections.observableArrayList(all);
            usersTable.setItems(masterList);
            updateStats(all);
            updateCharts(all);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    private void updateCharts(List<User> users) {
        long admins = users.stream().filter(u -> u instanceof Admin).count();
        long professors = users.stream().filter(u -> u instanceof Professor).count();
        long students = users.stream().filter(u -> u instanceof Student).count();
        long active = users.stream().filter(u -> Boolean.TRUE.equals(u.getIsActive())).count();
        long inactive = users.size() - active;

        userDistributionChart.setData(FXCollections.observableArrayList(
                new PieChart.Data("Admins", admins),
                new PieChart.Data("Formateurs", professors),
                new PieChart.Data("Étudiants", students)
        ));

        activeInactiveChart.setData(FXCollections.observableArrayList(
                new PieChart.Data("Actifs", active),
                new PieChart.Data("Inactifs", inactive)
        ));

        Map<String, Integer> perMonth = new LinkedHashMap<>();
        for (User u : users) {
            if (u.getDateInscription() == null) continue;
            String month = u.getDateInscription().substring(0, 7);
            perMonth.merge(month, 1, Integer::sum);
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Inscriptions");
        perMonth.forEach((month, count) -> series.getData().add(new XYChart.Data<>(month, count)));

        registrationsBarChart.getData().clear();
        registrationsBarChart.getData().add(series);
    }

    private void applySearch(String query) {
        if (query == null || query.isBlank()) {
            usersTable.setItems(masterList);
            return;
        }

        String lower = query.toLowerCase().trim();
        FilteredList<User> filtered = new FilteredList<>(masterList, user -> {
            if (user.getFirstName() != null && user.getFirstName().toLowerCase().contains(lower)) return true;
            if (user.getLastName() != null && user.getLastName().toLowerCase().contains(lower)) return true;
            if (user.getUserName() != null && user.getUserName().toLowerCase().contains(lower)) return true;
            if (user.getEmail() != null && user.getEmail().toLowerCase().contains(lower)) return true;
            if (String.valueOf(user.getId()).contains(lower)) return true;
            if (user instanceof Professor p && p.getSpecialty() != null
                    && p.getSpecialty().toLowerCase().contains(lower)) return true;
            return false;
        });

        usersTable.setItems(filtered);
    }

    @FXML
    void searchUser(ActionEvent event) {
        applySearch(searchField.getText());
    }

    private void updateStats(List<User> users) {
        long total = users.size();
        long students = users.stream().filter(u -> u instanceof Student).count();
        long professors = users.stream().filter(u -> u instanceof Professor).count();
        long active = users.stream().filter(u -> Boolean.TRUE.equals(u.getIsActive())).count();

        if (statTotalLabel != null) statTotalLabel.setText(String.valueOf(total));
        if (statStudentsLabel != null) statStudentsLabel.setText(String.valueOf(students));
        if (statProfessorsLabel != null) statProfessorsLabel.setText(String.valueOf(professors));
        if (statActiveLabel != null) statActiveLabel.setText(String.valueOf(active));
    }

    @FXML
    void goToAddUser(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddUserView.fxml"));
        Parent root = loader.load();
        addUserBtn.getScene().setRoot(root);
    }

    private void updateUser(User user) {
        try {
            if (user instanceof Admin a) adminService.ModifierAdmin(a);
            else if (user instanceof Professor p) professorService.ModifierProfessor(p);
            else if (user instanceof Student s) studentService.ModifierStudent(s);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    @FXML
    void SupprimerUser(ActionEvent event) {
        User selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Veuillez sélectionner un utilisateur.");
            return;
        }

        try {
            if (selected instanceof Admin a) adminService.SupprimerAdmin(a.getId());
            else if (selected instanceof Professor p) professorService.SupprimerProfessor(p.getId());
            else if (selected instanceof Student s) studentService.SupprimerStudent(s.getId());

            masterList.remove(selected);
            updateStats(masterList);
            updateCharts(masterList);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    @FXML
    void sortUsers(ActionEvent event) {
        String selected = sortComboBox.getValue();
        if (selected == null) return;

        switch (selected) {
            case "Nom alphabétique" -> {
                List<User> sorted = new ArrayList<>(masterList);
                sorted.sort((a, b) -> a.getFirstName().compareToIgnoreCase(b.getFirstName()));
                usersTable.setItems(FXCollections.observableArrayList(sorted));
            }
            case "Date inscription" -> {
                List<User> sorted = new ArrayList<>(masterList);
                sorted.sort((a, b) -> a.getDateInscription().compareTo(b.getDateInscription()));
                usersTable.setItems(FXCollections.observableArrayList(sorted));
            }
            case "Professors" -> usersTable.setItems(masterList.filtered(u -> u instanceof Professor));
            case "Students" -> usersTable.setItems(masterList.filtered(u -> u instanceof Student));
            case "Admins" -> usersTable.setItems(masterList.filtered(u -> u instanceof Admin));
        }
    }

    @FXML
    void reinitialiser(ActionEvent event) {
        sortComboBox.setValue(null);
        searchField.clear();
        usersTable.setItems(masterList);
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

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }

    private void desactiverAdmin(Admin admin) {
        try {
            admin.setIsActive(!admin.getIsActive());
            adminService.ModifierAdmin(admin);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    private void desactiverProfessor(Professor professor) {
        try {
            professor.setIsActive(!professor.getIsActive());
            professorService.ModifierProfessor(professor);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    private void desactiverStudent(Student student) {
        try {
            student.setIsActive(!student.getIsActive());
            studentService.ModifierStudent(student);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @FXML
    void desactiverUser(ActionEvent event) {
        User selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Veuillez selectionner un utilisateur.");
            return;
        }

        if (selected instanceof Admin a) desactiverAdmin(a);
        else if (selected instanceof Professor p) desactiverProfessor(p);
        else if (selected instanceof Student s) desactiverStudent(s);

        usersTable.refresh();
        updateCharts(masterList);
    }

    @FXML
    void exportToExcel(ActionEvent event) {
        ObservableList<User> currentItems = usersTable.getItems();
        if (currentItems.isEmpty()) {
            showError("Aucun utilisateur à exporter.");
            return;
        }

        try {
            org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
            org.apache.poi.xssf.usermodel.XSSFSheet sheet = workbook.createSheet("Utilisateurs");

            org.apache.poi.xssf.usermodel.XSSFCellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(new org.apache.poi.xssf.usermodel.XSSFColor(new byte[]{(byte) 44, (byte) 62, (byte) 80}, null));
            headerStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);

            org.apache.poi.xssf.usermodel.XSSFFont headerFont = workbook.createFont();
            headerFont.setColor(new org.apache.poi.xssf.usermodel.XSSFColor(new byte[]{(byte) 255, (byte) 255, (byte) 255}, null));
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            org.apache.poi.ss.usermodel.Row header = sheet.createRow(0);
            String[] columns = {"Prénom", "Nom", "Username", "Email", "Rôle", "Statut", "Date Inscription"};

            for (int i = 0; i < columns.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 5000);
            }

            int rowNum = 1;
            for (User u : currentItems) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(u.getFirstName() != null ? u.getFirstName() : "");
                row.createCell(1).setCellValue(u.getLastName() != null ? u.getLastName() : "");
                row.createCell(2).setCellValue(u.getUserName() != null ? u.getUserName() : "");
                row.createCell(3).setCellValue(u.getEmail() != null ? u.getEmail() : "");
                row.createCell(4).setCellValue(u instanceof Admin ? "Admin" : u instanceof Professor ? "Formateur" : "Étudiant");
                row.createCell(5).setCellValue(Boolean.TRUE.equals(u.getIsActive()) ? "Actif" : "Inactif");
                row.createCell(6).setCellValue(u.getDateInscription() != null ? u.getDateInscription() : "");
            }

            String path = System.getProperty("user.home") + "/Downloads/utilisateurs_" + java.time.LocalDate.now() + ".xlsx";
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(path)) {
                workbook.write(fos);
            }
            workbook.close();

            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Export réussi");
            success.setContentText("Fichier exporté vers :\n" + path);
            success.showAndWait();

        } catch (Exception e) {
            showError("Erreur lors de l'export : " + e.getMessage());
        }
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
    void goToAdminEvents(MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Event.fxml"));
        Parent root = loader.load();
        ((javafx.scene.Node) event.getSource()).getScene().setRoot(root);
    }
}
