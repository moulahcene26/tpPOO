package ferme.ui.controllers;

import ferme.enums.NiveauGravite;
import ferme.models.Alerte;
import ferme.ui.AppContext;
import ferme.ui.AppContextAware;
import ferme.ui.Refreshable;
import ferme.ui.components.StatCard;
import ferme.ui.navigation.NavigationController;
import ferme.ui.services.DialogService;
import ferme.ui.services.FileImportService;
import ferme.ui.services.UiFarmService;
import ferme.ui.viewmodels.AlertViewModel;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;

public class AlertsController implements AppContextAware, Refreshable {

    @FXML private FlowPane alertStatsContainer;
    @FXML private ComboBox<String> severityFilter;
    @FXML private ComboBox<String> zoneFilter;
    @FXML private CheckBox activeOnlyCheck;
    @FXML private Button sortButton;
    @FXML private Button ackButton;
    @FXML private Button deleteButton;
    @FXML private TableView<AlertViewModel> alertsTable;

    private UiFarmService farmService;
    private DialogService dialogService;
    private NavigationController navigationController;
    private final Map<String, StatCard> alertStatCards = new LinkedHashMap<>();

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
        setupAlertStats();
        setupTable();
        setupFilters();
        wireActions();
    }

    private void setupAlertStats() {
        alertStatCards.put("total", new StatCard("⊟", "Total", "0", "#6366f1"));
        alertStatCards.put("active", new StatCard("⚡", "Active", "0", "#f59e0b"));
        alertStatCards.put("critical", new StatCard("✕", "Critical", "0", "#ef4444"));
        alertStatCards.put("acked", new StatCard("✓", "Acked", "0", "#00e68a"));
        alertStatsContainer.getChildren().setAll(alertStatCards.values());
    }

    private void setupTable() {
        TableColumn<AlertViewModel, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);
        idCol.setMaxWidth(60);

        TableColumn<AlertViewModel, String> severityCol = new TableColumn<>("Severity");
        severityCol.setCellValueFactory(new PropertyValueFactory<>("severity"));
        severityCol.setPrefWidth(120);

        TableColumn<AlertViewModel, String> zoneCol = new TableColumn<>("Zone");
        zoneCol.setCellValueFactory(new PropertyValueFactory<>("zoneCode"));
        zoneCol.setPrefWidth(85);

        TableColumn<AlertViewModel, String> sensorCol = new TableColumn<>("Sensor");
        sensorCol.setCellValueFactory(new PropertyValueFactory<>("sensorCode"));
        sensorCol.setPrefWidth(110);

        TableColumn<AlertViewModel, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateCol.setPrefWidth(165);

        TableColumn<AlertViewModel, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("statusText"));
        statusCol.setPrefWidth(115);

        TableColumn<AlertViewModel, String> summaryCol = new TableColumn<>("Details");
        summaryCol.setCellValueFactory(new PropertyValueFactory<>("summary"));
        summaryCol.setPrefWidth(280);

        alertsTable.getColumns().setAll(idCol, severityCol, zoneCol, sensorCol, dateCol, statusCol, summaryCol);
        alertsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    }

    private void setupFilters() {
        severityFilter.getItems().setAll("", "CRITIQUE", "AVERTISSEMENT", "NORMAL");
        severityFilter.setValue("");
        severityFilter.valueProperty().addListener((obs, o, n) -> refresh());
        zoneFilter.valueProperty().addListener((obs, o, n) -> refresh());
        activeOnlyCheck.selectedProperty().addListener((obs, o, n) -> refresh());
    }

    private void wireActions() {
        sortButton.setOnAction(event ->
            alertsTable.getItems().sort((a, b) -> b.getSeverity().compareTo(a.getSeverity())));
        ackButton.setOnAction(event -> acknowledgeAlert());
        deleteButton.setOnAction(event -> deleteAlert());
    }

    @Override
    public void refresh() {
        if (farmService == null) return;

        zoneFilter.getItems().setAll(getZoneCodesWithAlerts());
        updateAlertStats();

        NiveauGravite niveau = parseSeverity(severityFilter.getValue());
        String zone = zoneFilter.getValue();
        Boolean active = activeOnlyCheck.isSelected() ? true : null;

        List<Alerte> filtered = farmService.filterAlerts(zone, niveau, active);
        List<AlertViewModel> models = new ArrayList<>();
        for (Alerte alert : filtered) {
            String sensor = alert.getReleve() != null ? alert.getReleve().getCodeCapteur() : "";
            models.add(new AlertViewModel(
                    alert.getId(), alert.getNiveau().name(), alert.getCodeZone(),
                    sensor, alert.getDateCreation(), alert.estAcquittee(), alert.toString()));
        }
        alertsTable.setItems(FXCollections.observableArrayList(models));
        if (navigationController != null) navigationController.refreshTopBar();
    }

    private void updateAlertStats() {
        List<Alerte> all = farmService.getAlerts();
        int total = all.size(), active = 0, critical = 0, acked = 0;
        for (Alerte a : all) {
            if (!a.estAcquittee()) active++; else acked++;
            if (a.getNiveau() == NiveauGravite.CRITIQUE) critical++;
        }
        alertStatCards.get("total").setValue(String.valueOf(total));
        alertStatCards.get("active").setValue(String.valueOf(active));
        alertStatCards.get("critical").setValue(String.valueOf(critical));
        alertStatCards.get("acked").setValue(String.valueOf(acked));
    }

    private List<String> getZoneCodesWithAlerts() {
        return farmService.getAlerts().stream()
                .map(Alerte::getCodeZone).distinct().sorted().collect(Collectors.toList());
    }

    private NiveauGravite parseSeverity(String text) {
        if (text == null || text.isEmpty()) return null;
        try { return NiveauGravite.valueOf(text); }
        catch (IllegalArgumentException e) { return null; }
    }

    private void acknowledgeAlert() {
        AlertViewModel selected = alertsTable.getSelectionModel().getSelectedItem();
        if (selected == null) { dialogService.showWarning("Selection Required", "Select an alert."); return; }
        if (selected.isAcknowledged()) { dialogService.showInfo("Already Acknowledged", "Alert #" + selected.getId() + " already acknowledged."); return; }
        if (dialogService.confirm("Acknowledge", "Acknowledge alert #" + selected.getId() + "?")) {
            farmService.acknowledgeAlert(selected.getId());
            refresh();
        }
    }

    private void deleteAlert() {
        AlertViewModel selected = alertsTable.getSelectionModel().getSelectedItem();
        if (selected == null) { dialogService.showWarning("Selection Required", "Select an alert."); return; }
        if (dialogService.confirm("Delete", "Delete alert #" + selected.getId() + "?")) {
            farmService.deleteAlert(selected.getId());
            refresh();
        }
    }
}
