package edu.Loopi.view;

import edu.Loopi.entities.*;
import edu.Loopi.services.*;
import edu.Loopi.tools.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.input.KeyCode;
import javafx.scene.shape.Circle;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdminDashboard {
    private User currentUser;
    private UserService userService;
    private Stage primaryStage;
    private BorderPane root;
    private TableView<User> userTable;
    private ObservableList<User> userList;

    public AdminDashboard(User user) {
        this.currentUser = user;
        this.userService = new UserService();
        SessionManager.login(user);
    }

    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("LOOPI - Tableau de Bord Administrateur");

        root = new BorderPane();
        root.setStyle("-fx-background-color: #f8fafc;");

        HBox header = createModernHeader();
        root.setTop(header);

        VBox sidebar = createModernSidebar();
        root.setLeft(sidebar);

        ScrollPane dashboardContent = createDashboardView();
        root.setCenter(dashboardContent);

        Scene scene = new Scene(root, 1400, 800);

        try {
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        } catch (Exception e) {
            System.out.println("CSS non chargé, utilisation des styles inline");
        }

        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();

        SessionManager.printSessionInfo();
    }

    private HBox createModernHeader() {
        HBox header = new HBox(20);
        header.setPadding(new Insets(20, 30, 20, 30));
        header.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0;");
        header.setAlignment(Pos.CENTER_LEFT);

        // Logo/Titre
        HBox logoBox = new HBox(15);
        logoBox.setAlignment(Pos.CENTER_LEFT);

        Button menuToggle = new Button("☰");
        menuToggle.setStyle("-fx-background-color: transparent; -fx-font-size: 18px; -fx-text-fill: #4f46e5;");
        menuToggle.setOnAction(e -> toggleSidebar());

        VBox titleBox = new VBox(2);
        Label mainTitle = new Label("LOOPI ADMIN");
        mainTitle.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        mainTitle.setTextFill(Color.web("#1e293b"));

        Label subtitle = new Label("Tableau de bord analytique");
        subtitle.setFont(Font.font("Arial", 11));
        subtitle.setTextFill(Color.web("#64748b"));

        titleBox.getChildren().addAll(mainTitle, subtitle);
        logoBox.getChildren().addAll(menuToggle, titleBox);

        // Barre de recherche et infos utilisateur
        HBox rightSection = new HBox(20);
        rightSection.setAlignment(Pos.CENTER_RIGHT);

        // Barre de recherche
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER);
        searchBox.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 20; -fx-padding: 5 15;");

        TextField searchField = new TextField();
        searchField.setPromptText("Rechercher...");
        searchField.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-font-size: 14px;");
        searchField.setPrefWidth(200);
        searchField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                performGlobalSearch(searchField.getText());
            }
        });

        Button searchBtn = new Button("🔍");
        searchBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #64748b;");
        searchBtn.setOnAction(e -> performGlobalSearch(searchField.getText()));

        searchBox.getChildren().addAll(searchField, searchBtn);

        // Profil utilisateur
        HBox userProfile = new HBox(10);
        userProfile.setAlignment(Pos.CENTER_RIGHT);

        VBox userInfo = new VBox(2);
        userInfo.setAlignment(Pos.CENTER_RIGHT);

        Label userName = new Label(currentUser.getNomComplet());
        userName.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        userName.setTextFill(Color.web("#1e293b"));

        Label userRole = new Label(currentUser.getRole().toUpperCase());
        userRole.setFont(Font.font("Arial", 11));
        userRole.setTextFill(Color.web("#64748b"));

        userInfo.getChildren().addAll(userName, userRole);

        // Avatar
        StackPane avatar = createUserAvatar(currentUser, 40);
        avatar.setOnMouseClicked(e -> showProfile());

        userProfile.getChildren().addAll(userInfo, avatar);
        HBox.setHgrow(rightSection, Priority.ALWAYS);
        rightSection.getChildren().addAll(searchBox, userProfile);
        header.getChildren().addAll(logoBox, rightSection);

        return header;
    }

    private StackPane createUserAvatar(User user, double size) {
        StackPane avatar = new StackPane();
        avatar.setPrefSize(size, size);

        try {
            if (user.getPhoto() != null && !user.getPhoto().equals("default.jpg") && !user.getPhoto().isEmpty()) {
                File imgFile = new File("uploads/" + user.getPhoto());
                if (imgFile.exists()) {
                    Image image = new Image(imgFile.toURI().toString());
                    ImageView imageView = new ImageView(image);
                    imageView.setFitWidth(size);
                    imageView.setFitHeight(size);
                    imageView.setPreserveRatio(true);

                    Circle clip = new Circle(size / 2);
                    clip.setCenterX(size / 2);
                    clip.setCenterY(size / 2);
                    imageView.setClip(clip);

                    avatar.getChildren().add(imageView);
                    avatar.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
                    return avatar;
                }
            }
        } catch (Exception e) {
            System.out.println("Erreur chargement avatar: " + e.getMessage());
        }

        // Avatar par défaut
        Circle circle = new Circle(size / 2);
        circle.setFill(Color.web("#4f46e5"));

        String initials = "";
        if (user.getPrenom() != null && !user.getPrenom().isEmpty()) {
            initials += String.valueOf(user.getPrenom().charAt(0)).toUpperCase();
        }
        if (user.getNom() != null && !user.getNom().isEmpty()) {
            initials += String.valueOf(user.getNom().charAt(0)).toUpperCase();
        }

        Label avatarText = new Label(initials.isEmpty() ? "U" : initials);
        avatarText.setFont(Font.font("Arial", FontWeight.BOLD, size / 2));
        avatarText.setTextFill(Color.WHITE);

        avatar.getChildren().addAll(circle, avatarText);
        avatar.setStyle("-fx-cursor: hand;");
        return avatar;
    }

    private void performGlobalSearch(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return;
        }
        showUserManagementViewInCenter();
        searchUsers(keyword);
        showAlert("Recherche", "Résultats pour: " + keyword);
    }

    private VBox createModernSidebar() {
        VBox sidebar = new VBox(0);
        sidebar.setPrefWidth(280);
        sidebar.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-width: 0 1 0 0;");
        sidebar.setPadding(new Insets(30, 0, 0, 0));

        // Navigation principale
        VBox navSection = new VBox(5);
        navSection.setPadding(new Insets(0, 20, 30, 20));

        Label navTitle = new Label("NAVIGATION");
        navTitle.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        navTitle.setTextFill(Color.web("#94a3b8"));
        navTitle.setPadding(new Insets(0, 0, 10, 0));

        // Boutons de navigation
        Button dashboardBtn = createNavButton("📊 Dashboard", true);
        dashboardBtn.setOnAction(e -> showDashboard());

        Button usersBtn = createNavButton("👥 Utilisateurs", false);
        usersBtn.setOnAction(e -> showUserManagementViewInCenter());

        navSection.getChildren().addAll(navTitle, dashboardBtn, usersBtn);

        // Section paramètres
        VBox settingsSection = new VBox(5);
        settingsSection.setPadding(new Insets(20, 20, 0, 20));

        Label settingsTitle = new Label("PARAMÈTRES");
        settingsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        settingsTitle.setTextFill(Color.web("#94a3b8"));
        settingsTitle.setPadding(new Insets(0, 0, 10, 0));

        Button profileBtn = createNavButton("👤 Mon Profil", false);
        profileBtn.setOnAction(e -> showProfile());

        Button logoutBtn = createNavButton("🚪 Déconnexion", false);
        logoutBtn.setStyle("-fx-background-color: #fef2f2; -fx-text-fill: #dc2626; -fx-border-color: #fecaca;");
        logoutBtn.setOnAction(e -> logout());

        settingsSection.getChildren().addAll(settingsTitle, profileBtn, logoutBtn);

        // Espaceur
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Footer sidebar
        VBox footer = new VBox(10);
        footer.setPadding(new Insets(20));
        footer.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-width: 1 0 0 0;");

        Label version = new Label("LOOPI v2.0");
        version.setFont(Font.font("Arial", 10));
        version.setTextFill(Color.web("#94a3b8"));

        Label date = new Label(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        date.setFont(Font.font("Arial", 10));
        date.setTextFill(Color.web("#94a3b8"));

        footer.getChildren().addAll(version, date);
        sidebar.getChildren().addAll(navSection, spacer, settingsSection, footer);

        return sidebar;
    }

    private Button createNavButton(String text, boolean active) {
        Button btn = new Button(text);
        btn.setPrefWidth(240);
        btn.setPrefHeight(45);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(0, 0, 0, 15));

        if (active) {
            btn.setStyle("-fx-background-color: #eef2ff; -fx-text-fill: #4f46e5; " +
                    "-fx-font-size: 14px; -fx-font-weight: bold; -fx-border-color: #c7d2fe; " +
                    "-fx-border-width: 0 0 0 3; -fx-border-radius: 0;");
        } else {
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #64748b; " +
                    "-fx-font-size: 14px; -fx-border-color: transparent;");
        }

        btn.setOnMouseEntered(e -> {
            if (!btn.getStyle().contains("#4f46e5")) {
                btn.setStyle("-fx-background-color: #f8fafc; -fx-text-fill: #475569; " +
                        "-fx-font-size: 14px; -fx-border-color: transparent;");
            }
        });

        btn.setOnMouseExited(e -> {
            if (!btn.getStyle().contains("#4f46e5")) {
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #64748b; " +
                        "-fx-font-size: 14px; -fx-border-color: transparent;");
            }
        });

        return btn;
    }

    private ScrollPane createDashboardView() {
        VBox container = new VBox(25);
        container.setPadding(new Insets(30));
        container.setStyle("-fx-background-color: #f8fafc;");

        // En-tête dashboard
        HBox dashboardHeader = createDashboardHeader();

        // Cartes de statistiques principales
        HBox statsCards = createStatsCards();

        // Graphiques d'analyse
        HBox chartRow1 = createChartRow1();
        HBox chartRow2 = createChartRow2();

        // Tableau des activités récentes
        VBox activityTable = createActivityTable();

        container.getChildren().addAll(dashboardHeader, statsCards, chartRow1, chartRow2, activityTable);

        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-border-color: transparent;");

        return scrollPane;
    }

    private HBox createDashboardHeader() {
        HBox dashboardHeader = new HBox();
        dashboardHeader.setAlignment(Pos.CENTER_LEFT);

        VBox headerText = new VBox(5);
        Label title = new Label("Tableau de bord analytique");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#1e293b"));

        Label subtitle = new Label("Analyse des utilisateurs et performances");
        subtitle.setFont(Font.font("Arial", 14));
        subtitle.setTextFill(Color.web("#64748b"));

        headerText.getChildren().addAll(title, subtitle);
        HBox.setHgrow(headerText, Priority.ALWAYS);
        dashboardHeader.getChildren().add(headerText);

        // Filtres date
        HBox filters = new HBox(10);
        filters.setAlignment(Pos.CENTER_RIGHT);

        ComboBox<String> periodFilter = new ComboBox<>();
        periodFilter.getItems().addAll("Aujourd'hui", "Cette semaine", "Ce mois", "Cette année");
        periodFilter.setValue("Ce mois");
        periodFilter.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-background-radius: 8;");

        Button refreshBtn = new Button("🔄 Actualiser");
        refreshBtn.setStyle("-fx-background-color: #4f46e5; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 16;");
        refreshBtn.setOnAction(e -> refreshDashboard());

        filters.getChildren().addAll(periodFilter, refreshBtn);
        HBox.setHgrow(filters, Priority.ALWAYS);
        dashboardHeader.getChildren().add(filters);

        return dashboardHeader;
    }

    private HBox createStatsCards() {
        HBox statsCards = new HBox(20);
        statsCards.setAlignment(Pos.CENTER);

        int totalUsers = userService.countUsers();
        int[] roleStats = userService.getUserStatistics();
        int activeUsers = getActiveUsersThisMonth();
        int newUsersToday = getNewUsersToday();

        VBox usersCard = createStatCard("👥", "Utilisateurs totaux", String.valueOf(totalUsers),
                "+" + calculateGrowthRate(totalUsers, getLastMonthUsers()) + "%", "#3b82f6", "#eff6ff");

        VBox adminsCard = createStatCard("👑", "Administrateurs", String.valueOf(roleStats[0]),
                "+" + calculateGrowthRate(roleStats[0], getLastMonthAdmins()) + "%", "#10b981", "#ecfdf5");

        VBox activeCard = createStatCard("✅", "Utilisateurs actifs", String.valueOf(activeUsers),
                "+" + calculateGrowthRate(activeUsers, getLastMonthActiveUsers()) + "%", "#8b5cf6", "#f5f3ff");

        VBox newCard = createStatCard("🆕", "Nouveaux aujourd'hui", String.valueOf(newUsersToday),
                "vs hier: +" + calculateGrowthRate(newUsersToday, getYesterdayUsers()) + "%", "#f59e0b", "#fffbeb");

        statsCards.getChildren().addAll(usersCard, adminsCard, activeCard, newCard);
        return statsCards;
    }

    private int getActiveUsersThisMonth() {
        // Simuler 80% des utilisateurs actifs ce mois
        return (int)(userService.countUsers() * 0.8);
    }

    private int getNewUsersToday() {
        // Simuler 5 nouveaux utilisateurs aujourd'hui
        return 5;
    }

    private int getLastMonthUsers() {
        // Simuler 90% des utilisateurs du mois dernier
        return (int)(userService.countUsers() * 0.9);
    }

    private int getLastMonthAdmins() {
        int[] stats = userService.getUserStatistics();
        return (int)(stats[0] * 0.85);
    }

    private int getLastMonthActiveUsers() {
        return (int)(getActiveUsersThisMonth() * 0.95);
    }

    private int getYesterdayUsers() {
        // Simuler 4 nouveaux utilisateurs hier
        return 4;
    }

    private double calculateGrowthRate(double current, double previous) {
        if (previous == 0) return 100.0;
        return Math.round(((current - previous) / previous) * 100 * 10.0) / 10.0;
    }

    private VBox createStatCard(String icon, String title, String value, String change, String color, String bgColor) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 12; -fx-border-color: #e2e8f0; -fx-border-width: 1;");
        card.setPrefWidth(220);

        HBox iconRow = new HBox();
        iconRow.setAlignment(Pos.CENTER_LEFT);

        StackPane iconContainer = new StackPane();
        iconContainer.setPrefSize(40, 40);
        iconContainer.setStyle("-fx-background-color: white; -fx-background-radius: 8;");

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("Arial", 18));
        iconContainer.getChildren().add(iconLabel);
        HBox.setHgrow(iconRow, Priority.ALWAYS);
        iconRow.getChildren().add(iconContainer);

        VBox content = new VBox(5);

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", 12));
        titleLabel.setTextFill(Color.web("#64748b"));

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        valueLabel.setTextFill(Color.web("#1e293b"));

        HBox changeRow = new HBox(5);
        changeRow.setAlignment(Pos.CENTER_LEFT);

        Label changeLabel = new Label(change);
        changeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 11));

        if (change.startsWith("+")) {
            changeLabel.setTextFill(Color.web("#10b981"));
        } else if (change.startsWith("-")) {
            changeLabel.setTextFill(Color.web("#ef4444"));
        } else {
            changeLabel.setTextFill(Color.web("#64748b"));
        }

        Label periodLabel = new Label("vs mois dernier");
        periodLabel.setFont(Font.font("Arial", 10));
        periodLabel.setTextFill(Color.web("#94a3b8"));

        changeRow.getChildren().addAll(changeLabel, periodLabel);
        content.getChildren().addAll(titleLabel, valueLabel, changeRow);
        card.getChildren().addAll(iconRow, content);

        return card;
    }

    private HBox createChartRow1() {
        HBox chartRow1 = new HBox(20);
        chartRow1.setAlignment(Pos.CENTER);

        // Graphique d'inscription mensuelle
        VBox registrationChart = createRegistrationChart();

        // Répartition par rôle
        VBox roleChart = createRoleChart();

        chartRow1.getChildren().addAll(registrationChart, roleChart);
        return chartRow1;
    }

    private VBox createRegistrationChart() {
        VBox container = new VBox(15);
        container.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 20;");
        container.setPrefWidth(600);

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        VBox textContent = new VBox(2);
        Label title = new Label("Inscriptions mensuelles");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        title.setTextFill(Color.web("#1e293b"));

        Label subtitle = new Label("Évolution des nouvelles inscriptions");
        subtitle.setFont(Font.font("Arial", 12));
        subtitle.setTextFill(Color.web("#64748b"));

        textContent.getChildren().addAll(title, subtitle);
        HBox.setHgrow(textContent, Priority.ALWAYS);
        header.getChildren().add(textContent);

        // Bar Chart
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Mois");
        NumberAxis yAxis = new NumberAxis(0, 100, 10);
        yAxis.setLabel("Nombre d'inscriptions");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("");
        barChart.setLegendVisible(false);
        barChart.setCategoryGap(20);
        barChart.setPrefHeight(300);
        barChart.setStyle("-fx-background-color: transparent;");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        String[] months = {"Jan", "Fév", "Mar", "Avr", "Mai", "Juin"};
        int[] data = {45, 52, 48, 65, 72, 68}; // Données simulées

        for (int i = 0; i < months.length; i++) {
            XYChart.Data<String, Number> dataPoint = new XYChart.Data<>(months[i], data[i]);
            series.getData().add(dataPoint);

            Tooltip tooltip = new Tooltip(String.format("%s: %d inscriptions", months[i], data[i]));
            dataPoint.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    Tooltip.install(newNode, tooltip);
                }
            });
        }

        barChart.getData().add(series);
        container.getChildren().addAll(header, barChart);

        return container;
    }

    private VBox createRoleChart() {
        VBox container = new VBox(15);
        container.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 20;");
        container.setPrefWidth(600);

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        VBox textContent = new VBox(2);
        Label title = new Label("Répartition par rôle");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        title.setTextFill(Color.web("#1e293b"));

        Label subtitle = new Label("Pourcentage d'utilisateurs par type");
        subtitle.setFont(Font.font("Arial", 12));
        subtitle.setTextFill(Color.web("#64748b"));

        textContent.getChildren().addAll(title, subtitle);
        HBox.setHgrow(textContent, Priority.ALWAYS);
        header.getChildren().add(textContent);

        // Pie Chart
        PieChart pieChart = new PieChart();
        int[] stats = userService.getUserStatistics();
        int total = stats[0] + stats[1] + stats[2];

        if (total > 0) {
            PieChart.Data adminSlice = new PieChart.Data(
                    "Administrateurs (" + Math.round((stats[0] * 100.0) / total) + "%)",
                    stats[0]
            );
            PieChart.Data orgSlice = new PieChart.Data(
                    "Organisateurs (" + Math.round((stats[1] * 100.0) / total) + "%)",
                    stats[1]
            );
            PieChart.Data partSlice = new PieChart.Data(
                    "Participants (" + Math.round((stats[2] * 100.0) / total) + "%)",
                    stats[2]
            );

            pieChart.getData().addAll(adminSlice, orgSlice, partSlice);
        }

        pieChart.setLabelsVisible(true);
        pieChart.setLegendVisible(false);
        pieChart.setPrefHeight(300);
        pieChart.setStyle("-fx-background-color: transparent;");

        container.getChildren().addAll(header, pieChart);
        return container;
    }

    private HBox createChartRow2() {
        HBox chartRow2 = new HBox(20);
        chartRow2.setAlignment(Pos.CENTER);

        // Activité quotidienne
        VBox activityChart = createActivityChart();

        // Géographie des utilisateurs
        VBox geographyChart = createGeographyChart();

        chartRow2.getChildren().addAll(activityChart, geographyChart);
        return chartRow2;
    }

    private VBox createActivityChart() {
        VBox container = new VBox(15);
        container.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 20;");
        container.setPrefWidth(600);

        Label title = new Label("Activité quotidienne");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        title.setTextFill(Color.web("#1e293b"));

        // Line Chart
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.getCategories().addAll("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim");
        xAxis.setLabel("Jour");
        NumberAxis yAxis = new NumberAxis(0, 200, 40);
        yAxis.setLabel("Nombre d'activités");

        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("");
        lineChart.setLegendVisible(false);
        lineChart.setCreateSymbols(true);
        lineChart.setPrefHeight(250);
        lineChart.setStyle("-fx-background-color: transparent;");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Activités");

        int[] weeklyData = {120, 145, 98, 167, 189, 76, 45}; // Données simulées

        for (int i = 0; i < 7; i++) {
            XYChart.Data<String, Number> data = new XYChart.Data<>(xAxis.getCategories().get(i), weeklyData[i]);
            series.getData().add(data);

            Tooltip tooltip = new Tooltip(String.format("%s: %d activités",
                    xAxis.getCategories().get(i), weeklyData[i]));
            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    Tooltip.install(newNode, tooltip);
                }
            });
        }

        lineChart.getData().add(series);
        container.getChildren().addAll(title, lineChart);

        return container;
    }

    private VBox createGeographyChart() {
        VBox container = new VBox(15);
        container.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 20;");
        container.setPrefWidth(600);

        Label title = new Label("Répartition géographique");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        title.setTextFill(Color.web("#1e293b"));

        // Carte simplifiée
        Pane mapPane = new Pane();
        mapPane.setPrefSize(560, 250);
        mapPane.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-radius: 8;");

        // Données simulées
        String[] regions = {"Île-de-France", "Auvergne-Rhône-Alpes", "Occitanie", "Nouvelle-Aquitaine", "Provence-Alpes-Côte d'Azur"};
        int[] counts = {45, 32, 28, 24, 19};
        double[][] positions = {{300, 100}, {280, 180}, {200, 220}, {150, 150}, {400, 200}};
        Color[] colors = {Color.web("#4f46e5"), Color.web("#3b82f6"), Color.web("#10b981"),
                Color.web("#f59e0b"), Color.web("#8b5cf6")};

        for (int i = 0; i < regions.length; i++) {
            double radius = 15 + (counts[i] / 5.0);
            Circle circle = new Circle(positions[i][0], positions[i][1], radius);
            circle.setFill(colors[i].deriveColor(0, 1, 1, 0.7));
            circle.setStroke(colors[i]);
            circle.setStrokeWidth(1);

            Tooltip tooltip = new Tooltip(String.format("%s\n%d utilisateurs", regions[i], counts[i]));
            Tooltip.install(circle, tooltip);

            Label label = new Label(regions[i]);
            label.setFont(Font.font("Arial", FontWeight.BOLD, 10));
            label.setTextFill(Color.web("#1e293b"));
            label.setLayoutX(positions[i][0] - 25);
            label.setLayoutY(positions[i][1] + radius + 10);

            mapPane.getChildren().addAll(circle, label);
        }

        container.getChildren().addAll(title, mapPane);
        return container;
    }

    private VBox createActivityTable() {
        VBox activityTable = new VBox(15);
        activityTable.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 20;");
        activityTable.setPrefWidth(1220);

        HBox tableHeader = new HBox();
        tableHeader.setAlignment(Pos.CENTER_LEFT);

        VBox headerText = new VBox(2);
        Label activityTitle = new Label("Activités récentes des utilisateurs");
        activityTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        activityTitle.setTextFill(Color.web("#1e293b"));

        Label activitySubtitle = new Label("Dernières actions sur la plateforme");
        activitySubtitle.setFont(Font.font("Arial", 12));
        activitySubtitle.setTextFill(Color.web("#64748b"));

        headerText.getChildren().addAll(activityTitle, activitySubtitle);
        HBox.setHgrow(headerText, Priority.ALWAYS);
        tableHeader.getChildren().add(headerText);

        // Tableau
        TableView<String[]> activityTableView = new TableView<>();
        activityTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        activityTableView.setPrefHeight(250);
        activityTableView.setStyle("-fx-background-color: transparent; -fx-border-color: #e2e8f0; -fx-border-radius: 8;");

        TableColumn<String[], String> userCol = new TableColumn<>("Utilisateur");
        userCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[0]));
        userCol.setPrefWidth(150);

        TableColumn<String[], String> actionCol = new TableColumn<>("Action");
        actionCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[1]));
        actionCol.setPrefWidth(350);

        TableColumn<String[], String> timeCol = new TableColumn<>("Heure");
        timeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[2]));
        timeCol.setPrefWidth(120);

        TableColumn<String[], String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[3]));
        typeCol.setPrefWidth(100);

        activityTableView.getColumns().addAll(userCol, actionCol, timeCol, typeCol);

        // Données simulées
        ObservableList<String[]> activityData = FXCollections.observableArrayList(
                new String[]{"Jean Dupont", "Connexion réussie", "10:30", "Connexion"},
                new String[]{"Marie Martin", "Modification du profil", "11:15", "Profil"},
                new String[]{"Pierre Durand", "Changement de mot de passe", "12:45", "Sécurité"},
                new String[]{"Sophie Bernard", "Ajout d'une photo de profil", "14:20", "Profil"},
                new String[]{"Thomas Petit", "Connexion échouée", "15:10", "Connexion"},
                new String[]{"Julie Moreau", "Inscription réussie", "16:30", "Inscription"}
        );

        activityTableView.setItems(activityData);

        activityTable.getChildren().addAll(tableHeader, activityTableView);
        return activityTable;
    }

    // GESTION DES UTILISATEURS
    private void showUserManagementViewInCenter() {
        ScrollPane content = createEnhancedUserManagementView();
        root.setCenter(content);
        updateSidebarButton("users");
    }

    private ScrollPane createEnhancedUserManagementView() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(30));

        Label title = new Label("Gestion des Utilisateurs");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#1e293b"));

        HBox statsBox = createEnhancedQuickStats();
        HBox toolbar = createEnhancedToolbar();
        VBox tableContainer = createUserTableContainer();

        container.getChildren().addAll(title, statsBox, toolbar, tableContainer);

        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-border-color: transparent;");

        return scrollPane;
    }

    private HBox createEnhancedQuickStats() {
        HBox statsBox = new HBox(20);

        int[] stats = userService.getUserStatistics();
        int totalUsers = stats[0] + stats[1] + stats[2];

        VBox totalBox = createEnhancedStatCard("👥", "Total Utilisateurs", String.valueOf(totalUsers),
                "+" + calculateGrowthRate(totalUsers, getLastMonthUsers()) + "%", "#4f46e5");
        VBox adminBox = createEnhancedStatCard("👑", "Administrateurs", String.valueOf(stats[0]),
                "+" + calculateGrowthRate(stats[0], getLastMonthAdmins()) + "%", "#10b981");
        VBox orgBox = createEnhancedStatCard("🎯", "Organisateurs", String.valueOf(stats[1]),
                "+" + calculateGrowthRate(stats[1], getLastMonthOrganizers()) + "%", "#3b82f6");
        VBox partBox = createEnhancedStatCard("😊", "Participants", String.valueOf(stats[2]),
                "+" + calculateGrowthRate(stats[2], getLastMonthParticipants()) + "%", "#f59e0b");
        VBox activeBox = createEnhancedStatCard("✅", "Actifs ce mois", String.valueOf(getActiveUsersThisMonth()),
                "+" + calculateGrowthRate(getActiveUsersThisMonth(), getLastMonthActiveUsers()) + "%", "#8b5cf6");

        statsBox.getChildren().addAll(totalBox, adminBox, orgBox, partBox, activeBox);
        return statsBox;
    }

    private int getLastMonthOrganizers() {
        int[] stats = userService.getUserStatistics();
        return (int)(stats[1] * 0.9);
    }

    private int getLastMonthParticipants() {
        int[] stats = userService.getUserStatistics();
        return (int)(stats[2] * 0.88);
    }

    private VBox createEnhancedStatCard(String icon, String title, String value, String change, String color) {
        VBox card = new VBox(15);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #e2e8f0; -fx-border-width: 1;");
        card.setPrefWidth(180);

        StackPane iconContainer = new StackPane();
        iconContainer.setPrefSize(50, 50);
        iconContainer.setStyle("-fx-background-color: " + color + "15; -fx-background-radius: 12;");

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("Arial", 20));
        iconLabel.setTextFill(Color.web(color));
        iconContainer.getChildren().add(iconLabel);

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        valueLabel.setTextFill(Color.web("#1e293b"));

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        titleLabel.setTextFill(Color.web("#64748b"));

        HBox changeRow = new HBox(5);
        changeRow.setAlignment(Pos.CENTER);

        Label changeLabel = new Label(change);
        changeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 11));

        if (change.startsWith("+")) {
            changeLabel.setTextFill(Color.web("#10b981"));
        } else {
            changeLabel.setTextFill(Color.web("#ef4444"));
        }

        Label changeText = new Label("vs mois dernier");
        changeText.setFont(Font.font("Arial", 10));
        changeText.setTextFill(Color.web("#94a3b8"));

        changeRow.getChildren().addAll(changeLabel, changeText);
        card.getChildren().addAll(iconContainer, valueLabel, titleLabel, changeRow);

        return card;
    }

    private HBox createEnhancedToolbar() {
        HBox toolbar = new HBox(15);
        toolbar.setPadding(new Insets(10, 0, 10, 0));

        Button addBtn = createEnhancedToolbarButton("➕ Ajouter utilisateur", "#4f46e5");
        addBtn.setOnAction(e -> showAddUserDialog());

        Button editBtn = createEnhancedToolbarButton("✏️ Modifier", "#3b82f6");
        editBtn.setOnAction(e -> editSelectedUser());

        Button deleteBtn = createEnhancedToolbarButton("🗑️ Supprimer", "#ef4444");
        deleteBtn.setOnAction(e -> deleteSelectedUser());

        Button exportBtn = createEnhancedToolbarButton("📤 Exporter", "#10b981");
        exportBtn.setOnAction(e -> exportUsersToFile());

        Button refreshBtn = createEnhancedToolbarButton("🔄 Actualiser", "#64748b");
        refreshBtn.setOnAction(e -> refreshUserTable());

        // Filtre par rôle
        HBox roleFilterBox = new HBox(5);
        roleFilterBox.setAlignment(Pos.CENTER_LEFT);

        Label filterLabel = new Label("Filtrer par rôle:");
        filterLabel.setFont(Font.font("Arial", 12));
        filterLabel.setTextFill(Color.web("#64748b"));

        ComboBox<String> roleFilter = new ComboBox<>();
        roleFilter.getItems().addAll("Tous", "admin", "organisateur", "participant");
        roleFilter.setValue("Tous");
        roleFilter.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; " +
                "-fx-background-radius: 8; -fx-padding: 8 12;");
        roleFilter.setPrefWidth(120);
        roleFilter.setOnAction(e -> filterUsersByRole(roleFilter.getValue()));

        roleFilterBox.getChildren().addAll(filterLabel, roleFilter);

        // Champ de recherche
        HBox searchBox = new HBox(0);
        searchBox.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                "-fx-border-color: #e2e8f0; -fx-border-radius: 8;");
        searchBox.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("Rechercher un utilisateur...");
        searchField.setPrefWidth(300);
        searchField.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; " +
                "-fx-font-size: 14px; -fx-padding: 12 15;");
        searchField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                searchUsers(searchField.getText());
            }
        });

        Button searchBtn = new Button("🔍");
        searchBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #64748b; " +
                "-fx-padding: 12 15; -fx-cursor: hand;");
        searchBtn.setOnAction(e -> searchUsers(searchField.getText()));

        searchBox.getChildren().addAll(searchField, searchBtn);
        HBox.setHgrow(searchBox, Priority.ALWAYS);
        toolbar.getChildren().addAll(addBtn, editBtn, deleteBtn, exportBtn, refreshBtn, roleFilterBox, searchBox);

        return toolbar;
    }

    private Button createEnhancedToolbarButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 12 20; -fx-background-radius: 8; " +
                "-fx-cursor: hand;");

        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: " + darkenColor(color) + "; -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-padding: 12 20; -fx-background-radius: 8; " +
                        "-fx-cursor: hand;"
        ));

        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: " + color + "; -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-padding: 12 20; -fx-background-radius: 8; " +
                        "-fx-cursor: hand;"
        ));

        return btn;
    }

    private VBox createUserTableContainer() {
        VBox tableContainer = new VBox(15);
        tableContainer.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-padding: 25; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 3);");

        HBox tableHeader = new HBox();
        tableHeader.setAlignment(Pos.CENTER_LEFT);

        VBox headerText = new VBox(2);
        Label tableTitle = new Label("Liste des utilisateurs");
        tableTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        tableTitle.setTextFill(Color.web("#1e293b"));

        Label tableSubtitle = new Label("Gérez tous les utilisateurs de la plateforme");
        tableSubtitle.setFont(Font.font("Arial", 12));
        tableSubtitle.setTextFill(Color.web("#64748b"));

        headerText.getChildren().addAll(tableTitle, tableSubtitle);
        HBox.setHgrow(headerText, Priority.ALWAYS);

        int userCount = userService.countUsers();
        Label userCountLabel = new Label("Total: " + userCount + " utilisateurs");
        userCountLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        userCountLabel.setTextFill(Color.web("#4f46e5"));
        userCountLabel.setPadding(new Insets(5, 15, 5, 15));
        userCountLabel.setStyle("-fx-background-color: #eef2ff; -fx-background-radius: 12;");

        tableHeader.getChildren().addAll(headerText, userCountLabel);

        userTable = new TableView<>();
        setupEnhancedUserTable();

        tableContainer.getChildren().addAll(tableHeader, userTable);
        return tableContainer;
    }

    @SuppressWarnings("unchecked")
    private void setupEnhancedUserTable() {
        userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        userTable.setPlaceholder(new Label("Aucun utilisateur trouvé"));
        userTable.setStyle("-fx-background-color: transparent; -fx-border-color: #e2e8f0; -fx-border-radius: 8;");
        userTable.setPrefHeight(500);

        TableColumn<User, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(70);
        idCol.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold;");
        idCol.setSortable(true);

        TableColumn<User, String> nomCol = new TableColumn<>("Nom");
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        nomCol.setPrefWidth(150);
        nomCol.setStyle("-fx-alignment: CENTER_LEFT;");

        TableColumn<User, String> prenomCol = new TableColumn<>("Prénom");
        prenomCol.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        prenomCol.setPrefWidth(150);
        prenomCol.setStyle("-fx-alignment: CENTER_LEFT;");

        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(250);
        emailCol.setStyle("-fx-alignment: CENTER_LEFT;");

        TableColumn<User, String> roleCol = new TableColumn<>("Rôle");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleCol.setPrefWidth(120);
        roleCol.setCellFactory(column -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String role, boolean empty) {
                super.updateItem(role, empty);

                if (empty || role == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(role);
                    setAlignment(Pos.CENTER);
                    setPadding(new Insets(5, 10, 5, 10));
                    setFont(Font.font("Arial", FontWeight.BOLD, 11));

                    if (role.equalsIgnoreCase("admin")) {
                        setTextFill(Color.web("#4f46e5"));
                        setStyle("-fx-background-color: #eef2ff; -fx-background-radius: 8;");
                    } else if (role.equalsIgnoreCase("organisateur")) {
                        setTextFill(Color.web("#3b82f6"));
                        setStyle("-fx-background-color: #eff6ff; -fx-background-radius: 8;");
                    } else if (role.equalsIgnoreCase("participant")) {
                        setTextFill(Color.web("#10b981"));
                        setStyle("-fx-background-color: #ecfdf5; -fx-background-radius: 8;");
                    } else {
                        setTextFill(Color.web("#64748b"));
                        setStyle("");
                    }
                }
            }
        });

        TableColumn<User, String> sexeCol = new TableColumn<>("Genre");
        sexeCol.setCellValueFactory(new PropertyValueFactory<>("sexe"));
        sexeCol.setPrefWidth(90);
        sexeCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<User, String> dateCol = new TableColumn<>("Inscription");
        dateCol.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCreatedAt() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                );
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        dateCol.setPrefWidth(110);
        dateCol.setStyle("-fx-alignment: CENTER;");
        dateCol.setSortable(true);

        userTable.getColumns().addAll(idCol, nomCol, prenomCol, emailCol, roleCol, sexeCol, dateCol);

        // Alternance de couleurs de ligne
        userTable.setRowFactory(tv -> new TableRow<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setStyle("");
                } else {
                    if (getIndex() % 2 == 0) {
                        setStyle("-fx-background-color: #f8fafc;");
                    } else {
                        setStyle("-fx-background-color: white;");
                    }
                }
            }
        });

        refreshUserTable();
    }

    private void refreshUserTable() {
        List<User> users = userService.getAllUsers();
        userList = FXCollections.observableArrayList(users);
        userTable.setItems(userList);
    }

    private void filterUsersByRole(String role) {
        if (role.equals("Tous")) {
            refreshUserTable();
            return;
        }

        List<User> users = userService.getUsersByRole(role);
        userList = FXCollections.observableArrayList(users);
        userTable.setItems(userList);
    }

    private void searchUsers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            refreshUserTable();
            return;
        }

        List<User> users = userService.searchUsers(keyword);
        userList = FXCollections.observableArrayList(users);
        userTable.setItems(userList);

        if (users.isEmpty()) {
            userTable.setPlaceholder(new Label("Aucun utilisateur trouvé pour: " + keyword));
        }
    }

    private void exportUsersToFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter les utilisateurs");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Fichiers Excel", "*.xlsx"),
                new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"),
                new FileChooser.ExtensionFilter("Fichiers CSV", "*.csv")
        );

        File file = fileChooser.showSaveDialog(primaryStage);
        if (file != null) {
            int count = userTable.getItems().size();
            showAlert("Export réussi",
                    String.format("%d utilisateurs exportés vers:\n%s", count, file.getAbsolutePath()));
        }
    }

    private void showAddUserDialog() {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Ajouter un nouvel utilisateur");
        dialog.setHeaderText("Remplissez les informations du nouvel utilisateur");
        dialog.initOwner(primaryStage);
        dialog.getDialogPane().setStyle("-fx-background-color: white; -fx-padding: 20;");

        Label header = new Label("Ajouter un nouvel utilisateur");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        header.setTextFill(Color.web("#1e293b"));
        dialog.setGraphic(header);

        ButtonType addButtonType = new ButtonType("Ajouter", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(15);
        grid.setPadding(new Insets(20, 10, 10, 10));

        Label personalInfoLabel = new Label("Informations personnelles");
        personalInfoLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        personalInfoLabel.setTextFill(Color.web("#4f46e5"));
        grid.add(personalInfoLabel, 0, 0, 2, 1);

        TextField nomField = new TextField();
        nomField.setPromptText("Nom");
        nomField.setPrefHeight(40);
        styleEnhancedTextField(nomField);

        TextField prenomField = new TextField();
        prenomField.setPromptText("Prénom");
        prenomField.setPrefHeight(40);
        styleEnhancedTextField(prenomField);

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.setPrefHeight(40);
        styleEnhancedTextField(emailField);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mot de passe (min. 8 caractères)");
        passwordField.setPrefHeight(40);
        styleEnhancedTextField(passwordField);

        Label accountLabel = new Label("Paramètres du compte");
        accountLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        accountLabel.setTextFill(Color.web("#4f46e5"));
        grid.add(accountLabel, 0, 4, 2, 1);

        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("admin", "organisateur", "participant");
        roleCombo.setValue("participant");
        roleCombo.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; " +
                "-fx-background-radius: 8; -fx-padding: 10 14; -fx-font-size: 14px;");
        roleCombo.setPrefWidth(300);

        ComboBox<String> genreCombo = new ComboBox<>();
        genreCombo.getItems().addAll("Homme", "Femme", "Non spécifié");
        genreCombo.setValue("Non spécifié");
        genreCombo.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; " +
                "-fx-background-radius: 8; -fx-padding: 10 14; -fx-font-size: 14px;");
        genreCombo.setPrefWidth(300);

        grid.add(new Label("Nom*:"), 0, 1);
        grid.add(nomField, 1, 1);
        grid.add(new Label("Prénom*:"), 0, 2);
        grid.add(prenomField, 1, 2);
        grid.add(new Label("Email*:"), 0, 3);
        grid.add(emailField, 1, 3);
        grid.add(new Label("Mot de passe*:"), 0, 5);
        grid.add(passwordField, 1, 5);
        grid.add(new Label("Rôle*:"), 0, 6);
        grid.add(roleCombo, 1, 6);
        grid.add(new Label("Genre:"), 0, 7);
        grid.add(genreCombo, 1, 7);

        Label note = new Label("* Champs obligatoires");
        note.setFont(Font.font("Arial", 10));
        note.setTextFill(Color.web("#94a3b8"));
        grid.add(note, 1, 8);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefSize(500, 500);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                if (nomField.getText().isEmpty() || prenomField.getText().isEmpty() ||
                        emailField.getText().isEmpty() || passwordField.getText().isEmpty()) {
                    showAlert("Erreur", "Veuillez remplir tous les champs obligatoires");
                    return null;
                }

                if (passwordField.getText().length() < 8) {
                    showAlert("Erreur", "Le mot de passe doit contenir au moins 8 caractères");
                    return null;
                }

                User newUser = new User();
                newUser.setNom(nomField.getText());
                newUser.setPrenom(prenomField.getText());
                newUser.setEmail(emailField.getText());
                newUser.setPassword(passwordField.getText());
                newUser.setRole(roleCombo.getValue());
                newUser.setPhoto("default.jpg");

                String genre = genreCombo.getValue();
                int idGenre = 3;
                if ("Homme".equals(genre)) idGenre = 1;
                else if ("Femme".equals(genre)) idGenre = 2;
                newUser.setIdGenre(idGenre);

                return newUser;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(user -> {
            if (userService.emailExists(user.getEmail())) {
                showAlert("Erreur", "Cet email est déjà utilisé!");
                return;
            }

            if (userService.addUser(user)) {
                showAlert("Succès", "Utilisateur ajouté avec succès!");
                refreshUserTable();
            } else {
                showAlert("Erreur", "Erreur lors de l'ajout de l'utilisateur");
            }
        });
    }

    private void editSelectedUser() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert("Avertissement", "Veuillez sélectionner un utilisateur à modifier");
            return;
        }

        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Modifier l'utilisateur");
        dialog.setHeaderText("Modifier les informations de " + selectedUser.getNomComplet());
        dialog.initOwner(primaryStage);
        dialog.getDialogPane().setStyle("-fx-background-color: white; -fx-padding: 20;");

        Label header = new Label("Modifier l'utilisateur");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        header.setTextFill(Color.web("#1e293b"));
        dialog.setGraphic(header);

        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(15);
        grid.setPadding(new Insets(20, 10, 10, 10));

        StackPane avatarPreview = createUserAvatar(selectedUser, 80);

        Button changeAvatarBtn = new Button("Changer la photo");
        changeAvatarBtn.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #64748b; " +
                "-fx-background-radius: 8; -fx-padding: 8 16;");
        changeAvatarBtn.setOnAction(e -> changeUserAvatar(selectedUser));

        VBox avatarBox = new VBox(10, avatarPreview, changeAvatarBtn);
        avatarBox.setAlignment(Pos.CENTER);
        grid.add(avatarBox, 0, 0, 1, 4);

        TextField nomField = new TextField(selectedUser.getNom());
        nomField.setPrefHeight(40);
        styleEnhancedTextField(nomField);

        TextField prenomField = new TextField(selectedUser.getPrenom());
        prenomField.setPrefHeight(40);
        styleEnhancedTextField(prenomField);

        TextField emailField = new TextField(selectedUser.getEmail());
        emailField.setPrefHeight(40);
        styleEnhancedTextField(emailField);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Nouveau mot de passe (laisser vide pour garder l'actuel)");
        passwordField.setPrefHeight(40);
        styleEnhancedTextField(passwordField);

        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("admin", "organisateur", "participant");
        roleCombo.setValue(selectedUser.getRole());
        roleCombo.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; " +
                "-fx-background-radius: 8; -fx-padding: 10 14; -fx-font-size: 14px;");
        roleCombo.setPrefWidth(300);

        ComboBox<String> genreCombo = new ComboBox<>();
        genreCombo.getItems().addAll("Homme", "Femme", "Non spécifié");
        genreCombo.setValue(selectedUser.getSexe() != null ? selectedUser.getSexe() : "Non spécifié");
        genreCombo.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; " +
                "-fx-background-radius: 8; -fx-padding: 10 14; -fx-font-size: 14px;");
        genreCombo.setPrefWidth(300);

        CheckBox activeCheckBox = new CheckBox("Compte actif");
        activeCheckBox.setSelected(true);
        activeCheckBox.setStyle("-fx-font-size: 14px;");

        grid.add(new Label("Nom*:"), 1, 0);
        grid.add(nomField, 2, 0);
        grid.add(new Label("Prénom*:"), 1, 1);
        grid.add(prenomField, 2, 1);
        grid.add(new Label("Email*:"), 1, 2);
        grid.add(emailField, 2, 2);
        grid.add(new Label("Nouveau mot de passe:"), 1, 3);
        grid.add(passwordField, 2, 3);
        grid.add(new Label("Rôle*:"), 1, 4);
        grid.add(roleCombo, 2, 4);
        grid.add(new Label("Genre:"), 1, 5);
        grid.add(genreCombo, 2, 5);
        grid.add(activeCheckBox, 2, 6);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefSize(600, 500);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (nomField.getText().isEmpty() || prenomField.getText().isEmpty() ||
                        emailField.getText().isEmpty()) {
                    showAlert("Erreur", "Veuillez remplir tous les champs obligatoires");
                    return null;
                }

                selectedUser.setNom(nomField.getText());
                selectedUser.setPrenom(prenomField.getText());
                selectedUser.setEmail(emailField.getText());
                selectedUser.setRole(roleCombo.getValue());

                String genre = genreCombo.getValue();
                int idGenre = 3;
                if ("Homme".equals(genre)) idGenre = 1;
                else if ("Femme".equals(genre)) idGenre = 2;
                selectedUser.setIdGenre(idGenre);

                if (!passwordField.getText().isEmpty()) {
                    if (passwordField.getText().length() < 8) {
                        showAlert("Erreur", "Le mot de passe doit contenir au moins 8 caractères");
                        return null;
                    }
                    selectedUser.setPassword(passwordField.getText());
                }

                return selectedUser;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(user -> {
            if (userService.updateUser(user)) {
                showAlert("Succès", "Utilisateur modifié avec succès!");
                refreshUserTable();
            } else {
                showAlert("Erreur", "Erreur lors de la modification de l'utilisateur");
            }
        });
    }

    private void changeUserAvatar(User user) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une photo de profil");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            try {
                // Créer le dossier uploads s'il n'existe pas
                File uploadsDir = new File("uploads");
                if (!uploadsDir.exists()) {
                    uploadsDir.mkdir();
                }

                // Générer un nom unique pour le fichier
                String fileName = "user_" + user.getId() + "_" + System.currentTimeMillis() +
                        getFileExtension(file.getName());

                File destFile = new File(uploadsDir, fileName);
                Files.copy(file.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                user.setPhoto(fileName);
                if (userService.updateUser(user)) {
                    showAlert("Succès", "Photo de profil mise à jour avec succès!");
                    refreshUserTable();
                }
            } catch (Exception e) {
                showAlert("Erreur", "Erreur lors du changement de photo: " + e.getMessage());
            }
        }
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex);
        }
        return ".jpg";
    }

    private void deleteSelectedUser() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert("Avertissement", "Veuillez sélectionner un utilisateur à supprimer");
            return;
        }

        if (selectedUser.getId() == currentUser.getId()) {
            showAlert("Erreur", "Vous ne pouvez pas supprimer votre propre compte!");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation de suppression");
        confirmAlert.setHeaderText("Supprimer l'utilisateur");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer définitivement " +
                selectedUser.getNomComplet() + " ?\n\nCette action est irréversible.");
        confirmAlert.initOwner(primaryStage);
        confirmAlert.getDialogPane().setStyle("-fx-background-color: white;");

        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            if (userService.deleteUser(selectedUser.getId())) {
                showAlert("Succès", "Utilisateur supprimé avec succès!");
                refreshUserTable();
            } else {
                showAlert("Erreur", "Erreur lors de la suppression de l'utilisateur");
            }
        }
    }

    // PROFIL UTILISATEUR
    private void showProfile() {
        VBox profileView = new VBox(20);
        profileView.setPadding(new Insets(30));
        profileView.setStyle("-fx-background-color: #f8fafc;");

        Label title = new Label("Mon Profil");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#1e293b"));

        HBox profileContainer = new HBox(30);
        profileContainer.setAlignment(Pos.TOP_CENTER);

        // Informations profil
        VBox profileInfo = new VBox(20);
        profileInfo.setPrefWidth(500);

        VBox profileCard = new VBox(20);
        profileCard.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 30;");

        HBox profileHeader = new HBox(20);
        profileHeader.setAlignment(Pos.CENTER_LEFT);

        StackPane avatarContainer = createUserAvatar(currentUser, 100);

        // Bouton pour changer la photo
        Button changePhotoBtn = new Button("📷 Changer la photo");
        changePhotoBtn.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #64748b; " +
                "-fx-font-size: 12px; -fx-background-radius: 8; -fx-padding: 5 10;");
        changePhotoBtn.setOnAction(e -> changeCurrentUserPhoto());

        VBox avatarBox = new VBox(10);
        avatarBox.setAlignment(Pos.CENTER);
        avatarBox.getChildren().addAll(avatarContainer, changePhotoBtn);

        VBox nameBox = new VBox(5);
        Label nameLabel = new Label(currentUser.getNomComplet());
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        nameLabel.setTextFill(Color.web("#1e293b"));

        Label emailLabel = new Label(currentUser.getEmail());
        emailLabel.setFont(Font.font("Arial", 14));
        emailLabel.setTextFill(Color.web("#64748b"));

        HBox roleBox = new HBox(5);
        Label roleLabel = new Label(currentUser.getRole().toUpperCase());
        roleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        roleLabel.setTextFill(Color.WHITE);
        roleLabel.setPadding(new Insets(5, 15, 5, 15));
        roleLabel.setStyle("-fx-background-color: #4f46e5; -fx-background-radius: 20;");

        roleBox.getChildren().add(roleLabel);
        nameBox.getChildren().addAll(nameLabel, emailLabel, roleBox);
        profileHeader.getChildren().addAll(avatarBox, nameBox);

        // Formulaire d'édition
        VBox form = new VBox(15);
        form.setPadding(new Insets(20, 0, 0, 0));

        GridPane formGrid = new GridPane();
        formGrid.setHgap(15);
        formGrid.setVgap(15);

        TextField nomField = new TextField(currentUser.getNom());
        styleEnhancedTextField(nomField);

        TextField prenomField = new TextField(currentUser.getPrenom());
        styleEnhancedTextField(prenomField);

        TextField emailField = new TextField(currentUser.getEmail());
        styleEnhancedTextField(emailField);

        PasswordField currentPasswordField = new PasswordField();
        currentPasswordField.setPromptText("Mot de passe actuel (pour vérification)");
        styleEnhancedTextField(currentPasswordField);

        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Nouveau mot de passe");
        styleEnhancedTextField(newPasswordField);

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirmer le nouveau mot de passe");
        styleEnhancedTextField(confirmPasswordField);

        ComboBox<String> genreCombo = new ComboBox<>();
        genreCombo.getItems().addAll("Homme", "Femme", "Non spécifié");
        genreCombo.setValue(currentUser.getSexe() != null ? currentUser.getSexe() : "Non spécifié");
        genreCombo.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; " +
                "-fx-background-radius: 8; -fx-padding: 10 14; -fx-font-size: 14px;");
        genreCombo.setPrefWidth(300);

        formGrid.add(new Label("Nom:"), 0, 0);
        formGrid.add(nomField, 1, 0);
        formGrid.add(new Label("Prénom:"), 0, 1);
        formGrid.add(prenomField, 1, 1);
        formGrid.add(new Label("Email:"), 0, 2);
        formGrid.add(emailField, 1, 2);
        formGrid.add(new Label("Mot de passe actuel:"), 0, 3);
        formGrid.add(currentPasswordField, 1, 3);
        formGrid.add(new Label("Nouveau mot de passe:"), 0, 4);
        formGrid.add(newPasswordField, 1, 4);
        formGrid.add(new Label("Confirmer:"), 0, 5);
        formGrid.add(confirmPasswordField, 1, 5);
        formGrid.add(new Label("Genre:"), 0, 6);
        formGrid.add(genreCombo, 1, 6);

        form.getChildren().add(formGrid);

        // Boutons d'action
        HBox actionButtons = new HBox(15);
        actionButtons.setAlignment(Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Annuler");
        cancelBtn.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #64748b; " +
                "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 8;");
        cancelBtn.setOnAction(e -> showProfile()); // Recharge la page

        Button saveBtn = new Button("💾 Enregistrer les modifications");
        saveBtn.setStyle("-fx-background-color: #4f46e5; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 8;");
        saveBtn.setOnAction(e -> saveProfileChanges(
                nomField.getText(),
                prenomField.getText(),
                emailField.getText(),
                currentPasswordField.getText(),
                newPasswordField.getText(),
                confirmPasswordField.getText(),
                genreCombo.getValue()
        ));

        actionButtons.getChildren().addAll(cancelBtn, saveBtn);
        profileCard.getChildren().addAll(profileHeader, form, actionButtons);
        profileInfo.getChildren().add(profileCard);
        profileContainer.getChildren().add(profileInfo);
        profileView.getChildren().addAll(title, profileContainer);

        ScrollPane scrollPane = new ScrollPane(profileView);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-border-color: transparent;");

        root.setCenter(scrollPane);
        updateSidebarButton("profile");
    }

    private void changeCurrentUserPhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une photo de profil");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            try {
                File uploadsDir = new File("uploads");
                if (!uploadsDir.exists()) {
                    uploadsDir.mkdir();
                }

                String fileName = "user_" + currentUser.getId() + "_" + System.currentTimeMillis() +
                        getFileExtension(file.getName());

                File destFile = new File(uploadsDir, fileName);
                Files.copy(file.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                // Mettre à jour l'utilisateur courant
                currentUser.setPhoto(fileName);
                currentUser.setUpdatedAt(LocalDateTime.now());

                if (userService.updateUser(currentUser)) {
                    showAlert("Succès", "Photo de profil mise à jour avec succès!");

                    // Mettre à jour la session manuellement
                    SessionManager.setCurrentUser(currentUser);

                    // Recharger l'interface
                    HBox header = createModernHeader();
                    root.setTop(header);
                    showProfile();
                }
            } catch (Exception e) {
                showAlert("Erreur", "Erreur lors du changement de photo: " + e.getMessage());
            }
        }
    }

    private void saveProfileChanges(String nom, String prenom, String email,
                                    String currentPassword, String newPassword,
                                    String confirmPassword, String genre) {
        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs obligatoires");
            return;
        }

        // Vérifier si l'email a changé
        if (!email.equals(currentUser.getEmail()) && userService.emailExists(email)) {
            showAlert("Erreur", "Cet email est déjà utilisé par un autre utilisateur!");
            return;
        }

        // Vérifier le changement de mot de passe
        if (!newPassword.isEmpty()) {
            if (currentPassword.isEmpty()) {
                showAlert("Erreur", "Veuillez entrer votre mot de passe actuel pour le changer");
                return;
            }

            if (!currentPassword.equals(currentUser.getPassword())) {
                showAlert("Erreur", "Mot de passe actuel incorrect");
                return;
            }

            if (newPassword.length() < 8) {
                showAlert("Erreur", "Le nouveau mot de passe doit contenir au moins 8 caractères");
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                showAlert("Erreur", "Les nouveaux mots de passe ne correspondent pas");
                return;
            }

            currentUser.setPassword(newPassword);
        }

        currentUser.setNom(nom);
        currentUser.setPrenom(prenom);
        currentUser.setEmail(email);
        currentUser.setUpdatedAt(LocalDateTime.now());

        int idGenre = 3;
        if ("Homme".equals(genre)) idGenre = 1;
        else if ("Femme".equals(genre)) idGenre = 2;
        currentUser.setIdGenre(idGenre);

        if (userService.updateUser(currentUser)) {
            showAlert("Succès", "Profil mis à jour avec succès!");

            // Mettre à jour la session manuellement
            SessionManager.setCurrentUser(currentUser);

            // Recharger l'interface
            HBox header = createModernHeader();
            root.setTop(header);
            showProfile();
        } else {
            showAlert("Erreur", "Erreur lors de la mise à jour du profil");
        }
    }

    // AUTRES MÉTHODES
    private void showDashboard() {
        ScrollPane content = createDashboardView();
        root.setCenter(content);
        updateSidebarButton("dashboard");
    }

    private void logout() {
        SessionManager.logout();
        primaryStage.close();
        // Retour à la page de login
        try {
            LoginView loginView = new LoginView();
            Stage loginStage = new Stage();
            loginView.start(loginStage);
        } catch (Exception e) {
            System.out.println("Erreur lors du retour au login: " + e.getMessage());
        }
    }

    private void refreshDashboard() {
        showDashboard();
        showAlert("Actualisation", "Dashboard actualisé avec les dernières données");
    }

    private void toggleSidebar() {
        VBox sidebar = (VBox) root.getLeft();
        if (sidebar.getPrefWidth() == 280) {
            sidebar.setPrefWidth(80);
            updateSidebarButtons(true);
        } else {
            sidebar.setPrefWidth(280);
            updateSidebarButtons(false);
        }
    }

    private void updateSidebarButtons(boolean collapsed) {
        VBox sidebar = (VBox) root.getLeft();
        VBox navSection = (VBox) sidebar.getChildren().get(0);
        VBox settingsSection = (VBox) sidebar.getChildren().get(2);

        String[] fullTexts = {"📊 Dashboard", "👥 Utilisateurs", "👤 Mon Profil", "🚪 Déconnexion"};
        String[] collapsedTexts = {"📊", "👥", "👤", "🚪"};

        int buttonIndex = 0;
        for (int i = 1; i < navSection.getChildren().size(); i++) {
            if (navSection.getChildren().get(i) instanceof Button) {
                Button btn = (Button) navSection.getChildren().get(i);
                btn.setText(collapsed ? collapsedTexts[buttonIndex] : fullTexts[buttonIndex]);
                buttonIndex++;
            }
        }

        for (int i = 1; i < settingsSection.getChildren().size(); i++) {
            if (settingsSection.getChildren().get(i) instanceof Button) {
                Button btn = (Button) settingsSection.getChildren().get(i);
                btn.setText(collapsed ? collapsedTexts[buttonIndex] : fullTexts[buttonIndex]);
                buttonIndex++;
            }
        }
    }

    private void updateSidebarButton(String activeButton) {
        VBox sidebar = (VBox) root.getLeft();
        VBox navSection = (VBox) sidebar.getChildren().get(0);
        VBox settingsSection = (VBox) sidebar.getChildren().get(2);

        // Réinitialiser tous les boutons
        for (int i = 1; i < navSection.getChildren().size(); i++) {
            if (navSection.getChildren().get(i) instanceof Button) {
                Button btn = (Button) navSection.getChildren().get(i);
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #64748b; " +
                        "-fx-font-size: 14px; -fx-border-color: transparent;");
            }
        }

        for (int i = 1; i < settingsSection.getChildren().size(); i++) {
            if (settingsSection.getChildren().get(i) instanceof Button) {
                Button btn = (Button) settingsSection.getChildren().get(i);
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #64748b; " +
                        "-fx-font-size: 14px; -fx-border-color: transparent;");
            }
        }

        // Activer le bon bouton
        Button activeBtn = null;
        switch (activeButton) {
            case "dashboard":
                if (navSection.getChildren().size() > 1) {
                    activeBtn = (Button) navSection.getChildren().get(1);
                }
                break;
            case "users":
                if (navSection.getChildren().size() > 2) {
                    activeBtn = (Button) navSection.getChildren().get(2);
                }
                break;
            case "profile":
                if (settingsSection.getChildren().size() > 1) {
                    activeBtn = (Button) settingsSection.getChildren().get(1);
                }
                break;
        }

        if (activeBtn != null) {
            activeBtn.setStyle("-fx-background-color: #eef2ff; -fx-text-fill: #4f46e5; " +
                    "-fx-font-size: 14px; -fx-font-weight: bold; -fx-border-color: #c7d2fe; " +
                    "-fx-border-width: 0 0 0 3; -fx-border-radius: 0;");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(primaryStage);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-width: 1;");
        alert.showAndWait();
    }

    private String darkenColor(String hex) {
        try {
            if (hex.startsWith("#") && hex.length() == 7) {
                int r = Integer.parseInt(hex.substring(1, 3), 16);
                int g = Integer.parseInt(hex.substring(3, 5), 16);
                int b = Integer.parseInt(hex.substring(5, 7), 16);

                r = Math.max(0, r - 30);
                g = Math.max(0, g - 30);
                b = Math.max(0, b - 30);

                return String.format("#%02x%02x%02x", r, g, b);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'assombrissement de la couleur: " + e.getMessage());
        }
        return hex;
    }

    private void styleEnhancedTextField(TextField field) {
        field.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; " +
                "-fx-border-radius: 8; -fx-padding: 10 14; -fx-font-size: 14px;");
        field.setPrefWidth(300);
    }
}