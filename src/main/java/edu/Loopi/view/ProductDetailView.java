package edu.Loopi.view;

import edu.Loopi.entities.Feedback;
import edu.Loopi.entities.Produit;
import edu.Loopi.entities.User;
import edu.Loopi.services.FeedbackService;
import edu.Loopi.services.ProduitService;
import edu.Loopi.services.FavorisService;
import edu.Loopi.tools.SessionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class ProductDetailView {
    private Produit produit;
    private User currentUser;
    private FeedbackService feedbackService = new FeedbackService();
    private ProduitService produitService = new ProduitService();
    private FavorisService favorisService = new FavorisService();
    private int selectedRating = 0;
    private Stage stage;
    private boolean estFavoris = false;

    private final Map<Integer, String> categoryNames = new HashMap<>() {{
        put(1, "Objets décoratifs");
        put(2, "Art mural");
        put(3, "Mobilier artistique");
        put(4, "Installations artistiques");
    }};

    public ProductDetailView(Produit produit) {
        this.produit = produit;
        this.currentUser = SessionManager.getCurrentUser();
    }

    public void show() {
        this.stage = new Stage();

        // Vérifier si le produit est déjà en favoris
        estFavoris = favorisService.estDansFavoris(currentUser.getId(), produit.getId());

        // Conteneur principal avec padding
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setStyle("-fx-background-color: #f5f5f5;");

        // Barre du haut
        HBox topBar = createTopBar();

        // === SECTION PRODUIT ===
        VBox productSection = createProductSection();

        // === SECTION FEEDBACK ===
        VBox feedbackSection = createFeedbackSection();

        // === PRODUITS SIMILAIRES ===
        VBox similarSection = createSimilarProductsSection();

        // Ajout de toutes les sections au conteneur principal
        mainContainer.getChildren().addAll(topBar, productSection, feedbackSection, similarSection);

        // ScrollPane pour permettre le défilement
        ScrollPane scrollPane = new ScrollPane(mainContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        Scene scene = new Scene(scrollPane, 900, 700);
        stage.setScene(scene);
        stage.setTitle("Détails du Produit - " + produit.getNom());
        stage.show();
    }

    private HBox createTopBar() {
        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER_LEFT);

        Button backBtn = new Button("← Retour");
        backBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5; -fx-cursor: hand;");
        backBtn.setOnAction(e -> stage.close());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topBar.getChildren().addAll(backBtn, spacer);
        return topBar;
    }

    private VBox createProductSection() {
        VBox productSection = new VBox(20);
        productSection.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        // Image du produit
        StackPane imageContainer = new StackPane();
        imageContainer.setStyle("-fx-background-color: #fafafa; -fx-background-radius: 10;");
        imageContainer.setPrefHeight(300);

        ImageView imageView = new ImageView();
        imageView.setFitWidth(400);
        imageView.setFitHeight(280);
        imageView.setPreserveRatio(true);

        try {
            if (produit.getImage() != null) {
                File file = new File(produit.getImage());
                if (file.exists()) {
                    imageView.setImage(new Image(file.toURI().toString()));
                } else {
                    imageView.setImage(new Image("https://via.placeholder.com/400x280?text=Image+non+disponible"));
                }
            }
        } catch (Exception e) {
            imageView.setImage(new Image("https://via.placeholder.com/400x280?text=Erreur+chargement"));
        }
        imageContainer.getChildren().add(imageView);

        // Titre et bouton favoris
        HBox titleBox = new HBox(10);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label(produit.getNom());
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Button favBtn = createFavorisButton();

        titleBox.getChildren().addAll(title, favBtn);
        HBox.setHgrow(title, Priority.ALWAYS);

        String catName = categoryNames.getOrDefault(produit.getIdCategorie(), "Catégorie inconnue");
        Label category = new Label("Catégorie: " + catName);
        category.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 14px;");

        Label description = new Label(produit.getDescription());
        description.setWrapText(true);
        description.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 14px;");

        productSection.getChildren().addAll(imageContainer, titleBox, category, description);
        return productSection;
    }

    private Button createFavorisButton() {
        Button favBtn = new Button();
        favBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 28px; -fx-cursor: hand;");

        // Mettre à jour le texte selon l'état
        updateFavorisButton(favBtn);

        // Animation au survol
        ScaleTransition st = new ScaleTransition(Duration.millis(200), favBtn);
        st.setToX(1.2);
        st.setToY(1.2);

        favBtn.setOnMouseEntered(e -> {
            st.setRate(1);
            st.play();
        });

        favBtn.setOnMouseExited(e -> {
            st.setRate(-1);
            st.play();
        });

        // Action du bouton
        favBtn.setOnAction(e -> {
            if (estFavoris) {
                // Retirer des favoris
                favorisService.supprimerFavoris(currentUser.getId(), produit.getId());
                estFavoris = false;
                showAlert("Succès", "Produit retiré des favoris ❌");
            } else {
                // Ajouter aux favoris
                favorisService.ajouterFavoris(currentUser.getId(), produit.getId());
                estFavoris = true;
                showAlert("Succès", "Produit ajouté aux favoris ❤️");
            }
            updateFavorisButton(favBtn);
        });

        return favBtn;
    }

    private void updateFavorisButton(Button btn) {
        if (estFavoris) {
            btn.setText("❤️"); // Cœur rouge plein
            btn.setStyle("-fx-background-color: transparent; -fx-font-size: 28px; -fx-text-fill: #e74c3c; -fx-cursor: hand;");
        } else {
            btn.setText("❤️"); // On garde le même symbole mais en gris
            btn.setStyle("-fx-background-color: transparent; -fx-font-size: 28px; -fx-text-fill: #95a5a6; -fx-cursor: hand;"); // Gris
        }
    }

    private VBox createFeedbackSection() {
        VBox feedbackSection = new VBox(20);
        feedbackSection.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        // En-tête avec note moyenne
        HBox ratingHeader = createRatingHeader();

        Label feedbackTitle = new Label("Avis des clients");
        feedbackTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Formulaire d'avis
        VBox formBox = new VBox(10);
        formBox.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15; -fx-background-radius: 5;");

        Label rateLabel = new Label("Donnez votre avis :");
        rateLabel.setStyle("-fx-font-weight: bold;");

        // Étoiles
        HBox starBox = new HBox(5);
        for (int i = 1; i <= 5; i++) {
            final int rating = i;
            Button star = new Button("☆");
            star.setStyle("-fx-font-size: 20px; -fx-background-color: transparent; -fx-text-fill: #f1c40f; -fx-cursor: hand;");
            star.setOnAction(e -> {
                selectedRating = rating;
                updateStars(starBox);
            });
            starBox.getChildren().add(star);
        }

        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Partagez votre expérience avec ce produit...");
        commentArea.setPrefRowCount(3);

        Button submitBtn = new Button("Publier mon avis");
        submitBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8; -fx-background-radius: 5; -fx-cursor: hand;");
        submitBtn.setMaxWidth(Double.MAX_VALUE);

        submitBtn.setOnAction(e -> handleSubmit(commentArea, starBox));

        formBox.getChildren().addAll(rateLabel, starBox, commentArea, submitBtn);

        // Liste des commentaires
        VBox commentsList = new VBox(10);

        List<Feedback> feedbacks = feedbackService.getFeedbacksByProduct(produit.getId());

        if (feedbacks.isEmpty()) {
            Label noComments = new Label("Aucun avis pour le moment. Soyez le premier à commenter !");
            noComments.setStyle("-fx-font-style: italic; -fx-text-fill: #999;");
            commentsList.getChildren().add(noComments);
        } else {
            for (Feedback f : feedbacks) {
                commentsList.getChildren().add(createCommentCard(f));
            }
        }

        feedbackSection.getChildren().addAll(ratingHeader, feedbackTitle, formBox, commentsList);
        return feedbackSection;
    }

    private HBox createRatingHeader() {
        HBox header = new HBox(30);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 10, 0));
        header.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0; -fx-padding: 0 0 15 0;");

        List<Feedback> feedbacks = feedbackService.getFeedbacksByProduct(produit.getId());

        if (feedbacks.isEmpty()) {
            Label noRating = new Label("Aucun avis pour le moment");
            noRating.setStyle("-fx-font-style: italic; -fx-text-fill: #999;");
            header.getChildren().add(noRating);
            return header;
        }

        double avgRating = feedbacks.stream()
                .mapToInt(Feedback::getNote)
                .average()
                .orElse(0.0);

        Label avgLabel = new Label(String.format("%.1f", avgRating));
        avgLabel.setStyle("-fx-font-size: 48px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        avgLabel.setPrefWidth(70);

        VBox starsBox = new VBox(5);

        HBox stars = new HBox(2);
        for (int i = 1; i <= 5; i++) {
            Label star = new Label();
            if (i <= Math.round(avgRating)) {
                star.setText("★");
                star.setStyle("-fx-text-fill: #f1c40f; -fx-font-size: 20px;");
            } else {
                star.setText("☆");
                star.setStyle("-fx-text-fill: #ccc; -fx-font-size: 20px;");
            }
            stars.getChildren().add(star);
        }

        Label reviewCount = new Label("(" + feedbacks.size() + " avis)");
        reviewCount.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 14px;");

        starsBox.getChildren().addAll(stars, reviewCount);

        header.getChildren().addAll(avgLabel, starsBox);
        return header;
    }

    private void updateStars(HBox starBox) {
        for (int i = 0; i < starBox.getChildren().size(); i++) {
            Button star = (Button) starBox.getChildren().get(i);
            if (i < selectedRating) {
                star.setText("★");
                star.setStyle("-fx-font-size: 20px; -fx-background-color: transparent; -fx-text-fill: #f1c40f; -fx-cursor: hand;");
            } else {
                star.setText("☆");
                star.setStyle("-fx-font-size: 20px; -fx-background-color: transparent; -fx-text-fill: #f1c40f; -fx-cursor: hand;");
            }
        }
    }

    private void handleSubmit(TextArea commentArea, HBox starBox) {
        if (selectedRating == 0) {
            showAlert("Erreur", "Veuillez sélectionner une note");
            return;
        }
        if (commentArea.getText().trim().isEmpty()) {
            showAlert("Erreur", "Veuillez écrire un commentaire");
            return;
        }

        Feedback feedback = new Feedback(
                currentUser.getId(),
                produit.getId(),
                selectedRating,
                commentArea.getText()
        );

        feedbackService.addFeedback(feedback);

        // Reset
        selectedRating = 0;
        commentArea.clear();
        updateStars(starBox);

        // Refresh
        refresh();
    }

    private VBox createCommentCard(Feedback f) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 12; -fx-background-radius: 5; -fx-border-color: #e0e0e0; -fx-border-radius: 5;");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label userLabel = new Label(f.getUserName());
        userLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        HBox stars = new HBox(2);
        for (int i = 1; i <= 5; i++) {
            Label star = new Label(i <= f.getNote() ? "★" : "☆");
            star.setStyle(i <= f.getNote() ? "-fx-text-fill: #f1c40f;" : "-fx-text-fill: #ccc;");
            stars.getChildren().add(star);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label dateLabel = new Label(formatDate(f.getDateCommentaire()));
        dateLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 11px;");

        header.getChildren().addAll(userLabel, stars, spacer, dateLabel);

        Label comment = new Label(f.getCommentaire());
        comment.setWrapText(true);
        comment.setStyle("-fx-text-fill: #555;");

        card.getChildren().addAll(header, comment);

        // Boutons pour ses propres commentaires
        if (f.getIdUser() == currentUser.getId()) {
            HBox actions = new HBox(10);
            actions.setAlignment(Pos.CENTER_RIGHT);

            Button editBtn = new Button("Modifier");
            editBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #3498db; -fx-border-color: #3498db; -fx-border-radius: 3; -fx-padding: 3 10; -fx-cursor: hand;");

            Button deleteBtn = new Button("Supprimer");
            deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-border-color: #e74c3c; -fx-border-radius: 3; -fx-padding: 3 10; -fx-cursor: hand;");

            final Feedback currentFeedback = f;

            editBtn.setOnAction(e -> showEditDialog(currentFeedback));
            deleteBtn.setOnAction(e -> confirmDelete(currentFeedback));

            actions.getChildren().addAll(editBtn, deleteBtn);
            card.getChildren().add(actions);
        }

        return card;
    }

    private void showEditDialog(Feedback f) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modifier votre avis");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        HBox starBox = new HBox(5);
        int currentRating = f.getNote();

        for (int i = 1; i <= 5; i++) {
            final int rating = i;
            Button star = new Button(i <= currentRating ? "★" : "☆");
            star.setStyle("-fx-font-size: 20px; -fx-background-color: transparent; -fx-text-fill: #f1c40f;");
            star.setOnAction(e -> {
                f.setNote(rating);
                updateDialogStars(starBox, rating);
            });
            starBox.getChildren().add(star);
        }

        TextArea commentArea = new TextArea(f.getCommentaire());
        commentArea.setPrefRowCount(3);

        content.getChildren().addAll(new Label("Note:"), starBox, new Label("Commentaire:"), commentArea);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (!commentArea.getText().trim().isEmpty()) {
                f.setCommentaire(commentArea.getText());
                feedbackService.updateFeedback(f);
                refresh();
            }
        }
    }

    private void updateDialogStars(HBox starBox, int rating) {
        for (int i = 0; i < starBox.getChildren().size(); i++) {
            Button star = (Button) starBox.getChildren().get(i);
            star.setText(i < rating ? "★" : "☆");
        }
    }

    private void confirmDelete(Feedback f) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer votre avis ?");
        alert.setContentText("Cette action est irréversible.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            feedbackService.deleteFeedback(f.getIdFeedback());
            refresh();
        }
    }

    private VBox createSimilarProductsSection() {
        VBox similarSection = new VBox(15);
        similarSection.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        Label similarTitle = new Label("Produits similaires");
        similarTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        HBox similarProducts = new HBox(15);
        similarProducts.setAlignment(Pos.CENTER_LEFT);

        List<Produit> relatedProducts = produitService.getAll().stream()
                .filter(p -> p.getIdCategorie() == produit.getIdCategorie() && p.getId() != produit.getId())
                .limit(4)
                .collect(Collectors.toList());

        if (relatedProducts.isEmpty()) {
            Label noSimilar = new Label("Aucun autre produit dans cette catégorie");
            noSimilar.setStyle("-fx-font-style: italic; -fx-text-fill: #999;");
            similarProducts.getChildren().add(noSimilar);
        } else {
            for (Produit p : relatedProducts) {
                similarProducts.getChildren().add(createSimilarProductCard(p));
            }
        }

        similarSection.getChildren().addAll(similarTitle, similarProducts);
        return similarSection;
    }

    private VBox createSimilarProductCard(Produit p) {
        VBox card = new VBox(5);
        card.setPrefWidth(150);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 8; -fx-background-radius: 5; -fx-cursor: hand;");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(130);
        imageView.setFitHeight(80);
        imageView.setPreserveRatio(true);

        try {
            if (p.getImage() != null) {
                File file = new File(p.getImage());
                if (file.exists()) {
                    imageView.setImage(new Image(file.toURI().toString()));
                }
            }
        } catch (Exception e) {
            // Ignorer
        }

        Label name = new Label(p.getNom());
        name.setWrapText(true);
        name.setMaxWidth(100);
        name.setAlignment(Pos.CENTER);
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");

        card.getChildren().addAll(imageView, name);

        final Produit currentProduit = p;
        final Stage currentStage = this.stage;

        card.setOnMouseClicked(e -> {
            ProductDetailView detailView = new ProductDetailView(currentProduit);
            detailView.show();
            currentStage.close();
        });

        card.setOnMouseEntered(e ->
                card.setStyle("-fx-background-color: #e8f5e9; -fx-padding: 8; -fx-background-radius: 5; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);"));
        card.setOnMouseExited(e ->
                card.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 8; -fx-background-radius: 5; -fx-cursor: hand;"));

        return card;
    }

    private String formatDate(java.time.LocalDateTime date) {
        if (date == null) return "Récemment";
        java.time.format.DateTimeFormatter formatter =
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return date.format(formatter);
    }

    private void refresh() {
        stage.close();
        this.show();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}