package ferme.ui.controllers;

import ferme.models.Alerte;
import ferme.models.Zone;
import ferme.ui.AppContext;
import ferme.ui.AppContextAware;
import ferme.ui.Refreshable;
import ferme.ui.components.SeverityBadge;
import ferme.ui.components.StatCard;
import ferme.ui.navigation.NavigationController;
import ferme.ui.services.DialogService;
import ferme.ui.services.FileImportService;
import ferme.ui.services.UiFarmService;
import ferme.ui.viewmodels.AlertViewModel;
import ferme.ui.viewmodels.ZoneViewModel;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;

public class DashboardController implements AppContextAware, Refreshable {

    @FXML private Label heroFarmName;
    @FXML private Label heroSubtitle;
    @FXML private Label pillZones;
    @FXML private Label pillSensors;
    @FXML private Label pillAlerts;
    @FXML private FlowPane statsContainer;
    @FXML private TableView<AlertViewModel> alertsTable;
    @FXML private TableView<ZoneViewModel> zonesTable;
    @FXML private HBox heroCard;

    private UiFarmService farmService;
    private NavigationController navigationController;
    private final Map<String, StatCard> statCards = new LinkedHashMap<>();
    private boolean tablesReady = false;

    @Override
    public void setContext(AppContext context, UiFarmService farmService,
            DialogService dialogService, FileImportService fileImportService,
            NavigationController navigationController) {
        this.farmService = farmService;
        this.navigationController = navigationController;
    }

    @FXML
    private void initialize() {
        setupTables();
        setupStatCards();
    }

