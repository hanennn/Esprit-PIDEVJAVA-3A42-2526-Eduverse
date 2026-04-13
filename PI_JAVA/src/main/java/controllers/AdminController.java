package controllers;

import Services.AdminService;
import Services.ProfessorService;
import Services.StudentService;
import entities.Admin;
import entities.Professor;
import entities.Student;
import entities.User;
import entities.Session;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import utils.DataBase;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AdminController {

    @FXML private TableView<User> usersTable;

    @FXML private TableColumn<User, Integer> idCol;
    @FXML private TableColumn<User, String> firstNameCol;
    @FXML private TableColumn<User, String> lastNameCol;
    @FXML private TableColumn<User, String> usernameCol;
    @FXML private TableColumn<User, String> emailCol;
    @FXML private TableColumn<User, Boolean> activeCol;
    @FXML private TableColumn<User, String> dateInscriptionCol;
    @FXML private TableColumn<User, String> roleCol;
    @FXML private TableColumn<User, String> specialtyCol;
    @FXML private TableColumn<User, String> experienceCol;
    @FXML private TableColumn<User, String> googleIdCol;

    @FXML private Button addUserBtn;
    @FXML private Button deleteUserBtn;

    @FXML private TextField searchField;

    @FXML private ComboBox<String> sortComboBox;

    AdminService adminService = new AdminService();
    ProfessorService professorService = new ProfessorService();
    StudentService studentService = new StudentService();

    @FXML
    public void initialize() {

        usersTable.setEditable(true);

        sortComboBox.setItems(FXCollections.observableArrayList(
                "Professors",
                "Students",
                "Admins",
                "Nom alphabétique",
                "Date inscription",
                "Activité"
        ));

        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        firstNameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        firstNameCol.setOnEditCommit(event -> {
            if (event.getNewValue().isBlank()){
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Erreur");
                alert.setContentText("Ce champ ne peut pas etre vide");
                alert.show();
                usersTable.refresh();

            }else {
                event.getRowValue().setFirstName(event.getNewValue());
                updateUser(event.getRowValue());
            }
        });
        lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        lastNameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        lastNameCol.setOnEditCommit(event -> {

            if (event.getNewValue().isBlank()){
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Erreur");
                alert.setContentText("Ce champ ne peut pas etre vide");
                alert.show();
                usersTable.refresh();

            }else {
                event.getRowValue().setLastName(event.getNewValue());
                updateUser(event.getRowValue());
            }
        });

        usernameCol.setCellValueFactory(new PropertyValueFactory<>("userName"));
        usernameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        usernameCol.setOnEditCommit(event -> {
            if (event.getNewValue().isBlank()){
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Erreur");
                alert.setContentText("Ce champ ne peut pas etre vide");
                alert.show();
                usersTable.refresh();

            }else {
                event.getRowValue().setUserName(event.getNewValue());
                updateUser(event.getRowValue());
            }
        });

        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setCellFactory(TextFieldTableCell.forTableColumn());
        emailCol.setOnEditCommit(event -> {
            if (event.getNewValue().isBlank()){
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Erreur");
                alert.setContentText("Ce champ ne peut pas etre vide");
                alert.show();
                usersTable.refresh();

            }else {
                event.getRowValue().setEmail(event.getNewValue());
                updateUser(event.getRowValue());
            }
        });
        activeCol.setCellValueFactory(new PropertyValueFactory<>("isActive"));
        dateInscriptionCol.setCellValueFactory(new PropertyValueFactory<>("dateInscription"));

        roleCol.setCellValueFactory(cell -> {
            User user = cell.getValue();
            if (user instanceof Admin) return new SimpleStringProperty("Admin");
            if (user instanceof Professor) return new SimpleStringProperty("Professor");
            if (user instanceof Student) return new SimpleStringProperty("Student");
            return new SimpleStringProperty("Unknown");
        });

        specialtyCol.setCellValueFactory(cell -> {
            User user = cell.getValue();
            if (user instanceof Professor p) return new SimpleStringProperty(p.getSpecialty());
            return new SimpleStringProperty("N/A");
        });

        experienceCol.setCellValueFactory(cell -> {
            User user = cell.getValue();
            if (user instanceof Professor p) return new SimpleStringProperty(p.getExperience());
            return new SimpleStringProperty("N/A");
        });

        googleIdCol.setCellValueFactory(cell -> {
            User user = cell.getValue();
            if (user instanceof Professor p) return new SimpleStringProperty(p.getGoogleId());
            if (user instanceof Student s) return new SimpleStringProperty(s.getGoogleId());
            return new SimpleStringProperty("N/A");
        });


        loadAllUsers();
    }

    private void loadAllUsers() {
        try {
            List<User> allUsers = new ArrayList<>();
            allUsers.addAll(adminService.AfficherAdmin());
            allUsers.addAll(professorService.AfficherProfessors());
            allUsers.addAll(studentService.AfficherStudents());

            ObservableList<User> list = FXCollections.observableArrayList(allUsers);
            usersTable.setItems(list);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    @FXML
    void goToAddUser(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddUserView.fxml"));
        Parent root = loader.load();
        addUserBtn.getScene().setRoot(root);
    }

    private void updateUser(User user) {
        try {

            if (user instanceof Admin a) {
                adminService.ModifierAdmin(a);
            } else if (user instanceof Professor p) {
                professorService.ModifierProfessor(p);
            } else if (user instanceof Student s) {
                studentService.ModifierStudent(s);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }


    @FXML
    void SupprimerUser(ActionEvent event) {
        User selected = usersTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            System.out.println("Aucun utilisateur n'est selectionne");
            return;
        }

        try {
            if (selected instanceof Admin a) {
                adminService.SupprimerAdmin(a.getId());
            } else if (selected instanceof Professor p) {
                professorService.SupprimerProfessor(p.getId());
            } else if (selected instanceof Student s) {
                studentService.SupprimerStudent(s.getId());
            }
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Erreur");
            alert.setContentText("Ce champ ne peut pas etre vide");
            alert.show();
            usersTable.getItems().remove(selected);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }


    @FXML
    void searchUser(ActionEvent event) {
        String username = searchField.getText();

        if (username.isBlank()) {
            loadAllUsers();
            return;
        }

        try {
            List<User> results = new ArrayList<>();
            Admin admin = adminService.FindAdminByUsername(username);
            Professor professor = professorService.FindProfessorByUsername(username);
            Student student = studentService.FindStudentByUsername(username);

            if (admin != null) results.add(admin);
            if (professor != null) results.add(professor);
            if (student != null) results.add(student);

            usersTable.setItems(FXCollections.observableArrayList(results));
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }


    private void SortUsersByInscriptionDate() {
        try {
            List<User> allUsers = new ArrayList<>();
            allUsers.addAll(adminService.AfficherAdmin());
            allUsers.addAll(professorService.AfficherProfessors());
            allUsers.addAll(studentService.AfficherStudents());

            allUsers.sort((a, b) -> a.getDateInscription().compareTo(b.getDateInscription()));

            ObservableList<User> list = FXCollections.observableArrayList(allUsers);
            usersTable.setItems(list);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    private void SortUsersByActivity() {
        try {
            List<User> allUsers = new ArrayList<>();
            allUsers.addAll(adminService.AfficherAdmin());
            allUsers.addAll(professorService.AfficherProfessors());
            allUsers.addAll(studentService.AfficherStudents());

            allUsers.sort((a, b) -> Boolean.compare(b.getIsActive(), a.getIsActive()));

            ObservableList<User> list = FXCollections.observableArrayList(allUsers);
            usersTable.setItems(list);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    private void SortUsersByNameAlphabeticalOrder() {
        try {
            List<User> allUsers = new ArrayList<>();
            allUsers.addAll(adminService.AfficherAdmin());
            allUsers.addAll(professorService.AfficherProfessors());
            allUsers.addAll(studentService.AfficherStudents());

            allUsers.sort((a, b) -> a.getFirstName().compareToIgnoreCase(b.getFirstName()));

            ObservableList<User> list = FXCollections.observableArrayList(allUsers);
            usersTable.setItems(list);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    @FXML
    void sortUsers(ActionEvent event) throws SQLException {
        String selected = sortComboBox.getValue();
        if (selected == null) return;

        if (selected.equals("Nom alphabétique")) {
            SortUsersByNameAlphabeticalOrder();
        } else if (selected.equals("Date inscription")) {
            SortUsersByInscriptionDate();
        } else if (selected.equals("Activité")) {
            SortUsersByActivity();
        }else if (selected.equals("Professors")) {
            try {
                ProfessorService Service = new ProfessorService();
                List<User> allUsers = new ArrayList<>();
                allUsers.addAll(Service.AfficherProfessors());
                ObservableList<User> list = FXCollections.observableArrayList(allUsers);
                usersTable.setItems(list);
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }else if (selected.equals("Students")) {
            try {
                StudentService Service = new StudentService();
                List<User> allUsers = new ArrayList<>();
                allUsers.addAll(Service.AfficherStudents());
                ObservableList<User> list = FXCollections.observableArrayList(allUsers);
                usersTable.setItems(list);
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }else if (selected.equals("Admins")) {
            try {
                AdminService Service = new AdminService();
                List<User> allUsers = new ArrayList<>();
                allUsers.addAll(Service.AfficherAdmin());
                ObservableList<User> list = FXCollections.observableArrayList(allUsers);
                usersTable.setItems(list);
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }

    }

    @FXML
    void reinitialiser(ActionEvent event) {
        sortComboBox.setValue(null);
        searchField.clear();
        loadAllUsers();
    }

    @FXML
    void logout(ActionEvent event) throws SQLException {
        try {
            Session.logout();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainView.fxml"));
            Parent root = loader.load();
            addUserBtn.getScene().setRoot(root);
        }catch (IOException e){
            System.err.println(e.getMessage());
        }
    }
}