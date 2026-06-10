package ferme.ui.controllers;

import ferme.ui.AppContext;
import ferme.ui.AppContextAware;
import ferme.ui.Refreshable;
import ferme.ui.navigation.NavigationController;
import ferme.ui.services.DialogService;
import ferme.ui.services.FileImportService;
import ferme.ui.services.UiFarmService;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class SettingsController implements AppContextAware, Refreshable {

    @FXML private Label currentFarmLabel;
    @FXML private Button createFarmButton;
    @FXML private Button clearFarmButton;
    @FXML private TextArea aboutArea;

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
        setupAbout();
        wireActions();
    }

    private void setupAbout() {
        aboutArea.setEditable(false);
        aboutArea.setText("Smart Farm Manager v2.0\n\n"
                + "An intelligent farm management system for monitoring\n"
                + "crops, livestock, aquaculture, and environmental sensors.\n\n"
                + "Features:\n"
                + "- Multi-zone management (Culture, Livestock, Aquaculture)\n"
                + "- Real-time sensor monitoring (Env, Soil, Bio, GPS, Water)\n"
                + "- Automated alert generation\n"
                + "- Production history tracking\n"
                + "- Data import from text files\n\n"
                + "Built with JavaFX and a clean domain-driven architecture.");
    }

    private void wireActions() {
        createFarmButton.setOnAction(event -> createFarm());
        clearFarmButton.setOnAction(event -> clearFarm());
    }

    @Override
    public void refresh() {
        currentFarmLabel.setText(farmService.getCurrentFarmName());
        if (navigationController != null) {
            navigationController.refreshTopBar();
        }
    }

    private void createFarm() {
        boolean replacing = farmService.hasFarm();
        String prompt = replacing
                ? "Current farm will be replaced. Enter new farm name:"
                : "Enter farm name:";
        Optional<String> name = dialogService.promptText("Create Farm", prompt);
        if (name.isPresent()) {
            try {
                farmService.createFarm(name.get());
                refresh();
                dialogService.showInfo("Farm Created", "Farm '" + name.get() + "' created successfully.");
            } catch (RuntimeException e) {
                dialogService.showError("Create Farm Failed", e.getMessage());
            }
        }
    }

    private void clearFarm() {
        if (!farmService.hasFarm()) {
            dialogService.showInfo("No Farm", "No farm is currently loaded.");
            return;
        }
        if (dialogService.confirm("Clear Farm",
                "Are you sure you want to clear the current farm?\n\n"
                        + "All zones, sensors, readings, and alerts will be lost.\n"
                        + "This action cannot be undone.")) {
            farmService.clearFarm();
            refresh();
            dialogService.showInfo("Farm Cleared", "The farm has been cleared.");
        }
    }
}
