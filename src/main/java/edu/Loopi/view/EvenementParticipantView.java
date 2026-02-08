package edu.Loopi.view;

import edu.Loopi.entities.Evenement;
import edu.Loopi.entities.Participation;
import edu.Loopi.entities.User;
import edu.Loopi.services.EvenementService;
import edu.Loopi.services.ParticipationService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.util.Optional;

@SuppressWarnings("unchecked")
public class EvenementParticipantView extends BorderPane {

    private User currentUser;
    private EvenementService evenementService;
    private ParticipationService participationService;
    private TableView<Evenement> table;
    private ObservableList<Evenement> evenementList;

    public EvenementParticipantView(User user) {
        this.currentUser = user;
        this.evenementService = new EvenementService();
        this.participationService = new ParticipationService();
        this.evenementList = FXCollections.observableArrayList();

        initView();
        loadEvenements();
    }

    private void initView() {
        setPadding(new Insets(20));

        Label titleLabel = new Label("Événements disponibles");
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

        Button inscrireButton = new Button("S'inscrire");
        HBox buttonBox = new HBox(10, inscrireButton);
        buttonBox.setPadding(new Insets(10, 0, 10, 0));

        inscrireButton.setOnAction(e -> inscrire());

        VBox vbox = new VBox(10, titleLabel, table, buttonBox);
        setCenter(vbox);
    }

    private void loadEvenements() {
        evenementList.clear();
        evenementList.addAll(evenementService.getAllEvenements());
    }

    private void inscrire() {
        Evenement selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (participationService.isUserAlreadyRegistered(currentUser.getId(), selected.getIdEvenement())) {
                showAlert(Alert.AlertType.WARNING, "Déjà inscrit", "Vous êtes déjà inscrit à cet événement.");
                return;
            }

            if (evenementService.isEvenementComplet(selected.getIdEvenement())) {
                showAlert(Alert.AlertType.WARNING, "Événement complet", "Désolé, cet événement est complet.");
                return;
            }

            Dialog<Participation> dialog = new Dialog<>();
            dialog.setTitle("Inscription à l'événement");
            dialog.setHeaderText("Veuillez fournir vos informations de contact");

            ButtonType saveButtonType = new ButtonType("S'inscrire", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            TextField contactField = new TextField();
            contactField.setPromptText("Email ou téléphone");
            TextField ageField = new TextField();
            ageField.setPromptText("Âge");

            grid.add(new Label("Contact:"), 0, 0);
            grid.add(contactField, 1, 0);
            grid.add(new Label("Âge:"), 0, 1);
            grid.add(ageField, 1, 1);

            dialog.getDialogPane().setContent(grid);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    try {
                        Integer age = ageField.getText().isEmpty() ? null : Integer.parseInt(ageField.getText());
                        Participation participation = new Participation(
                                currentUser.getId(),
                                selected.getIdEvenement(),
                                contactField.getText(),
                                age,
                                "inscrit"
                        );
                        return participation;
                    } catch (NumberFormatException e) {
                        showAlert(Alert.AlertType.ERROR, "Âge invalide", "L'âge doit être un nombre.");
                        return null;
                    }
                }
                return null;
            });

            Optional<Participation> result = dialog.showAndWait();
            result.ifPresent(participation -> {
                participationService.addParticipation(participation);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Vous êtes inscrit à l'événement !");
            });

        } else {
            showAlert(Alert.AlertType.WARNING, "Aucun événement sélectionné", "Veuillez sélectionner un événement.");
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