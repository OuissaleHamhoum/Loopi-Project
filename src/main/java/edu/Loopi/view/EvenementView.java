package edu.Loopi.view;

import edu.Loopi.entities.Evenement;
import edu.Loopi.entities.User;
import edu.Loopi.services.EvenementService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

@SuppressWarnings("unchecked")
public class EvenementView extends BorderPane {
    private User currentUser;
    private EvenementService evenementService;
    private TableView<Evenement> table;
    private ObservableList<Evenement> evenementList;

    public EvenementView(User user) {
        this.currentUser = user;
        this.evenementService = new EvenementService();
        this.evenementList = FXCollections.observableArrayList();

        initView();
        loadEvenements();
    }

    private void initView() {
        setPadding(new Insets(20));

        Label titleLabel = new Label("Gestion des Événements");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Evenement, String> titreCol = new TableColumn<>("Titre");
        titreCol.setCellValueFactory(new PropertyValueFactory<>("titre"));

        TableColumn<Evenement, String> descriptionCol = new TableColumn<>("Description");
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<Evenement, LocalDateTime> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("dateEvenement"));

        TableColumn<Evenement, String> lieuCol = new TableColumn<>("Lieu");
        lieuCol.setCellValueFactory(new PropertyValueFactory<>("lieu"));

        TableColumn<Evenement, Integer> capaciteCol = new TableColumn<>("Capacité");
        capaciteCol.setCellValueFactory(new PropertyValueFactory<>("capaciteMax"));

        table.getColumns().addAll(titreCol, descriptionCol, dateCol, lieuCol, capaciteCol);
        table.setItems(evenementList);

        Button addButton = new Button("Ajouter");
        Button editButton = new Button("Modifier");
        Button deleteButton = new Button("Supprimer");
        Button viewParticipantsButton = new Button("Voir les participants");

        HBox buttonBox = new HBox(10, addButton, editButton, deleteButton, viewParticipantsButton);
        buttonBox.setPadding(new Insets(10, 0, 10, 0));

        addButton.setOnAction(e -> openAddDialog());
        editButton.setOnAction(e -> openEditDialog());
        deleteButton.setOnAction(e -> deleteEvenement());
        viewParticipantsButton.setOnAction(e -> viewParticipants());

