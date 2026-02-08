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

    public UserDashboard(User user) {
        this.currentUser = user;
        SessionManager.login(user);
    }

    public void start(Stage stage) {
        stage.setTitle("LOOPI - Espace Participant");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f5f5;");

        // Header
        HBox header = createHeader();
        root.setTop(header);

        // Menu latéral
        VBox sidebar = createSidebar(stage);
        root.setLeft(sidebar);

        // Contenu principal
        VBox content = createMainContent();
        root.setCenter(content);

        Scene scene = new Scene(root, 1200, 700);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();

        SessionManager.printSessionInfo();
    }

    private HBox createHeader() {
        HBox header = new HBox(20);
        header.setStyle("-fx-background-color: #4CAF50; -fx-padding: 15 30;");
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("LOOPI PARTICIPANT");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setTextFill(Color.WHITE);

        HBox userBox = new HBox(10);
        userBox.setAlignment(Pos.CENTER_RIGHT);

        Label welcome = new Label("Bienvenue, " + currentUser.getPrenom());
        welcome.setTextFill(Color.WHITE);
        welcome.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        userBox.getChildren().add(welcome);
        HBox.setHgrow(userBox, Priority.ALWAYS);

        header.getChildren().addAll(title, userBox);
        return header;
    }

    private VBox createSidebar(Stage stage) {
        VBox sidebar = new VBox(5);
        sidebar.setStyle("-fx-background-color: #333;");
        sidebar.setPrefWidth(220);
        sidebar.setPadding(new Insets(20, 0, 0, 0));

        Button browseBtn = createMenuButton("🛒 Boutique");
        browseBtn.setOnAction(e -> showProducts());

        Button ordersBtn = createMenuButton("📋 Mes commandes");
        ordersBtn.setOnAction(e -> showOrders());

        Button eventsBtn = createMenuButton("📅 Événements");
        eventsBtn.setOnAction(e -> showEvents());

        Button donationsBtn = createMenuButton("💰 Mes dons");
        donationsBtn.setOnAction(e -> showDonations());

        Button couponsBtn = createMenuButton("🎫 Mes coupons");
        couponsBtn.setOnAction(e -> showCoupons());

        Button profileBtn = createMenuButton("👤 Mon profil");
        profileBtn.setOnAction(e -> showProfile());

        Button settingsBtn = createMenuButton("⚙️ Paramètres");
        settingsBtn.setOnAction(e -> showSettings());

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = createMenuButton("🚪 Déconnexion");
        logoutBtn.setStyle("-fx-background-color: #d32f2f;");
        logoutBtn.setOnAction(e -> logout(stage));

        sidebar.getChildren().addAll(browseBtn, ordersBtn, eventsBtn, donationsBtn,
                couponsBtn, profileBtn, settingsBtn, spacer, logoutBtn);
        return sidebar;
    }

    private Button createMenuButton(String text) {
        Button btn = new Button(text);
        btn.setPrefWidth(220);
        btn.setPrefHeight(50);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-alignment: center-left; -fx-padding: 0 20;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #444; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-alignment: center-left; -fx-padding: 0 20;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-alignment: center-left; -fx-padding: 0 20;"));
        return btn;
    }

    private VBox createMainContent() {
        VBox content = new VBox(30);
        content.setPadding(new Insets(40));
        content.setAlignment(Pos.CENTER);

        Label welcome = new Label("Bienvenue dans votre espace Participant!");
        welcome.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        welcome.setTextFill(Color.web("#4CAF50"));

        Label subtitle = new Label("Découvrez les produits recyclés, participez à des événements écologiques\n" +
                "et contribuez à l'économie circulaire");
        subtitle.setFont(Font.font("Arial", 16));
        subtitle.setTextFill(Color.web("#666"));
        subtitle.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        // Cartes de statistiques
        HBox statsBox = new HBox(20);
        statsBox.setAlignment(Pos.CENTER);

        VBox ordersCard = createInfoCard("📦", "Mes commandes", "0 commandes");
        VBox eventsCard = createInfoCard("📅", "Événements", "0 participations");
        VBox donationsCard = createInfoCard("💰", "Mes dons", "0.00 DT donnés");
        VBox couponsCard = createInfoCard("🎫", "Coupons", "0 coupons actifs");

        statsBox.getChildren().addAll(ordersCard, eventsCard, donationsCard, couponsCard);

        // Actions rapides
        VBox quickActions = new VBox(10);
        quickActions.setAlignment(Pos.CENTER);

        Label actionsTitle = new Label("Que souhaitez-vous faire ?");
        actionsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        actionsTitle.setTextFill(Color.web("#333"));

        HBox actionsBox = new HBox(15);
        actionsBox.setAlignment(Pos.CENTER);

        Button browseBtn = createQuickActionButton("🛒 Explorer la boutique");
        Button eventsBtn = createQuickActionButton("📅 Voir les événements");
        Button donateBtn = createQuickActionButton("❤️ Faire un don");

        actionsBox.getChildren().addAll(browseBtn, eventsBtn, donateBtn);
        quickActions.getChildren().addAll(actionsTitle, actionsBox);

        content.getChildren().addAll(welcome, subtitle, statsBox, quickActions);
        return content;
    }

    private VBox createInfoCard(String icon, String title, String value) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-border-color: #e0e0e0; -fx-border-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        card.setPrefSize(200, 150);

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("Arial", 36));

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.web("#333"));

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Arial", 14));
        valueLabel.setTextFill(Color.web("#666"));

        card.getChildren().addAll(iconLabel, titleLabel, valueLabel);
        return card;
    }

    private Button createQuickActionButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 12 25; -fx-background-radius: 8;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #388E3C; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 12 25; -fx-background-radius: 8;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 12 25; -fx-background-radius: 8;"));
        return btn;
    }

    private void showProducts() {
        showAlert("Info", "Boutique - En développement");
    }

    private void showOrders() {
        showAlert("Info", "Mes commandes - En développement");
    }

    private void showEvents() {
        showAlert("Info", "Événements - En développement");
    }

    private void showDonations() {
        showAlert("Info", "Mes dons - En développement");
    }

    private void showCoupons() {
        showAlert("Info", "Mes coupons - En développement");
    }

    private void showProfile() {
        showAlert("Info", "Mon profil - En développement");
    }

    private void showSettings() {
        showAlert("Info", "Paramètres - En développement");
    }

    private void logout(Stage stage) {
        SessionManager.logout();
        stage.close();
        new LoginView().start(new Stage());
    }

    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}