    private void setupTables() {
        if (tablesReady) return;
        tablesReady = true;

        TableColumn<AlertViewModel, Integer> alertId = new TableColumn<>("ID");
        alertId.setCellValueFactory(new PropertyValueFactory<>("id"));
        alertId.setPrefWidth(45);

        TableColumn<AlertViewModel, String> alertSeverity = new TableColumn<>("SEVERITY");
        alertSeverity.setCellValueFactory(new PropertyValueFactory<>("severity"));
        alertSeverity.setPrefWidth(105);
        alertSeverity.setCellFactory(column -> new TableCell<>() {
            private final SeverityBadge badge = new SeverityBadge();
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isBlank()) {
                    setGraphic(null); setText(null); return;
                }
                badge.setSeverity(item);
                setGraphic(badge); setText(null);
            }
        });

        TableColumn<AlertViewModel, String> alertZone = new TableColumn<>("ZONE");
        alertZone.setCellValueFactory(new PropertyValueFactory<>("zoneCode"));
        alertZone.setPrefWidth(65);

        TableColumn<AlertViewModel, String> alertSensor = new TableColumn<>("SENSOR");
        alertSensor.setCellValueFactory(new PropertyValueFactory<>("sensorCode"));
        alertSensor.setPrefWidth(90);

        TableColumn<AlertViewModel, String> alertDate = new TableColumn<>("DATE");
        alertDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        alertDate.setPrefWidth(145);

        TableColumn<AlertViewModel, String> alertStatus = new TableColumn<>("STATUS");
        alertStatus.setCellValueFactory(new PropertyValueFactory<>("statusText"));
        alertStatus.setPrefWidth(105);

        alertsTable.getColumns().setAll(alertId, alertSeverity, alertZone, alertSensor, alertDate, alertStatus);
        alertsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        TableColumn<ZoneViewModel, String> zoneCode = new TableColumn<>("CODE");
        zoneCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        zoneCode.setPrefWidth(60);

        TableColumn<ZoneViewModel, String> zoneName = new TableColumn<>("NAME");
        zoneName.setCellValueFactory(new PropertyValueFactory<>("name"));
        zoneName.setPrefWidth(135);

        TableColumn<ZoneViewModel, String> zoneType = new TableColumn<>("TYPE");
        zoneType.setCellValueFactory(new PropertyValueFactory<>("type"));
        zoneType.setPrefWidth(105);

        TableColumn<ZoneViewModel, String> zoneStatus = new TableColumn<>("STATUS");
        zoneStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        zoneStatus.setPrefWidth(90);

        TableColumn<ZoneViewModel, Integer> zoneSensors = new TableColumn<>("SENSORS");
        zoneSensors.setCellValueFactory(new PropertyValueFactory<>("sensorCount"));
        zoneSensors.setPrefWidth(70);

        zonesTable.getColumns().setAll(zoneCode, zoneName, zoneType, zoneStatus, zoneSensors);
        zonesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    }

    private void setupStatCards() {
        statsContainer.getChildren().clear();
        statCards.clear();

        addStat("Total Zones", "0", "#10b981");
        addStat("Active Zones", "0", "#22c55e");
        addStat("Suspended", "0", "#f59e0b");
        addStat("Sensors", "0", "#3b82f6");
        addStat("Active Sensors", "0", "#06b6d4");
        addStat("Defective", "0", "#ef4444");
        addStat("Alerts", "0", "#f59e0b");
        addStat("Critical", "0", "#ef4444");
        addStat("Animals", "0", "#a78bfa");
        addStat("Crops", "0", "#10b981");
    }

    private void addStat(String label, String value, String accent) {
        StatCard card = new StatCard(null, label, value, accent);
        statCards.put(label, card);
        statsContainer.getChildren().add(card);
    }

    @Override
    public void refresh() {
        if (farmService == null) return;
        updateHero();
        updateStats();
        updateTables();
        if (navigationController != null) navigationController.refreshTopBar();
    }

    private void updateHero() {
        heroFarmName.setText(farmService.getCurrentFarmName());
        int activeZones = farmService.getActiveZoneCount();
        int activeSensors = farmService.getActiveSensorCount();
        int activeAlerts = farmService.getActiveAlertCount();
        int totalZones = farmService.getZoneCount();
        int totalSensors = farmService.getSensorCount();
        heroSubtitle.setText("Monitoring " + totalZones + " zones and "
                + totalSensors + " sensors in real time");
        pillZones.setText(String.valueOf(activeZones));
        pillSensors.setText(String.valueOf(activeSensors));
        pillAlerts.setText(String.valueOf(activeAlerts));
    }

    private void updateStats() {
        setCard("Total Zones", farmService.getZoneCount());
        setCard("Active Zones", farmService.getActiveZoneCount());
        setCard("Suspended", farmService.getSuspendedZoneCount());
        setCard("Sensors", farmService.getSensorCount());
        setCard("Active Sensors", farmService.getActiveSensorCount());
        setCard("Defective", farmService.getDefectiveSensorCount());
        setCard("Alerts", farmService.getAlertCount());
        setCard("Critical", farmService.getCriticalAlertCount());
        setCard("Animals", farmService.getAnimalCount());
        setCard("Crops", farmService.getCropCount());
    }

    private void setCard(String label, int value) {
        StatCard card = statCards.get(label);
        if (card != null) card.setValue(String.valueOf(value));
    }

    private void updateTables() {
        List<AlertViewModel> alertModels = new ArrayList<>();
        for (Alerte alert : farmService.getRecentAlerts(50)) {
            if (alert.estAcquittee()) continue;
            String sensor = alert.getReleve() != null ? alert.getReleve().getCodeCapteur() : "";
            alertModels.add(new AlertViewModel(
                    alert.getId(), alert.getNiveau().name(), alert.getCodeZone(),
                    sensor, alert.getDateCreation(), alert.estAcquittee(), alert.toString()));
        }
        alertsTable.setItems(FXCollections.observableArrayList(alertModels));

        List<ZoneViewModel> zoneModels = new ArrayList<>();
        for (Zone zone : farmService.getZones()) {
            zoneModels.add(new ZoneViewModel(
                    zone.getCode(), zone.getNom(), zone.getTypeZone(),
                    zone.getStatut().name(), zone.getCapteursAssoc().size(),
                    zone.getHistoriqueProduction().moyenneProduction()));
        }
        zonesTable.setItems(FXCollections.observableArrayList(zoneModels));
    }
}
