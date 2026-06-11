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
import ferme.ui.components.SeverityBadge;
import ferme.ui.navigation.NavigationController;
import ferme.ui.services.DialogService;
import ferme.ui.services.FileImportService;
import ferme.ui.services.UiFarmService;
import ferme.ui.viewmodels.SensorViewModel;
import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class SensorsController implements AppContextAware, Refreshable {

    @FXML private TextField searchField;
    @FXML private TableView<SensorViewModel> sensorsTable;
    @FXML private Button addSensorButton;
    @FXML private Button configureSensorButton;
    @FXML private Button changeStatusButton;
    @FXML private Button suspendSensorButton;
    @FXML private Button repairSensorButton;
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
        severityCol.setCellFactory(column -> new TableCell<>() {
            private final SeverityBadge badge = new SeverityBadge();

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isBlank()) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                badge.setSeverity(item);
                setGraphic(badge);
                setText(null);
            }
        });

        sensorsTable.getColumns().setAll(codeCol, familyCol, typeCol, zoneCol, statusCol, minCol, maxCol,
                readingCol, severityCol);
        sensorsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    }

    private void wireActions() {
        addSensorButton.setOnAction(event -> showAddSensorDialog());
        configureSensorButton.setOnAction(event -> configureSensor());
        changeStatusButton.setOnAction(event -> changeStatus());
        suspendSensorButton.setOnAction(event -> suspendSensor());
        repairSensorButton.setOnAction(event -> repairSensor());
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
        ComboBox<Object> typeBox = new ComboBox<>();
        typeBox.setPromptText("Select type");

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
        grid.add(typeBox, 1, row);
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
            } else {
                populateTypeBox(typeBox, n);
                typeLabel.setText("Type");
            }
        });
        familyBox.setValue("Environmental");

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
                        farmService.addEnvSensor(code, zoneCode, (TypeCapteurEnv) typeBox.getValue(),
                                parseDouble(minField), parseDouble(maxField));
                        break;
                    case "Soil":
                        farmService.addSoilSensor(code, zoneCode, (TypeCapteurSol) typeBox.getValue(),
                                parseDouble(minField), parseDouble(maxField));
                        break;
                    case "Biometric":
                        farmService.addBioSensor(code, zoneCode, (TypeCapteurBio) typeBox.getValue(),
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
                        farmService.addWaterSensor(code, zoneCode, (TypeCapteurEau) typeBox.getValue(),
                                parseDouble(minField), parseDouble(maxField));
                        break;
                    default:
                        throw new IllegalArgumentException("Select a valid sensor family.");
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

    private void suspendSensor() {
        SensorViewModel selected = getSelectedSensor();
        if (selected == null) {
            return;
        }
        if (dialogService.confirm("Suspend Sensor", "Suspend sensor " + selected.getCode() + "?")) {
            farmService.changeSensorStatus(selected.getCode(), StatutCapteur.SUSPENDU);
            refresh();
        }
    }

    private void repairSensor() {
        SensorViewModel selected = getSelectedSensor();
        if (selected == null) {
            return;
        }
        if (dialogService.confirm("Repair Sensor", "Set sensor " + selected.getCode() + " back to active?")) {
            farmService.changeSensorStatus(selected.getCode(), StatutCapteur.ACTIF);
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
        Capteur capteur = findCapteur(selected.getCode());
        if (capteur == null) {
            return;
        }

        DatePicker startPicker = new DatePicker();
        DatePicker endPicker = new DatePicker();
        startPicker.setPromptText("Start date");
        endPicker.setPromptText("End date");

        GridPane filterGrid = new GridPane();
        filterGrid.setHgap(12);
        filterGrid.setVgap(10);
        filterGrid.add(new Label("Start date"), 0, 0);
        filterGrid.add(startPicker, 1, 0);
        filterGrid.add(new Label("End date"), 0, 1);
        filterGrid.add(endPicker, 1, 1);

        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setWrapText(false);
        textArea.setPrefSize(600, 360);

        Runnable loadHistory = () -> {
            StringBuilder builder = new StringBuilder();
            builder.append("Reading history - Sensor ").append(capteur.getCode()).append("\n\n");
            int count = 0;
            for (int i = 0; i < capteur.getNbReleves(); i++) {
                Releve r = capteur.getReleve(i);
                if (r == null) continue;
                if (startPicker.getValue() != null && r.getDate().compareTo(startPicker.getValue().toString()) < 0) continue;
                if (endPicker.getValue() != null && r.getDate().compareTo(endPicker.getValue().toString()) > 0) continue;
                builder.append("  ").append(r).append("\n");
                count++;
            }
            if (count == 0) {
                builder.append("No readings in this date range.");
            }
            textArea.setText(builder.toString());
        };
        loadHistory.run();

        startPicker.valueProperty().addListener((obs, o, n) -> loadHistory.run());
        endPicker.valueProperty().addListener((obs, o, n) -> loadHistory.run());

        ScrollPane scrollPane = new ScrollPane(textArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        VBox vbox = new VBox(12, filterGrid, scrollPane);
        vbox.setPrefSize(640, 480);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Sensor History - " + capteur.getCode());
        DialogPane pane = dialog.getDialogPane();
        pane.setContent(vbox);
        pane.getButtonTypes().addAll(ButtonType.CLOSE);
        pane.setPrefSize(660, 520);
        dialog.showAndWait();
    }

    private void showScrollableDialog(String title, String content) {
        TextArea textArea = new TextArea(content);
        textArea.setEditable(false);
        textArea.setWrapText(false);
        textArea.setPrefSize(600, 400);
        ScrollPane scrollPane = new ScrollPane(textArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        VBox vbox = new VBox(12, scrollPane);
        vbox.setPrefSize(640, 440);
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        DialogPane pane = dialog.getDialogPane();
        pane.setContent(vbox);
        pane.getButtonTypes().addAll(ButtonType.CLOSE);
        pane.setPrefSize(660, 480);
        dialog.showAndWait();
    }

    private Capteur findCapteur(String code) {
        for (Capteur c : farmService.getSensors()) {
            if (c.getCode().equals(code)) return c;
        }
        return null;
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

    private void populateTypeBox(ComboBox<Object> typeBox, String family) {
        typeBox.getItems().clear();
        if ("Environmental".equals(family)) {
            typeBox.getItems().addAll((Object[]) TypeCapteurEnv.values());
            typeBox.setValue(TypeCapteurEnv.TEMPERATURE);
        } else if ("Soil".equals(family)) {
            typeBox.getItems().addAll((Object[]) TypeCapteurSol.values());
            typeBox.setValue(TypeCapteurSol.PH);
        } else if ("Biometric".equals(family)) {
            typeBox.getItems().addAll((Object[]) TypeCapteurBio.values());
            typeBox.setValue(TypeCapteurBio.TEMPERATURE_CORPORELLE);
        } else if ("Water".equals(family)) {
            typeBox.getItems().addAll((Object[]) TypeCapteurEau.values());
            typeBox.setValue(TypeCapteurEau.TEMPERATURE_EAU);
        } else {
            typeBox.setValue(null);
        }
    }
}
