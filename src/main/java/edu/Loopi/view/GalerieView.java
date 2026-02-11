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
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class GalerieView {
    private ProduitService ps = new ProduitService();
    private FlowPane flowPane = new FlowPane();
    private String selectedImagePath = "";

    // Nouveaux composants pour les filtres
    private TextField searchField = new TextField();
    private ComboBox<String> categoryFilter = new ComboBox<>();
    //dddddddddd
    private ComboBox<String> sortCombo = new ComboBox<>();
    //
    private HBox statsBar = new HBox(20);


    private final Map<String, Integer> categories = new HashMap<>() {{
        put("Objets décoratifs", 1);
        put("Art mural ", 2);
        put("Mobilier artistique", 3);
        put("Installations artistiques", 4);
    }};

    public VBox getView() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(30));
        container.setStyle("-fx-background-color: #f8f9fa;");

        // --- TITRE ET BOUTON AJOUT ---
        Label title = new Label("Ma Galerie de Produits");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Button addBtn = new Button("➕ Ajouter un Produit");
        addBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-padding: 10 20; -fx-background-radius: 20; -fx-cursor: hand;");
        addBtn.setOnAction(e -> showProductForm(null));

        HBox header = new HBox(title, new Region(), addBtn);
        HBox.setHgrow(header.getChildren().get(1), Priority.ALWAYS);
        header.setAlignment(Pos.CENTER_LEFT);

        // --- SECTION STATISTIQUES ---
        statsBar.setAlignment(Pos.CENTER);
        statsBar.setPadding(new Insets(10));

        // --- BARRE DE RECHERCHE ET FILTRE ---
        HBox filterBar = new HBox(15);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.setPadding(new Insets(15));
        filterBar.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        Button refreshBtn = new Button("🔄 Actualiser");
        refreshBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        refreshBtn.setOnAction(e -> refreshData());

        categoryFilter.getItems().add("Toutes les catégories");
        categoryFilter.getItems().addAll(categories.keySet());
        categoryFilter.setValue("Toutes les catégories");
        categoryFilter.setPrefWidth(200);
        categoryFilter.setOnAction(e -> applyFilters());

        searchField.setPromptText("🔍 Rechercher par nom...");
        searchField.setPrefWidth(250);
        searchField.setStyle("-fx-background-radius: 5;");
        searchField.setOnKeyReleased(e -> applyFilters());


        //ddd
        // Config Tri
        sortCombo.getItems().addAll(
                "Nom (A-Z)",
                "Nom (Z-A)",
                "Plus récent",
                "Moins récent");

        sortCombo.setValue("Trier par : Nom (A-Z)");
        sortCombo.setOnAction(e -> applyFilters());

        filterBar.getChildren().addAll(refreshBtn, new Separator(javafx.geometry.Orientation.VERTICAL),
                new Label("Catégorie:"), categoryFilter,
                new Label("Nom:"), searchField,
                new Separator(javafx.geometry.Orientation.VERTICAL),
                new Label("Tri:"), sortCombo);
        //



        //filterBar.getChildren().addAll(refreshBtn, new Separator(javafx.geometry.Orientation.VERTICAL), new Label("Catégorie:"), categoryFilter, new Label("Nom:"), searchField);

        flowPane.setHgap(25);
        flowPane.setVgap(25);
        refreshData();

        ScrollPane scroll = new ScrollPane(flowPane);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        container.getChildren().addAll(header, statsBar, filterBar, scroll);
        return container;





    }

    private void updateStats(List<Produit> list) {
        statsBar.getChildren().clear();

        // Carte Total
        statsBar.getChildren().add(createStatCard("Total", String.valueOf(list.size()), "#2c3e50"));

        // Stats par catégorie
        for (Map.Entry<String, Integer> entry : categories.entrySet()) {
            long count = list.stream().filter(p -> p.getIdCategorie() == entry.getValue()).count();
            if (count > 0) {
                statsBar.getChildren().add(createStatCard(entry.getKey(), String.valueOf(count), "#27ae60"));
            }
        }
    }

    private VBox createStatCard(String label, String value, String color) {
        VBox card = new VBox(2);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(10, 20, 10, 20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: " + color + "; -fx-border-width: 0 0 4 0; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        Label valLbl = new Label(value);
        valLbl.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        Label titleLbl = new Label(label);
        titleLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #7f8c8d; -fx-font-weight: bold;");

        card.getChildren().addAll(valLbl, titleLbl);
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

        // Filtrage avec Stream API
        List<Produit> filteredList = allProduits.stream()
                .filter(p -> p.getNom().toLowerCase().contains(search))
                .filter(p -> {
                    if (selectedCat == null || selectedCat.equals("Toutes les catégories")) return true;
                    return p.getIdCategorie() == categories.get(selectedCat);
                })
                .collect(Collectors.toList());

        // MODIFICATION : Logique de Tri appliquée sur la liste filtrée
        switch (selectedSort) {
            case "Nom (A-Z)":
                filteredList.sort(Comparator.comparing(p -> p.getNom().toLowerCase()));
                break;
            case "Nom (Z-A)":
                filteredList.sort(Comparator.comparing((Produit p) -> p.getNom().toLowerCase()).reversed());
                break;
            case "Plus récent":
                filteredList.sort(Comparator.comparingInt(Produit::getId).reversed());
                break;
            case "Moins récent":
                filteredList.sort(Comparator.comparingInt(Produit::getId));
                break;
        }


        // MODIFICATION : Mise à jour des statistiques basée sur la liste totale
        updateStats(allProduits);

        for (Produit p : filteredList) {
            flowPane.getChildren().add(createProductCard(p));
        }
    }

    private VBox createProductCard(Produit p) {
        VBox card = new VBox(10);
        card.setPrefWidth(240);
        card.setMaxWidth(240);
        card.setAlignment(Pos.TOP_CENTER);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 15; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 5);");

        StackPane imageContainer = new StackPane();
        imageContainer.setPrefSize(210, 140);
        imageContainer.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 10;");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(210);
        imageView.setFitHeight(140);
        imageView.setPreserveRatio(false);

        Rectangle clip = new Rectangle(210, 140);
        clip.setArcWidth(20); clip.setArcHeight(20);
        imageView.setClip(clip);

        try {
            if (p.getImage() != null && !p.getImage().isEmpty()) {
                File file = new File(p.getImage());
                if (file.exists()) imageView.setImage(new Image(file.toURI().toString()));
                else imageView.setImage(new Image("https://via.placeholder.com/210x140?text=Fichier+Perdu"));
            }
        } catch (Exception e) {
            imageView.setImage(new Image("https://via.placeholder.com/210x140?text=Erreur"));
        }
        imageContainer.getChildren().add(imageView);

        Label name = new Label(p.getNom());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #34495e;");

        String catName = "Inconnue";
        for (Map.Entry<String, Integer> entry : categories.entrySet()) {
            if (entry.getValue() == p.getIdCategorie()) { catName = entry.getKey(); break; }
        }
        Label categoryTag = new Label(catName);
        categoryTag.setStyle("-fx-background-color: #ebf5fb; -fx-text-fill: #3498db; -fx-font-size: 10px; " +
                "-fx-padding: 2 8; -fx-background-radius: 10; -fx-font-weight: bold;");

        Label description = new Label(p.getDescription());
        description.setWrapText(true);
        description.setMinHeight(40);
        description.setMaxHeight(60);
        description.setAlignment(Pos.TOP_LEFT);
        description.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px; -fx-font-style: italic;");
        VBox.setMargin(description, new Insets(5, 0, 5, 0));

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER);
        Button edit = new Button("MODIFIER✏️");
        edit.setOnAction(e -> showProductForm(p));

        Button del = new Button("SUPPRIMER🗑️");
        del.setStyle("-fx-text-fill: #e74c3c;");
        del.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText("Supprimer le produit ?");
            alert.setContentText("Êtes-vous sûr de vouloir supprimer '" + p.getNom() + "' ?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                ps.supprimerProduit(p.getId());
                refreshData();
            }
        });

        actions.getChildren().addAll(edit, del);
        card.getChildren().addAll(imageContainer, name, categoryTag, description, actions);
        return card;
    }

    private void showProductForm(Produit existingProduct) {
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

        // --- CONTRÔLES DE SAISIE ---
        // --- REMPLACER UNIQUEMENT LE BLOC "CONTRÔLES DE SAISIE" DANS showProductForm ---

        final Button okButton = (Button) dialog.getDialogPane().lookupButton(saveBtnType);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String nom = nomF.getText().trim();
            String desc = descF.getText().trim();
            StringBuilder errorMsg = new StringBuilder();

            // 1. Contrôle du NOM
            if (nom.isEmpty()) {
                errorMsg.append("- Le nom est obligatoire.\n");
            } else if (nom.length() < 3 || nom.length() > 50) {
                errorMsg.append("- Le nom doit contenir entre 3 et 50 caractères.\n");
            } else if (!nom.matches("^[a-zA-Z0-9\\sàâäéèêëïîôöùûüçÀÂÄÉÈÊËÏÎÔÖÙÛÜÇ]+$")) {
                // Regex autorisant lettres (accentuées), chiffres et espaces
                errorMsg.append("- Le nom ne doit pas contenir de caractères spéciaux.\n");
            }

            // 2. Contrôle de la DESCRIPTION
            if (desc.isEmpty()) {
                errorMsg.append("- La description est obligatoire.\n");
            } else if (desc.length() < 10 || desc.length() > 500) {
                errorMsg.append("- La description doit contenir entre 10 et 500 caractères.\n");
            }

            // 3. Contrôle de l'IMAGE (Doublon)
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

            // AFFICHAGE DES ERREURS
            if (errorMsg.length() > 0) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur de validation");
                alert.setHeaderText("Données invalides");
                alert.setContentText(errorMsg.toString());

                // Style pour l'alerte
                alert.getDialogPane().setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2;");

                alert.showAndWait();
                event.consume(); // Empêche la fermeture du dialogue si erreurs
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