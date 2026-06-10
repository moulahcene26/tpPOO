package ferme.ui.navigation;

import ferme.ui.AppContext;
import ferme.ui.AppContextAware;
import ferme.ui.Refreshable;
import ferme.ui.services.DialogService;
import ferme.ui.services.FileImportService;
import ferme.ui.services.UiFarmService;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;

public class NavigationController {

    @FXML
    private Button navDashboard;
    @FXML
    private Button navZones;
    @FXML
    private Button navCultures;
    @FXML
    private Button navAnimals;
    @FXML
    private Button navSensors;
    @FXML
    private Button navReadings;
    @FXML
    private Button navAlerts;
    @FXML
    private Button navImports;
    @FXML
    private Button navSettings;

    @FXML
    private Button newFarmButton;
    @FXML
    private Button importButton;
    @FXML
    private Label farmNameLabel;
    @FXML
    private Label alertBadgeLabel;
    @FXML
    private StackPane contentPane;

    private final Map<Button, String> viewMap = new HashMap<>();

    private AppContext context;
    private UiFarmService farmService;
    private DialogService dialogService;
    private FileImportService fileImportService;

    private Button activeButton;
    private Object currentController;

    public void init(AppContext context,
            UiFarmService farmService,
            DialogService dialogService,
            FileImportService fileImportService) {
        this.context = context;
        this.farmService = farmService;
        this.dialogService = dialogService;
        this.fileImportService = fileImportService;

        viewMap.put(navDashboard, "/ferme/ui/fxml/DashboardView.fxml");
        viewMap.put(navZones, "/ferme/ui/fxml/ZonesView.fxml");
        viewMap.put(navCultures, "/ferme/ui/fxml/CulturesView.fxml");
        viewMap.put(navAnimals, "/ferme/ui/fxml/AnimalsView.fxml");
        viewMap.put(navSensors, "/ferme/ui/fxml/SensorsView.fxml");
        viewMap.put(navReadings, "/ferme/ui/fxml/ReadingsView.fxml");
        viewMap.put(navAlerts, "/ferme/ui/fxml/AlertsView.fxml");
        viewMap.put(navImports, "/ferme/ui/fxml/ImportsView.fxml");
        viewMap.put(navSettings, "/ferme/ui/fxml/SettingsView.fxml");

        wireNavigation();
        wireTopBar();
        navigate(navDashboard);
    }

    private void wireTopBar() {
        newFarmButton.setOnAction(event -> requestFarmCreation());
        importButton.setOnAction(event -> navigate(navImports));
        refreshTopBar();
    }

    private void wireNavigation() {
        for (Button button : viewMap.keySet()) {
            button.setOnAction(event -> navigate(button));
        }
    }

    public void showFirstRunIfNeeded() {
        if (!farmService.hasFarm()) {
            requestFarmCreation();
        }
    }

    private void requestFarmCreation() {
        Optional<String> name = dialogService.promptText("Create Farm", "Farm name:");
        if (name.isPresent()) {
            try {
                farmService.createFarm(name.get());
                refreshTopBar();
                refreshCurrent();
            } catch (RuntimeException e) {
                dialogService.showError("Create Farm Failed", e.getMessage());
            }
        }
    }

    private void navigate(Button button) {
        String view = viewMap.get(button);
        if (view == null) {
            return;
        }
        setActive(button);
        loadView(view);
    }

    private void loadView(String viewPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(viewPath));
            Parent view = loader.load();
            Object controller = loader.getController();
            this.currentController = controller;

            if (controller instanceof AppContextAware) {
                ((AppContextAware) controller).setContext(context, farmService, dialogService, fileImportService, this);
            }
            if (controller instanceof Refreshable) {
                ((Refreshable) controller).refresh();
            }

            // Wrap in ScrollPane for global scrolling
            ScrollPane scroller = new ScrollPane(view);
            scroller.setFitToWidth(true);
            scroller.setFitToHeight(false);
            scroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scroller.getStyleClass().add("scroll-pane");
            scroller.setPannable(true);

            contentPane.getChildren().setAll(scroller);
            refreshTopBar();
        } catch (Exception e) {
            dialogService.showError("Navigation Error", "Unable to load view: " + viewPath);
        }
    }

    private void setActive(Button button) {
        if (activeButton != null) {
            activeButton.getStyleClass().remove("nav-button-active");
        }
        activeButton = button;
        if (!activeButton.getStyleClass().contains("nav-button-active")) {
            activeButton.getStyleClass().add("nav-button-active");
        }
    }

    public void refreshTopBar() {
        farmNameLabel.setText(farmService.getCurrentFarmName());
        int alertCount = farmService.getActiveAlertCount();
        alertBadgeLabel.setText(String.valueOf(alertCount));
        alertBadgeLabel.setVisible(alertCount > 0);
    }

    public void refreshCurrent() {
        if (currentController instanceof Refreshable) {
            ((Refreshable) currentController).refresh();
        }
    }

    public FileImportService getFileImportService() {
        return fileImportService;
    }
}
