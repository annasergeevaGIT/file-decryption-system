package com.sergeeva.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
public class Logger {

    private Connection conn;
    public Logger(String pathToDatabase){
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:" + pathToDatabase);
            createLogTable();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void createLogTable() {
        String sql = "CREATE TABLE IF NOT EXISTS log ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "action TEXT, "
                + "result TEXT, "
                + "logged_at DATETIME DEFAULT CURRENT_TIMESTAMP)";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void log(String action, String result) {
        System.out.println(action + " " + result);
        try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO log (action, result) VALUES (?, ?)")) {
            pstmt.setString(1, action);
            pstmt.setString(2, result);

            pstmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
