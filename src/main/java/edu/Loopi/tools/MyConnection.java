package edu.Loopi.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/loopi_db";
    private static final String USERNAME = "root";
    private static final String PASSWORD = ""; // Mettre votre mot de passe MySQL

    private Connection connection;
    private static MyConnection instance;

    private MyConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("✅ Connexion à loopi_db établie avec succès!");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Driver MySQL non trouvé: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("❌ Erreur de connexion à la base de données: " + e.getMessage());
        }
    }

    public static MyConnection getInstance() {
        if (instance == null) {
            synchronized (MyConnection.class) {
                if (instance == null) {
                    instance = new MyConnection();
                }
            }
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    public static boolean testConnection() {
        try (Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD)) {
            return true;
        } catch (SQLException e) {
            System.err.println("❌ Test de connexion échoué: " + e.getMessage());
            return false;
        }
    }
}