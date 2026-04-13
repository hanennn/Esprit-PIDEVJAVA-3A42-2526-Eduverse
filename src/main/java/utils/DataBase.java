package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DataBase {
    private static DataBase instance;
    private Connection connection;

    private final String URL_SERVER = "jdbc:mysql://localhost:3306/";
    private final String DB_NAME = "gestionbourses";
    private final String URL = URL_SERVER + DB_NAME;
    private final String USERNAME = "root";
    private final String PASSWORD = "";

    private DataBase() {
        try {
            // 1. Création de la base de données si elle n'existe pas
            Connection tempConnection = DriverManager.getConnection(URL_SERVER, USERNAME, PASSWORD);
            Statement stmt = tempConnection.createStatement();
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
            stmt.close();
            tempConnection.close();

            // 2. Connexion à la base de données gestionbourses
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("Connexion à la base de données établie avec succès !");

            // 3. Création automatique des tables manquantes
            createTablesIfNotExist();

        } catch (SQLException e) {
            System.out.println("Erreur de connexion à la base de données : " + e.getMessage());
        }
    }

    private void createTablesIfNotExist() {
        try {
            Statement stmt = connection.createStatement();
            
            // Table bourses
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

            // Table demande
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
