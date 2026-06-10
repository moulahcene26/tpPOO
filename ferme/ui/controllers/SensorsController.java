package ferme.ui.controllers;

import ferme.enums.StatutCapteur;
import ferme.enums.TypeCapteurBio;
import ferme.enums.TypeCapteurEau;
import ferme.enums.TypeCapteurEnv;
import ferme.enums.TypeCapteurSol;
import ferme.models.Capteur;
import ferme.models.CapteurBiometrique;
import ferme.models.CapteurGPS;
import ferme.models.Releve;
import ferme.models.Zone;
import ferme.ui.AppContext;
import ferme.ui.AppContextAware;
import ferme.ui.Refreshable;
import ferme.ui.navigation.NavigationController;
import ferme.ui.services.DialogService;
import ferme.ui.services.FileImportService;
import ferme.ui.services.UiFarmService;
import ferme.ui.viewmodels.SensorViewModel;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

public class SensorsController implements AppContextAware, Refreshable {

    @FXML private TextField searchField;
    @FXML private TableView<SensorViewModel> sensorsTable;
    @FXML private Button addSensorButton;
    @FXML private Button configureSensorButton;
    @FXML private Button changeStatusButton;
    @FXML private Button importSensorsButton;
    @FXML private Button viewHistoryButton;

    private UiFarmService farmService;
    private DialogService dialogService;
    private FileImportService fileImportService;
    private NavigationController navigationController;

    @Override
    public void setContext(AppContext context, UiFarmService farmService,
            DialogService dialogService, FileImportService fileImportService,
            NavigationController navigationController) {
        this.farmService = farmService;
        this.dialogService = dialogService;
        this.fileImportService = fileImportService;
        this.navigationController = navigationController;
    }

    @FXML
    private void initialize() {
        setupTable();
        wireActions();
        searchField.textProperty().addListener((obs, o, n) -> refresh());
    }

    private void setupTable() {
        TableColumn<SensorViewModel, String> codeCol = new TableColumn<>("Code");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("code"));
        codeCol.setPrefWidth(80);

        TableColumn<SensorViewModel, String> familyCol = new TableColumn<>("Family");
        familyCol.setCellValueFactory(new PropertyValueFactory<>("family"));
        familyCol.setPrefWidth(70);

        TableColumn<SensorViewModel, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeCol.setPrefWidth(160);

        TableColumn<SensorViewModel, String> zoneCol = new TableColumn<>("Zone");
        zoneCol.setCellValueFactory(new PropertyValueFactory<>("zoneCode"));
        zoneCol.setPrefWidth(75);

        TableColumn<SensorViewModel, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(90);

        TableColumn<SensorViewModel, Double> minCol = new TableColumn<>("Min");
        minCol.setCellValueFactory(new PropertyValueFactory<>("min"));
        minCol.setPrefWidth(65);

        TableColumn<SensorViewModel, Double> maxCol = new TableColumn<>("Max");
        maxCol.setCellValueFactory(new PropertyValueFactory<>("max"));
        maxCol.setPrefWidth(65);

        TableColumn<SensorViewModel, String> readingCol = new TableColumn<>("Latest Reading");
        readingCol.setCellValueFactory(new PropertyValueFactory<>("latestReading"));
        readingCol.setPrefWidth(200);

        TableColumn<SensorViewModel, String> severityCol = new TableColumn<>("Severity");
        severityCol.setCellValueFactory(new PropertyValueFactory<>("latestSeverity"));
        severityCol.setPrefWidth(90);

