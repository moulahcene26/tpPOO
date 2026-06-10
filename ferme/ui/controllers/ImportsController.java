package ferme.ui.controllers;

import ferme.ui.AppContext;
import ferme.ui.AppContextAware;
import ferme.ui.Refreshable;
import ferme.ui.navigation.NavigationController;
import ferme.ui.services.DialogService;
import ferme.ui.services.FileImportService;
import ferme.ui.services.UiFarmService;
import java.io.File;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class ImportsController implements AppContextAware, Refreshable {

    @FXML private Label globalFileLabel;
    @FXML private Button chooseGlobalFileButton;
    @FXML private Button importGlobalButton;

    @FXML private Label sensorFileLabel;
    @FXML private Button chooseSensorFileButton;
    @FXML private Button importSensorButton;

    @FXML private Label readingFileLabel;
    @FXML private Button chooseReadingFileButton;
    @FXML private Button importReadingButton;

    @FXML private TextArea importStatusArea;

    private UiFarmService farmService;
    private DialogService dialogService;
    private FileImportService fileImportService;
    private NavigationController navigationController;

    private File globalFile;
    private File sensorFile;
    private File readingFile;

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
        wireActions();
    }

    private void wireActions() {
        chooseGlobalFileButton.setOnAction(event -> {
            globalFile = fileImportService.pickFile("Select Global Data File");
            if (globalFile != null) {
                globalFileLabel.setText(globalFile.getName());
            }
        });
        importGlobalButton.setOnAction(event -> importGlobalData());

        chooseSensorFileButton.setOnAction(event -> {
            sensorFile = fileImportService.pickFile("Select Sensor File");
            if (sensorFile != null) {
                sensorFileLabel.setText(sensorFile.getName());
            }
        });
        importSensorButton.setOnAction(event -> importSensors());

        chooseReadingFileButton.setOnAction(event -> {
            readingFile = fileImportService.pickFile("Select Readings File");
            if (readingFile != null) {
                readingFileLabel.setText(readingFile.getName());
            }
        });
        importReadingButton.setOnAction(event -> importReadings());
    }

    @Override
    public void refresh() {
        if (navigationController != null) {
            navigationController.refreshTopBar();
        }
    }

    private void importGlobalData() {
        if (!farmService.hasFarm()) {
            dialogService.showError("No Farm", "Create a farm before importing data.");
            return;
        }
        if (globalFile == null) {
            dialogService.showWarning("No File", "Select a global data file first.");
            return;
        }
        try {
            int count = farmService.importGlobalData(globalFile);
            appendStatus("Global data: " + count + " entities imported from " + globalFile.getName());
            dialogService.showInfo("Import Complete", count + " entities imported.");
            refreshAll();
        } catch (RuntimeException e) {
            dialogService.showError("Import Failed", e.getMessage());
            appendStatus("Global data import FAILED: " + e.getMessage());
        }
    }

    private void importSensors() {
        if (!farmService.hasFarm()) {
            dialogService.showError("No Farm", "Create a farm before importing sensors.");
            return;
        }
        if (sensorFile == null) {
            dialogService.showWarning("No File", "Select a sensor file first.");
            return;
        }
        try {
            int count = farmService.importSensors(sensorFile);
            appendStatus("Sensors: " + count + " loaded from " + sensorFile.getName());
            dialogService.showInfo("Import Complete", count + " sensors loaded.");
            refreshAll();
        } catch (RuntimeException e) {
            dialogService.showError("Import Failed", e.getMessage());
            appendStatus("Sensor import FAILED: " + e.getMessage());
        }
    }

    private void importReadings() {
        if (!farmService.hasFarm()) {
            dialogService.showError("No Farm", "Create a farm before importing readings.");
            return;
        }
        if (readingFile == null) {
            dialogService.showWarning("No File", "Select a readings file first.");
            return;
        }
        try {
            int count = farmService.importReadings(readingFile);
            appendStatus("Readings: " + count + " loaded from " + readingFile.getName());
            dialogService.showInfo("Import Complete", count + " readings loaded.");
            refreshAll();
        } catch (RuntimeException e) {
            dialogService.showError("Import Failed", e.getMessage());
            appendStatus("Readings import FAILED: " + e.getMessage());
        }
    }

    private void appendStatus(String message) {
        importStatusArea.appendText(message + "\n");
    }

    private void refreshAll() {
        if (navigationController != null) {
            navigationController.refreshCurrent();
            navigationController.refreshTopBar();
        }
    }
}
