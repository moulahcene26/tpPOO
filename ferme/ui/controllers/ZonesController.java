package ferme.ui.controllers;

import ferme.enums.TypeElevage;
import ferme.models.Culture;
import ferme.models.EspeceAquacole;
import ferme.models.HistoriqueProduction;
import ferme.models.Zone;
import ferme.models.ZoneAquacole;
import ferme.models.ZoneCulture;
import ferme.models.ZoneElevage;
import ferme.ui.AppContext;
import ferme.ui.AppContextAware;
import ferme.ui.Refreshable;
import ferme.ui.navigation.NavigationController;
import ferme.ui.services.DialogService;
import ferme.ui.services.FileImportService;
import ferme.ui.services.UiFarmService;
import ferme.ui.viewmodels.ZoneViewModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class ZonesController implements AppContextAware, Refreshable {

    @FXML
    private TextField searchField;
    @FXML
    private TableView<ZoneViewModel> zonesTable;
    @FXML
    private Button addCultureZoneButton;
    @FXML
    private Button addLivestockZoneButton;
    @FXML
    private Button addAquacultureZoneButton;
    @FXML
    private Button renameZoneButton;
    @FXML
    private Button suspendZoneButton;
    @FXML
    private Button activateZoneButton;
    @FXML
    private Button detailsZoneButton;

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
        wireActions();
        searchField.textProperty().addListener((obs, oldVal, newVal) -> refresh());
    }

    private void setupTable() {
        TableColumn<ZoneViewModel, String> codeCol = new TableColumn<>("Code");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("code"));
        codeCol.setPrefWidth(75);
        TableColumn<ZoneViewModel, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(160);
        TableColumn<ZoneViewModel, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeCol.setPrefWidth(130);
        TableColumn<ZoneViewModel, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(105);
        TableColumn<ZoneViewModel, Integer> sensorsCol = new TableColumn<>("Sensors");
        sensorsCol.setCellValueFactory(new PropertyValueFactory<>("sensorCount"));
        sensorsCol.setPrefWidth(80);
        TableColumn<ZoneViewModel, Double> avgCol = new TableColumn<>("Production Avg");
        avgCol.setCellValueFactory(new PropertyValueFactory<>("productionAverage"));
        avgCol.setPrefWidth(120);
        zonesTable.getColumns().setAll(codeCol, nameCol, typeCol, statusCol, sensorsCol, avgCol);
        zonesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    }

    private void wireActions() {
        addCultureZoneButton.setOnAction(event -> addCultureZone());
        addLivestockZoneButton.setOnAction(event -> addLivestockZone());
        addAquacultureZoneButton.setOnAction(event -> addAquacultureZone());
        renameZoneButton.setOnAction(event -> renameZone());
        suspendZoneButton.setOnAction(event -> suspendZone());
        activateZoneButton.setOnAction(event -> activateZone());
        detailsZoneButton.setOnAction(event -> showDetails());
    }

    @Override
    public void refresh() {
        if (farmService == null) {
            return;
        }
        String filter = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        List<ZoneViewModel> viewModels = new ArrayList<>();
        for (Zone zone : farmService.getZones()) {
            if (!filter.isEmpty()) {
                String hay = (zone.getCode() + " " + zone.getNom() + " " + zone.getTypeZone()).toLowerCase();
                if (!hay.contains(filter)) {
                    continue;
                }
            }
            viewModels.add(new ZoneViewModel(
                    zone.getCode(),
                    zone.getNom(),
                    zone.getTypeZone(),
                    zone.getStatut().name(),
                    zone.getCapteursAssoc().size(),
                    zone.getHistoriqueProduction().moyenneProduction()));
        }
        zonesTable.setItems(FXCollections.observableArrayList(viewModels));
        if (navigationController != null) {
            navigationController.refreshTopBar();
        }
    }

    private Zone getSelectedZone() {
        ZoneViewModel selected = zonesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            dialogService.showWarning("Selection Required", "Select a zone first.");
            return null;
        }
        for (Zone zone : farmService.getZones()) {
            if (zone.getCode().equals(selected.getCode())) {
                return zone;
            }
        }
        return null;
    }

    private void addCultureZone() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Add Culture Zone");
        TextField codeField = new TextField();
        TextField nameField = new TextField();
        GridPane grid = buildGrid();
        grid.add(new Label("Code"), 0, 0);
        grid.add(codeField, 1, 0);
        grid.add(new Label("Name"), 0, 1);
        grid.add(nameField, 1, 1);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(javafx.scene.control.ButtonType.OK,
                javafx.scene.control.ButtonType.CANCEL);
        var result = dialog.showAndWait();
        if (result.isPresent() && result.isPresent()) {
            try {
                if (farmService.addCultureZone(codeField.getText().trim(), nameField.getText().trim())) {
                    refresh();
                }
            } catch (RuntimeException e) {
                dialogService.showError("Add Zone Failed", e.getMessage());
            }
        }
    }

    private void addLivestockZone() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Add Livestock Zone");
        TextField codeField = new TextField();
        TextField nameField = new TextField();
        ComboBox<TypeElevage> typeBox = new ComboBox<>();
        typeBox.getItems().setAll(TypeElevage.values());
        TextField latField = new TextField();
        TextField lonField = new TextField();
        TextField radiusField = new TextField();
        GridPane grid = buildGrid();
        grid.add(new Label("Code"), 0, 0);
        grid.add(codeField, 1, 0);
        grid.add(new Label("Name"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("Type"), 0, 2);
        grid.add(typeBox, 1, 2);
        grid.add(new Label("Latitude"), 0, 3);
        grid.add(latField, 1, 3);
        grid.add(new Label("Longitude"), 0, 4);
        grid.add(lonField, 1, 4);
        grid.add(new Label("Radius (m)"), 0, 5);
        grid.add(radiusField, 1, 5);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(javafx.scene.control.ButtonType.OK,
                javafx.scene.control.ButtonType.CANCEL);
        var result = dialog.showAndWait();
        if (result.isPresent() && result.isPresent()) {
            try {
                double lat = Double.parseDouble(latField.getText().trim());
                double lon = Double.parseDouble(lonField.getText().trim());
                double radius = Double.parseDouble(radiusField.getText().trim());
                if (farmService.addLivestockZone(codeField.getText().trim(), nameField.getText().trim(),
                        typeBox.getValue(), lat, lon, radius)) {
                    refresh();
                }
            } catch (RuntimeException e) {
                dialogService.showError("Add Zone Failed", e.getMessage());
            }
        }
    }

    private void addAquacultureZone() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Add Aquaculture Zone");
        TextField codeField = new TextField();
        TextField nameField = new TextField();
        TextField speciesField = new TextField();
        TextField quantityField = new TextField();
        GridPane grid = buildGrid();
        grid.add(new Label("Code"), 0, 0);
        grid.add(codeField, 1, 0);
        grid.add(new Label("Name"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("Species (optional)"), 0, 2);
        grid.add(speciesField, 1, 2);
        grid.add(new Label("Quantity"), 0, 3);
        grid.add(quantityField, 1, 3);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(javafx.scene.control.ButtonType.OK,
                javafx.scene.control.ButtonType.CANCEL);
        var result = dialog.showAndWait();
        if (result.isPresent() && result.isPresent()) {
            try {
                if (farmService.addAquacultureZone(codeField.getText().trim(), nameField.getText().trim())) {
                    String species = speciesField.getText().trim();
                    if (!species.isEmpty()) {
                        int qty = quantityField.getText().trim().isEmpty() ? 0
                                : Integer.parseInt(quantityField.getText().trim());
                        farmService.addAquacultureSpecies(codeField.getText().trim(), species, qty);
                    }
                    refresh();
                }
            } catch (RuntimeException e) {
                dialogService.showError("Add Zone Failed", e.getMessage());
            }
        }
    }

    private void renameZone() {
        Zone zone = getSelectedZone();
        if (zone == null) {
            return;
        }
        Optional<String> name = dialogService.promptText("Rename Zone", "New name:");
        if (name.isPresent()) {
            if (farmService.renameZone(zone.getCode(), name.get())) {
                refresh();
            }
        }
    }

    private void suspendZone() {
        Zone zone = getSelectedZone();
        if (zone == null) {
            return;
        }
        if (dialogService.confirm("Suspend Zone", "Suspend zone " + zone.getCode() + "?")) {
            if (farmService.suspendZone(zone.getCode())) {
                refresh();
            }
        }
    }

    private void activateZone() {
        Zone zone = getSelectedZone();
        if (zone == null) {
            return;
        }
        if (dialogService.confirm("Activate Zone", "Activate zone " + zone.getCode() + "?")) {
            if (farmService.activateZone(zone.getCode())) {
                refresh();
            }
        }
    }

    private void showDetails() {
        Zone zone = getSelectedZone();
        if (zone == null) {
            return;
        }
        StringBuilder builder = new StringBuilder();
        builder.append("Code: ").append(zone.getCode()).append("\n");
        builder.append("Name: ").append(zone.getNom()).append("\n");
        builder.append("Type: ").append(zone.getTypeZone()).append("\n");
        builder.append("Status: ").append(zone.getStatut().name()).append("\n\n");

        if (zone instanceof ZoneCulture) {
            ZoneCulture zc = (ZoneCulture) zone;
            builder.append("Cultures:\n");
            for (Culture culture : zc.getCultures()) {
                builder.append("- ").append(culture.getNom())
                        .append(" (stage: ").append(culture.getStadeActuel().name()).append(")\n");
            }
        } else if (zone instanceof ZoneElevage) {
            ZoneElevage ze = (ZoneElevage) zone;
            builder.append("Livestock type: ").append(ze.getTypeElevage().name()).append("\n");
            builder.append("Animals: ").append(ze.getAnimaux().size()).append("\n");
        } else if (zone instanceof ZoneAquacole) {
            ZoneAquacole za = (ZoneAquacole) zone;
            builder.append("Species:\n");
            for (EspeceAquacole esp : za.getEspeces()) {
                builder.append("- ").append(esp.getNom()).append(" x").append(esp.getNombreAnimaux()).append("\n");
            }
        }

        builder.append("\nProduction History:\n");
        HistoriqueProduction hist = zone.getHistoriqueProduction();
        for (int i = 0; i < hist.getNbEntrees(); i++) {
            builder.append("- ").append(hist.getDate(i)).append(": ")
                    .append(hist.getValeur(i)).append(" ").append(hist.getUnite()).append("\n");
        }
        TextArea textArea = new TextArea(builder.toString());
        textArea.setEditable(false);
        textArea.setWrapText(false);
        textArea.setPrefSize(540, 400);
        ScrollPane scrollPane = new ScrollPane(textArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        VBox vbox = new VBox(12, scrollPane);
        vbox.setPrefSize(580, 440);
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Zone Details");
        DialogPane pane = dialog.getDialogPane();
        pane.setContent(vbox);
        pane.getButtonTypes().addAll(ButtonType.CLOSE);
        pane.setPrefSize(600, 480);
        dialog.showAndWait();
    }

    private GridPane buildGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        return grid;
    }
}
