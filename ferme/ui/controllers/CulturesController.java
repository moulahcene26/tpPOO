package ferme.ui.controllers;

import ferme.enums.FamilleCulture;
import ferme.enums.StadeCroissance;
import ferme.models.Zone;
import ferme.models.ZoneCulture;
import ferme.ui.AppContext;
import ferme.ui.AppContextAware;
import ferme.ui.Refreshable;
import ferme.ui.navigation.NavigationController;
import ferme.ui.services.DialogService;
import ferme.ui.services.FileImportService;
import ferme.ui.services.UiFarmService;
import ferme.ui.viewmodels.CultureViewModel;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

public class CulturesController implements AppContextAware, Refreshable {

    @FXML
    private ComboBox<FamilleCulture> familyFilter;
    @FXML
    private TableView<CultureViewModel> culturesTable;
    @FXML
    private Button addCultureButton;
    @FXML
    private Button updateStageButton;
    @FXML
    private Button reportButton;

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
        familyFilter.getItems().setAll(FamilleCulture.values());
        familyFilter.valueProperty().addListener((obs, oldVal, newVal) -> refresh());
        addCultureButton.setOnAction(event -> addCulture());
        updateStageButton.setOnAction(event -> updateStage());
        reportButton.setOnAction(event -> generateReport());
    }

    private void setupTable() {
        TableColumn<CultureViewModel, String> zoneCol = new TableColumn<>("Zone");
        zoneCol.setCellValueFactory(new PropertyValueFactory<>("zoneCode"));
        zoneCol.setPrefWidth(75);
        TableColumn<CultureViewModel, String> nameCol = new TableColumn<>("Crop Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(130);
        TableColumn<CultureViewModel, String> familyCol = new TableColumn<>("Family");
        familyCol.setCellValueFactory(new PropertyValueFactory<>("family"));
        familyCol.setPrefWidth(100);
        TableColumn<CultureViewModel, String> stageCol = new TableColumn<>("Stage");
        stageCol.setCellValueFactory(new PropertyValueFactory<>("stage"));
        stageCol.setPrefWidth(100);
        TableColumn<CultureViewModel, String> plantCol = new TableColumn<>("Planted");
        plantCol.setCellValueFactory(new PropertyValueFactory<>("plantingDate"));
        plantCol.setPrefWidth(110);
        TableColumn<CultureViewModel, String> harvestCol = new TableColumn<>("Harvest");
        harvestCol.setCellValueFactory(new PropertyValueFactory<>("harvestDate"));
        harvestCol.setPrefWidth(110);
        culturesTable.getColumns().setAll(zoneCol, nameCol, familyCol, stageCol, plantCol, harvestCol);
        culturesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    }

    @Override
    public void refresh() {
        if (farmService == null) {
            return;
        }
        FamilleCulture filter = familyFilter.getValue();
        List<CultureViewModel> models = new ArrayList<>();
        for (Zone zone : farmService.getZones()) {
            if (!(zone instanceof ZoneCulture)) {
                continue;
            }
            ZoneCulture zc = (ZoneCulture) zone;
            zc.getCultures().forEach(culture -> {
                if (filter != null && culture.getFamille() != filter) {
                    return;
                }
                String phRange = culture.getExigences().getPhMin() + " - " + culture.getExigences().getPhMax();
                String humRange = culture.getExigences().getHumiditeMin() + " - "
                        + culture.getExigences().getHumiditeMax();
                models.add(new CultureViewModel(
                        zone.getCode(),
                        culture.getNom(),
                        culture.getFamille().name(),
                        culture.getStadeActuel().name(),
                        culture.getDatePlantation(),
                        culture.getDateRecoltePrevue(),
                        phRange,
                        humRange));
            });
        }
        culturesTable.setItems(FXCollections.observableArrayList(models));
        if (navigationController != null) {
            navigationController.refreshTopBar();
        }
    }

    private void addCulture() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Add Crop");

        ComboBox<ZoneCulture> zoneBox = new ComboBox<>();
        for (Zone zone : farmService.getZones()) {
            if (zone instanceof ZoneCulture) {
                zoneBox.getItems().add((ZoneCulture) zone);
            }
        }
        zoneBox.setCellFactory(list -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(ZoneCulture item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getCode() + " - " + item.getNom());
            }
        });
        zoneBox.setButtonCell(zoneBox.getCellFactory().call(null));

        TextField nameField = new TextField();
        ComboBox<FamilleCulture> familyBox = new ComboBox<>();
        familyBox.getItems().setAll(FamilleCulture.values());
        DatePicker plantingDate = new DatePicker();
        DatePicker harvestDate = new DatePicker();
        TextField phMinField = new TextField();
        TextField phMaxField = new TextField();
        TextField humMinField = new TextField();
        TextField humMaxField = new TextField();

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.add(new Label("Zone"), 0, 0);
        grid.add(zoneBox, 1, 0);
        grid.add(new Label("Name"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("Family"), 0, 2);
        grid.add(familyBox, 1, 2);
        grid.add(new Label("Planting"), 0, 3);
        grid.add(plantingDate, 1, 3);
        grid.add(new Label("Harvest"), 0, 4);
        grid.add(harvestDate, 1, 4);
        grid.add(new Label("pH min"), 0, 5);
        grid.add(phMinField, 1, 5);
        grid.add(new Label("pH max"), 0, 6);
        grid.add(phMaxField, 1, 6);
        grid.add(new Label("Humidity min"), 0, 7);
        grid.add(humMinField, 1, 7);
        grid.add(new Label("Humidity max"), 0, 8);
        grid.add(humMaxField, 1, 8);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(javafx.scene.control.ButtonType.OK,
                javafx.scene.control.ButtonType.CANCEL);
        var result = dialog.showAndWait();
        if (result.isPresent() && result.isPresent()) {
            try {
                ZoneCulture zone = zoneBox.getValue();
                LocalDate plant = plantingDate.getValue();
                LocalDate harvest = harvestDate.getValue();
                if (zone == null || plant == null || harvest == null) {
                    throw new IllegalArgumentException("Zone and dates are required.");
                }
                double phMin = Double.parseDouble(phMinField.getText().trim());
                double phMax = Double.parseDouble(phMaxField.getText().trim());
                double humMin = Double.parseDouble(humMinField.getText().trim());
                double humMax = Double.parseDouble(humMaxField.getText().trim());
                farmService.addCulture(zone.getCode(), nameField.getText().trim(), familyBox.getValue(),
                        plant.toString(), harvest.toString(), phMin, phMax, humMin, humMax);
                refresh();
            } catch (RuntimeException e) {
                dialogService.showError("Add Crop Failed", e.getMessage());
            }
        }
    }

    private void updateStage() {
        CultureViewModel selected = culturesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            dialogService.showWarning("Selection Required", "Select a crop first.");
            return;
        }
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Update Stage");
        ComboBox<StadeCroissance> stageBox = new ComboBox<>();
        stageBox.getItems().setAll(StadeCroissance.values());
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.add(new Label("Stage"), 0, 0);
        grid.add(stageBox, 1, 0);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(javafx.scene.control.ButtonType.OK,
                javafx.scene.control.ButtonType.CANCEL);
        var result = dialog.showAndWait();
        if (result.isPresent() && result.isPresent()) {
            try {
                ZoneCulture zone = (ZoneCulture) farmService.getZones().stream()
                        .filter(z -> z.getCode().equals(selected.getZoneCode()))
                        .findFirst().orElse(null);
                if (zone == null) {
                    throw new IllegalArgumentException("Zone not found.");
                }
                int index = zone.getCultures().stream()
                        .map(c -> c.getNom())
                        .toList().indexOf(selected.getName());
                if (index < 0) {
                    throw new IllegalArgumentException("Crop not found.");
                }
                farmService.updateCultureStage(zone.getCode(), index, stageBox.getValue());
                refresh();
            } catch (RuntimeException e) {
                dialogService.showError("Update Failed", e.getMessage());
            }
        }
    }

    private void generateReport() {
        StringBuilder report = new StringBuilder();
        report.append("Crop Report\n\n");
        List<CultureViewModel> items = culturesTable.getItems();
        if (items.isEmpty()) {
            report.append("No crops found.");
        } else {
            items.forEach(c -> report.append(c.getZoneCode()).append(" - ")
                    .append(c.getName()).append(" (")
                    .append(c.getFamily()).append(") stage: ")
                    .append(c.getStage()).append("\n"));
        }
        dialogService.showInfo("Culture Report", report.toString());
    }
}