        VBox vbox = new VBox(10, titleLabel, table, buttonBox);
        setCenter(vbox);
    }

    private void loadEvenements() {
        evenementList.clear();
        evenementList.addAll(evenementService.getEvenementsByOrganisateur(currentUser.getId()));
    }

    private void openAddDialog() {
        Dialog<Evenement> dialog = new Dialog<>();
        dialog.setTitle("Ajouter un événement");
        dialog.setHeaderText("Remplissez les détails de l'événement");

        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField titreField = new TextField();
        titreField.setPromptText("Titre");
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Description");
        descriptionArea.setPrefHeight(100);
        DatePicker datePicker = new DatePicker();
        TextField heureField = new TextField();
        heureField.setPromptText("HH:mm");
        TextField lieuField = new TextField();
        lieuField.setPromptText("Lieu");
        TextField capaciteField = new TextField();
        capaciteField.setPromptText("Capacité maximale");

        grid.add(new Label("Titre:"), 0, 0);
        grid.add(titreField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionArea, 1, 1);
        grid.add(new Label("Date:"), 0, 2);
        grid.add(datePicker, 1, 2);
        grid.add(new Label("Heure (HH:mm):"), 0, 3);
        grid.add(heureField, 1, 3);
        grid.add(new Label("Lieu:"), 0, 4);
        grid.add(lieuField, 1, 4);
        grid.add(new Label("Capacité:"), 0, 5);
        grid.add(capaciteField, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    LocalDate date = datePicker.getValue();
                    LocalTime time = LocalTime.parse(heureField.getText());
                    LocalDateTime dateTime = LocalDateTime.of(date, time);
                    Integer capacite = capaciteField.getText().isEmpty() ? null : Integer.parseInt(capaciteField.getText());

                    Evenement evenement = new Evenement(
                            titreField.getText(),
                            descriptionArea.getText(),
                            dateTime,
                            lieuField.getText(),
                            currentUser.getId(),
                            capacite,
                            null
                    );
                    return evenement;
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur de format", "Veuillez vérifier les données saisies.");
                    return null;
                }
            }
            return null;
        });

        Optional<Evenement> result = dialog.showAndWait();
        result.ifPresent(evenement -> {
            evenementService.addEvenement(evenement);
            loadEvenements();
        });
    }

    private void openEditDialog() {
        Evenement selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Dialog<Evenement> dialog = new Dialog<>();
            dialog.setTitle("Modifier l'événement");
            dialog.setHeaderText("Modifiez les détails de l'événement");

            ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            TextField titreField = new TextField(selected.getTitre());
            TextArea descriptionArea = new TextArea(selected.getDescription());
            descriptionArea.setPrefHeight(100);
            DatePicker datePicker = new DatePicker(selected.getDateEvenement().toLocalDate());
            TextField heureField = new TextField(selected.getDateEvenement().toLocalTime().toString());
            TextField lieuField = new TextField(selected.getLieu());
            TextField capaciteField = new TextField(selected.getCapaciteMax() == null ? "" : selected.getCapaciteMax().toString());

            grid.add(new Label("Titre:"), 0, 0);
            grid.add(titreField, 1, 0);
            grid.add(new Label("Description:"), 0, 1);
            grid.add(descriptionArea, 1, 1);
            grid.add(new Label("Date:"), 0, 2);
            grid.add(datePicker, 1, 2);
            grid.add(new Label("Heure (HH:mm):"), 0, 3);
            grid.add(heureField, 1, 3);
            grid.add(new Label("Lieu:"), 0, 4);
            grid.add(lieuField, 1, 4);
            grid.add(new Label("Capacité:"), 0, 5);
            grid.add(capaciteField, 1, 5);

            dialog.getDialogPane().setContent(grid);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    try {
                        LocalDate date = datePicker.getValue();
                        LocalTime time = LocalTime.parse(heureField.getText());
                        LocalDateTime dateTime = LocalDateTime.of(date, time);
                        Integer capacite = capaciteField.getText().isEmpty() ? null : Integer.parseInt(capaciteField.getText());
                        selected.setTitre(titreField.getText());
                        selected.setDescription(descriptionArea.getText());
                        selected.setDateEvenement(dateTime);
                        selected.setLieu(lieuField.getText());
                        selected.setCapaciteMax(capacite);
                        return selected;
                    } catch (Exception e) {
                        showAlert(Alert.AlertType.ERROR, "Erreur de format", "Veuillez vérifier les données saisies.");
                        return null;
                    }
                }
                return null;
            });

            Optional<Evenement> result = dialog.showAndWait();
            result.ifPresent(evenement -> {
                evenementService.updateEvenement(evenement);
                loadEvenements();
            });
        } else {
            showAlert(Alert.AlertType.WARNING, "Aucun événement sélectionné", "Veuillez sélectionner un événement à modifier.");
        }
    }

    private void deleteEvenement() {
        Evenement selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Supprimer l'événement");
            alert.setContentText("Êtes-vous sûr de vouloir supprimer l'événement : " + selected.getTitre() + " ?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                evenementService.deleteEvenement(selected.getIdEvenement());
                loadEvenements();
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Aucun événement sélectionné", "Veuillez sélectionner un événement à supprimer.");
        }
    }

    private void viewParticipants() {
        Evenement selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            int nbParticipants = new edu.Loopi.services.ParticipationService().getNombreParticipants(selected.getIdEvenement());
            showAlert(Alert.AlertType.INFORMATION, "Participants",
                    "Nombre de participants pour l'événement '" + selected.getTitre() + "' : " + nbParticipants);
        } else {
            showAlert(Alert.AlertType.WARNING, "Aucun événement sélectionné", "Veuillez sélectionner un événement pour voir ses participants.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public VBox getView() {
        VBox container = new VBox();
        container.getChildren().add(this);
        return container;
    }
}