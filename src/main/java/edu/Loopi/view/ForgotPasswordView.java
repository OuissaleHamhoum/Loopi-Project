package edu.Loopi.view;

import edu.Loopi.services.AuthService;
import edu.Loopi.services.EmailService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class ForgotPasswordView {
    private AuthService authService = new AuthService();
    private EmailService emailService = new EmailService();

    public void start(Stage stage) {
        stage.setTitle("LOOPI - Mot de passe oublié");

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
        Label title = new Label("MOT DE PASSE OUBLIÉ");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#2E7D32"));

        // Instructions
        Label instruction = new Label("Entrez votre adresse email pour recevoir un code de réinitialisation");
        instruction.setFont(Font.font("Arial", 14));
        instruction.setTextFill(Color.web("#666"));
        instruction.setWrapText(true);
        instruction.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        // Email field
        VBox emailBox = new VBox(5);
        Label emailLabel = new Label("Adresse Email");
        emailLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        emailLabel.setTextFill(Color.web("#555"));

        TextField emailField = new TextField();
        emailField.setPromptText("exemple@email.com");
        emailField.setPrefHeight(45);
        styleTextField(emailField);

        HBox emailContainer = new HBox();
        emailContainer.setAlignment(Pos.CENTER_LEFT);
        emailContainer.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10; " +
                "-fx-border-color: #e9ecef; -fx-border-radius: 10;");

        Label emailIcon = new Label("✉️");
        emailIcon.setPadding(new Insets(0, 15, 0, 15));

        emailContainer.getChildren().addAll(emailIcon, emailField);
        HBox.setHgrow(emailField, Priority.ALWAYS);

        emailBox.getChildren().addAll(emailLabel, emailContainer);

        // Options de récupération
        VBox optionsBox = new VBox(10);
        optionsBox.setAlignment(Pos.CENTER_LEFT);

        Label chooseMethod = new Label("Choisissez une méthode de récupération:");
        chooseMethod.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        chooseMethod.setTextFill(Color.web("#555"));

        ToggleGroup methodGroup = new ToggleGroup();

        RadioButton emailOption = new RadioButton("Recevoir un code par email");
        emailOption.setToggleGroup(methodGroup);
        emailOption.setSelected(true);
        emailOption.setFont(Font.font("Arial", 12));

        RadioButton phoneOption = new RadioButton("Recevoir un SMS (à venir)");
        phoneOption.setToggleGroup(methodGroup);
        phoneOption.setFont(Font.font("Arial", 12));
        phoneOption.setDisable(true);

        RadioButton qrOption = new RadioButton("Scanner un QR Code (à venir)");
        qrOption.setToggleGroup(methodGroup);
        qrOption.setFont(Font.font("Arial", 12));
        qrOption.setDisable(true);

        optionsBox.getChildren().addAll(chooseMethod, emailOption, phoneOption, qrOption);

        // "Je ne suis pas un robot" checkbox
        CheckBox notRobotCheck = new CheckBox("Je ne suis pas un robot");
        notRobotCheck.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        notRobotCheck.setTextFill(Color.web("#555"));

        // Error label
        Label errorLabel = new Label();
        errorLabel.setFont(Font.font("Arial", 12));
        errorLabel.setTextFill(Color.RED);
        errorLabel.setWrapText(true);
        errorLabel.setVisible(false);

        // Buttons
        HBox buttonsBox = new HBox(15);
        buttonsBox.setAlignment(Pos.CENTER);

        Button backButton = new Button("← Retour");
        backButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #4CAF50; " +
                "-fx-font-weight: bold; -fx-border-color: #4CAF50; -fx-border-radius: 8; " +
                "-fx-padding: 10 25; -fx-cursor: hand;");
        backButton.setOnAction(e -> {
            new LoginView().start(stage);
        });

        Button nextButton = new Button("Suivant →");
        nextButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-background-radius: 8; " +
                "-fx-padding: 10 30; -fx-cursor: hand;");
        nextButton.setOnAction(e -> {
            handleNextStep(emailField.getText().trim(), methodGroup, notRobotCheck.isSelected(),
                    errorLabel, stage);
        });

        // Hover effects
        nextButton.setOnMouseEntered(e -> nextButton.setStyle(
                "-fx-background-color: #388E3C; -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-background-radius: 8; " +
                        "-fx-padding: 10 30; -fx-cursor: hand;"
        ));
        nextButton.setOnMouseExited(e -> nextButton.setStyle(
                "-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-background-radius: 8; " +
                        "-fx-padding: 10 30; -fx-cursor: hand;"
        ));

        buttonsBox.getChildren().addAll(backButton, nextButton);

        card.getChildren().addAll(title, instruction, emailBox, optionsBox,
                notRobotCheck, errorLabel, buttonsBox);
        mainLayout.getChildren().add(card);

        Scene scene = new Scene(mainLayout, 600, 650);
        stage.setScene(scene);
        stage.show();
    }

    private void styleTextField(TextField field) {
        field.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; " +
                "-fx-font-size: 14px; -fx-padding: 0 10;");
        field.setPrefWidth(300);
    }

    private void handleNextStep(String email, ToggleGroup methodGroup, boolean notRobot,
                                Label errorLabel, Stage stage) {
        // Validation
        if (email.isEmpty()) {
            showError(errorLabel, "Veuillez entrer votre adresse email");
            return;
        }

        if (!isValidEmail(email)) {
            showError(errorLabel, "Adresse email invalide");
            return;
        }

        if (!notRobot) {
            showError(errorLabel, "Veuillez confirmer que vous n'êtes pas un robot");
            return;
        }

        // Vérifier si l'email existe dans la base
        if (!authService.emailExists(email)) {
            showError(errorLabel, "Cette adresse email n'est pas enregistrée");
            return;
        }

        RadioButton selected = (RadioButton) methodGroup.getSelectedToggle();
        if (selected == null) {
            showError(errorLabel, "Veuillez choisir une méthode de récupération");
            return;
        }

        String method = selected.getText();

        if (method.contains("email")) {
            // Générer et envoyer le code
            String code = authService.generateResetCode(email);

            // Simuler l'envoi d'email
            boolean sent = emailService.sendResetCode(email, code);

            if (sent) {
                // Ouvrir la vue de vérification du code
                VerifyCodeView verifyView = new VerifyCodeView(email, code);
                verifyView.start(stage);
            } else {
                showError(errorLabel, "Impossible d'envoyer l'email. Réessayez plus tard.");
            }
        } else {
            showError(errorLabel, "Cette méthode n'est pas encore disponible");
        }
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    private void showError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}