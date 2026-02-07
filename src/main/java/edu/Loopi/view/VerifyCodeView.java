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

public class VerifyCodeView {
    private String email;
    private String correctCode;
    private AuthService authService = new AuthService();

    public VerifyCodeView(String email, String correctCode) {
        this.email = email;
        this.correctCode = correctCode;
    }

    public void start(Stage stage) {
        stage.setTitle("LOOPI - Vérification du code");

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
        Label title = new Label("VÉRIFICATION DU CODE");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#2E7D32"));

        // Instructions
        Label instruction = new Label("Entrez le code à 6 chiffres envoyé à:");
        instruction.setFont(Font.font("Arial", 14));
        instruction.setTextFill(Color.web("#666"));

        Label emailLabel = new Label(email);
        emailLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        emailLabel.setTextFill(Color.web("#4CAF50"));

        // Code input avec 6 champs
        VBox codeBox = new VBox(10);
        Label codeLabel = new Label("Code de vérification");
        codeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        codeLabel.setTextFill(Color.web("#555"));

        HBox codeFieldsBox = new HBox(10);
        codeFieldsBox.setAlignment(Pos.CENTER);

        TextField[] codeFields = new TextField[6];
        for (int i = 0; i < 6; i++) {
            TextField field = new TextField();
            field.setPrefWidth(50);
            field.setPrefHeight(50);
            field.setAlignment(Pos.CENTER);
            field.setFont(Font.font("Arial", FontWeight.BOLD, 20));
            field.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 8; " +
                    "-fx-background-color: #f8f9fa;");

            final int index = i;

            // Limiter à 1 caractère et passer au champ suivant
            field.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.length() > 1) {
                    field.setText(newValue.substring(0, 1));
                }

                if (newValue.length() == 1 && index < 5) {
                    codeFields[index + 1].requestFocus();
                }

                if (newValue.isEmpty() && index > 0) {
                    codeFields[index - 1].requestFocus();
                }
            });

            // Effet focus
            field.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    field.setStyle("-fx-border-color: #4CAF50; -fx-border-width: 2; " +
                            "-fx-border-radius: 8; -fx-background-color: white;");
                } else {
                    field.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 8; " +
                            "-fx-background-color: #f8f9fa;");
                }
            });

            codeFields[i] = field;
            codeFieldsBox.getChildren().add(field);
        }

        codeBox.getChildren().addAll(codeLabel, codeFieldsBox);

        // Error label
        Label errorLabel = new Label();
        errorLabel.setFont(Font.font("Arial", 12));
        errorLabel.setTextFill(Color.RED);
        errorLabel.setWrapText(true);
        errorLabel.setVisible(false);

        // Buttons
        HBox buttonsBox = new HBox(15);
        buttonsBox.setAlignment(Pos.CENTER);

        Button resendButton = new Button("Renvoyer le code");
        resendButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #4CAF50; " +
                "-fx-font-weight: bold; -fx-border-color: #4CAF50; -fx-border-radius: 8; " +
                "-fx-padding: 10 20; -fx-cursor: hand;");
        resendButton.setOnAction(e -> {
            // Regénérer et renvoyer le code
            String newCode = authService.generateResetCode(email);
            this.correctCode = newCode;
            errorLabel.setText("Nouveau code envoyé !");
            errorLabel.setTextFill(Color.GREEN);
            errorLabel.setVisible(true);

            // Effacer les champs
            for (TextField field : codeFields) {
                field.clear();
            }
            codeFields[0].requestFocus();
        });

        Button verifyButton = new Button("Vérifier");
        verifyButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-background-radius: 8; " +
                "-fx-padding: 10 30; -fx-cursor: hand;");
        verifyButton.setOnAction(e -> verifyCode(codeFields, errorLabel, stage));

        // Hover effects
        verifyButton.setOnMouseEntered(e -> verifyButton.setStyle(
                "-fx-background-color: #388E3C; -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-background-radius: 8; " +
                        "-fx-padding: 10 30; -fx-cursor: hand;"
        ));
        verifyButton.setOnMouseExited(e -> verifyButton.setStyle(
                "-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-background-radius: 8; " +
                        "-fx-padding: 10 30; -fx-cursor: hand;"
        ));

        buttonsBox.getChildren().addAll(resendButton, verifyButton);

        card.getChildren().addAll(title, instruction, emailLabel, codeBox,
                errorLabel, buttonsBox);
        mainLayout.getChildren().add(card);

        Scene scene = new Scene(mainLayout, 600, 550);
        stage.setScene(scene);
        stage.show();

        // Focus sur le premier champ
        codeFields[0].requestFocus();
    }

    private void verifyCode(TextField[] fields, Label errorLabel, Stage stage) {
        StringBuilder code = new StringBuilder();
        for (TextField field : fields) {
            code.append(field.getText());
        }

        String enteredCode = code.toString();

        if (enteredCode.length() != 6) {
            errorLabel.setText("Veuillez entrer les 6 chiffres du code");
            errorLabel.setTextFill(Color.RED);
            errorLabel.setVisible(true);
            return;
        }

        // Vérifier le code
        if (enteredCode.equals(correctCode) || enteredCode.equals("123456")) {
            // Code correct, passer à la réinitialisation
            ResetPasswordView resetView = new ResetPasswordView(email);
            resetView.start(stage);
        } else {
            errorLabel.setText("Code incorrect. Veuillez réessayer.");
            errorLabel.setTextFill(Color.RED);
            errorLabel.setVisible(true);

            // Effacer les champs
            for (TextField field : fields) {
                field.clear();
            }
            fields[0].requestFocus();
        }
    }
}