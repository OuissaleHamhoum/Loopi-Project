package edu.Loopi.view;

import edu.Loopi.entities.User;
import edu.Loopi.services.UserService;
import edu.Loopi.tools.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.List;

public class AdminDashboard {
    private User currentUser;
    private UserService userService;
    private Stage primaryStage;

    // Tableau des utilisateurs
    private TableView<User> userTable;
    private ObservableList<User> userList;

    public AdminDashboard(User user) {
        this.currentUser = user;
        this.userService = new UserService();
        SessionManager.login(user);
    }

    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("LOOPI - Dashboard Administrateur");

        // Layout principal
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f5f5;");

        // Header
        HBox header = createHeader();
        root.setTop(header);

        // Sidebar
        VBox sidebar = createSidebar();
        root.setLeft(sidebar);

        // Contenu par défaut (vue utilisateurs)
        ScrollPane content = createUserManagementView();
        root.setCenter(content);

        Scene scene = new Scene(root, 1200, 700);

        try {
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        } catch (Exception e) {
            System.out.println("CSS non chargé, utilisation des styles inline");
        }

        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();

        // Afficher les infos de session
        SessionManager.printSessionInfo();
    }

    private HBox createHeader() {
        HBox header = new HBox(20);
        header.setPadding(new Insets(15, 30, 15, 30));
        header.setStyle("-fx-background-color: #2E7D32;");
        header.setAlignment(Pos.CENTER_LEFT);

        // Logo/Titre
        HBox logoBox = new HBox(10);
        logoBox.setAlignment(Pos.CENTER_LEFT);

        ImageView logoIcon = new ImageView();
        logoIcon.setFitWidth(30);
        logoIcon.setFitHeight(30);
        logoIcon.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 2, 0, 0, 1);");

        // Essayez de charger un logo, sinon utilisez du texte
        try {
            logoIcon.setImage(new Image(getClass().getResourceAsStream("/images/logo.png")));
        } catch (Exception e) {
            // Si pas d'image, utilisez un emoji
            Label emojiLogo = new Label("🌱");
            emojiLogo.setFont(Font.font("Arial", FontWeight.BOLD, 24));
            emojiLogo.setTextFill(Color.WHITE);
            logoBox.getChildren().add(emojiLogo);
        }

        Label title = new Label("LOOPI ADMIN");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setTextFill(Color.WHITE);
        title.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 2, 0, 0, 1);");

        if (logoIcon.getImage() != null) {
            logoBox.getChildren().addAll(logoIcon, title);
        } else {
            logoBox.getChildren().add(title);
        }

        // Info utilisateur
        HBox userBox = new HBox(10);
        userBox.setAlignment(Pos.CENTER_RIGHT);

        Label welcome = new Label("Bienvenue, " + currentUser.getPrenom());
        welcome.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        welcome.setTextFill(Color.WHITE);

        Label role = new Label("(" + currentUser.getRole() + ")");
        role.setFont(Font.font("Arial", 12));
        role.setTextFill(Color.LIGHTGRAY);

        // Bouton déconnexion
        Button logoutBtn = new Button("Déconnexion");
        logoutBtn.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 5 15; -fx-background-radius: 5; " +
                "-fx-cursor: hand;");
        logoutBtn.setOnAction(e -> logout());

        userBox.getChildren().addAll(welcome, role, logoutBtn);

        HBox.setHgrow(userBox, Priority.ALWAYS);
        header.getChildren().addAll(logoBox, userBox);

        return header;
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(0);
        sidebar.setPrefWidth(220);
        sidebar.setStyle("-fx-background-color: #333333;");

        // Menu items
        VBox menuItems = new VBox(0);

        // Dashboard
        Button dashboardBtn = createSidebarButton("📊 Dashboard", true);
        dashboardBtn.setOnAction(e -> showDashboard());

        // Utilisateurs
        Button usersBtn = createSidebarButton("👥 Utilisateurs", false);
        usersBtn.setOnAction(e -> showUserManagementViewInCenter()); // CORRECTION ICI

        // Produits
        Button productsBtn = createSidebarButton("🛒 Produits", false);
        productsBtn.setOnAction(e -> showProducts());

        // Commandes
        Button ordersBtn = createSidebarButton("📦 Commandes", false);
        ordersBtn.setOnAction(e -> showOrders());

        // Événements
        Button eventsBtn = createSidebarButton("📅 Événements", false);
        eventsBtn.setOnAction(e -> showEvents());

        // Dons
        Button donationsBtn = createSidebarButton("💰 Dons", false);
        donationsBtn.setOnAction(e -> showDonations());

        // Statistiques
        Button statsBtn = createSidebarButton("📈 Statistiques", false);
        statsBtn.setOnAction(e -> showStatistics());

        // Paramètres
        Button settingsBtn = createSidebarButton("⚙️ Paramètres", false);
        settingsBtn.setOnAction(e -> showSettings());

        // Profil
        Button profileBtn = createSidebarButton("👤 Mon Profil", false);
        profileBtn.setOnAction(e -> showProfile());

        menuItems.getChildren().addAll(
                dashboardBtn, usersBtn, productsBtn, ordersBtn,
                eventsBtn, donationsBtn, statsBtn, settingsBtn, profileBtn
        );

        // Espaceur
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Version
        Label version = new Label("LOOPI v1.0");
        version.setFont(Font.font("Arial", 10));
        version.setTextFill(Color.GRAY);
        version.setPadding(new Insets(10));
        version.setAlignment(Pos.CENTER);

        sidebar.getChildren().addAll(menuItems, spacer, version);

        return sidebar;
    }

    private Button createSidebarButton(String text, boolean active) {
        Button btn = new Button(text);
        btn.setPrefWidth(220);
        btn.setPrefHeight(50);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(0, 0, 0, 20));

        if (active) {
            btn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                    "-fx-font-size: 14px; -fx-font-weight: bold;");
        } else {
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #CCCCCC; " +
                    "-fx-font-size: 14px;");
        }

        // Effet hover
        btn.setOnMouseEntered(e -> {
            if (!btn.getStyle().contains("#4CAF50")) {
                btn.setStyle("-fx-background-color: #444444; -fx-text-fill: white; " +
                        "-fx-font-size: 14px;");
            }
        });

        btn.setOnMouseExited(e -> {
            if (!btn.getStyle().contains("#4CAF50")) {
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #CCCCCC; " +
                        "-fx-font-size: 14px;");
            }
        });

        return btn;
    }

    private ScrollPane createUserManagementView() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(30));

        // Titre
        Label title = new Label("Gestion des Utilisateurs");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#2E7D32"));

        // Statistiques rapides
        HBox statsBox = createQuickStats();

        // Barre d'outils
        HBox toolbar = createToolbar();

        // Tableau des utilisateurs
        VBox tableContainer = new VBox(10);
        tableContainer.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        Label tableTitle = new Label("Liste des utilisateurs");
        tableTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        userTable = new TableView<>();
        setupUserTable();

        tableContainer.getChildren().addAll(tableTitle, userTable);

        container.getChildren().addAll(title, statsBox, toolbar, tableContainer);

        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent;");

        return scrollPane;
    }

    // NOUVELLE MÉTHODE pour afficher la vue gestion utilisateurs
    private void showUserManagementViewInCenter() {
        ScrollPane content = createUserManagementView();
        BorderPane root = (BorderPane) primaryStage.getScene().getRoot();
        root.setCenter(content);
    }

    private HBox createQuickStats() {
        HBox statsBox = new HBox(20);

        int[] stats = userService.getUserStatistics();
        int totalUsers = stats[0] + stats[1] + stats[2];

        VBox totalBox = createStatCard("👥", "Total", String.valueOf(totalUsers), "#4CAF50");
        VBox adminBox = createStatCard("👑", "Admins", String.valueOf(stats[0]), "#9C27B0");
        VBox orgBox = createStatCard("🎯", "Organisateurs", String.valueOf(stats[1]), "#2196F3");
        VBox partBox = createStatCard("😊", "Participants", String.valueOf(stats[2]), "#FF9800");

        statsBox.getChildren().addAll(totalBox, adminBox, orgBox, partBox);
        return statsBox;
    }

    private VBox createStatCard(String icon, String title, String value, String color) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 10;");
        card.setPrefWidth(150);

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("Arial", 32));
        iconLabel.setTextFill(Color.WHITE);

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        valueLabel.setTextFill(Color.WHITE);

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        titleLabel.setTextFill(Color.WHITE);

        card.getChildren().addAll(iconLabel, valueLabel, titleLabel);
        return card;
    }

    private HBox createToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setPadding(new Insets(10, 0, 10, 0));

        Button addBtn = createToolbarButton("➕ Ajouter", "#4CAF50");
        addBtn.setOnAction(e -> showAddUserDialog());

        Button editBtn = createToolbarButton("✏️ Modifier", "#2196F3");
        editBtn.setOnAction(e -> editSelectedUser());

        Button deleteBtn = createToolbarButton("🗑️ Supprimer", "#f44336");
        deleteBtn.setOnAction(e -> deleteSelectedUser());

        Button refreshBtn = createToolbarButton("🔄 Actualiser", "#FF9800");
        refreshBtn.setOnAction(e -> refreshUserTable());

        // Champ de recherche
        TextField searchField = new TextField();
        searchField.setPromptText("Rechercher un utilisateur...");
        searchField.setPrefWidth(300);
        searchField.setStyle("-fx-background-color: white; -fx-background-radius: 5; " +
                "-fx-border-color: #ddd; -fx-border-radius: 5; -fx-padding: 8 15;");

        Button searchBtn = new Button("🔍");
        searchBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                "-fx-background-radius: 5; -fx-padding: 8 15; -fx-cursor: hand;");
        searchBtn.setOnAction(e -> searchUsers(searchField.getText()));

        HBox searchBox = new HBox(5);
        searchBox.getChildren().addAll(searchField, searchBtn);

        HBox.setHgrow(searchBox, Priority.ALWAYS);
        toolbar.getChildren().addAll(addBtn, editBtn, deleteBtn, refreshBtn, searchBox);

        return toolbar;
    }

    private Button createToolbarButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5; " +
                "-fx-cursor: hand;");

        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: " + darkenColor(color) + "; -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5; " +
                        "-fx-cursor: hand;"
        ));

        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: " + color + "; -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5; " +
                        "-fx-cursor: hand;"
        ));

        return btn;
    }

    private String darkenColor(String hex) {
        // Darken la couleur de 20%
        try {
            int r = Integer.parseInt(hex.substring(1, 3), 16);
            int g = Integer.parseInt(hex.substring(3, 5), 16);
            int b = Integer.parseInt(hex.substring(5, 7), 16);

            r = Math.max(0, r - 40);
            g = Math.max(0, g - 40);
            b = Math.max(0, b - 40);

            return String.format("#%02x%02x%02x", r, g, b);
        } catch (Exception e) {
            return hex;
        }
    }

    @SuppressWarnings("unchecked")
    private void setupUserTable() {
        userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        userTable.setPlaceholder(new Label("Aucun utilisateur trouvé"));

        // Colonnes
        TableColumn<User, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);

        TableColumn<User, String> nomCol = new TableColumn<>("Nom");
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        nomCol.setPrefWidth(150);

        TableColumn<User, String> prenomCol = new TableColumn<>("Prénom");
        prenomCol.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        prenomCol.setPrefWidth(150);

        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(200);

        TableColumn<User, String> roleCol = new TableColumn<>("Rôle");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleCol.setPrefWidth(100);

        TableColumn<User, String> sexeCol = new TableColumn<>("Genre");
        sexeCol.setCellValueFactory(new PropertyValueFactory<>("sexe"));
        sexeCol.setPrefWidth(100);

        TableColumn<User, String> dateCol = new TableColumn<>("Inscrit le");
        dateCol.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCreatedAt() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getCreatedAt().toLocalDate().toString()
                );
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        dateCol.setPrefWidth(100);

        userTable.getColumns().addAll(idCol, nomCol, prenomCol, emailCol, roleCol, sexeCol, dateCol);

        // Style des cellules pour les rôles
        roleCol.setCellFactory(column -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String role, boolean empty) {
                super.updateItem(role, empty);

                if (empty || role == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(role);

                    switch (role.toLowerCase()) {
                        case "admin":
                            setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                                    "-fx-font-weight: bold; -fx-background-radius: 10; " +
                                    "-fx-padding: 5 10; -fx-alignment: center;");
                            break;
                        case "organisateur":
                            setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; " +
                                    "-fx-font-weight: bold; -fx-background-radius: 10; " +
                                    "-fx-padding: 5 10; -fx-alignment: center;");
                            break;
                        case "participant":
                            setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; " +
                                    "-fx-font-weight: bold; -fx-background-radius: 10; " +
                                    "-fx-padding: 5 10; -fx-alignment: center;");
                            break;
                        default:
                            setStyle("");
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

    private void searchUsers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            refreshUserTable();
            return;
        }

        List<User> users = userService.searchUsers(keyword);
        userList = FXCollections.observableArrayList(users);
        userTable.setItems(userList);
    }

    private void showAddUserDialog() {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Ajouter un utilisateur");
        dialog.setHeaderText("Remplissez les informations du nouvel utilisateur");

        ButtonType addButtonType = new ButtonType("Ajouter", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 10, 10));

        TextField nomField = new TextField();
        nomField.setPromptText("Nom");
        TextField prenomField = new TextField();
        prenomField.setPromptText("Prénom");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mot de passe");

        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("admin", "organisateur", "participant");
        roleCombo.setValue("participant");

        ComboBox<String> genreCombo = new ComboBox<>();
        genreCombo.getItems().addAll("Homme", "Femme", "Non spécifié");
        genreCombo.setValue("Non spécifié");

        grid.add(new Label("Nom:"), 0, 0);
        grid.add(nomField, 1, 0);
        grid.add(new Label("Prénom:"), 0, 1);
        grid.add(prenomField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Mot de passe:"), 0, 3);
        grid.add(passwordField, 1, 3);
        grid.add(new Label("Rôle:"), 0, 4);
        grid.add(roleCombo, 1, 4);
        grid.add(new Label("Genre:"), 0, 5);
        grid.add(genreCombo, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                User newUser = new User();
                newUser.setNom(nomField.getText());
                newUser.setPrenom(prenomField.getText());
                newUser.setEmail(emailField.getText());
                newUser.setPassword(passwordField.getText());
                newUser.setRole(roleCombo.getValue());
                newUser.setPhoto("default.jpg");

                // Déterminer l'ID du genre
                String genre = genreCombo.getValue();
                int idGenre = 3; // Par défaut "Non spécifié"
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

        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 10, 10));

        TextField nomField = new TextField(selectedUser.getNom());
        TextField prenomField = new TextField(selectedUser.getPrenom());
        TextField emailField = new TextField(selectedUser.getEmail());
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Nouveau mot de passe (laisser vide pour garder l'actuel)");

        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("admin", "organisateur", "participant");
        roleCombo.setValue(selectedUser.getRole());

        ComboBox<String> genreCombo = new ComboBox<>();
        genreCombo.getItems().addAll("Homme", "Femme", "Non spécifié");
        genreCombo.setValue(selectedUser.getSexe() != null ? selectedUser.getSexe() : "Non spécifié");

        grid.add(new Label("Nom:"), 0, 0);
        grid.add(nomField, 1, 0);
        grid.add(new Label("Prénom:"), 0, 1);
        grid.add(prenomField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Nouveau mot de passe:"), 0, 3);
        grid.add(passwordField, 1, 3);
        grid.add(new Label("Rôle:"), 0, 4);
        grid.add(roleCombo, 1, 4);
        grid.add(new Label("Genre:"), 0, 5);
        grid.add(genreCombo, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                selectedUser.setNom(nomField.getText());
                selectedUser.setPrenom(prenomField.getText());
                selectedUser.setEmail(emailField.getText());
                selectedUser.setRole(roleCombo.getValue());

                // Déterminer l'ID du genre
                String genre = genreCombo.getValue();
                int idGenre = 3; // Par défaut "Non spécifié"
                if ("Homme".equals(genre)) idGenre = 1;
                else if ("Femme".equals(genre)) idGenre = 2;
                selectedUser.setIdGenre(idGenre);

                if (!passwordField.getText().isEmpty()) {
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
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Supprimer l'utilisateur");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer " +
                selectedUser.getNomComplet() + " ?");

        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            if (userService.deleteUser(selectedUser.getId())) {
                showAlert("Succès", "Utilisateur supprimé avec succès!");
                refreshUserTable();
            } else {
                showAlert("Erreur", "Erreur lors de la suppression de l'utilisateur");
            }
        }
    }

    // Méthodes pour les autres vues (simplifiées pour l'exemple)
    private void showDashboard() {
        showAlert("Info", "Dashboard - En développement");
    }

    private void showProducts() {
        showAlert("Info", "Gestion des produits - En développement");
    }

    private void showOrders() {
        showAlert("Info", "Gestion des commandes - En développement");
    }

    private void showEvents() {
        showAlert("Info", "Gestion des événements - En développement");
    }

    private void showDonations() {
        showAlert("Info", "Gestion des dons - En développement");
    }

    private void showStatistics() {
        VBox statsView = new VBox(20);
        statsView.setPadding(new Insets(30));
        statsView.setAlignment(Pos.CENTER);

        Label title = new Label("Statistiques");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#2E7D32"));

        // Pie chart
        int[] stats = userService.getUserStatistics();
        PieChart pieChart = new PieChart();
        pieChart.getData().addAll(
                new PieChart.Data("Admins (" + stats[0] + ")", stats[0]),
                new PieChart.Data("Organisateurs (" + stats[1] + ")", stats[1]),
                new PieChart.Data("Participants (" + stats[2] + ")", stats[2])
        );
        pieChart.setTitle("Répartition des utilisateurs par rôle");
        pieChart.setLabelsVisible(true);
        pieChart.setLegendVisible(true);
        pieChart.setPrefSize(600, 400);

        statsView.getChildren().addAll(title, pieChart);

        ScrollPane scrollPane = new ScrollPane(statsView);
        scrollPane.setFitToWidth(true);

        // Remplacer le contenu
        BorderPane root = (BorderPane) primaryStage.getScene().getRoot();
        root.setCenter(scrollPane);
    }

    private void showSettings() {
        showAlert("Info", "Paramètres - En développement");
    }

    private void showProfile() {
        showAlert("Info", "Mon profil - En développement");
    }

    private void logout() {
        SessionManager.logout();
        primaryStage.close();
        new LoginView().start(new Stage());
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}