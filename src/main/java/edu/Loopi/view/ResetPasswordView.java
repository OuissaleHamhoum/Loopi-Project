package edu.Loopi.view;

import edu.Loopi.services.AuthService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class ResetPasswordView {
    private String email;
    private AuthService authService = new AuthService();

    public ResetPasswordView(String email) {
        this.email = email;
    }

    public void start(Stage stage) {
        stage.setTitle("LOOPI - Nouveau mot de passe");

        VBox mainLayout = new VBox(20);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setPadding(new Insets(40));
        mainLayout.setStyle("-fx-background-color: linear-gradient(to bottom right, #2E7D32, #4CAF50);");

        // Card container
        VBox card = new VBox(25);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(40, 30, 30, 30));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 20; " +
                "-fx-border-color: rgba(0,0,0,0.1); -fx-border-radius: 20; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 25, 0, 0, 5);");
        card.setMaxWidth(450);

        // Title
        Label title = new Label("NOUVEAU MOT DE PASSE");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#2E7D32"));

        // Instructions
        Label instruction = new Label("Créez un nouveau mot de passe sécurisé");
        instruction.setFont(Font.font("Arial", 14));
        instruction.setTextFill(Color.web("#666"));

        // Nouveau mot de passe
        VBox newPasswordBox = new VBox(5);
        Label newPasswordLabel = new Label("Nouveau mot de passe");
        newPasswordLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        newPasswordLabel.setTextFill(Color.web("#555"));

        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Au moins 6 caractères");
        newPasswordField.setPrefHeight(45);
        stylePasswordField(newPasswordField);

        HBox newPasswordContainer = new HBox();
        newPasswordContainer.setAlignment(Pos.CENTER_LEFT);
        newPasswordContainer.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10; " +
                "-fx-border-color: #e9ecef; -fx-border-radius: 10;");

        Label passwordIcon = new Label("🔑");
        passwordIcon.setPadding(new Insets(0, 15, 0, 15));

        newPasswordContainer.getChildren().addAll(passwordIcon, newPasswordField);
        HBox.setHgrow(newPasswordField, Priority.ALWAYS);

        newPasswordBox.getChildren().addAll(newPasswordLabel, newPasswordContainer);

        // Indicateur de force du mot de passe
        ProgressBar strengthBar = new ProgressBar(0);
        strengthBar.setPrefWidth(350);
        strengthBar.setPrefHeight(6);

        Label strengthLabel = new Label("Force du mot de passe");
        strengthLabel.setFont(Font.font("Arial", 11));
        strengthLabel.setTextFill(Color.web("#999"));

        // Écouteur pour la force du mot de passe
        newPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            double strength = calculatePasswordStrength(newValue);
            strengthBar.setProgress(strength);

            if (strength < 0.3) {
                strengthBar.setStyle("-fx-accent: #f44336;");
                strengthLabel.setText("Faible");
                strengthLabel.setTextFill(Color.web("#f44336"));
            } else if (strength < 0.7) {
                strengthBar.setStyle("-fx-accent: #ff9800;");
                strengthLabel.setText("Moyenne");
                strengthLabel.setTextFill(Color.web("#ff9800"));
            } else {
                strengthBar.setStyle("-fx-accent: #4CAF50;");
                strengthLabel.setText("Forte");
                strengthLabel.setTextFill(Color.web("#4CAF50"));
            }
        });

        // Confirmation mot de passe
        VBox confirmPasswordBox = new VBox(5);
        Label confirmPasswordLabel = new Label("Confirmer le mot de passe");
        confirmPasswordLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        confirmPasswordLabel.setTextFill(Color.web("#555"));

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Répétez le mot de passe");
        confirmPasswordField.setPrefHeight(45);
        stylePasswordField(confirmPasswordField);

        HBox confirmPasswordContainer = new HBox();
        confirmPasswordContainer.setAlignment(Pos.CENTER_LEFT);
        confirmPasswordContainer.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10; " +
                "-fx-border-color: #e9ecef; -fx-border-radius: 10;");

        Label confirmIcon = new Label("✓");
        confirmIcon.setPadding(new Insets(0, 15, 0, 15));

        confirmPasswordContainer.getChildren().addAll(confirmIcon, confirmPasswordField);
        HBox.setHgrow(confirmPasswordField, Priority.ALWAYS);

        confirmPasswordBox.getChildren().addAll(confirmPasswordLabel, confirmPasswordContainer);

        // Error label
        Label errorLabel = new Label();
        errorLabel.setFont(Font.font("Arial", 12));
        errorLabel.setTextFill(Color.RED);
        errorLabel.setWrapText(true);
        errorLabel.setVisible(false);

        // Buttons
        HBox buttonsBox = new HBox(15);
        buttonsBox.setAlignment(Pos.CENTER);

        Button resetButton = new Button("RÉINITIALISER");
        resetButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-background-radius: 8; " +
                "-fx-padding: 12 40; -fx-cursor: hand; -fx-font-size: 14px;");
        resetButton.setOnAction(e -> handleReset(newPasswordField.getText(),
                confirmPasswordField.getText(),
                errorLabel, stage));

        // Hover effects
        resetButton.setOnMouseEntered(e -> resetButton.setStyle(
                "-fx-background-color: #388E3C; -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-background-radius: 8; " +
                        "-fx-padding: 12 40; -fx-cursor: hand; -fx-font-size: 14px;"
        ));
        resetButton.setOnMouseExited(e -> resetButton.setStyle(
                "-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-background-radius: 8; " +
                        "-fx-padding: 12 40; -fx-cursor: hand; -fx-font-size: 14px;"
        ));

        buttonsBox.getChildren().add(resetButton);

        card.getChildren().addAll(title, instruction, newPasswordBox,
                strengthBar, strengthLabel, confirmPasswordBox,
                errorLabel, buttonsBox);
        mainLayout.getChildren().add(card);

        Scene scene = new Scene(mainLayout, 600, 650);
        stage.setScene(scene);
        stage.show();
    }

    private void stylePasswordField(PasswordField field) {
        field.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; " +
                "-fx-font-size: 14px; -fx-padding: 0 10;");
        field.setPrefWidth(300);
    }

    private double calculatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) return 0;

        double strength = 0;

        // Longueur
        if (password.length() >= 6) strength += 0.2;
        if (password.length() >= 8) strength += 0.2;
        if (password.length() >= 12) strength += 0.2;

        // Diversité
        if (password.matches(".*[A-Z].*")) strength += 0.2; // Majuscule
        if (password.matches(".*[a-z].*")) strength += 0.1; // Minuscule
        if (password.matches(".*\\d.*")) strength += 0.2;   // Chiffre
        if (password.matches(".*[!@#$%^&*].*")) strength += 0.3; // Spécial

        return Math.min(strength, 1.0);
    }

    private void handleReset(String newPassword, String confirmPassword,
                             Label errorLabel, Stage stage) {
        // Validation
        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showError(errorLabel, "Veuillez remplir tous les champs");
            return;
        }

        if (newPassword.length() < 6) {
            showError(errorLabel, "Le mot de passe doit contenir au moins 6 caractères");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showError(errorLabel, "Les mots de passe ne correspondent pas");
            return;
        }

        // Réinitialiser le mot de passe
        boolean success = authService.resetPassword(email, newPassword);

        if (success) {
            // Afficher message de succès
            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Succès");
            successAlert.setHeaderText(null);
            successAlert.setContentText("Votre mot de passe a été réinitialisé avec succès !");
            successAlert.showAndWait();

            // Retour à la page de connexion
            new LoginView().start(stage);
        } else {
            showError(errorLabel, "Erreur lors de la réinitialisation. Veuillez réessayer.");
        }
    }

    private void showError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}