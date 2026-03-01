package edu.Loopi.tests;

import edu.Loopi.view.LoginView;
import javafx.application.Application;

public class MainClass {
    public static void main(String[] args) {
        System.out.println("=====================================");
        System.out.println("       LOOPI - Plateforme");
        System.out.println("  Économie Circulaire & Solidarité");
        System.out.println("=====================================");
        System.out.println("Version: 1.0.0");
        System.out.println("Développé par: Équipe LOOPI");
        System.out.println("=====================================\n");

        // Démarrer l'application JavaFX
        Application.launch(LoginView.class, args);
    }
}