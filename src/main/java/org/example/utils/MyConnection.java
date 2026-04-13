package org.example.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyConnection {

    private static MyConnection instance;
    private Connection cnx;

    private final String url = "jdbc:mysql://localhost:3306/eduverse_java?useSSL=false&serverTimezone=UTC";
    private final String user = "root"; // change si besoin
    private final String password = ""; // change si tu as un mot de passe

    private MyConnection() {
        try {
            cnx = DriverManager.getConnection(url, user, password);
            System.out.println("Connexion réussie !");
        } catch (SQLException e) {
            System.out.println("Erreur connexion : " + e.getMessage());
        }
    }

    public static MyConnection getInstance() {
        if (instance == null) {
            instance = new MyConnection();
        }
        return instance;
    }

    public Connection getCnx() {
        return cnx;
    }
}