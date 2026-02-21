package edu.Loopi.view;

import edu.Loopi.entities.Produit;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class SocialShareDialog {

    /**
     * Affiche une boîte de dialogue avec toutes les options de partage social
     */
    public static void showShareDialog(Produit produit) {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setTitle("Partager sur les réseaux sociaux");

        VBox dialogPane = new VBox(20);
        dialogPane.setPadding(new Insets(30));
        dialogPane.setStyle("-fx-background-color: white; -fx-background-radius: 15;");

        Label title = new Label("📱 Partager ce produit");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label productName = new Label(produit.getNom());
        productName.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");

        Label instruction = new Label("Choisissez un réseau social :");
        instruction.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        GridPane socialGrid = new GridPane();
        socialGrid.setHgap(15);
        socialGrid.setVgap(15);
        socialGrid.setAlignment(Pos.CENTER);

        // Boutons de réseaux sociaux
        Button facebookBtn = createSocialButton("Facebook", "#1877f2", "F", produit, dialogStage);
        Button twitterBtn = createSocialButton("Twitter", "#1da1f2", "🐦", produit, dialogStage);
        Button whatsappBtn = createSocialButton("WhatsApp", "#25d366", "📱", produit, dialogStage);
        Button linkedinBtn = createSocialButton("LinkedIn", "#0077b5", "in", produit, dialogStage);
        Button telegramBtn = createSocialButton("Telegram", "#0088cc", "✈️", produit, dialogStage);
        Button emailBtn = createSocialButton("Email", "#ea4335", "📧", produit, dialogStage);

        socialGrid.add(facebookBtn, 0, 0);
        socialGrid.add(twitterBtn, 1, 0);
        socialGrid.add(whatsappBtn, 2, 0);
        socialGrid.add(linkedinBtn, 0, 1);
        socialGrid.add(telegramBtn, 1, 1);
        socialGrid.add(emailBtn, 2, 1);

        // Lien direct
        VBox linkBox = new VBox(10);
        linkBox.setPadding(new Insets(20, 0, 10, 0));
        linkBox.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15; -fx-background-radius: 8;");

        Label linkLabel = new Label("Lien direct :");
        linkLabel.setStyle("-fx-font-weight: bold;");

        TextField linkField = new TextField(getProductUrl(produit));
        linkField.setEditable(false);
        linkField.setStyle("-fx-background-color: white; -fx-background-radius: 5;");

        Button copyBtn = new Button("📋 Copier le lien");
        copyBtn.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5; -fx-cursor: hand;");
        copyBtn.setMaxWidth(Double.MAX_VALUE);

        copyBtn.setOnAction(e -> {
            javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
            javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
            content.putString(getProductUrl(produit));
            clipboard.setContent(content);
            showAlert(dialogStage, "Copié !", "Le lien a été copié dans le presse-papiers.");
        });

        linkBox.getChildren().addAll(linkLabel, linkField, copyBtn);

        // Bouton fermer
        Button closeBtn = new Button("Fermer");
        closeBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5; -fx-cursor: hand;");
        closeBtn.setMaxWidth(Double.MAX_VALUE);
        closeBtn.setOnAction(e -> dialogStage.close());

        dialogPane.getChildren().addAll(title, productName, instruction, socialGrid, linkBox, closeBtn);

        Scene scene = new Scene(dialogPane, 500, 550);
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }

    private static Button createSocialButton(String text, String color, String icon, Produit produit, Stage dialogStage) {
        Button btn = new Button(icon + "  " + text);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 12 15; -fx-background-radius: 8; -fx-cursor: hand;");
        btn.setPrefWidth(150);
        btn.setPrefHeight(50);

        btn.setOnAction(e -> {
            shareOnSocial(text.toLowerCase(), produit, dialogStage);
        });

        // Effet de survol
        btn.setOnMouseEntered(e -> {
            btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                    "-fx-font-weight: bold; -fx-padding: 12 15; -fx-background-radius: 8; -fx-cursor: hand; " +
                    "-fx-effect: dropshadow(gaussian, " + color + "80, 15, 0, 0, 3); -fx-scale-x: 1.02; -fx-scale-y: 1.02;");
        });
        btn.setOnMouseExited(e -> {
            btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                    "-fx-font-weight: bold; -fx-padding: 12 15; -fx-background-radius: 8; -fx-cursor: hand;");
        });

        return btn;
    }

    private static void shareOnSocial(String network, Produit produit, Stage dialogStage) {
        try {
            String url = "";
            String message = "Découvrez " + produit.getNom() + " sur LOOPI - Art et Création !";
            String productUrl = getProductUrl(produit);

            switch (network) {
                case "facebook":
                    url = "https://www.facebook.com/sharer/sharer.php?u=" + URLEncoder.encode(productUrl, StandardCharsets.UTF_8);
                    break;
                case "twitter":
                    url = "https://twitter.com/intent/tweet?text=" + URLEncoder.encode(message, StandardCharsets.UTF_8) +
                            "&url=" + URLEncoder.encode(productUrl, StandardCharsets.UTF_8);
                    break;
                case "whatsapp":
                    url = "https://api.whatsapp.com/send?text=" + URLEncoder.encode(message + " " + productUrl, StandardCharsets.UTF_8);
                    break;
                case "linkedin":
                    url = "https://www.linkedin.com/sharing/share-offsite/?url=" + URLEncoder.encode(productUrl, StandardCharsets.UTF_8);
                    break;
                case "telegram":
                    url = "https://t.me/share/url?url=" + URLEncoder.encode(productUrl, StandardCharsets.UTF_8) +
                            "&text=" + URLEncoder.encode(message, StandardCharsets.UTF_8);
                    break;
                case "email":
                    url = "mailto:?subject=" + URLEncoder.encode("Découvrez " + produit.getNom() + " sur LOOPI", StandardCharsets.UTF_8) +
                            "&body=" + URLEncoder.encode(message + "\n\n" + productUrl, StandardCharsets.UTF_8);
                    break;
            }

            if (!url.isEmpty()) {
                java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
                dialogStage.close();
            }
        } catch (Exception e) {
            showAlert(dialogStage, "Erreur", "Impossible d'ouvrir " + network + ": " + e.getMessage());
        }
    }

    private static String getProductUrl(Produit produit) {
        return "https://www.loopi.tn/produit/" + produit.getId() + "/" +
                produit.getNom().toLowerCase().replace(" ", "-");
    }

    private static void showAlert(Stage owner, String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(owner);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}