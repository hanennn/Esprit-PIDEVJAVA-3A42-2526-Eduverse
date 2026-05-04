package org.example.scratch;

import org.example.utils.MyDataBase;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class DbCheck {
    public static void main(String[] args) {
        try {
            Connection conn = MyDataBase.getInstance().getConnection();
            ResultSet rs = conn.getMetaData().getTables("workshop1_java", null, "%", new String[]{"TABLE"});
            System.out.println("Tables in workshop1_java:");
            while (rs.next()) {
                System.out.println("- " + rs.getString("TABLE_NAME"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
