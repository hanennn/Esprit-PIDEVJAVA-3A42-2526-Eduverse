package controllers;

import Services.AdminService;
import Services.ProfessorService;
import Services.StudentService;
import entities.Admin;
import entities.Professor;
import entities.Student;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import utils.DataBase;
import utils.Helpers;

import java.io.IOException;

public class AddUserController {

    @FXML private ComboBox<String> roleComboBox;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextField passwordField;
    @FXML private Label specialtyLabel;
    @FXML private TextField specialtyField;
    @FXML private Label experienceLabel;
    @FXML private TextField experienceField;
    @FXML private Button saveBtn;
    @FXML private Button backBtn;

    @FXML
    public void initialize() {

        roleComboBox.setItems(FXCollections.observableArrayList("Admin", "Professor", "Student"));


        toggleProfessorFields(false);


        roleComboBox.setOnAction(e -> {
            toggleProfessorFields(roleComboBox.getValue().equals("Professor"));
        });
    }

    private void toggleProfessorFields(boolean visible) {
        specialtyLabel.setVisible(visible);
        specialtyField.setVisible(visible);
        experienceLabel.setVisible(visible);
        experienceField.setVisible(visible);
    }

    @FXML
    void saveUser(ActionEvent event) {
        String role = roleComboBox.getValue();
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();

        try {

            if (firstName.length() < 5) {
                Helpers.showAlert("Erreure" , "le nom doit etre composé d'au moins 5 lettres");
                return;
            }
            if (lastName.length() < 5) {
                Helpers.showAlert("Erreure" , "le prenom doit etre composé d'au moins 5 lettres");
                return;
            }
            if  (username.length() < 3) {
                Helpers.showAlert("Erreure", "le nom d'utilisateur doit etre composé d'au moins 5 lettres");
                return;
            }
            if (new AdminService().userNameExists(username)){
                Helpers.showAlert("Erreure", "Ce nom d'utilisateur existe deja");
                return;
            }
            if (!Helpers.checkEmail(email)){
                Helpers.showAlert("Erreure", "Cet Email n'est pas valid");
                return;
            }
            if (new AdminService().EmailExists(email)){
                Helpers.showAlert("Erreure", "Cet Email existe deja");
                return;
            }
            if (!Helpers.checkPassword(password)) {
                Helpers.showAlert("Erreure", "Le mot de passe doit contenir au moins :\n" +
                        "un caractere minuscule \n" +
                        "un caractere majuscule \n" +
                        "un chiffre \n" +
                        "un caractere special \n" +
                        "8 caracteres");
                return;
            }
            if (role.equals("Admin")) {
                new AdminService().AjouterAdmin(
                        new Admin(firstName, lastName, username, email, password, true)
                );
            } else if (role.equals("Professor")) {
                new ProfessorService().AjouterProfessor(
                        new Professor(firstName, lastName, username, email, password, true,
                                null, specialtyField.getText(), experienceField.getText(), null)
                );
                //System.out.println("sar appel lil ajout prfessor ");
            } else if (role.equals("Student")) {
                new StudentService().AjouterStudent(
                        new Student(firstName, lastName, username, email, password, true, null, null)

                );
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Reussi");
            alert.setContentText(role + " ajoute ");
            alert.show();

            goBack(event);

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @FXML
    void goBack(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdminView.fxml"));
        Parent root = loader.load();
        backBtn.getScene().setRoot(root);
    }


}