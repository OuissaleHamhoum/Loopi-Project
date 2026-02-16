package edu.Loopi.tools;

import org.junit.jupiter.api.Test;
import java.sql.Connection;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests simples pour vérifier la connexion à la base de données
 */
public class MyConnectionTest {

    /**
     * Test 1 : Vérifier que le singleton fonctionne
     */
    @Test
    void testSingleton() {
        // Obtenir deux instances
        MyConnection instance1 = MyConnection.getInstance();
        MyConnection instance2 = MyConnection.getInstance();

        // Vérifier que c'est la même instance
        assertSame(instance1, instance2, "Les instances devraient être identiques");

        System.out.println("✅ Singleton OK");
    }

    /**
     * Test 2 : Vérifier la connexion à la base de données
     */
    @Test
    void testConnexion() {
        // Obtenir la connexion
        Connection connection = MyConnection.getInstance().getConnection();

        // Vérifier que la connexion n'est pas null
        assertNotNull(connection, "La connexion ne devrait pas être null");

        System.out.println("✅ Connexion à la base OK");
    }

    /**
     * Test 3 : Test simple avec la méthode utilitaire
     */
    @Test
    void testConnectionUtilitaire() {
        boolean estConnecte = MyConnection.testConnection();

        assertTrue(estConnecte, "La connexion devrait réussir");

        System.out.println("✅ Test utilitaire OK");
    }
}