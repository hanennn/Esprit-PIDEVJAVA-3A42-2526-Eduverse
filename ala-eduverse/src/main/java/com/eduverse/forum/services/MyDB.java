package com.eduverse.forum.services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class MyDB {
    private static final String URL = "jdbc:mysql://localhost:3306/ala";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private static MyDB instance;

    private MyDB() {}

    public static synchronized MyDB getInstance() {
        if (instance == null) {
            instance = new MyDB();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}