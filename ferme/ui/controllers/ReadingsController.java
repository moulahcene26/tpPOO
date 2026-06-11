package ferme.ui.controllers;

import ferme.enums.NiveauGravite;
import ferme.models.Capteur;
import ferme.models.CapteurGPS;
import ferme.models.Releve;
import ferme.models.ReleveGPS;
import ferme.models.ReleveNumerique;
import ferme.models.Zone;
import ferme.ui.AppContext;
import ferme.ui.AppContextAware;
import ferme.ui.Refreshable;
import ferme.ui.components.SeverityBadge;
import ferme.ui.navigation.NavigationController;
import ferme.ui.services.DialogService;
import ferme.ui.services.FileImportService;
import ferme.ui.services.UiFarmService;
import ferme.ui.viewmodels.ReadingViewModel;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableCell;
import javafx.scene.Node;

public class ReadingsController implements AppContextAware, Refreshable {

    @FXML private TabPane readingsTabPane;
    @FXML private ComboBox<Capteur> sensorComboNumeric;
    @FXML private TextField valueField;
    @FXML private TextField unitField;
    @FXML private DatePicker datePickerNumeric;
    @FXML private TextField hourFieldNumeric;
    @FXML private Button submitNumericButton;

    @FXML private ComboBox<Capteur> sensorComboGps;
    @FXML private TextField latField;
    @FXML private TextField lonField;
    @FXML private DatePicker datePickerGps;
    @FXML private TextField hourFieldGps;
    @FXML private Button submitGpsButton;

    @FXML private ComboBox<Zone> zoneComboProduction;
    @FXML private TextField productionValueField;
    @FXML private DatePicker productionDatePicker;
    @FXML private Button submitProductionButton;
    @FXML private DatePicker prodQueryStartPicker;
    @FXML private DatePicker prodQueryEndPicker;
    @FXML private Button queryProductionButton;
    @FXML private TextArea productionResultArea;

    @FXML private ComboBox<Capteur> sensorComboHistory;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Button loadHistoryButton;
    @FXML private TableView<ReadingViewModel> historyTable;

