package org.example.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {
    final String USERNAME = "root";
    final String PASSWORD = "";
    final String URL = "jdbc:mysql://localhost:3306/workshop1_java";

    Connection connection;
    static MyDataBase instance;

    private MyDataBase() {
        connect();
    }

    private void connect() {
        try {
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("Connected to database successfully");
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static MyDataBase getInstance() {
        if (instance == null) {
            instance = new MyDataBase();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            // Si connexion fermée ou invalide → reconnexion automatique
            if (connection == null || connection.isClosed() || !connection.isValid(2)) {
                System.out.println("Connexion perdue, reconnexion...");
                connect();
            }
        } catch (SQLException e) {
            System.out.println("Erreur validation connexion : " + e.getMessage());
            connect();
        }
        return connection;
    }
}