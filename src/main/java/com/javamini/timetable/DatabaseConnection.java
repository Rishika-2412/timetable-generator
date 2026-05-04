/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.javamini.timetable;

import java.sql.*;

/**
 *
 * @author prabodhmayekar
 */
public class DatabaseConnection {
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost/TimeTable";
    static final String USER = "root";
    static final String PASS = "1234";

    // Singleton connection — reused across all instances
    private static Connection sharedConn = null;
    private static boolean migrated = false;

    Connection conn = null;
    Statement stmt = null;
    ResultSet rs = null;

    DatabaseConnection() {
        try {
            // Reuse shared connection; reconnect if closed
            if (sharedConn == null || sharedConn.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                sharedConn = DriverManager.getConnection(DB_URL, USER, PASS);
            }
            conn = sharedConn;

            // Run migrations only once
            if (!migrated) {
                migrated = true;
                try {
                    stmt = conn.createStatement();
                    try {
                        stmt.executeUpdate("ALTER TABLE TimeTable ADD COLUMN isFixed BOOLEAN DEFAULT FALSE");
                    } catch (SQLException ignored) {
                    }
                    try {
                        stmt.executeUpdate("ALTER TABLE Subject ADD COLUMN credits INT DEFAULT 3");
                    } catch (SQLException ignored) {
                    }
                    try {
                        stmt.executeUpdate("ALTER TABLE Subject ADD COLUMN isLab BOOLEAN DEFAULT FALSE");
                    } catch (SQLException ignored) {
                    }
                    try {
                        stmt.executeUpdate("ALTER TABLE Subject ADD COLUMN isOE BOOLEAN DEFAULT FALSE");
                    } catch (SQLException ignored) {
                    }
                    // Auto-create Sports subject (hardcoded for Thursday last hour)
                    try {
                        stmt.executeUpdate("INSERT INTO Subject(SubName,SubId,credits,isLab,isOE) VALUES('Sports','SPORTS-00',1,false,false)");
                    } catch (SQLException ignored) { /* already exists */ }
                    // Auto-create Technical Skills subject (hardcoded for Friday full day)
                    try {
                        stmt.executeUpdate("INSERT INTO Subject(SubName,SubId,credits,isLab,isOE) VALUES('Technical Skills','TECHSKILLS-00',1,false,false)");
                    } catch (SQLException ignored) { /* already exists */ }
                    try {
                        stmt.executeUpdate("INSERT INTO Subject(SubName,SubId,credits,isLab,isOE) VALUES('LUNCH','LUNCH-00',0,false,false)");
                    } catch (SQLException ignored) { /* already exists */ }
                    
                    // Add Users Table
                    try {
                        stmt.executeUpdate(
                            "CREATE TABLE IF NOT EXISTS Users (" +
                            "Id INT AUTO_INCREMENT PRIMARY KEY, " +
                            "Username VARCHAR(50) UNIQUE NOT NULL, " +
                            "Password VARCHAR(50) NOT NULL, " +
                            "Role VARCHAR(20) NOT NULL, " +
                            "Approved BOOLEAN DEFAULT FALSE, " +
                            "GrpId INT)"
                        );
                        // Add TeacherId to existing
                        try {
                            stmt.executeUpdate("ALTER TABLE Users ADD COLUMN TeacherId INT DEFAULT NULL");
                        } catch (SQLException ignored) {}

                        // Add default admin
                        stmt.executeUpdate("INSERT IGNORE INTO Users(Username, Password, Role, Approved, GrpId) VALUES('admin','admin','ADMIN',true,NULL)");
                    } catch (SQLException ignored) { }

                    // Add Notifications Table
                    try {
                        stmt.executeUpdate(
                            "CREATE TABLE IF NOT EXISTS Notifications (" +
                            "Id INT AUTO_INCREMENT PRIMARY KEY, " +
                            "GrpId INT NOT NULL, " +
                            "Message TEXT NOT NULL, " +
                            "DatePosted DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                            "Author VARCHAR(50) NOT NULL)"
                        );
                    } catch (SQLException ignored) { }

                } catch (SQLException ignored) {
                }
            }

        } catch (SQLException | ClassNotFoundException se) {
            se.printStackTrace();
        }
    }

    public Connection conn() {
        return this.conn;
    }

    public void close() {
        // Don't close the shared connection
    }

    public ResultSet executeQuery(String sql) {
        try {
            Statement s = conn.createStatement();
            return s.executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void executeUpdate(String sql) {
        try {
            Statement s = conn.createStatement();
            s.executeUpdate(sql);
            s.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}