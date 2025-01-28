package com.example.javafxproject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private static final String URL = "jdbc:mysql://localhost:3306/WomenStoreDB";
    private static final String USER = "root"; // Remplace par ton utilisateur MySQL
    private static final String PASSWORD = "Mazigh_2002"; // Remplace par ton mot de passe MySQL

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
