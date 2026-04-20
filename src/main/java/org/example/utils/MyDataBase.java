package org.example.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {
    final String USERNAME="root";
    final String PASSWORD="";
    final String URL ="jdbc:mysql://localhost:3306/eduverse1_java";

    Connection connection;
    static MyDataBase instance;

    private MyDataBase(){
        try {
            connection= DriverManager.getConnection(URL,USERNAME,PASSWORD); //ouvre conx
            System.out.println("Connected to database successfully");
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
//pattern singleton
    public static MyDataBase getInstance(){
        if(instance==null){
            instance=new MyDataBase();
        }
        return instance;
    }

    public Connection getConnection(){
        return connection;
    }
}
