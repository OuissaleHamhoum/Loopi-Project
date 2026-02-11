package edu.Loopi.view;

import edu.Loopi.entities.User;
import edu.Loopi.tools.SessionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class UserDashboard {
    private User currentUser;
    private BorderPane root;

    public UserDashboard(User user) {
        this.currentUser = user;
        SessionManager.login(user);
    }

    public void start(Stage stage) {
        stage.setTitle("LOOPI - Espace Participant");
        root = new BorderPane();
        root.setStyle("-fx-background-color: #f8fafc;");

        root.setTop(createHeader());
        root.setLeft(createSidebar(stage));
        root.setCenter(createMainContent());

        Scene scene = new Scene(root, 1200, 700);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    private HBox createHeader() {
        HBox header = new HBox(20);
        header.setStyle("-fx-background-color: #059669; -fx-padding: 15 30;");
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("LOOPI PARTICIPANT");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        title.setTextFill(Color.WHITE);

        HBox userBox = new HBox(10);
        userBox.setAlignment(Pos.CENTER_RIGHT);
        Label welcome = new Label("Bienvenue, " + currentUser.getPrenom());
        welcome.setTextFill(Color.WHITE);
        welcome.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        userBox.getChildren().add(welcome);
        HBox.setHgrow(userBox, Priority.ALWAYS);

        header.getChildren().addAll(title, userBox);
        return header;
    }

    private VBox createSidebar(Stage stage) {
        VBox sidebar = new VBox(5);
        sidebar.setStyle("-fx-background-color: #064e3b;");
        sidebar.setPrefWidth(240);
        sidebar.setPadding(new Insets(20, 0, 0, 0));

        // MENU BUTTONS
        sidebar.getChildren().addAll(
                createMenuButton("🏠 Accueil", e -> root.setCenter(createMainContent())),
                createMenuButton("🛒 Boutique", e -> showAlert("Boutique", "Bientôt...")),
                createMenuButton("📋 Mes commandes", e -> showAlert("Commandes", "Bientôt...")),
                createMenuButton("📅 Événements", e -> showAlert("Événements", "Bientôt...")),
                createMenuButton("🌿 Les Campagnes", e -> root.setCenter(new ParticipantCampaignView(currentUser).getView())),
                createMenuButton("💰 Mes Dons", e -> root.setCenter(new DonationHistoryView(currentUser).getView())), // CHANGED BUTTON
                createMenuButton("👤 Mon profil", e -> showAlert("Profil", "Bientôt..."))
        );

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = new Button("🚪 Déconnexion");
        logoutBtn.setPrefWidth(240);
        logoutBtn.setPrefHeight(50);
        logoutBtn.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white; -fx-alignment: center-left; -fx-padding: 0 20;");
        logoutBtn.setOnAction(e -> logout(stage));

        sidebar.getChildren().addAll(spacer, logoutBtn);
        return sidebar;
    }

    private Button createMenuButton(String text, javafx.event.EventHandler<javafx.event.ActionEvent> event) {
        Button btn = new Button(text);
        btn.setPrefWidth(240); btn.setPrefHeight(50);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ecfdf5; -fx-font-size: 14px; -fx-alignment: center-left; -fx-padding: 0 20; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #065f46; -fx-text-fill: white; -fx-alignment: center-left; -fx-padding: 0 20; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ecfdf5; -fx-alignment: center-left; -fx-padding: 0 20; -fx-cursor: hand;"));
        btn.setOnAction(event);
        return btn;
    }

    private VBox createMainContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(40));
        content.setAlignment(Pos.CENTER);

        Label l = new Label("Espace Eco-Citoyen");
        l.setFont(Font.font("System", FontWeight.BOLD, 24));
        content.getChildren().add(l);
        return content;
    }


    private void logout(Stage stage) {
        SessionManager.logout();
        stage.close();
        new LoginView().start(new Stage());
    }

    private void showAlert(String title, String msg) {
        javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}