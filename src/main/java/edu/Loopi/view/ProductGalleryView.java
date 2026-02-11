package edu.Loopi.view;

import edu.Loopi.entities.Produit;
import edu.Loopi.services.ProduitService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ProductGalleryView {
    private ProduitService ps = new ProduitService();

    // Synchronized with your GalerieView categories
    private final Map<Integer, String> categoryNames = new HashMap<>() {{
        put(1, "Objets décoratifs");
        put(2, "Art mural");
        put(3, "Mobilier artistique");
        put(4, "Installations artistiques");
    }};

    public void show() {
        Stage stage = new Stage();
        stage.setTitle("LOOPI - Boutique Participant");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f4f7f6;");

        // --- Header ---
        VBox header = new VBox(10);
        header.setPadding(new Insets(25));
        header.setAlignment(Pos.CENTER);
        header.setStyle("-fx-background-color: #27ae60;"); // Green brand color

        Label title = new Label("DÉCOUVREZ NOS TRÉSORS");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");
        header.getChildren().add(title);
        root.setTop(header);

        // --- Gallery ---
        FlowPane flowPane = new FlowPane(25, 25);
        flowPane.setPadding(new Insets(30));
        flowPane.setAlignment(Pos.TOP_CENTER);

        for (Produit p : ps.getAll()) {
            flowPane.getChildren().add(createEnhancedCard(p));
        }

        ScrollPane scroll = new ScrollPane(flowPane);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        root.setCenter(scroll);

        Scene scene = new Scene(root, 1200, 800);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    private VBox createEnhancedCard(Produit p) {
        VBox card = new VBox(10);
        card.setPrefWidth(260);
        card.setAlignment(Pos.TOP_CENTER);
        card.setStyle("-fx-background-color: white; -fx-padding: 0; -fx-background-radius: 15; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5); -fx-cursor: hand;");

        // --- Image Area ---
        StackPane imgContainer = new StackPane();
        imgContainer.setPrefSize(260, 180);

        ImageView iv = new ImageView();
        iv.setFitWidth(260);
        iv.setFitHeight(180);

        // Rounded corners for the image top
        Rectangle clip = new Rectangle(260, 180);
        clip.setArcWidth(30); clip.setArcHeight(30);
        iv.setClip(clip);

        try {
            if (p.getImage() != null && !p.getImage().isEmpty()) {
                File file = new File(p.getImage());
                if (file.exists()) {
                    iv.setImage(new Image(file.toURI().toString()));
                } else {
                    iv.setImage(new Image("https://via.placeholder.com/260x180?text=Image+Non+Trouvée"));
                }
            }
        } catch (Exception e) {
            iv.setImage(new Image("https://via.placeholder.com/260x180?text=Erreur+Chargement"));
        }
        imgContainer.getChildren().add(iv);

        // --- Info Area ---
        VBox info = new VBox(5);
        info.setPadding(new Insets(12));
        info.setAlignment(Pos.CENTER_LEFT);

        Label name = new Label(p.getNom().toUpperCase());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #2c3e50;");

        String catName = categoryNames.getOrDefault(p.getIdCategorie(), "Autre");
        Label category = new Label("🏷️ " + catName);
        category.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 12px; -fx-font-weight: bold;");

        Label desc = new Label(p.getDescription());
        desc.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11px;");
        desc.setWrapText(true);
        desc.setMaxHeight(40);

        info.getChildren().addAll(name, category, desc);
        card.getChildren().addAll(imgContainer, info);

        // Hover Effect
        card.setOnMouseEntered(e -> card.setStyle(card.getStyle() + "-fx-scale-x: 1.03; -fx-scale-y: 1.03;"));
        card.setOnMouseExited(e -> card.setStyle(card.getStyle().replace("-fx-scale-x: 1.03; -fx-scale-y: 1.03;", "")));

        // Click to open Detail View
        card.setOnMouseClicked(e -> new ProductDetailView(p).show());

        return card;
    }
}