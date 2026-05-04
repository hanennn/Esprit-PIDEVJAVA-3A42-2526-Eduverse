package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataBase {
    private static DataBase instance;
    private Connection connection;

    private DataBase() {
        try {
            // Configuration demandée: localhost:3306/eduverse, root, mdp vide
            this.connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/eduverse", "root", "");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
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
