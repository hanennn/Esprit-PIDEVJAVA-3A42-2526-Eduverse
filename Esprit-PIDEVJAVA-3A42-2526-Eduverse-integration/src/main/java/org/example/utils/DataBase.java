package org.example.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataBase {

    final String URL = "jdbc:mysql://localhost:3306/workshop1_java";
    final String UserName = "root";
    final String Password ="";
    Connection connection;
    public static DataBase Instance ;


    private DataBase() {
        try {
            this.connection = DriverManager.getConnection(this.URL, this.UserName, this.Password);
            System.out.println("Connected to database successfully");
        }catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static DataBase getInstance() {
        if (Instance == null) {
            Instance = new DataBase();
        }
        return Instance;
    }

    public String getURL() {
        return URL;
    }
    public String getUserName() {
        return UserName;
    }
    public String getPassword() {
        return Password;
    }
    public Connection getConnection() {
        return connection;
    }





}
