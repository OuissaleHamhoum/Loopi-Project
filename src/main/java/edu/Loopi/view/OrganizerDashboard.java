package edu.Loopi.view;

import edu.Loopi.entities.User;
import edu.Loopi.tools.SessionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.input.KeyCode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.File;

public class OrganizerDashboard {
    private User currentUser;
    private BorderPane root;
    private StackPane mainContentArea;
    private boolean sidebarCollapsed = false;

    public OrganizerDashboard(User user) {
        this.currentUser = user;
        SessionManager.login(user);
    }

    public void start(Stage stage) {
        stage.setTitle("LOOPI - Espace Organisateur");

        root = new BorderPane();
        root.setStyle("-fx-background-color: #E6F8F6;");

        // Créer le header modernisé
        VBox header = createModernHeader();
        root.setTop(header);

        // Créer le sidebar avec le style modernisé
        VBox sidebar = createModernSidebar(stage);
        root.setLeft(sidebar);

        // Zone de contenu principale
        mainContentArea = new StackPane();
        mainContentArea.setPadding(new Insets(0));
        mainContentArea.setStyle("-fx-background-color: transparent;");

        // Charger le dashboard par défaut
        showDashboardContent();
        root.setCenter(mainContentArea);

        Scene scene = new Scene(root, 1400, 800);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();

        SessionManager.printSessionInfo();
    }

