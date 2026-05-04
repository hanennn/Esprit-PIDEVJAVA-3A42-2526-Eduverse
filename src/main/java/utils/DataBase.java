package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DataBase {
    private static DataBase instance;
    private Connection connection;

    private final String URL_SERVER = "jdbc:mysql://localhost:3306/";
    private final String DB_NAME = "workshop1_java";
    private final String URL = URL_SERVER + DB_NAME;
    private final String USERNAME = "root";
    private final String PASSWORD = "";

    private DataBase() {
        try {
            Connection tempConnection = DriverManager.getConnection(URL_SERVER, USERNAME, PASSWORD);
            Statement stmt = tempConnection.createStatement();
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
            stmt.close();
            tempConnection.close();

            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("Connexion à la base de données établie avec succès !");

            createTablesIfNotExist();

        } catch (SQLException e) {
            System.out.println("Erreur de connexion à la base de données : " + e.getMessage());
        }
    }

    private void createTablesIfNotExist() {
        try {
            Statement stmt = connection.createStatement();
            
            String createBourses = "CREATE TABLE IF NOT EXISTS bourses (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "titre VARCHAR(255) NOT NULL, " +
                    "description TEXT, " +
                    "image VARCHAR(255), " +
                    "date_attribution DATETIME, " +
                    "date_fin DATETIME, " +
                    "montant DOUBLE" +
                    ")";
            stmt.executeUpdate(createBourses);

            String createDemandes = "CREATE TABLE IF NOT EXISTS demande (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "date_demande DATETIME, " +
                    "niveau_etudes VARCHAR(255), " +
                    "statut VARCHAR(50), " +
                    "lettre_motivation TEXT, " +
                    "note VARCHAR(255), " +
                    "etudiant_id INT, " +
                    "bourse_id INT, " +
                    "FOREIGN KEY (bourse_id) REFERENCES bourses(id) ON DELETE CASCADE" +
                    ")";
            stmt.executeUpdate(createDemandes);

            String createUsers = "CREATE TABLE IF NOT EXISTS utilisateurs (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "nom VARCHAR(255), " +
                    "prenom VARCHAR(255), " +
                    "email VARCHAR(255)" +
                    ")";
            stmt.executeUpdate(createUsers);

            String insertDummyUser = "INSERT IGNORE INTO utilisateurs (id, nom, prenom, email) VALUES (1, 'Klibi', 'Fatma', 'fatma.klibi@esprit.tn')";
            stmt.executeUpdate(insertDummyUser);
            
            String createAnalyseInterview = "CREATE TABLE IF NOT EXISTS analyse_interview (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "demande_id INT NOT NULL, " +
                    "transcription TEXT, " +
                    "scores_emotions TEXT, " +
                    "features_audio TEXT, " +
                    "profil_global TEXT, " +
                    "recommandation TEXT, " +
                    "date_analyse DATETIME, " +
                    "FOREIGN KEY (demande_id) REFERENCES demande(id) ON DELETE CASCADE" +
                    ")";
            stmt.executeUpdate(createAnalyseInterview);

            String createNotification = "CREATE TABLE IF NOT EXISTS notification (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "demande_id INT NOT NULL, " +
                    "message TEXT, " +
                    "lu BOOLEAN DEFAULT FALSE, " +
                    "date_creation DATETIME, " +
                    "FOREIGN KEY (demande_id) REFERENCES demande(id) ON DELETE CASCADE" +
                    ")";
            stmt.executeUpdate(createNotification);

            try { stmt.executeUpdate("ALTER TABLE demande ADD COLUMN cv_analyse TEXT"); } catch (SQLException ignored) {}
            try { stmt.executeUpdate("ALTER TABLE demande ADD COLUMN cv_path VARCHAR(500)"); } catch (SQLException ignored) {}

            stmt.close();
            System.out.println("Vérification des tables effectuée avec succès !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la création des tables : " + e.getMessage());
        }
    }

    public static DataBase getInstance() {
        if (instance == null) {
            instance = new DataBase();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}