    @FXML private ComboBox<Capteur> chartSensorCombo;
    @FXML private ComboBox<Zone> chartZoneCombo;
    @FXML private DatePicker chartStartDatePicker;
    @FXML private DatePicker chartEndDatePicker;
    @FXML private BarChart<String, Number> sensorChart;
    @FXML private LineChart<String, Number> zoneChart;
    @FXML private Button showSensorChartBtn;
    @FXML private Button showZoneChartBtn;

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
        setupHistoryTable();
        setupCharts();
        wireActions();
        wireChartActions();
    }

    private void setupHistoryTable() {
        TableColumn<ReadingViewModel, String> sensorCol = new TableColumn<>("Sensor");
        sensorCol.setCellValueFactory(new PropertyValueFactory<>("sensorCode"));
        TableColumn<ReadingViewModel, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        TableColumn<ReadingViewModel, String> valueCol = new TableColumn<>("Value");
        valueCol.setCellValueFactory(new PropertyValueFactory<>("value"));
        TableColumn<ReadingViewModel, String> unitCol = new TableColumn<>("Unit");
        unitCol.setCellValueFactory(new PropertyValueFactory<>("unit"));
        TableColumn<ReadingViewModel, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        TableColumn<ReadingViewModel, String> severityCol = new TableColumn<>("Severity");
        severityCol.setCellValueFactory(new PropertyValueFactory<>("severity"));
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
        historyTable.getColumns().setAll(sensorCol, typeCol, valueCol, unitCol, dateCol, severityCol);
        historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

    private void wireActions() {
        submitNumericButton.setOnAction(event -> submitNumericReading());
        submitGpsButton.setOnAction(event -> submitGpsReading());
        submitProductionButton.setOnAction(event -> submitProduction());
        queryProductionButton.setOnAction(event -> queryProduction());
        loadHistoryButton.setOnAction(event -> loadHistory());
    }

    private void setupCharts() {
        if (sensorChart != null) {
            sensorChart.setTitle("Evolution par capteur");
            sensorChart.setAnimated(false);
            ((CategoryAxis) sensorChart.getXAxis()).setLabel("Date");
            ((NumberAxis) sensorChart.getYAxis()).setLabel("Valeur");
        }
        if (zoneChart != null) {
            zoneChart.setTitle("Evolution par zone");
            zoneChart.setAnimated(false);
            zoneChart.setCreateSymbols(false);
            ((CategoryAxis) zoneChart.getXAxis()).setLabel("Date");
            ((NumberAxis) zoneChart.getYAxis()).setLabel("Valeur");
        }
    }

    private void wireChartActions() {
        chartSensorCombo.setOnAction(event -> updateSensorChart());
        chartZoneCombo.setOnAction(event -> updateZoneChart());
        chartStartDatePicker.valueProperty().addListener((obs, oldValue, newValue) -> refreshCharts());
        chartEndDatePicker.valueProperty().addListener((obs, oldValue, newValue) -> refreshCharts());
        showSensorChartBtn.setOnAction(e -> {
            sensorChart.setVisible(true);  sensorChart.setManaged(true);
            zoneChart.setVisible(false);   zoneChart.setManaged(false);
            showSensorChartBtn.getStyleClass().setAll("primary-button");
            showZoneChartBtn.getStyleClass().setAll("button");
        });
        showZoneChartBtn.setOnAction(e -> {
            zoneChart.setVisible(true);    zoneChart.setManaged(true);
            sensorChart.setVisible(false); sensorChart.setManaged(false);
            showZoneChartBtn.getStyleClass().setAll("primary-button");
            showSensorChartBtn.getStyleClass().setAll("button");
        });
    }

    @Override
    public void refresh() {
        if (farmService == null) {
            return;
        }
        populateSensorCombos();
        populateZoneCombo();
        populateChartCombos();
        refreshCharts();
        if (navigationController != null) {
            navigationController.refreshTopBar();
        }
    }

    private void autoSelectFirst(ComboBox<?> combo) {
        if (combo.getValue() == null && !combo.getItems().isEmpty()) {
            combo.getSelectionModel().selectFirst();
        }
    }

    private void populateChartCombos() {
        Capteur selectedSensor = chartSensorCombo.getValue();
        Zone selectedZone = chartZoneCombo.getValue();

        chartSensorCombo.getItems().clear();
        chartZoneCombo.getItems().clear();

        for (Capteur c : farmService.getSensors()) {
            chartSensorCombo.getItems().add(c);
        }
        for (Zone z : farmService.getZones()) {
            chartZoneCombo.getItems().add(z);
        }

        formatSensorCombo(chartSensorCombo);
        chartZoneCombo.setCellFactory(list -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Zone item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getCode() + " - " + item.getNom());
            }
        });
        chartZoneCombo.setButtonCell(chartZoneCombo.getCellFactory().call(null));

        if (selectedSensor != null && farmService.getSensors().contains(selectedSensor)) {
            chartSensorCombo.setValue(selectedSensor);
        } else {
            autoSelectFirst(chartSensorCombo);
        }
        if (selectedZone != null && farmService.getZones().contains(selectedZone)) {
            chartZoneCombo.setValue(selectedZone);
        } else {
            autoSelectFirst(chartZoneCombo);
        }
    }

    private void refreshCharts() {
        updateSensorChart();
        updateZoneChart();
    }

    private void colorDataPoint(XYChart.Data<String, Number> data, NiveauGravite niveau) {
        data.nodeProperty().addListener((obs, oldNode, newNode) -> {
            if (newNode != null) {
                String color;
                if (niveau == NiveauGravite.CRITIQUE) {
                    color = "#ef4444";
                } else if (niveau == NiveauGravite.AVERTISSEMENT) {
                    color = "#eab308";
                } else {
                    color = "#22c55e";
                }
                newNode.setStyle("-fx-bar-fill: " + color + "; -fx-background-color: " + color + ";");
            }
        });
    }

    private void updateSensorChart() {
        if (sensorChart == null) {
            return;
        }
        sensorChart.getData().clear();
        Capteur sensor = chartSensorCombo.getValue();
        if (sensor == null) {
            sensorChart.setTitle("Evolution par capteur");
            return;
        }

        LocalDate start = chartStartDatePicker.getValue();
        LocalDate end = chartEndDatePicker.getValue();
        TreeSet<String> categories = new TreeSet<>();

        if (sensor instanceof CapteurGPS) {
            XYChart.Series<String, Number> latitudeSeries = new XYChart.Series<>();
            latitudeSeries.setName(sensor.getCode() + " - Latitude");
            XYChart.Series<String, Number> longitudeSeries = new XYChart.Series<>();
            longitudeSeries.setName(sensor.getCode() + " - Longitude");

            for (int i = 0; i < sensor.getNbReleves(); i++) {
                Releve releve = sensor.getReleve(i);
                if (releve == null || !isInRange(releve.getDate(), start, end) || !releve.estGPS()) {
                    continue;
                }
                categories.add(releve.getDate());
                if (releve.getPosition() != null) {
                    XYChart.Data<String, Number> latData = new XYChart.Data<>(releve.getDate(), releve.getPosition().getLatitude());
                    XYChart.Data<String, Number> lonData = new XYChart.Data<>(releve.getDate(), releve.getPosition().getLongitude());
                    colorDataPoint(latData, releve.getNiveau());
                    colorDataPoint(lonData, releve.getNiveau());
                    latitudeSeries.getData().add(latData);
                    longitudeSeries.getData().add(lonData);
                }
            }

            if (!latitudeSeries.getData().isEmpty()) {
                sensorChart.getData().add(latitudeSeries);
                sensorChart.getData().add(longitudeSeries);
                applyCategories(sensorChart, categories);
                sensorChart.setTitle("Evolution GPS - " + sensor.getCode());
            } else {
                sensorChart.setTitle("Evolution GPS - aucun relevé");
            }
            return;
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(sensor.getCode());
        for (int i = 0; i < sensor.getNbReleves(); i++) {
            Releve releve = sensor.getReleve(i);
            if (releve == null || releve.estGPS() || !isInRange(releve.getDate(), start, end)) {
                continue;
            }
            categories.add(releve.getDate());
            XYChart.Data<String, Number> point = new XYChart.Data<>(releve.getDate(), releve.getValeur());
            colorDataPoint(point, releve.getNiveau());
            series.getData().add(point);
        }

        if (!series.getData().isEmpty()) {
            sensorChart.getData().add(series);
            applyCategories(sensorChart, categories);
            sensorChart.setTitle("Evolution capteur - " + sensor.getCode());
        } else {
            sensorChart.setTitle("Evolution capteur - aucun relevé");
        }
    }

    private void updateZoneChart() {
        if (zoneChart == null) {
            return;
        }
        zoneChart.getData().clear();
        Zone zone = chartZoneCombo.getValue();
        if (zone == null) {
            zoneChart.setTitle("Evolution par zone");
            return;
        }

        LocalDate start = chartStartDatePicker.getValue();
        LocalDate end = chartEndDatePicker.getValue();
        TreeSet<String> categories = new TreeSet<>();

        List<Capteur> sensors = new ArrayList<>(zone.getCapteursAssoc());
        sensors.sort(Comparator.comparing(Capteur::getCode));
        for (Capteur sensor : sensors) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(sensor.getCode());
            for (int i = 0; i < sensor.getNbReleves(); i++) {
                Releve releve = sensor.getReleve(i);
                if (releve == null || releve.estGPS() || !isInRange(releve.getDate(), start, end)) {
                    continue;
                }
                categories.add(releve.getDate());
                XYChart.Data<String, Number> point = new XYChart.Data<>(releve.getDate(), releve.getValeur());
                colorDataPoint(point, releve.getNiveau());
                series.getData().add(point);
            }
            if (!series.getData().isEmpty()) {
                zoneChart.getData().add(series);
            }
        }

        if (!zoneChart.getData().isEmpty()) {
            applyCategories(zoneChart, categories);
            zoneChart.setTitle("Evolution zone - " + zone.getCode());
        } else {
            zoneChart.setTitle("Evolution zone - aucun relevé");
        }
    }

    private boolean isInRange(String readingDate, LocalDate start, LocalDate end) {
        String datePart = readingDate.length() >= 10 ? readingDate.substring(0, 10) : readingDate;
        if (start != null && datePart.compareTo(start.toString()) < 0) {
            return false;
        }
        if (end != null && datePart.compareTo(end.toString()) > 0) {
            return false;
        }
        return true;
    }

    private void applyCategories(XYChart<String, Number> chart, TreeSet<String> categories) {
        ((CategoryAxis) chart.getXAxis()).setCategories(FXCollections.observableArrayList(categories));
    }

    private void populateSensorCombos() {
        sensorComboNumeric.getItems().clear();
        sensorComboGps.getItems().clear();
        sensorComboHistory.getItems().clear();
        for (Capteur c : farmService.getSensors()) {
            if (c instanceof CapteurGPS) {
                sensorComboGps.getItems().add(c);
            } else {
                sensorComboNumeric.getItems().add(c);
            }
            sensorComboHistory.getItems().add(c);
        }
        formatSensorCombo(sensorComboNumeric);
        formatSensorCombo(sensorComboGps);
        formatSensorCombo(sensorComboHistory);
        autoSelectFirst(sensorComboNumeric);
        autoSelectFirst(sensorComboGps);
        autoSelectFirst(sensorComboHistory);
    }

    private void formatSensorCombo(ComboBox<Capteur> combo) {
        combo.setCellFactory(list -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Capteur item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getCode() + " (" + item.getTypeCapteur() + ")");
            }
        });
        combo.setButtonCell(combo.getCellFactory().call(null));
    }

    private void populateZoneCombo() {
        zoneComboProduction.getItems().clear();
        for (Zone z : farmService.getZones()) {
            zoneComboProduction.getItems().add(z);
        }
        zoneComboProduction.setCellFactory(list -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Zone item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getCode() + " - " + item.getNom());
            }
        });
        zoneComboProduction.setButtonCell(zoneComboProduction.getCellFactory().call(null));
        autoSelectFirst(zoneComboProduction);
    }

    private String buildDate(LocalDate date, TextField hourField) {
        if (date == null) {
            throw new IllegalArgumentException("Date is required.");
        }
        int hour = 0;
        String hourText = hourField.getText().trim();
        if (!hourText.isEmpty()) {
            hour = Integer.parseInt(hourText);
        }
        return date + "-" + String.format("%02d", hour) + "h";
    }

    private void submitNumericReading() {
        try {
            Capteur sensor = sensorComboNumeric.getValue();
            if (sensor == null) {
                throw new IllegalArgumentException("Select a sensor.");
            }
            double value = Double.parseDouble(valueField.getText().trim());
            String unit = unitField.getText().trim();
            if (unit.isEmpty()) {
                unit = sensor.getUnite();
            }
            String date = buildDate(datePickerNumeric.getValue(), hourFieldNumeric);
            ReleveNumerique releve = farmService.recordNumericReading(sensor.getCode(), value, unit, date);
            if (releve != null) {
                String msg = "Reading recorded.\nValue: " + value + " " + unit
                        + "\nSeverity: " + releve.getNiveau().name();
                if (releve.getNiveau() != ferme.enums.NiveauGravite.NORMAL) {
                    dialogService.showWarning("Alert Generated", msg);
                } else {
                    dialogService.showInfo("Reading Recorded", msg);
                }
                refresh();
            } else {
                dialogService.showError("Failed", "Could not record reading. Check sensor/zone status.");
            }
        } catch (RuntimeException e) {
            dialogService.showError("Invalid Input", e.getMessage());
        }
    }

    private void submitGpsReading() {
        try {
            Capteur sensor = sensorComboGps.getValue();
            if (sensor == null) {
                throw new IllegalArgumentException("Select a GPS sensor.");
            }
            double lat = Double.parseDouble(latField.getText().trim());
            double lon = Double.parseDouble(lonField.getText().trim());
            String date = buildDate(datePickerGps.getValue(), hourFieldGps);
            ReleveGPS releve = farmService.recordGpsReading(sensor.getCode(), lat, lon, date);
            if (releve != null) {
                String msg = "GPS Reading recorded.\nPosition: " + lat + ", " + lon
                        + "\nSeverity: " + releve.getNiveau().name();
                if (releve.getNiveau() != ferme.enums.NiveauGravite.NORMAL) {
                    dialogService.showWarning("Alert Generated", msg);
                } else {
                    dialogService.showInfo("Reading Recorded", msg);
                }
                refresh();
            } else {
                dialogService.showError("Failed", "Could not record GPS reading. Check sensor/zone status.");
            }
        } catch (RuntimeException e) {
            dialogService.showError("Invalid Input", e.getMessage());
        }
    }

    private void submitProduction() {
        try {
            Zone zone = zoneComboProduction.getValue();
            if (zone == null) {
                throw new IllegalArgumentException("Select a zone.");
            }
            double value = Double.parseDouble(productionValueField.getText().trim());
            LocalDate date = productionDatePicker.getValue();
            if (date == null) {
                throw new IllegalArgumentException("Date is required.");
            }
            farmService.recordProduction(zone.getCode(), value, date.toString());
            dialogService.showInfo("Production Recorded",
                    "Production recorded for zone " + zone.getCode() + ": " + value);
            refresh();
        } catch (RuntimeException e) {
            dialogService.showError("Invalid Input", e.getMessage());
        }
    }

    private void queryProduction() {
        Zone zone = zoneComboProduction.getValue();
        if (zone == null) {
            dialogService.showWarning("Selection Required", "Select a zone.");
            return;
        }
        String start = prodQueryStartPicker.getValue() != null ? prodQueryStartPicker.getValue().toString() : null;
        String end = prodQueryEndPicker.getValue() != null ? prodQueryEndPicker.getValue().toString() : null;
        List<String[]> entries = farmService.getProductionEntries(zone.getCode(), start, end);
        if (entries.isEmpty()) {
            productionResultArea.setText("No production entries found for zone " + zone.getCode() + " in the selected date range.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Production - Zone ").append(zone.getCode()).append("\n\n");
        for (String[] entry : entries) {
            sb.append("  ").append(entry[0]).append(" : ").append(entry[1]).append(" ").append(entry[2]).append("\n");
        }
        productionResultArea.setText(sb.toString());
    }

    private void loadHistory() {
        Capteur sensor = sensorComboHistory.getValue();
        if (sensor == null) {
            dialogService.showWarning("Selection Required", "Select a sensor.");
            return;
        }
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();
        String startStr = start != null ? start.toString() : null;
        String endStr = end != null ? end.toString() : null;

        List<ReadingViewModel> models = new ArrayList<>();
        for (int i = 0; i < sensor.getNbReleves(); i++) {
            Releve r = sensor.getReleve(i);
            if (r == null) {
                continue;
            }
            if (startStr != null && r.getDate().compareTo(startStr) < 0) {
                continue;
            }
            if (endStr != null && r.getDate().compareTo(endStr) > 0) {
                continue;
            }
            String type = r.estGPS() ? "GPS" : "Numeric";
            String value = r.estGPS() && r.getPosition() != null
                    ? r.getPosition().toString()
                    : String.valueOf(r.getValeur());
            models.add(new ReadingViewModel(
                    r.getCodeCapteur(),
                    type,
                    value,
                    r.getUnite(),
                    r.getDate(),
                    r.getNiveau().name()));
        }
        historyTable.setItems(FXCollections.observableArrayList(models));
    }
}