    // ============ HEADER MODERNISÉ ============
    private VBox createModernHeader() {
        VBox header = new VBox();
        header.setStyle("-fx-background-color: linear-gradient(to right, #03414D, #03414D);");

        // Barre supérieure
        HBox topBar = new HBox(20);
        topBar.setPadding(new Insets(15, 30, 15, 30));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: transparent;");

        // Bouton menu toggle
        Button menuToggle = new Button("☰");
        menuToggle.setStyle("-fx-background-color: transparent; -fx-text-fill: white; " +
                "-fx-font-size: 20px; -fx-min-width: 40; -fx-min-height: 40; -fx-cursor: hand;");
        menuToggle.setOnAction(e -> toggleSidebar());

        // Logo et titre
        HBox logoBox = new HBox(15);
        logoBox.setAlignment(Pos.CENTER_LEFT);

        Circle logoCircle = new Circle(20);
        logoCircle.setFill(Color.web("#FFFFFF"));

        VBox titleBox = new VBox(2);
        Label mainTitle = new Label("LOOPI");
        mainTitle.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        mainTitle.setTextFill(Color.WHITE);

        Label subtitle = new Label("Organizer Dashboard");
        subtitle.setFont(Font.font("Arial", 11));
        subtitle.setTextFill(Color.web("#E6F8F6"));

        titleBox.getChildren().addAll(mainTitle, subtitle);
        logoBox.getChildren().addAll(logoCircle, titleBox);

        // Espaceur
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Barre de recherche
        HBox searchBox = new HBox(0);
        searchBox.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 8;");
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setPadding(new Insets(5, 15, 5, 15));

        TextField searchField = new TextField();
        searchField.setPromptText("Search events, products...");
        searchField.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; " +
                "-fx-font-size: 14px; -fx-text-fill: white;");
        searchField.setPrefWidth(250);
        searchField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                performSearch(searchField.getText());
            }
        });

        Button searchBtn = new Button("🔍");
        searchBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-cursor: hand;");
        searchBtn.setOnAction(e -> performSearch(searchField.getText()));

        searchBox.getChildren().addAll(searchField, searchBtn);

        // Notifications et profil
        HBox rightControls = new HBox(15);
        rightControls.setAlignment(Pos.CENTER_RIGHT);

        // Bouton notifications
        Button notificationsBtn = new Button("🔔");
        notificationsBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; " +
                "-fx-font-size: 18px; -fx-cursor: hand;");
        notificationsBtn.setTooltip(new Tooltip("Notifications"));

        // Profil utilisateur
        StackPane profileContainer = new StackPane();
        profileContainer.setOnMouseClicked(e -> showProfile());

        Circle profileCircle = new Circle(22);
        profileCircle.setFill(Color.web("#FFFFFF"));

        // Charger l'image de profil
        ImageView profileImageView = loadProfileImage(currentUser, 44);
        if (profileImageView != null) {
            profileContainer.getChildren().add(profileImageView);
        } else {
            String initials = getInitials(currentUser);
            Label profileText = new Label(initials);
            profileText.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            profileText.setTextFill(Color.web("#03414D"));
            profileContainer.getChildren().addAll(profileCircle, profileText);
        }

        VBox profileInfo = new VBox(2);
        profileInfo.setAlignment(Pos.CENTER_LEFT);

        Label profileName = new Label(currentUser.getNomComplet());
        profileName.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        profileName.setTextFill(Color.WHITE);

        Label profileRole = new Label(currentUser.getRole().toUpperCase());
        profileRole.setFont(Font.font("Arial", 10));
        profileRole.setTextFill(Color.web("#E6F8F6"));

        profileInfo.getChildren().addAll(profileName, profileRole);
        profileContainer.setStyle("-fx-cursor: hand;");

        HBox profileBox = new HBox(10);
        profileBox.setAlignment(Pos.CENTER_RIGHT);
        profileBox.getChildren().addAll(profileInfo, profileContainer);

        rightControls.getChildren().addAll(searchBox, notificationsBtn, profileBox);
        topBar.getChildren().addAll(menuToggle, logoBox, spacer, rightControls);

        // Barre des onglets
        HBox tabBar = createTabBar();

        header.getChildren().addAll(topBar, tabBar);
        return header;
    }

    private HBox createTabBar() {
        HBox tabBar = new HBox();
        tabBar.setPadding(new Insets(0, 30, 0, 30));
        tabBar.setStyle("-fx-background-color: #72DFD0;");
        tabBar.setPrefHeight(40);

        HBox tabsContainer = new HBox(0);
        tabsContainer.setAlignment(Pos.CENTER_LEFT);

        Button dashboardTab = createTabButton("Dashboard", true);
        Button productsTab = createTabButton("Products", false);
        Button eventsTab = createTabButton("Events", false);
        Button campaignsTab = createTabButton("Campaigns", false);

        tabsContainer.getChildren().addAll(dashboardTab, productsTab, eventsTab, campaignsTab);
        tabBar.getChildren().add(tabsContainer);

        return tabBar;
    }

    private Button createTabButton(String text, boolean active) {
        Button tab = new Button(text);
        tab.setPadding(new Insets(10, 20, 10, 20));
        tab.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        if (active) {
            tab.setStyle("-fx-background-color: #A0F6D2; -fx-text-fill: #03414D; " +
                    "-fx-border-color: transparent; -fx-cursor: hand;");
        } else {
            tab.setStyle("-fx-background-color: transparent; -fx-text-fill: #000000; " +
                    "-fx-border-color: transparent; -fx-cursor: hand;");
        }

        tab.setOnAction(e -> switchTab(text));

        tab.setOnMouseEntered(e -> {
            if (!tab.getStyle().contains("#A0F6D2")) {
                tab.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: #03414D; " +
                        "-fx-border-color: transparent; -fx-cursor: hand;");
            }
        });

        tab.setOnMouseExited(e -> {
            if (!tab.getStyle().contains("#A0F6D2")) {
                tab.setStyle("-fx-background-color: transparent; -fx-text-fill: #000000; " +
                        "-fx-border-color: transparent; -fx-cursor: hand;");
            }
        });

        return tab;
    }

    // ============ SIDEBAR MODERNISÉ ============
    private VBox createModernSidebar(Stage stage) {
        VBox sidebar = new VBox(0);
        sidebar.setPrefWidth(260);
        sidebar.setStyle("-fx-background-color: #72DFD0; -fx-border-color: #03414D; -fx-border-width: 0 1 0 0;");
        sidebar.setPadding(new Insets(25, 0, 20, 0));

        // Logo sidebar
        HBox sidebarLogo = new HBox(15);
        sidebarLogo.setPadding(new Insets(0, 0, 25, 25));
        sidebarLogo.setAlignment(Pos.CENTER_LEFT);

        Label sidebarIcon = new Label("🎯");
        sidebarIcon.setFont(Font.font("Arial", 24));

        VBox logoText = new VBox(2);
        Label logoTitle = new Label("LOOPI Organizer");
        logoTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        logoTitle.setTextFill(Color.web("#03414D"));

        Label logoSubtitle = new Label("Management");
        logoSubtitle.setFont(Font.font("Arial", 11));
        logoSubtitle.setTextFill(Color.web("#03414D"));

        logoText.getChildren().addAll(logoTitle, logoSubtitle);
        sidebarLogo.getChildren().addAll(sidebarIcon, logoText);

        // Navigation
        VBox navSection = new VBox(5);
        navSection.setPadding(new Insets(10, 15, 20, 15));

        // Dashboard Section
        Label dashboardLabel = new Label("DASHBOARD");
        dashboardLabel.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        dashboardLabel.setTextFill(Color.web("#03414D"));
        dashboardLabel.setPadding(new Insets(15, 0, 10, 10));

        Button dashboardBtn = createSidebarButton("📊", "Dashboard", true);
        dashboardBtn.setOnAction(e -> showDashboardContent());

        Button productsBtn = createSidebarButton("📦", "My Products", false);
        productsBtn.setOnAction(e -> showMyProducts());

        Button eventsBtn = createSidebarButton("📅", "My Events", false);
        eventsBtn.setOnAction(e -> showMyEvents());

        Button campaignsBtn = createSidebarButton("💰", "Campaigns", false);
        campaignsBtn.setOnAction(e -> showCampaigns());

        // Applications Section
        Label appsLabel = new Label("ACTIONS");
        appsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        appsLabel.setTextFill(Color.web("#03414D"));
        appsLabel.setPadding(new Insets(20, 0, 10, 10));

        Button addProductBtn = createSidebarButton("➕", "Add Product", false);
        addProductBtn.setOnAction(e -> addProduct());

        Button createEventBtn = createSidebarButton("➕", "Create Event", false);
        createEventBtn.setOnAction(e -> createEvent());

        Button statsBtn = createSidebarButton("📊", "Statistics", false);
        statsBtn.setOnAction(e -> showStatistics());

        Button profileBtn = createSidebarButton("👤", "My Profile", false);
        profileBtn.setOnAction(e -> showProfile());

        // Support Section
        Label supportLabel = new Label("SUPPORT");
        supportLabel.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        supportLabel.setTextFill(Color.web("#03414D"));
        supportLabel.setPadding(new Insets(20, 0, 10, 10));

        Button helpBtn = createSidebarButton("❓", "Help Center", false);
        helpBtn.setOnAction(e -> showHelpCenter());

        Button supportBtn = createSidebarButton("🆘", "Contact Support", false);
        supportBtn.setOnAction(e -> showContactSupport());

        // Espaceur
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Footer sidebar
        VBox footer = new VBox(15);
        footer.setPadding(new Insets(20, 20, 20, 20));
        footer.setStyle("-fx-background-color: #A0F6D2; -fx-border-color: #03414D; -fx-border-width: 1 0 0 0;");

        // User info in footer
        HBox userFooter = new HBox(10);
        userFooter.setAlignment(Pos.CENTER_LEFT);

        StackPane footerAvatarContainer = new StackPane();
        Circle footerAvatar = new Circle(20);
        footerAvatar.setFill(Color.web("#03414D"));

        ImageView footerImageView = loadProfileImage(currentUser, 40);
        if (footerImageView != null) {
            footerAvatarContainer.getChildren().add(footerImageView);
        } else {
            Label footerInitials = new Label(getInitials(currentUser));
            footerInitials.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            footerInitials.setTextFill(Color.WHITE);
            footerAvatarContainer.getChildren().addAll(footerAvatar, footerInitials);
        }

        VBox userInfo = new VBox(2);
        Label footerName = new Label(currentUser.getNomComplet());
        footerName.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        footerName.setTextFill(Color.web("#03414D"));

        Label footerEmail = new Label(currentUser.getEmail());
        footerEmail.setFont(Font.font("Arial", 10));
        footerEmail.setTextFill(Color.web("#03414D"));

        userInfo.getChildren().addAll(footerName, footerEmail);
        userFooter.getChildren().addAll(footerAvatarContainer, userInfo);

        Button logoutBtn = new Button("🚪 Logout");
        logoutBtn.setPrefWidth(220);
        logoutBtn.setPrefHeight(40);
        logoutBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
        logoutBtn.setOnAction(e -> logout(stage));

        footer.getChildren().addAll(userFooter, logoutBtn);

        navSection.getChildren().addAll(
                dashboardLabel, dashboardBtn, productsBtn, eventsBtn, campaignsBtn,
                appsLabel, addProductBtn, createEventBtn, statsBtn, profileBtn,
                supportLabel, helpBtn, supportBtn
        );

        sidebar.getChildren().addAll(sidebarLogo, navSection, spacer, footer);
        return sidebar;
    }

    private Button createSidebarButton(String icon, String text, boolean active) {
        Button btn = new Button(text);
        btn.setPrefWidth(230);
        btn.setPrefHeight(45);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(0, 0, 0, 15));
        btn.setFont(Font.font("Arial", 14));
        btn.setGraphicTextGap(15);

        // Ajouter l'icône
        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("Arial", 16));
        btn.setGraphic(iconLabel);

        if (active) {
            btn.setStyle("-fx-background-color: #A0F6D2; -fx-text-fill: #03414D; " +
                    "-fx-font-weight: bold; -fx-border-color: transparent; " +
                    "-fx-border-radius: 8;");
        } else {
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #03414D; " +
                    "-fx-border-color: transparent; -fx-border-radius: 8;");
        }

        btn.setOnMouseEntered(e -> {
            if (!btn.getStyle().contains("#A0F6D2")) {
                btn.setStyle("-fx-background-color: #A0F6D2; -fx-text-fill: #03414D; " +
                        "-fx-font-size: 14px; -fx-border-color: transparent; " +
                        "-fx-border-radius: 8;");
            }
        });

        btn.setOnMouseExited(e -> {
            if (!btn.getStyle().contains("#A0F6D2")) {
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #03414D; " +
                        "-fx-font-size: 14px; -fx-border-color: transparent; " +
                        "-fx-border-radius: 8;");
            }
        });

        return btn;
    }

    // ============ MÉTHODES DE NAVIGATION ============
    private void switchTab(String tabName) {
        switch (tabName) {
            case "Dashboard":
                showDashboardContent();
                break;
            case "Products":
                showMyProducts();
                break;
            case "Events":
                showMyEvents();
                break;
            case "Campaigns":
                showCampaigns();
                break;
        }
    }

    // ============ MÉTHODES D'AFFICHAGE ============
    private void showDashboardContent() {
        ScrollPane dashboardContent = createDashboardContent();
        mainContentArea.getChildren().clear();
        mainContentArea.getChildren().add(dashboardContent);
    }

    private ScrollPane createDashboardContent() {
        VBox container = new VBox(25);
        container.setPadding(new Insets(30));
        container.setStyle("-fx-background-color: #E6F8F6;");

        // En-tête du dashboard
        HBox dashboardHeader = new HBox();
        dashboardHeader.setAlignment(Pos.CENTER_LEFT);

        VBox headerText = new VBox(5);
        Label title = new Label("Dashboard Overview");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#03414D"));

        Label subtitle = new Label("Welcome back, " + currentUser.getPrenom() + "! Here's what's happening with your organization.");
        subtitle.setFont(Font.font("Arial", 14));
        subtitle.setTextFill(Color.web("#03414D"));

        headerText.getChildren().addAll(title, subtitle);
        HBox.setHgrow(headerText, Priority.ALWAYS);

        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setStyle("-fx-background-color: #FFFFFF; -fx-text-fill: #03414D; " +
                "-fx-font-weight: bold; -fx-border-color: #03414D; -fx-border-radius: 8; " +
                "-fx-padding: 10 20; -fx-cursor: hand;");
        refreshBtn.setOnAction(e -> refreshDashboard());

        dashboardHeader.getChildren().addAll(headerText, refreshBtn);

        // Cartes de statistiques
        HBox statsCards = createStatsCards();

        // Actions rapides
        VBox quickActions = createQuickActions();

        // Graphiques (placeholder)
        VBox chartsSection = createChartsSection();

        container.getChildren().addAll(dashboardHeader, statsCards, quickActions, chartsSection);

        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-border-color: transparent;");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        return scrollPane;
    }

    private HBox createStatsCards() {
        HBox statsCards = new HBox(20);
        statsCards.setAlignment(Pos.CENTER);

        VBox productsCard = createStatCard("📦", "Active Products", "12", "+2 this month", "#72DFD0", true);
        VBox salesCard = createStatCard("💰", "Total Sales", "2,450 DT", "+15% from last month", "#A0F6D2", false);
        VBox eventsCard = createStatCard("📅", "Active Events", "5", "2 upcoming", "#72DFD0", false);
        VBox campaignsCard = createStatCard("❤️", "Active Campaigns", "3", "80% average progress", "#A0F6D2", false);

        statsCards.getChildren().addAll(productsCard, salesCard, eventsCard, campaignsCard);
        return statsCards;
    }

    private VBox createStatCard(String icon, String title, String value, String subtitle, String color, boolean isMain) {
        VBox card = new VBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(25));
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12; " +
                "-fx-border-color: #72DFD0; -fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);");
        card.setPrefWidth(isMain ? 300 : 250);
        card.setPrefHeight(150);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        StackPane iconContainer = new StackPane();
        Circle iconCircle = new Circle(25);
        iconCircle.setFill(Color.web(color));

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("Arial", 16));
        iconLabel.setTextFill(Color.web("#03414D"));

        iconContainer.getChildren().addAll(iconCircle, iconLabel);

        VBox titleBox = new VBox(2);
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.web("#03414D"));

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setFont(Font.font("Arial", 12));
        subtitleLabel.setTextFill(Color.web("#03414D"));

        titleBox.getChildren().addAll(titleLabel, subtitleLabel);
        header.getChildren().addAll(iconContainer, titleBox);

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        valueLabel.setTextFill(Color.web("#03414D"));

        card.getChildren().addAll(header, valueLabel);
        return card;
    }

    private VBox createQuickActions() {
        VBox quickActions = new VBox(15);
        quickActions.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12; -fx-padding: 25; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        VBox textContent = new VBox(2);
        Label title = new Label("Quick Actions");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setTextFill(Color.web("#03414D"));

        Label subtitle = new Label("Common tasks you can perform quickly");
        subtitle.setFont(Font.font("Arial", 12));
        subtitle.setTextFill(Color.web("#03414D"));

        textContent.getChildren().addAll(title, subtitle);
        HBox.setHgrow(textContent, Priority.ALWAYS);
        header.getChildren().add(textContent);

        HBox actionsGrid = new HBox(20);
        actionsGrid.setAlignment(Pos.CENTER);
        actionsGrid.setPadding(new Insets(20, 0, 0, 0));

        Button addProductBtn = createActionButton("➕ Add Product", "#72DFD0");
        addProductBtn.setOnAction(e -> addProduct());

        Button createEventBtn = createActionButton("📅 Create Event", "#A0F6D2");
        createEventBtn.setOnAction(e -> createEvent());

        Button viewProductsBtn = createActionButton("📦 View Products", "#72DFD0");
        viewProductsBtn.setOnAction(e -> showMyProducts());

        Button viewCampaignsBtn = createActionButton("💰 View Campaigns", "#A0F6D2");
        viewCampaignsBtn.setOnAction(e -> showCampaigns());

        actionsGrid.getChildren().addAll(addProductBtn, createEventBtn, viewProductsBtn, viewCampaignsBtn);
        quickActions.getChildren().addAll(header, actionsGrid);

        return quickActions;
    }

    private Button createActionButton(String text, String color) {
        Button btn = new Button(text);
        btn.setPrefWidth(200);
        btn.setPrefHeight(80);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: #03414D; " +
                "-fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 12; " +
                "-fx-cursor: hand; -fx-alignment: CENTER;");
        btn.setWrapText(true);

        btn.setOnMouseEntered(e -> {
            btn.setStyle("-fx-background-color: #03414D; -fx-text-fill: white; " +
                    "-fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 12; " +
                    "-fx-cursor: hand; -fx-alignment: CENTER;");
        });

        btn.setOnMouseExited(e -> {
            btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: #03414D; " +
                    "-fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 12; " +
                    "-fx-cursor: hand; -fx-alignment: CENTER;");
        });

        return btn;
    }

    private VBox createChartsSection() {
        VBox chartsSection = new VBox(15);
        chartsSection.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12; -fx-padding: 25; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        VBox textContent = new VBox(2);
        Label title = new Label("Performance Overview");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setTextFill(Color.web("#03414D"));

        Label subtitle = new Label("Your organization's performance metrics");
        subtitle.setFont(Font.font("Arial", 12));
        subtitle.setTextFill(Color.web("#03414D"));

        textContent.getChildren().addAll(title, subtitle);
        HBox.setHgrow(textContent, Priority.ALWAYS);
        header.getChildren().add(textContent);

        // Placeholder pour les graphiques
        VBox chartsPlaceholder = new VBox(20);
        chartsPlaceholder.setAlignment(Pos.CENTER);
        chartsPlaceholder.setPrefHeight(200);
        chartsPlaceholder.setStyle("-fx-background-color: #E6F8F6; -fx-background-radius: 8;");

        Label placeholderText = new Label("Charts and analytics coming soon...");
        placeholderText.setFont(Font.font("Arial", 14));
        placeholderText.setTextFill(Color.web("#03414D"));

        chartsPlaceholder.getChildren().add(placeholderText);
        chartsSection.getChildren().addAll(header, chartsPlaceholder);

        return chartsSection;
    }

    private void showMyProducts() {
        VBox productsView = new VBox(20);
        productsView.setPadding(new Insets(30));
        productsView.setStyle("-fx-background-color: #E6F8F6;");

        Label title = new Label("My Products");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#03414D"));

        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        Label comingSoon = new Label("Product management features coming soon...");
        comingSoon.setFont(Font.font("Arial", 16));
        comingSoon.setTextFill(Color.web("#03414D"));

        content.getChildren().add(comingSoon);
        productsView.getChildren().addAll(title, content);

        mainContentArea.getChildren().clear();
        mainContentArea.getChildren().add(productsView);
    }

    private void showMyEvents() {
        EvenementView evenementView = new EvenementView(currentUser);
        mainContentArea.getChildren().clear();
        mainContentArea.getChildren().add(evenementView.getView());
    }

    private void showCampaigns() {
        CollectionView collectionView = new CollectionView(currentUser);
        mainContentArea.getChildren().clear();
        mainContentArea.getChildren().add(collectionView.getView());
    }

    private void addProduct() {
        showAlert("Info", "Add Product - Coming soon");
    }

    private void createEvent() {
        showMyEvents();
    }

    private void showStatistics() {
        showAlert("Info", "Statistics - Coming soon");
    }

    private void showProfile() {
        showAlert("Info", "My Profile - Coming soon");
    }

    private void showHelpCenter() {
        showAlert("Help Center", "For assistance, please contact: support@loopi.tn");
    }

    private void showContactSupport() {
        showAlert("Contact Support", "Email: support@loopi.tn\nPhone: +216 XX XXX XXX");
    }

    // ============ MÉTHODES UTILITAIRES ============
    private String getInitials(User user) {
        String initials = "";
        if (user.getPrenom() != null && !user.getPrenom().isEmpty()) {
            initials += String.valueOf(user.getPrenom().charAt(0)).toUpperCase();
        }
        if (user.getNom() != null && !user.getNom().isEmpty()) {
            initials += String.valueOf(user.getNom().charAt(0)).toUpperCase();
        }
        return initials.isEmpty() ? "U" : initials;
    }

    private ImageView loadProfileImage(User user, double size) {
        if (user.getPhoto() != null && !user.getPhoto().isEmpty() && !user.getPhoto().equals("default.jpg")) {
            try {
                String photoPath = user.getPhoto();
                File imageFile = new File(photoPath);

                if (imageFile.exists()) {
                    Image avatarImage = new Image("file:" + imageFile.getAbsolutePath(), size, size, true, true, true);
                    ImageView avatarImageView = new ImageView(avatarImage);
                    avatarImageView.setFitWidth(size);
                    avatarImageView.setFitHeight(size);
                    avatarImageView.setPreserveRatio(true);
                    avatarImageView.setStyle("-fx-background-radius: 50%;");
                    return avatarImageView;
                }
            } catch (Exception e) {
                System.out.println("Error loading profile image: " + e.getMessage());
            }
        }
        return null;
    }

    private void toggleSidebar() {
        VBox sidebar = (VBox) root.getLeft();
        if (sidebarCollapsed) {
            sidebar.setPrefWidth(260);
            sidebarCollapsed = false;
        } else {
            sidebar.setPrefWidth(80);
            sidebarCollapsed = true;
        }
    }

    private void performSearch(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return;
        }
        showAlert("Search", "Searching for: " + keyword);
    }

    private void refreshDashboard() {
        showDashboardContent();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void logout(Stage stage) {
        SessionManager.logout();
        stage.close();
        new LoginView().start(new Stage());
    }
}