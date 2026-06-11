package ferme.ui.controllers;

import ferme.models.EspeceAquacole;
import ferme.models.ProgrammeAlimentation;
import ferme.models.ZoneAquacole;
import ferme.ui.AppContext;
import ferme.ui.AppContextAware;
import ferme.ui.Refreshable;
import ferme.ui.navigation.NavigationController;
import ferme.ui.services.DialogService;
import ferme.ui.services.FileImportService;
import ferme.ui.services.UiFarmService;
import ferme.ui.viewmodels.AquacultureZoneViewModel;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class AquacultureController implements AppContextAware, Refreshable {

    @FXML private TableView<AquacultureZoneViewModel> aquacultureTable;
    @FXML private Button editFeedingProgramButton;
    @FXML private Button recordHarvestButton;
    @FXML private Button addSpeciesButton;
    @FXML private VBox speciesDetailBox;
    @FXML private TableView<EspeceAquacole> speciesTable;

    private UiFarmService farmService;
    private DialogService dialogService;
    private NavigationController navigationController;

    @Override
    public void setContext(AppContext context, UiFarmService farmService,
            DialogService dialogService, FileImportService fileImportService,
            NavigationController navigationController) {
        this.farmService = farmService;
        this.dialogService = dialogService;
        this.navigationController = navigationController;
    }

    @FXML
    private void initialize() {
        setupTable();
        setupSpeciesTable();
        wireActions();
        aquacultureTable.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> showSpecies(sel));
    }

    private void setupSpeciesTable() {
        TableColumn<EspeceAquacole, String> nameCol = new TableColumn<>("Species");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNom()));
        nameCol.setPrefWidth(150);
        TableColumn<EspeceAquacole, Integer> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getNombreAnimaux()).asObject());
        qtyCol.setPrefWidth(100);
        speciesTable.getColumns().setAll(nameCol, qtyCol);
        speciesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    }

    private void showSpecies(AquacultureZoneViewModel selected) {
        if (selected == null) {
            speciesDetailBox.setVisible(false);
            speciesDetailBox.setManaged(false);
            return;
        }
        for (ZoneAquacole zone : farmService.getAquacultureZones()) {
            if (zone.getCode().equals(selected.getCode())) {
                List<EspeceAquacole> especes = zone.getEspeces();
                if (especes.isEmpty()) {
                    speciesDetailBox.setVisible(false);
                    speciesDetailBox.setManaged(false);
                } else {
                    speciesTable.setItems(FXCollections.observableArrayList(especes));
                    speciesDetailBox.setVisible(true);
                    speciesDetailBox.setManaged(true);
                }
                return;
            }
        }
    }

    private void setupTable() {
        TableColumn<AquacultureZoneViewModel, String> codeCol = new TableColumn<>("Code");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("code"));
        codeCol.setPrefWidth(70);

        TableColumn<AquacultureZoneViewModel, String> nameCol = new TableColumn<>("Zone");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(150);

        TableColumn<AquacultureZoneViewModel, String> speciesCol = new TableColumn<>("Species");
        speciesCol.setCellValueFactory(new PropertyValueFactory<>("speciesSummary"));
        speciesCol.setPrefWidth(200);

        TableColumn<AquacultureZoneViewModel, Number> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("totalAnimals"));
        totalCol.setPrefWidth(60);

        TableColumn<AquacultureZoneViewModel, String> feedCol = new TableColumn<>("Feed Type");
        feedCol.setCellValueFactory(new PropertyValueFactory<>("feedType"));
        feedCol.setPrefWidth(130);

        TableColumn<AquacultureZoneViewModel, String> qtyCol = new TableColumn<>("Quantity / Meal");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantityPerMeal"));
        qtyCol.setPrefWidth(110);

        TableColumn<AquacultureZoneViewModel, String> mealsCol = new TableColumn<>("Meals / Day");
        mealsCol.setCellValueFactory(new PropertyValueFactory<>("mealsPerDay"));
        mealsCol.setPrefWidth(95);

        TableColumn<AquacultureZoneViewModel, Double> harvestAvgCol = new TableColumn<>("Harvest Avg");
        harvestAvgCol.setCellValueFactory(new PropertyValueFactory<>("harvestAverage"));
        harvestAvgCol.setPrefWidth(95);

        aquacultureTable.getColumns().setAll(codeCol, nameCol, speciesCol, totalCol, feedCol, qtyCol, mealsCol, harvestAvgCol);
        aquacultureTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    }

    private void wireActions() {
        addSpeciesButton.setOnAction(event -> addSpecies());
        editFeedingProgramButton.setOnAction(event -> editFeedingProgram());
        recordHarvestButton.setOnAction(event -> recordHarvestWeight());
    }

    @Override
    public void refresh() {
        if (farmService == null) {
            return;
        }
        List<AquacultureZoneViewModel> models = new ArrayList<>();
        for (ZoneAquacole zone : farmService.getAquacultureZones()) {
            ProgrammeAlimentation programme = zone.getProgrammeAlimentation();
            String feedType = programme != null ? programme.getTypeAliment() : "-";
            String quantity = programme != null ? String.valueOf(programme.getQuantiteParRepas()) : "-";
            String meals = programme != null ? String.valueOf(programme.getNombreRepasParJour()) : "-";
            List<EspeceAquacole> especes = zone.getEspeces();
            String speciesSummary = especes.stream()
                    .map(e -> e.getNom() + " x" + e.getNombreAnimaux())
                    .collect(Collectors.joining(", "));
            if (speciesSummary.isEmpty()) speciesSummary = "-";
            int totalAnimals = especes.stream().mapToInt(EspeceAquacole::getNombreAnimaux).sum();
            models.add(new AquacultureZoneViewModel(
                    zone.getCode(),
                    zone.getNom(),
                    speciesSummary,
                    totalAnimals,
                    feedType,
                    quantity,
                    meals,
                    zone.getHistoriqueProduction().moyenneProduction()));
        }
        aquacultureTable.setItems(FXCollections.observableArrayList(models));
        if (navigationController != null) {
            navigationController.refreshTopBar();
        }
    }

    private void addSpecies() {
        AquacultureZoneViewModel selected = aquacultureTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            dialogService.showWarning("Selection Required", "Select an aquaculture zone first.");
            return;
        }
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Add Species");
        TextField nameField = new TextField();
        TextField qtyField = new TextField();
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.add(new Label("Species name"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Quantity"), 0, 1);
        grid.add(qtyField, 1, 1);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(javafx.scene.control.ButtonType.OK,
                javafx.scene.control.ButtonType.CANCEL);
        var result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                String name = nameField.getText().trim();
                int qty = Integer.parseInt(qtyField.getText().trim());
                farmService.addAquacultureSpecies(selected.getCode(), name, qty);
                refresh();
            } catch (RuntimeException e) {
                dialogService.showError("Add Species Failed", e.getMessage());
            }
        }
    }

    private ZoneAquacole getSelectedZone() {
        AquacultureZoneViewModel selected = aquacultureTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            dialogService.showWarning("Selection Required", "Select an aquaculture zone first.");
            return null;
        }
        for (ZoneAquacole zone : farmService.getAquacultureZones()) {
            if (zone.getCode().equals(selected.getCode())) {
                return zone;
            }
        }
        return null;
    }

    private void editFeedingProgram() {
        ZoneAquacole zone = getSelectedZone();
        if (zone == null) {
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Edit Feeding Program");

        TextField feedField = new TextField();
        TextField quantityField = new TextField();
        TextField mealsField = new TextField();

        ProgrammeAlimentation current = zone.getProgrammeAlimentation();
        if (current != null) {
            feedField.setText(current.getTypeAliment());
            quantityField.setText(String.valueOf(current.getQuantiteParRepas()));
            mealsField.setText(String.valueOf(current.getNombreRepasParJour()));
        }

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.add(new Label("Feed type"), 0, 0);
        grid.add(feedField, 1, 0);
        grid.add(new Label("Quantity / meal"), 0, 1);
        grid.add(quantityField, 1, 1);
        grid.add(new Label("Meals / day"), 0, 2);
        grid.add(mealsField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(javafx.scene.control.ButtonType.OK,
                javafx.scene.control.ButtonType.CANCEL);
        var result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                double quantity = Double.parseDouble(quantityField.getText().trim());
                int meals = Integer.parseInt(mealsField.getText().trim());
                farmService.updateFeedingProgram(zone.getCode(), feedField.getText().trim(), quantity, meals);
                refresh();
            } catch (RuntimeException e) {
                dialogService.showError("Update Feeding Program Failed", e.getMessage());
            }
        }
    }

    private void recordHarvestWeight() {
        ZoneAquacole zone = getSelectedZone();
        if (zone == null) {
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Record Harvest Weight");
        TextField weightField = new TextField();
        DatePicker datePicker = new DatePicker();

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.add(new Label("Weight"), 0, 0);
        grid.add(weightField, 1, 0);
        grid.add(new Label("Date"), 0, 1);
        grid.add(datePicker, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(javafx.scene.control.ButtonType.OK,
                javafx.scene.control.ButtonType.CANCEL);
        var result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                double weight = Double.parseDouble(weightField.getText().trim());
                LocalDate date = datePicker.getValue();
                if (date == null) {
                    throw new IllegalArgumentException("Date is required.");
                }
                farmService.recordHarvestWeight(zone.getCode(), weight, date.toString());
                refresh();
            } catch (RuntimeException e) {
                dialogService.showError("Record Harvest Failed", e.getMessage());
            }
        }
    }
}