        sensorsTable.getColumns().setAll(codeCol, familyCol, typeCol, zoneCol, statusCol, minCol, maxCol,
                readingCol, severityCol);
        sensorsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    }

    private void wireActions() {
        addSensorButton.setOnAction(event -> showAddSensorDialog());
        configureSensorButton.setOnAction(event -> configureSensor());
        changeStatusButton.setOnAction(event -> changeStatus());
        importSensorsButton.setOnAction(event -> importSensors());
        viewHistoryButton.setOnAction(event -> viewHistory());
    }

    @Override
    public void refresh() {
        if (farmService == null) {
            return;
        }
        String filter = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        List<SensorViewModel> models = new ArrayList<>();
        for (Capteur capteur : farmService.getSensors()) {
            if (!filter.isEmpty()) {
                String hay = (capteur.getCode() + " " + capteur.getTypeCapteur() + " " + capteur.getCodeZone())
                        .toLowerCase();
                if (!hay.contains(filter)) {
                    continue;
                }
            }
            String latestReading = "";
            String latestSeverity = "";
            if (capteur.getNbReleves() > 0) {
                Releve last = capteur.getReleve(capteur.getNbReleves() - 1);
                if (last != null) {
                    latestReading = last.toString();
                    latestSeverity = last.getNiveau().name();
                }
            }
            models.add(new SensorViewModel(
                    capteur.getCode(),
                    getFamily(capteur),
                    capteur.getTypeCapteur(),
                    capteur.getCodeZone(),
                    capteur.getStatut().name(),
                    capteur.getSeuilMin(),
                    capteur.getSeuilMax(),
                    latestReading,
                    latestSeverity));
        }
        sensorsTable.setItems(FXCollections.observableArrayList(models));
        if (navigationController != null) {
            navigationController.refreshTopBar();
        }
    }

    private String getFamily(Capteur capteur) {
        if (capteur instanceof CapteurGPS) {
            return "GPS";
        }
        if (capteur instanceof CapteurBiometrique) {
            return "Bio";
        }
        if (capteur instanceof ferme.models.CapteurEau) {
            return "Water";
        }
        if (capteur instanceof ferme.models.CapteurSol) {
            return "Soil";
        }
        return "Env";
    }

    private SensorViewModel getSelectedSensor() {
        SensorViewModel selected = sensorsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            dialogService.showWarning("Selection Required", "Select a sensor first.");
        }
        return selected;
    }

    private void showAddSensorDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Add Sensor");

        ComboBox<String> familyBox = new ComboBox<>();
        familyBox.getItems().setAll("Environmental", "Soil", "Biometric", "GPS", "Water");
        familyBox.setPromptText("Select family");

        TextField codeField = new TextField();

        ComboBox<Zone> zoneBox = new ComboBox<>();
        for (Zone z : farmService.getZones()) {
            zoneBox.getItems().add(z);
        }
        zoneBox.setCellFactory(list -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Zone item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getCode() + " - " + item.getNom());
            }
        });
        zoneBox.setButtonCell(zoneBox.getCellFactory().call(null));

        // Dynamic fields
        ComboBox<TypeCapteurEnv> envTypeBox = new ComboBox<>();
        envTypeBox.getItems().setAll(TypeCapteurEnv.values());

        ComboBox<TypeCapteurSol> soilTypeBox = new ComboBox<>();
        soilTypeBox.getItems().setAll(TypeCapteurSol.values());

        ComboBox<TypeCapteurBio> bioTypeBox = new ComboBox<>();
        bioTypeBox.getItems().setAll(TypeCapteurBio.values());

        ComboBox<TypeCapteurEau> waterTypeBox = new ComboBox<>();
        waterTypeBox.getItems().setAll(TypeCapteurEau.values());

        TextField minField = new TextField();
        TextField maxField = new TextField();
        TextField animalField = new TextField();
        TextField latField = new TextField();
        TextField lonField = new TextField();
        TextField radiusField = new TextField();

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.add(new Label("Family"), 0, 0);
        grid.add(familyBox, 1, 0);
        grid.add(new Label("Code"), 0, 1);
        grid.add(codeField, 1, 1);
        grid.add(new Label("Zone"), 0, 2);
        grid.add(zoneBox, 1, 2);

        int row = 3;
        Label typeLabel = new Label("Type");
        grid.add(typeLabel, 0, row);
        grid.add(envTypeBox, 1, row);
        int typeRow = row;
        row++;
        Label minLabel = new Label("Min");
        grid.add(minLabel, 0, row);
        grid.add(minField, 1, row);
        row++;
        Label maxLabel = new Label("Max");
        grid.add(maxLabel, 0, row);
        grid.add(maxField, 1, row);
        row++;
        Label animalLabel = new Label("Animal #");
        grid.add(animalLabel, 0, row);
        grid.add(animalField, 1, row);
        int animalRow = row;
        row++;
        Label latLabel = new Label("Latitude");
        grid.add(latLabel, 0, row);
        grid.add(latField, 1, row);
        int latRow = row;
        row++;
        Label lonLabel = new Label("Longitude");
        grid.add(lonLabel, 0, row);
        grid.add(lonField, 1, row);
        int lonRow = row;
        row++;
        Label radiusLabel = new Label("Radius (m)");
        grid.add(radiusLabel, 0, row);
        grid.add(radiusField, 1, row);
        int radiusRow = row;

        // Default: hide GPS-specific, show type+min+max
        setVisible(grid, latRow, false);
        setVisible(grid, lonRow, false);
        setVisible(grid, radiusRow, false);

        familyBox.valueProperty().addListener((obs, o, n) -> {
            boolean isGps = "GPS".equals(n);
            boolean isBio = "Biometric".equals(n);
            boolean hasType = n != null && !isGps;

            setVisible(grid, typeRow, hasType);
            setVisible(grid, minLabel, !isGps);
            setVisible(grid, minField, !isGps);
            setVisible(grid, maxLabel, !isGps);
            setVisible(grid, maxField, !isGps);
            setVisible(grid, animalRow, isGps || isBio);
            setVisible(grid, latRow, isGps);
            setVisible(grid, lonRow, isGps);
            setVisible(grid, radiusRow, isGps);

            if (isGps) {
                typeLabel.setText("GPS sensor - no type needed");
                envTypeBox.setVisible(false);
                soilTypeBox.setVisible(false);
                bioTypeBox.setVisible(false);
                waterTypeBox.setVisible(false);
            } else if (isBio) {
                typeLabel.setText("Bio Type");
                envTypeBox.setVisible(false);
                soilTypeBox.setVisible(false);
                bioTypeBox.setVisible(true);
                waterTypeBox.setVisible(false);
            } else if ("Soil".equals(n)) {
                typeLabel.setText("Soil Type");
                envTypeBox.setVisible(false);
                soilTypeBox.setVisible(true);
                bioTypeBox.setVisible(false);
                waterTypeBox.setVisible(false);
            } else if ("Water".equals(n)) {
                typeLabel.setText("Water Type");
                envTypeBox.setVisible(false);
                soilTypeBox.setVisible(false);
                bioTypeBox.setVisible(false);
                waterTypeBox.setVisible(true);
            } else {
                typeLabel.setText("Env Type");
                envTypeBox.setVisible(true);
                soilTypeBox.setVisible(false);
                bioTypeBox.setVisible(false);
                waterTypeBox.setVisible(false);
            }
        });

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(javafx.scene.control.ButtonType.OK, javafx.scene.control.ButtonType.CANCEL);
        var result = dialog.showAndWait();
        if (result.isPresent() && result.isPresent()) {
            try {
                String family = familyBox.getValue();
                String code = codeField.getText().trim();
                Zone zone = zoneBox.getValue();
                if (family == null || code.isEmpty() || zone == null) {
                    throw new IllegalArgumentException("Family, code, and zone are required.");
                }
                String zoneCode = zone.getCode();
                switch (family) {
                    case "Environmental":
                        farmService.addEnvSensor(code, zoneCode, envTypeBox.getValue(),
                                parseDouble(minField), parseDouble(maxField));
                        break;
                    case "Soil":
                        farmService.addSoilSensor(code, zoneCode, soilTypeBox.getValue(),
                                parseDouble(minField), parseDouble(maxField));
                        break;
                    case "Biometric":
                        farmService.addBioSensor(code, zoneCode, bioTypeBox.getValue(),
                                parseDouble(minField), parseDouble(maxField),
                                Integer.parseInt(animalField.getText().trim()));
                        break;
                    case "GPS":
                        farmService.addGpsSensor(code, zoneCode,
                                Integer.parseInt(animalField.getText().trim()),
                                parseDouble(latField), parseDouble(lonField),
                                parseDouble(radiusField));
                        break;
                    case "Water":
                        farmService.addWaterSensor(code, zoneCode, waterTypeBox.getValue(),
                                parseDouble(minField), parseDouble(maxField));
                        break;
                }
                refresh();
            } catch (RuntimeException e) {
                dialogService.showError("Add Sensor Failed", e.getMessage());
            }
        }
    }

    private void configureSensor() {
        SensorViewModel selected = getSelectedSensor();
        if (selected == null) {
            return;
        }
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Configure Thresholds - " + selected.getCode());
        TextField minField = new TextField(String.valueOf(selected.getMin()));
        TextField maxField = new TextField(String.valueOf(selected.getMax()));
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.add(new Label("Min"), 0, 0);
        grid.add(minField, 1, 0);
        grid.add(new Label("Max"), 0, 1);
        grid.add(maxField, 1, 1);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(javafx.scene.control.ButtonType.OK, javafx.scene.control.ButtonType.CANCEL);
        var result = dialog.showAndWait();
        if (result.isPresent() && result.isPresent()) {
            try {
                farmService.configureSensor(selected.getCode(),
                        Double.parseDouble(minField.getText().trim()),
                        Double.parseDouble(maxField.getText().trim()));
                refresh();
            } catch (RuntimeException e) {
                dialogService.showError("Configure Failed", e.getMessage());
            }
        }
    }

    private void changeStatus() {
        SensorViewModel selected = getSelectedSensor();
        if (selected == null) {
            return;
        }
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Change Status - " + selected.getCode());
        ComboBox<StatutCapteur> statusBox = new ComboBox<>();
        statusBox.getItems().setAll(StatutCapteur.values());
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.add(new Label("Status"), 0, 0);
        grid.add(statusBox, 1, 0);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(javafx.scene.control.ButtonType.OK, javafx.scene.control.ButtonType.CANCEL);
        var result = dialog.showAndWait();
        if (result.isPresent() && result.isPresent() && statusBox.getValue() != null) {
            farmService.changeSensorStatus(selected.getCode(), statusBox.getValue());
            refresh();
        }
    }

    private void importSensors() {
        File file = fileImportService.pickFile("Select sensor file");
        if (file != null) {
            int count = farmService.importSensors(file);
            dialogService.showInfo("Import Complete", count + " sensors loaded.");
            refresh();
        }
    }

    private void viewHistory() {
        SensorViewModel selected = getSelectedSensor();
        if (selected == null) {
            return;
        }
        Capteur capteur = null;
        for (Capteur c : farmService.getSensors()) {
            if (c.getCode().equals(selected.getCode())) {
                capteur = c;
                break;
            }
        }
        if (capteur == null) {
            return;
        }
        StringBuilder builder = new StringBuilder();
        builder.append("Reading history - Sensor ").append(capteur.getCode()).append("\n\n");
        for (int i = 0; i < capteur.getNbReleves(); i++) {
            Releve r = capteur.getReleve(i);
            if (r != null) {
                builder.append(r).append("\n");
            }
        }
        if (capteur.getNbReleves() == 0) {
            builder.append("No readings recorded.");
        }
        dialogService.showInfo("Sensor History", builder.toString());
    }

    private double parseDouble(TextField field) {
        return Double.parseDouble(field.getText().trim());
    }

    private void setVisible(GridPane grid, int row, boolean visible) {
        for (javafx.scene.Node node : grid.getChildren()) {
            Integer nodeRow = GridPane.getRowIndex(node);
            if (nodeRow != null && nodeRow == row) {
                node.setVisible(visible);
                node.setManaged(visible);
            }
        }
    }

    private void setVisible(GridPane grid, Label label, boolean visible) {
        label.setVisible(visible);
        label.setManaged(visible);
    }

    private void setVisible(GridPane grid, TextField field, boolean visible) {
        field.setVisible(visible);
        field.setManaged(visible);
    }
}
