package com.example.javafxproject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TestConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/WomenStoreDB"; // Remplace par le nom de ta base
    private static final String USER = "root"; // Ton utilisateur MySQL
    private static final String PASSWORD = "Mazigh_2002"; // Ton mot de passe MySQL

    public static void main(String[] args) {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            System.out.println("Connexion réussie à la base de données !");
        } catch (SQLException e) {
            System.err.println("Erreur de connexion : " + e.getMessage());
        }
    }
}
