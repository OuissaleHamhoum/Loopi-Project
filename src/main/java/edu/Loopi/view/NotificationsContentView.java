package edu.Loopi.view;

import edu.Loopi.entities.Notification;
import edu.Loopi.entities.User;
import edu.Loopi.services.NotificationService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class NotificationsContentView {
    private User currentUser;
    private NotificationService notificationService;
    private ListView<Notification> notificationList;
    private Label unreadCountLabel;

    public NotificationsContentView(User currentUser, NotificationService notificationService) {
        this.currentUser = currentUser;
        this.notificationService = notificationService;
    }

    public VBox getView() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(30));
        container.setStyle("-fx-background-color: #f8fafc;");

        // HEADER
        HBox header = createHeader();

        // STATISTIQUES
        HBox statsBox = createStatsBox();

        // FILTRES
        HBox filterBar = createFilterBar();

        // LISTE DES NOTIFICATIONS
        notificationList = new ListView<>();
        notificationList.setPrefHeight(500);
        notificationList.setCellFactory(lv -> new ListCell<Notification>() {
            @Override
            protected void updateItem(Notification n, boolean empty) {
                super.updateItem(n, empty);
                if (empty || n == null) {
                    setGraphic(null);
                } else {
                    setGraphic(createNotificationCell(n));
                }
            }
        });

        loadNotifications();

        // BOUTON TOUT MARQUER COMME LU
        Button markAllReadBtn = new Button("✓ Tout marquer comme lu");
        markAllReadBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 12 25; -fx-background-radius: 8; -fx-cursor: hand;");
        markAllReadBtn.setMaxWidth(Double.MAX_VALUE);
        markAllReadBtn.setOnAction(e -> {
            notificationService.marquerToutesCommeLues(currentUser.getId());
            loadNotifications();
            updateStats();
        });

        container.getChildren().addAll(header, statsBox, filterBar, notificationList, markAllReadBtn);
        VBox.setVgrow(notificationList, Priority.ALWAYS);

        return container;
    }

    private HBox createHeader() {
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 20, 0));
        header.setStyle("-fx-border-color: #e2e8f0; -fx-border-width: 0 0 2 0;");

        Label iconLabel = new Label("🔔");
        iconLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 48));

        VBox headerText = new VBox(5);
        Label title = new Label("Notifications");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        title.setTextFill(Color.web("#0f172a"));

        unreadCountLabel = new Label();
        unreadCountLabel.setFont(Font.font("Segoe UI", 14));
        unreadCountLabel.setTextFill(Color.web("#475569"));

        headerText.getChildren().addAll(title, unreadCountLabel);
        header.getChildren().addAll(iconLabel, headerText);

        return header;
    }

    private HBox createStatsBox() {
        HBox statsBox = new HBox(20);
        statsBox.setPadding(new Insets(20));
        statsBox.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 2);");
        statsBox.setAlignment(Pos.CENTER);

        List<Notification> allNotifs = notificationService.getNotificationsForOrganisateur(currentUser.getId());
        int total = allNotifs.size();
        int nonLues = notificationService.countNotificationsNonLues(currentUser.getId());

        VBox totalCard = createStatCard("📊 Total", String.valueOf(total), "#3b82f6");
        VBox unreadCard = createStatCard("🔔 Non lues", String.valueOf(nonLues), "#f97316");
        VBox readCard = createStatCard("✅ Lues", String.valueOf(total - nonLues), "#10b981");

        statsBox.getChildren().addAll(totalCard, unreadCard, readCard);
        return statsBox;
    }

    private VBox createStatCard(String title, String value, String color) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(15, 30, 15, 30));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-border-color: " + color + "; -fx-border-width: 0 0 3 0;");

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        valueLabel.setTextFill(Color.web(color));

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", 12));
        titleLabel.setTextFill(Color.web("#64748b"));

        card.getChildren().addAll(valueLabel, titleLabel);
        return card;
    }

    private HBox createFilterBar() {
        HBox filterBar = new HBox(15);
        filterBar.setPadding(new Insets(10, 0, 10, 0));
        filterBar.setAlignment(Pos.CENTER_LEFT);

        Label filterLabel = new Label("Filtrer par type:");
        filterLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        filterLabel.setTextFill(Color.web("#0f172a"));

        ComboBox<String> typeFilter = new ComboBox<>();
        typeFilter.getItems().addAll("Toutes", "Nouveaux participants", "Participations annulées");
        typeFilter.setValue("Toutes");
        typeFilter.setStyle("-fx-background-radius: 8; -fx-padding: 8 15; -fx-background-color: white; -fx-border-color: #e2e8f0;");
        typeFilter.setOnAction(e -> filterNotifications(typeFilter.getValue()));

        Button refreshBtn = new Button("🔄 Actualiser");
        refreshBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 8; -fx-cursor: hand;");
        refreshBtn.setOnAction(e -> {
            loadNotifications();
            updateStats();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        filterBar.getChildren().addAll(filterLabel, typeFilter, spacer, refreshBtn);
        return filterBar;
    }

    private void filterNotifications(String filterType) {
        List<Notification> allNotifs = notificationService.getNotificationsForOrganisateur(currentUser.getId());

        if ("Toutes".equals(filterType)) {
            notificationList.getItems().setAll(allNotifs);
        } else {
            String type = filterType.equals("Nouveaux participants") ? "NOUVEAU_PARTICIPANT" : "PARTICIPANT_ANNULE";
            List<Notification> filtered = allNotifs.stream()
                    .filter(n -> type.equals(n.getType()))
                    .toList();
            notificationList.getItems().setAll(filtered);
        }
    }

    private void loadNotifications() {
        List<Notification> notifications = notificationService.getNotificationsForOrganisateur(currentUser.getId());
        notificationList.getItems().setAll(notifications);
        updateStats();
    }

    private void updateStats() {
        int nonLues = notificationService.countNotificationsNonLues(currentUser.getId());
        unreadCountLabel.setText(nonLues + " notification(s) non lue(s)");
    }

    private VBox createNotificationCell(Notification n) {
        VBox cell = new VBox(12);
        cell.setPadding(new Insets(15));
        cell.setStyle("-fx-background-color: " + (n.isRead() ? "#f8fafc" : "#eff6ff") + "; " +
                "-fx-background-radius: 8; -fx-border-color: " + (n.isRead() ? "#e2e8f0" : "#3b82f6") + "; " +
                "-fx-border-radius: 8; -fx-border-width: " + (n.isRead() ? "1" : "2") + ";");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        String icon = "NOUVEAU_PARTICIPANT".equals(n.getType()) ? "👤" : "🚫";
        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));

        VBox textContent = new VBox(5);
        HBox.setHgrow(textContent, Priority.ALWAYS);

        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(n.getTitre());
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.web("#0f172a"));

        if (!n.isRead()) {
            Label newBadge = new Label("NOUVEAU");
            newBadge.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                    "-fx-padding: 2 10; -fx-background-radius: 12; -fx-font-size: 10; -fx-font-weight: bold;");
            titleRow.getChildren().addAll(titleLabel, newBadge);
        } else {
            titleRow.getChildren().add(titleLabel);
        }

        Label messageLabel = new Label(n.getMessage());
        messageLabel.setFont(Font.font("Segoe UI", 14));
        messageLabel.setTextFill(Color.web("#475569"));
        messageLabel.setWrapText(true);

        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_LEFT);

        Label dateLabel = new Label(n.getFormattedDate());
        dateLabel.setFont(Font.font(11));
        dateLabel.setTextFill(Color.web("#64748b"));

        if (n.getEventTitre() != null && !n.getEventTitre().isEmpty()) {
            Label eventLabel = new Label("📅 " + n.getEventTitre());
            eventLabel.setFont(Font.font(11));
            eventLabel.setTextFill(Color.web("#64748b"));
            footer.getChildren().add(eventLabel);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button markReadBtn = new Button("✓ Marquer comme lu");
        markReadBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #3b82f6; " +
                "-fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 4 8;");
        markReadBtn.setVisible(!n.isRead());
        markReadBtn.setOnAction(e -> {
            notificationService.marquerCommeLue(n.getId());
            loadNotifications();
            updateStats();
        });

        textContent.getChildren().addAll(titleRow, messageLabel, footer);
        header.getChildren().addAll(iconLabel, textContent, spacer, markReadBtn);

        cell.getChildren().add(header);

        // Marquer comme lu en cliquant sur la cellule
        cell.setOnMouseClicked(e -> {
            if (!n.isRead()) {
                notificationService.marquerCommeLue(n.getId());
                loadNotifications();
                updateStats();
            }
        });

        return cell;
    }
}