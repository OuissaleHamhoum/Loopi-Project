package edu.Loopi.view;

import edu.Loopi.entities.Produit;
import edu.Loopi.services.ProduitService;
import edu.Loopi.tools.SessionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.scene.shape.Rectangle;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class GalerieView {
    private ProduitService ps = new ProduitService();
    private FlowPane flowPane = new FlowPane(25, 25);
    private String selectedImagePath = "";

    // Constantes de couleurs
    private static final String PRIMARY_COLOR = "#4361ee";
    private static final String SUCCESS_COLOR = "#2ecc71";
    private static final String DANGER_COLOR = "#e74c3c";
    private static final String WARNING_COLOR = "#f39c12";
    private static final String DARK_COLOR = "#2c3e50";
    private static final String LIGHT_GRAY = "#f8f9fa";
    private static final String BORDER_COLOR = "#e9ecef";

    // Composants pour les filtres
    private TextField searchField = new TextField();
    private ComboBox<String> categoryFilter = new ComboBox<>();
    private ComboBox<String> sortCombo = new ComboBox<>();
    private HBox statsBar = new HBox(15); // Réduit l'espacement
    private VBox statisticsPanel;

    private final Map<String, Integer> categories = new LinkedHashMap<>() {{
        put("Objets décoratifs", 1);
        put("Art mural", 2);
        put("Mobilier artistique", 3);
        put("Installations artistiques", 4);
    }};

    public VBox getView() {
        VBox container = new VBox(10); // Réduit l'espacement vertical
        container.setPadding(new Insets(0));
        container.setStyle("-fx-background-color: " + LIGHT_GRAY + ";");

        // En-tête compact
        VBox header = createCompactHeader();

        // Statistiques compactes
        statisticsPanel = createCompactStatisticsPanel();

        // Barre de filtres compacte
        HBox filterBar = createCompactFilterBar();

        // Grille de produits - PLUS GRANDE
        flowPane.setPadding(new Insets(20, 30, 30, 30)); // Réduit padding top
        flowPane.setAlignment(Pos.TOP_CENTER);
        flowPane.setHgap(25);
        flowPane.setVgap(25);

        refreshData();

        ScrollPane scroll = new ScrollPane(flowPane);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: null;");
        scroll.setPadding(new Insets(0));
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        container.getChildren().addAll(header, statisticsPanel, filterBar, scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS); // Le scroll prend tout l'espace restant

        return container;
    }

    /**
     * Crée un en-tête compact
     */
    private VBox createCompactHeader() {
        VBox header = new VBox(8); // Réduit l'espacement
        header.setPadding(new Insets(15, 40, 10, 40)); // Réduit le padding
        header.setStyle("-fx-background: linear-gradient(to right, #4361ee, #3a0ca3);");

        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("📦 Ma Galerie");
        title.setFont(Font.font("System", FontWeight.BOLD, 28)); // Réduit taille police
        title.setTextFill(Color.WHITE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("➕ Nouveau");
        addBtn.setStyle("-fx-background-color: white; -fx-text-fill: " + PRIMARY_COLOR + "; " +
                "-fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 8 20; " + // Réduit padding
                "-fx-background-radius: 25; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0, 0, 2);");

        addBtn.setOnMouseEntered(e -> addBtn.setStyle(addBtn.getStyle() + "-fx-scale-x: 1.03; -fx-scale-y: 1.03;"));
        addBtn.setOnMouseExited(e -> addBtn.setStyle(addBtn.getStyle().replace("-fx-scale-x: 1.03; -fx-scale-y: 1.03;", "")));
        addBtn.setOnAction(e -> showProductForm(null));

        topRow.getChildren().addAll(title, spacer, addBtn);

        Label subtitle = new Label("Gérez vos créations artistiques");
        subtitle.setFont(Font.font("System", 14)); // Plus petit
        subtitle.setTextFill(Color.rgb(255, 255, 255, 0.9));

        header.getChildren().addAll(topRow, subtitle);
        return header;
    }

    /**
     * Crée un panneau de statistiques compact
     */
    private VBox createCompactStatisticsPanel() {
        VBox panel = new VBox(8); // Réduit l'espacement
        panel.setPadding(new Insets(10, 40, 5, 40)); // Réduit le padding
        panel.setStyle("-fx-background-color: white; -fx-border-color: " + BORDER_COLOR + "; -fx-border-width: 0 0 1 0;");

        Label statsTitle = new Label("📊 Aperçu");
        statsTitle.setFont(Font.font("System", FontWeight.BOLD, 16)); // Plus petit
        statsTitle.setTextFill(Color.web(DARK_COLOR));

        statsBar.setAlignment(Pos.CENTER_LEFT);
        statsBar.setPadding(new Insets(0, 0, 5, 0));
        statsBar.setSpacing(10); // Réduit l'espacement

        panel.getChildren().addAll(statsTitle, statsBar);
        return panel;
    }

    /**
     * Crée une barre de filtres compacte
     */
    private HBox createCompactFilterBar() {
        HBox filterBar = new HBox(15); // Réduit l'espacement
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.setPadding(new Insets(10, 40, 10, 40)); // Réduit le padding
        filterBar.setStyle("-fx-background-color: white;");

        // Bouton d'actualisation
        Button refreshBtn = new Button("🔄");
        refreshBtn.setStyle("-fx-background-color: " + PRIMARY_COLOR + "; -fx-text-fill: white; " +
                "-fx-font-size: 16px; -fx-padding: 8 12; -fx-background-radius: 10; -fx-cursor: hand;");
        refreshBtn.setTooltip(new Tooltip("Actualiser"));
        refreshBtn.setOnAction(e -> refreshData());

        // Champ de recherche
        HBox searchBox = new HBox(8);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setStyle("-fx-background-color: " + LIGHT_GRAY + "; -fx-background-radius: 20; -fx-padding: 5 12;");
        searchBox.setPrefWidth(250); // Plus petit

        Label searchIcon = new Label("🔍");
        searchIcon.setStyle("-fx-font-size: 14px;");

        searchField.setPromptText("Rechercher...");
        searchField.setStyle("-fx-background-color: transparent; -fx-pref-width: 200px; -fx-font-size: 13px;");
        searchField.setOnKeyReleased(e -> applyFilters());

        searchBox.getChildren().addAll(searchIcon, searchField);

        // Séparateur
        Separator sep1 = new Separator(javafx.geometry.Orientation.VERTICAL);
        sep1.setStyle("-fx-background-color: " + BORDER_COLOR + ";");

        // Filtre catégorie
        VBox categoryBox = new VBox(2);
        Label catLabel = new Label("Catégorie");
        catLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #6c757d; -fx-font-weight: bold;");

        categoryFilter.getItems().add("Toutes");
        categoryFilter.getItems().addAll(categories.keySet());
        categoryFilter.setValue("Toutes");
        categoryFilter.setStyle("-fx-background-color: " + LIGHT_GRAY + "; -fx-background-radius: 10; -fx-padding: 6 12; -fx-font-size: 12px;");
        categoryFilter.setPrefWidth(150);
        categoryFilter.setOnAction(e -> applyFilters());

        categoryBox.getChildren().addAll(catLabel, categoryFilter);

        // Séparateur
        Separator sep2 = new Separator(javafx.geometry.Orientation.VERTICAL);
        sep2.setStyle("-fx-background-color: " + BORDER_COLOR + ";");

        // Tri
        VBox sortBox = new VBox(2);
        Label sortLabel = new Label("Trier par");
        sortLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #6c757d; -fx-font-weight: bold;");

        sortCombo.getItems().addAll(
                "📅 Plus récent",
                "📅 Moins récent",
                "🔤 A-Z",
                "🔤 Z-A"
        );
        sortCombo.setValue("📅 Plus récent");
        sortCombo.setStyle("-fx-background-color: " + LIGHT_GRAY + "; -fx-background-radius: 10; -fx-padding: 6 12; -fx-font-size: 12px;");
        sortCombo.setPrefWidth(130);
        sortCombo.setOnAction(e -> applyFilters());

        sortBox.getChildren().addAll(sortLabel, sortCombo);

        // Bouton réinitialiser
        Button resetBtn = new Button("✕ Réinitialiser");
        resetBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + DANGER_COLOR + "; " +
                "-fx-border-color: " + DANGER_COLOR + "; -fx-border-radius: 15; -fx-padding: 6 15; " +
                "-fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 12px;");
        resetBtn.setOnAction(e -> resetFilters());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        filterBar.getChildren().addAll(refreshBtn, searchBox, sep1, categoryBox, sep2, sortBox, spacer, resetBtn);
        return filterBar;
    }

    private void updateStats(List<Produit> list) {
        statsBar.getChildren().clear();

        // Carte Total avec design compact
        statsBar.getChildren().add(createCompactStatCard(
                "📦 Total",
                String.valueOf(list.size()),
                PRIMARY_COLOR,
                "Tous vos produits"
        ));

        // Stats par catégorie avec design compact
        for (Map.Entry<String, Integer> entry : categories.entrySet()) {
            long count = list.stream().filter(p -> p.getIdCategorie() == entry.getValue()).count();
            if (count > 0) {
                String color = getCategoryColor(entry.getValue());
                statsBar.getChildren().add(createCompactStatCard(
                        entry.getKey(),
                        String.valueOf(count),
                        color,
                        "Produits dans cette catégorie"
                ));
            }
        }
    }

    private String getCategoryColor(int categoryId) {
        switch(categoryId) {
            case 1: return SUCCESS_COLOR;      // Objets décoratifs - Vert
            case 2: return WARNING_COLOR;      // Art mural - Orange
            case 3: return "#9b59b6";          // Mobilier artistique - Violet
            case 4: return DANGER_COLOR;        // Installations artistiques - Rouge
            default: return "#6c757d";          // Gris par défaut
        }
    }

    private VBox createCompactStatCard(String label, String value, String color, String tooltip) {
        VBox card = new VBox(3);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(8, 15, 8, 15)); // Réduit le padding
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-border-color: " + color + "; -fx-border-width: 0 0 0 3; " + // Bordure plus fine
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.03), 5, 0, 0, 1);");
        card.setPrefWidth(150); // Plus petit

        Tooltip.install(card, new Tooltip(tooltip));

        Label valLbl = new Label(value);
        valLbl.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + color + ";"); // Plus petit

        Label titleLbl = new Label(label);
        titleLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d; -fx-font-weight: 600;"); // Plus petit

        card.getChildren().addAll(valLbl, titleLbl);

        // Hover effect simplifié
        card.setOnMouseEntered(e ->
                card.setStyle(card.getStyle() + "-fx-scale-x: 1.01; -fx-scale-y: 1.01; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);"));
        card.setOnMouseExited(e ->
                card.setStyle(card.getStyle().replace("-fx-scale-x: 1.01; -fx-scale-y: 1.01; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);", "")));

        return card;
    }

    private void refreshData() {
        if (SessionManager.getCurrentUser() == null) return;
        applyFilters();
    }

    private void applyFilters() {
        flowPane.getChildren().clear();
        int userId = SessionManager.getCurrentUser().getId();
        List<Produit> allProduits = ps.getProduitsParOrganisateur(userId);

        String search = searchField.getText().toLowerCase().trim();
        String selectedCat = categoryFilter.getValue();
        String selectedSort = sortCombo.getValue();

        // Filtrage
        List<Produit> filteredList = allProduits.stream()
                .filter(p -> p.getNom().toLowerCase().contains(search) ||
                        p.getDescription().toLowerCase().contains(search))
                .filter(p -> {
                    if (selectedCat == null || selectedCat.equals("Toutes") || selectedCat.equals("Toutes les catégories")) return true;
                    return p.getIdCategorie() == categories.get(selectedCat);
                })
                .collect(Collectors.toList());

        // Tri
        if (selectedSort != null) {
            switch (selectedSort) {
                case "🔤 A-Z":
                case "🔤 Nom (A-Z)":
                    filteredList.sort(Comparator.comparing(p -> p.getNom().toLowerCase()));
                    break;
                case "🔤 Z-A":
                case "🔤 Nom (Z-A)":
                    filteredList.sort(Comparator.comparing((Produit p) -> p.getNom().toLowerCase()).reversed());
                    break;
                case "📅 Plus récent":
                    filteredList.sort(Comparator.comparingInt(Produit::getId).reversed());
                    break;
                case "📅 Moins récent":
                    filteredList.sort(Comparator.comparingInt(Produit::getId));
                    break;
            }
        }

        // Mise à jour des statistiques
        updateStats(allProduits);

        // Affichage des résultats
        if (filteredList.isEmpty()) {
            showEmptyState();
        } else {
            for (Produit p : filteredList) {
                flowPane.getChildren().add(createModernProductCard(p));
            }
        }
    }

    private void resetFilters() {
        searchField.clear();
        categoryFilter.setValue("Toutes");
        sortCombo.setValue("📅 Plus récent");
        applyFilters();
    }

    private void showEmptyState() {
        VBox emptyBox = new VBox(15);
        emptyBox.setAlignment(Pos.CENTER);
        emptyBox.setPadding(new Insets(40));
        emptyBox.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 15, 0, 0, 3);");
        emptyBox.setMaxWidth(400);

        Label icon = new Label("🎨");
        icon.setFont(Font.font("Segoe UI", 48));

        Label message = new Label("Aucun produit trouvé");
        message.setFont(Font.font("System", FontWeight.BOLD, 20));
        message.setTextFill(Color.web(DARK_COLOR));

        Label suggestion = new Label("Modifiez vos critères ou créez un nouveau produit");
        suggestion.setStyle("-fx-font-size: 13px; -fx-text-fill: #6c757d;");
        suggestion.setWrapText(true);
        suggestion.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        Button newProductBtn = new Button("➕ Créer un produit");
        newProductBtn.setStyle("-fx-background-color: " + PRIMARY_COLOR + "; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 10 20; -fx-background-radius: 20; -fx-cursor: hand;");
        newProductBtn.setOnAction(e -> showProductForm(null));

        emptyBox.getChildren().addAll(icon, message, suggestion, newProductBtn);
        flowPane.getChildren().add(emptyBox);
    }

    private VBox createModernProductCard(Produit p) {
        VBox card = new VBox(12);
        card.setPrefWidth(260);
        card.setAlignment(Pos.TOP_CENTER);
        card.setStyle("-fx-background-color: white; -fx-padding: 0; -fx-background-radius: 18; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 12, 0, 0, 3); -fx-cursor: hand;");

        // Image Container avec overlay
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefSize(260, 160);
        imageContainer.setStyle("-fx-background-color: " + LIGHT_GRAY + "; -fx-background-radius: 18 18 0 0;");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(260);
        imageView.setFitHeight(160);
        imageView.setPreserveRatio(false);

        try {
            if (p.getImage() != null && !p.getImage().isEmpty()) {
                File file = new File(p.getImage());
                if (file.exists()) {
                    imageView.setImage(new Image(file.toURI().toString()));
                } else {
                    imageView.setImage(new Image("https://via.placeholder.com/260x160/4361ee/ffffff?text=LOOPI"));
                }
            }
        } catch (Exception e) {
            imageView.setImage(new Image("https://via.placeholder.com/260x160/4361ee/ffffff?text=LOOPI"));
        }
        imageContainer.getChildren().add(imageView);

        // Badge de catégorie
        String catName = "Inconnue";
        for (Map.Entry<String, Integer> entry : categories.entrySet()) {
            if (entry.getValue() == p.getIdCategorie()) {
                catName = entry.getKey();
                break;
            }
        }

        String catColor = getCategoryColor(p.getIdCategorie());

        Label categoryBadge = new Label(catName);
        categoryBadge.setStyle("-fx-background-color: " + catColor + "20; -fx-text-fill: " + catColor + "; " +
                "-fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 3 10; -fx-background-radius: 15;");
        StackPane.setAlignment(categoryBadge, Pos.TOP_LEFT);
        StackPane.setMargin(categoryBadge, new Insets(8, 0, 0, 8));
        imageContainer.getChildren().add(categoryBadge);

        // Content Container
        VBox contentBox = new VBox(10);
        contentBox.setPadding(new Insets(12, 15, 15, 15));
        contentBox.setAlignment(Pos.CENTER_LEFT);

        // Nom du produit
        Label name = new Label(p.getNom());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: " + DARK_COLOR + ";");
        name.setWrapText(true);
        name.setMaxWidth(220);

        // Description courte
        String shortDesc = p.getDescription().length() > 50
                ? p.getDescription().substring(0, 50) + "..."
                : p.getDescription();
        Label description = new Label(shortDesc);
        description.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 12px; -fx-line-spacing: 2;");
        description.setWrapText(true);
        description.setMinHeight(35);
        description.setMaxHeight(50);

        // === BOUTONS CORRIGÉS ===
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER);
        actions.setPadding(new Insets(8, 0, 0, 0));

        // Bouton Modifier
        Button editBtn = new Button("Modifier");
        editBtn.setStyle("-fx-background-color: " + PRIMARY_COLOR + "; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 8 0; -fx-background-radius: 8; " +
                "-fx-cursor: hand;");
        editBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(editBtn, Priority.ALWAYS);

        // Effet de survol pour le bouton Modifier
        editBtn.setOnMouseEntered(e -> {
            editBtn.setStyle("-fx-background-color: #3651c4; -fx-text-fill: white; " +
                    "-fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 8 0; -fx-background-radius: 8; " +
                    "-fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(67,97,238,0.4), 10, 0, 0, 2);");
        });
        editBtn.setOnMouseExited(e -> {
            editBtn.setStyle("-fx-background-color: " + PRIMARY_COLOR + "; -fx-text-fill: white; " +
                    "-fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 8 0; -fx-background-radius: 8; " +
                    "-fx-cursor: hand;");
        });
        editBtn.setOnAction(e -> showProductForm(p));

        // Bouton Supprimer
        Button deleteBtn = new Button("Supprimer");
        deleteBtn.setStyle("-fx-background-color: white; -fx-text-fill: " + DANGER_COLOR + "; " +
                "-fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 8 0; -fx-background-radius: 8; " +
                "-fx-cursor: hand; -fx-border-color: " + DANGER_COLOR + "; -fx-border-width: 1.5;");
        deleteBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(deleteBtn, Priority.ALWAYS);

        // Effet de survol pour le bouton Supprimer
        deleteBtn.setOnMouseEntered(e -> {
            deleteBtn.setStyle("-fx-background-color: " + DANGER_COLOR + "; -fx-text-fill: white; " +
                    "-fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 8 0; -fx-background-radius: 8; " +
                    "-fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(231,76,60,0.4), 10, 0, 0, 2);");
        });
        deleteBtn.setOnMouseExited(e -> {
            deleteBtn.setStyle("-fx-background-color: white; -fx-text-fill: " + DANGER_COLOR + "; " +
                    "-fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 8 0; -fx-background-radius: 8; " +
                    "-fx-cursor: hand; -fx-border-color: " + DANGER_COLOR + "; -fx-border-width: 1.5;");
        });
        deleteBtn.setOnAction(e -> confirmDelete(p));

        actions.getChildren().addAll(editBtn, deleteBtn);
        // === FIN BOUTONS CORRIGÉS ===

        contentBox.getChildren().addAll(name, description, actions);
        card.getChildren().addAll(imageContainer, contentBox);

        // Hover Effects sur la carte
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.1));
        shadow.setRadius(15);

        card.setOnMouseEntered(e -> {
            card.setEffect(shadow);
            card.setStyle(card.getStyle() + "-fx-scale-x: 1.01; -fx-scale-y: 1.01;");
        });

        card.setOnMouseExited(e -> {
            card.setEffect(null);
            card.setStyle(card.getStyle().replace("-fx-scale-x: 1.01; -fx-scale-y: 1.01;", ""));
        });

        return card;
    }

    private void confirmDelete(Produit p) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Supprimer \"" + p.getNom() + "\" ?");

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white; -fx-background-radius: 14;");
        dialogPane.getButtonTypes().stream()
                .map(buttonType -> dialogPane.lookupButton(buttonType))
                .forEach(button -> button.setStyle("-fx-cursor: hand; -fx-font-weight: bold; -fx-padding: 6 16; -fx-background-radius: 6;"));

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            ps.supprimerProduit(p.getId());
            refreshData();
            showAlert("Succès", "Produit supprimé !");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white; -fx-background-radius: 14;");

        alert.showAndWait();
    }

    private void showProductForm(Produit existingProduct) {
        // [Votre code existant pour le formulaire - inchangé]
        Dialog<Produit> dialog = new Dialog<>();
        dialog.setTitle(existingProduct == null ? "Nouveau Trésor" : "Modifier le Produit");
        dialog.getDialogPane().setStyle("-fx-background-color: #f1f8e9; -fx-border-color: #27ae60; -fx-border-width: 2;");

        ButtonType saveBtnType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);

        VBox mainForm = new VBox(15);
        mainForm.setPadding(new Insets(20));
        mainForm.setPrefWidth(400);

        // --- 1. APERÇU ---
        VBox groupPreview = new VBox(5);
        Label lblPreview = new Label("Image :");
        lblPreview.setStyle("-fx-font-weight: bold; -fx-text-fill: #2d5a27;");
        ImageView preview = new ImageView();
        preview.setFitWidth(200); preview.setFitHeight(120); preview.setPreserveRatio(true);
        StackPane frame = new StackPane(preview);
        frame.setStyle("-fx-background-color: white; -fx-padding: 8; -fx-border-color: #a5d6a7; -fx-border-radius: 10; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        groupPreview.getChildren().addAll(lblPreview, frame);

        // --- 2. NOM ---
        VBox groupNom = new VBox(5);
        Label lblNom = new Label("Nom :");
        lblNom.setStyle("-fx-font-weight: bold; -fx-text-fill: #2d5a27;");
        TextField nomF = new TextField(existingProduct != null ? existingProduct.getNom() : "");
        nomF.setStyle("-fx-background-radius: 10; -fx-padding: 8;");
        groupNom.getChildren().addAll(lblNom, nomF);

        // --- 3. CATÉGORIE ---
        VBox groupCat = new VBox(5);
        Label lblCat = new Label("Catégorie :");
        lblCat.setStyle("-fx-font-weight: bold; -fx-text-fill: #2d5a27;");
        ComboBox<String> catCombo = new ComboBox<>();
        catCombo.getItems().addAll(categories.keySet());
        catCombo.setMaxWidth(Double.MAX_VALUE);
        if (existingProduct != null) {
            int currentId = existingProduct.getIdCategorie();
            categories.entrySet().stream()
                    .filter(e -> e.getValue() == currentId)
                    .findFirst().ifPresent(e -> catCombo.setValue(e.getKey()));
        } else {
            catCombo.getSelectionModel().selectFirst();
        }
        groupCat.getChildren().addAll(lblCat, catCombo);

        // --- 4. DESCRIPTION ---
        VBox groupDesc = new VBox(5);
        Label lblDesc = new Label("Description :");
        lblDesc.setStyle("-fx-font-weight: bold; -fx-text-fill: #2d5a27;");
        TextArea descF = new TextArea(existingProduct != null ? existingProduct.getDescription() : "");
        descF.setPrefRowCount(3); descF.setWrapText(true);
        descF.setStyle("-fx-background-radius: 10;");
        groupDesc.getChildren().addAll(lblDesc, descF);

        // --- 5. MÉDIA ---
        VBox groupMedia = new VBox(5);
        Button fileBtn = new Button("♻️ Remplacer l'image");
        fileBtn.setMaxWidth(Double.MAX_VALUE);
        fileBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10;");
        groupMedia.getChildren().addAll(new Label("Média :"), fileBtn);

        mainForm.getChildren().addAll(groupPreview, groupNom, groupCat, groupDesc, groupMedia);

        if (existingProduct != null && existingProduct.getImage() != null) {
            File file = new File(existingProduct.getImage());
            if (file.exists()) preview.setImage(new Image(file.toURI().toString()));
        }

        fileBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            File f = fc.showOpenDialog(null);
            if (f != null) {
                selectedImagePath = f.getAbsolutePath();
                preview.setImage(new Image(f.toURI().toString()));
            }
        });

        dialog.getDialogPane().setContent(mainForm);

        final Button okButton = (Button) dialog.getDialogPane().lookupButton(saveBtnType);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String nom = nomF.getText().trim();
            String desc = descF.getText().trim();
            StringBuilder errorMsg = new StringBuilder();

            if (nom.isEmpty()) {
                errorMsg.append("- Le nom est obligatoire.\n");
            } else if (nom.length() < 3 || nom.length() > 50) {
                errorMsg.append("- Le nom doit contenir entre 3 et 50 caractères.\n");
            } else if (!nom.matches("^[a-zA-Z0-9\\sàâäéèêëïîôöùûüçÀÂÄÉÈÊËÏÎÔÖÙÛÜÇ]+$")) {
                errorMsg.append("- Le nom ne doit pas contenir de caractères spéciaux.\n");
            }

            if (desc.isEmpty()) {
                errorMsg.append("- La description est obligatoire.\n");
            } else if (desc.length() < 10 || desc.length() > 500) {
                errorMsg.append("- La description doit contenir entre 10 et 500 caractères.\n");
            }

            String currentImagePath = (selectedImagePath.isEmpty() && existingProduct != null) ? existingProduct.getImage() : selectedImagePath;

            if (currentImagePath == null || currentImagePath.isEmpty()) {
                errorMsg.append("- Veuillez sélectionner une image.\n");
            } else {
                boolean imageChanged = existingProduct == null || !currentImagePath.equals(existingProduct.getImage());
                if (imageChanged) {
                    boolean exists = ps.getProduitsParOrganisateur(SessionManager.getCurrentUser().getId())
                            .stream().anyMatch(p -> currentImagePath.equals(p.getImage()));
                    if (exists) errorMsg.append("- Cette image est déjà utilisée pour un autre de vos produits.\n");
                }
            }

            if (errorMsg.length() > 0) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur de validation");
                alert.setHeaderText("Données invalides");
                alert.setContentText(errorMsg.toString());
                alert.getDialogPane().setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2;");
                alert.showAndWait();
                event.consume();
            }
        });

        dialog.setResultConverter(btn -> {
            if (btn == saveBtnType) {
                int userId = SessionManager.getCurrentUser().getId();
                int catId = categories.get(catCombo.getValue());
                String img = (selectedImagePath.isEmpty() && existingProduct != null) ? existingProduct.getImage() : selectedImagePath;
                if (existingProduct == null) return new Produit(0, nomF.getText().trim(), descF.getText().trim(), img, catId, userId);
                else {
                    existingProduct.setNom(nomF.getText().trim());
                    existingProduct.setDescription(descF.getText().trim());
                    existingProduct.setImage(img);
                    existingProduct.setIdCategorie(catId);
                    return existingProduct;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(p -> {
            if (existingProduct == null) ps.ajouterProduit(p);
            else ps.modifierProduit(p);
            selectedImagePath = "";
            refreshData();
        });
    }
}