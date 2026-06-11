package ferme.ui.controllers;

import ferme.enums.EtatSante;
import ferme.enums.TypeElevage;
import ferme.models.Animal;
import ferme.models.PositionGPS;
import ferme.models.ProgrammeAlimentation;
import ferme.models.Zone;
import ferme.models.ZoneAquacole;
import ferme.models.ZoneElevage;
import ferme.ui.AppContext;
import ferme.ui.AppContextAware;
import ferme.ui.Refreshable;
import ferme.ui.navigation.NavigationController;
import ferme.ui.services.DialogService;
import ferme.ui.services.FileImportService;
import ferme.ui.services.UiFarmService;
import ferme.ui.viewmodels.AnimalViewModel;
import ferme.ui.viewmodels.FeedingScheduleViewModel;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class AnimalsController implements AppContextAware, Refreshable {

    @FXML
    private ComboBox<String> zoneFilter;
    @FXML
    private ComboBox<EtatSante> healthFilter;
    @FXML
    private TableView<AnimalViewModel> animalsTable;
    @FXML
    private Button addAnimalButton;
    @FXML
    private Button updateHealthButton;
    @FXML
    private Button addEventButton;
    @FXML
    private Button addWeightButton;
    @FXML
    private Button updateScheduleButton;
    @FXML
    private Button viewHistoryButton;
    @FXML
    private TableView<FeedingScheduleViewModel> feedingSchedulesTable;

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
        setupSchedulesTable();
        healthFilter.getItems().setAll(EtatSante.values());
        zoneFilter.valueProperty().addListener((obs, o, n) -> refresh());
        healthFilter.valueProperty().addListener((obs, o, n) -> refresh());
        addAnimalButton.setOnAction(event -> addAnimal());
        updateHealthButton.setOnAction(event -> updateHealth());
        addEventButton.setOnAction(event -> addEvent());
        addWeightButton.setOnAction(event -> addWeight());
        viewHistoryButton.setOnAction(event -> viewHistory());
        updateScheduleButton.setOnAction(event -> updateFeedingSchedule());
    }

    private void setupTable() {
        TableColumn<AnimalViewModel, Integer> numberCol = new TableColumn<>("#");
        numberCol.setCellValueFactory(new PropertyValueFactory<>("number"));
        numberCol.setPrefWidth(50);
        TableColumn<AnimalViewModel, String> speciesCol = new TableColumn<>("Species");
        speciesCol.setCellValueFactory(new PropertyValueFactory<>("species"));
        speciesCol.setPrefWidth(120);
        TableColumn<AnimalViewModel, String> zoneCol = new TableColumn<>("Zone");
        zoneCol.setCellValueFactory(new PropertyValueFactory<>("zoneCode"));
        zoneCol.setPrefWidth(75);
        TableColumn<AnimalViewModel, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("livestockType"));
        typeCol.setPrefWidth(100);
        TableColumn<AnimalViewModel, Integer> ageCol = new TableColumn<>("Age");
        ageCol.setCellValueFactory(new PropertyValueFactory<>("age"));
        ageCol.setPrefWidth(55);
        TableColumn<AnimalViewModel, Double> weightCol = new TableColumn<>("Weight (kg)");
        weightCol.setCellValueFactory(new PropertyValueFactory<>("weight"));
        weightCol.setPrefWidth(95);
        TableColumn<AnimalViewModel, String> healthCol = new TableColumn<>("Health");
        healthCol.setCellValueFactory(new PropertyValueFactory<>("healthStatus"));
        healthCol.setPrefWidth(105);
        TableColumn<AnimalViewModel, String> posCol = new TableColumn<>("Last GPS Position");
        posCol.setCellValueFactory(new PropertyValueFactory<>("lastPosition"));
        posCol.setPrefWidth(170);
        animalsTable.getColumns().setAll(numberCol, speciesCol, zoneCol, typeCol, ageCol, weightCol, healthCol, posCol);
        animalsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    }

    private void setupSchedulesTable() {
        TableColumn<FeedingScheduleViewModel, String> zoneCol = new TableColumn<>("Zone");
        zoneCol.setCellValueFactory(new PropertyValueFactory<>("zoneCode"));
        zoneCol.setPrefWidth(80);

        TableColumn<FeedingScheduleViewModel, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryCol.setPrefWidth(140);

        TableColumn<FeedingScheduleViewModel, String> feedCol = new TableColumn<>("Feed Type");
        feedCol.setCellValueFactory(new PropertyValueFactory<>("feedType"));
        feedCol.setPrefWidth(130);

        TableColumn<FeedingScheduleViewModel, String> quantityCol = new TableColumn<>("Quantity / Meal");
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantityPerMeal"));
        quantityCol.setPrefWidth(110);

        TableColumn<FeedingScheduleViewModel, Integer> mealsCol = new TableColumn<>("Meals / Day");
        mealsCol.setCellValueFactory(new PropertyValueFactory<>("mealsPerDay"));
        mealsCol.setPrefWidth(95);

        feedingSchedulesTable.getColumns().setAll(zoneCol, categoryCol, feedCol, quantityCol, mealsCol);
        feedingSchedulesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    }

    @Override
    public void refresh() {
        if (farmService == null) {
            return;
        }
        zoneFilter.getItems().setAll(getZoneCodes());
        EtatSante health = healthFilter.getValue();
        String zoneCode = zoneFilter.getValue();
        List<AnimalViewModel> models = new ArrayList<>();
        for (Zone zone : farmService.getZones()) {
            if (!(zone instanceof ZoneElevage)) {
                continue;
            }
            if (zoneCode != null && !zoneCode.isEmpty() && !zone.getCode().equals(zoneCode)) {
                continue;
            }
            ZoneElevage ze = (ZoneElevage) zone;
            for (Animal animal : ze.getAnimaux()) {
                if (health != null && animal.getEtatSante() != health) {
                    continue;
                }
                PositionGPS pos = animal.getPositionActuelle();
                String posText = pos == null ? "" : pos.toString();
                models.add(new AnimalViewModel(
                        animal.getNumero(),
                        animal.getEspece(),
                        zone.getCode(),
                        animal.getTypeElevage().name(),
                        animal.getAge(),
                        animal.getPoids(),
                        animal.getEtatSante().name(),
                        posText));
            }
        }
        animalsTable.setItems(FXCollections.observableArrayList(models));

        List<FeedingScheduleViewModel> schedules = new ArrayList<>();
        for (Zone zone : farmService.getFeedableZones()) {
            ProgrammeAlimentation programme = farmService.getFeedingProgram(zone);
            String feedType = programme != null ? programme.getTypeAliment() : "-";
            String quantity = programme != null ? String.valueOf(programme.getQuantiteParRepas()) : "-";
            int meals = programme != null ? programme.getNombreRepasParJour() : 0;
            schedules.add(new FeedingScheduleViewModel(
                    zone.getCode(),
                    zone.getTypeZone(),
                    feedType,
                    quantity,
                    meals));
        }
        feedingSchedulesTable.setItems(FXCollections.observableArrayList(schedules));
        if (navigationController != null) {
            navigationController.refreshTopBar();
        }
    }

    private List<String> getZoneCodes() {
        List<String> codes = new ArrayList<>();
        for (Zone zone : farmService.getZones()) {
            if (zone instanceof ZoneElevage) {
                codes.add(zone.getCode());
            }
        }
        return codes;
    }

    private AnimalViewModel getSelectedAnimal() {
        AnimalViewModel selected = animalsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            dialogService.showWarning("Selection Required", "Select an animal first.");
        }
        return selected;
    }

    private void addAnimal() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Add Animal");
        ComboBox<ZoneElevage> zoneBox = new ComboBox<>();
        for (Zone zone : farmService.getZones()) {
            if (zone instanceof ZoneElevage) {
                zoneBox.getItems().add((ZoneElevage) zone);
            }
        }
        zoneBox.setCellFactory(list -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(ZoneElevage item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getCode() + " - " + item.getNom());
            }
        });
        zoneBox.setButtonCell(zoneBox.getCellFactory().call(null));

        TextField numberField = new TextField();
        TextField speciesField = new TextField();
        ComboBox<TypeElevage> typeBox = new ComboBox<>();
        typeBox.getItems().setAll(TypeElevage.values());
        TextField ageField = new TextField();
        TextField weightField = new TextField();
        ComboBox<EtatSante> healthBox = new ComboBox<>();
        healthBox.getItems().setAll(EtatSante.values());

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.add(new Label("Zone"), 0, 0);
        grid.add(zoneBox, 1, 0);
        grid.add(new Label("Number"), 0, 1);
        grid.add(numberField, 1, 1);
        grid.add(new Label("Species"), 0, 2);
        grid.add(speciesField, 1, 2);
        grid.add(new Label("Type"), 0, 3);
        grid.add(typeBox, 1, 3);
        grid.add(new Label("Age"), 0, 4);
        grid.add(ageField, 1, 4);
        grid.add(new Label("Weight"), 0, 5);
        grid.add(weightField, 1, 5);
        grid.add(new Label("Health"), 0, 6);
        grid.add(healthBox, 1, 6);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(javafx.scene.control.ButtonType.OK,
                javafx.scene.control.ButtonType.CANCEL);
        var result = dialog.showAndWait();
        if (result.isPresent() && result.isPresent()) {
            try {
                ZoneElevage zone = zoneBox.getValue();
                if (zone == null) {
                    throw new IllegalArgumentException("Zone is required.");
                }
                int number = Integer.parseInt(numberField.getText().trim());
                int age = Integer.parseInt(ageField.getText().trim());
                double weight = Double.parseDouble(weightField.getText().trim());
                farmService.addAnimal(zone.getCode(), number, speciesField.getText().trim(), typeBox.getValue(),
                        age, weight, healthBox.getValue());
                refresh();
            } catch (RuntimeException e) {
                dialogService.showError("Add Animal Failed", e.getMessage());
            }
        }
    }

    private void updateHealth() {
        AnimalViewModel selected = getSelectedAnimal();
        if (selected == null) {
            return;
        }
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Update Health");
        ComboBox<EtatSante> healthBox = new ComboBox<>();
        healthBox.getItems().setAll(EtatSante.values());
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.add(new Label("Health"), 0, 0);
        grid.add(healthBox, 1, 0);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(javafx.scene.control.ButtonType.OK,
                javafx.scene.control.ButtonType.CANCEL);
        var result = dialog.showAndWait();
        if (result.isPresent() && result.isPresent()) {
            farmService.updateAnimalHealth(selected.getNumber(), healthBox.getValue());
            refresh();
        }
    }

    private void addEvent() {
        AnimalViewModel selected = getSelectedAnimal();
        if (selected == null) {
            return;
        }
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Add Health Event");
        TextField descField = new TextField();
        DatePicker datePicker = new DatePicker();
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.add(new Label("Description"), 0, 0);
        grid.add(descField, 1, 0);
        grid.add(new Label("Date"), 0, 1);
        grid.add(datePicker, 1, 1);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(javafx.scene.control.ButtonType.OK,
                javafx.scene.control.ButtonType.CANCEL);
        var result = dialog.showAndWait();
        if (result.isPresent() && result.isPresent()) {
            LocalDate date = datePicker.getValue();
            if (date == null) {
                dialogService.showWarning("Invalid Date", "Provide a valid date.");
                return;
            }
            farmService.addAnimalEvent(selected.getNumber(), descField.getText().trim(), date.toString());
            refresh();
        }
    }

    private void addWeight() {
        AnimalViewModel selected = getSelectedAnimal();
        if (selected == null) {
            return;
        }
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Record Weight");
        TextField weightField = new TextField();
        DatePicker datePicker = new DatePicker();
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.add(new Label("Weight"), 0, 0);
        grid.add(weightField, 1, 0);
        grid.add(new Label("Date"), 0, 1);
        grid.add(datePicker, 1, 1);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(javafx.scene.control.ButtonType.OK,
                javafx.scene.control.ButtonType.CANCEL);
        var result = dialog.showAndWait();
        if (result.isPresent() && result.isPresent()) {
            try {
                double weight = Double.parseDouble(weightField.getText().trim());
                LocalDate date = datePicker.getValue();
                if (date == null) {
                    throw new IllegalArgumentException("Date is required.");
                }
                farmService.addAnimalWeight(selected.getNumber(), weight, date.toString());
                refresh();
            } catch (RuntimeException e) {
                dialogService.showError("Record Weight Failed", e.getMessage());
            }
        }
    }

    private void viewHistory() {
        AnimalViewModel selected = getSelectedAnimal();
        if (selected == null) return;
        Animal animal = farmService.findAnimal(selected.getNumber());
        if (animal == null) {
            dialogService.showWarning("Not Found", "Animal #" + selected.getNumber() + " not found.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Animal #").append(animal.getNumero()).append(" - ").append(animal.getEspece()).append("\n\n");

        sb.append("--- Health Events ---\n");
        List<String> eventDates = animal.getEvenementsDates();
        List<String> eventDescs = animal.getEvenementsDescriptions();
        if (eventDates.isEmpty()) {
            sb.append("  No health events recorded.\n");
        } else {
            for (int i = 0; i < eventDates.size(); i++) {
                sb.append("  ").append(eventDates.get(i)).append(" : ").append(eventDescs.get(i)).append("\n");
            }
        }

        sb.append("\n--- Weight History ---\n");
        List<String> weightDates = animal.getHistoriquePoidsDate();
        List<Double> weightValues = animal.getHistoriquePoids();
        if (weightDates.isEmpty()) {
            sb.append("  No weight records.\n");
        } else {
            for (int i = 0; i < weightDates.size(); i++) {
                sb.append("  ").append(weightDates.get(i)).append(" : ").append(weightValues.get(i)).append(" kg\n");
            }
        }

        TextArea textArea = new TextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setWrapText(false);
        textArea.setPrefSize(500, 350);
        ScrollPane scrollPane = new ScrollPane(textArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        VBox vbox = new VBox(12, scrollPane);
        vbox.setPrefSize(540, 390);
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Animal History - #" + animal.getNumero());
        DialogPane pane = dialog.getDialogPane();
        pane.setContent(vbox);
        pane.getButtonTypes().addAll(ButtonType.CLOSE);
        pane.setPrefSize(560, 430);
        dialog.showAndWait();
    }

    private void updateFeedingSchedule() {
        FeedingScheduleViewModel selected = feedingSchedulesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            dialogService.showWarning("Selection Required", "Select a feeding schedule first.");
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Add / Update Feeding Schedule");
        String feedType = "-".equals(selected.getFeedType()) ? "" : selected.getFeedType();
        String quantity = "-".equals(selected.getQuantityPerMeal()) ? "" : selected.getQuantityPerMeal();
        String meals = selected.getMealsPerDay() <= 0 ? "" : String.valueOf(selected.getMealsPerDay());
        TextField feedField = new TextField(feedType);
        TextField quantityField = new TextField(quantity);
        TextField mealsField = new TextField(meals);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.add(new Label("Feed type"), 0, 0);
        grid.add(feedField, 1, 0);
        grid.add(new Label("Quantity / meal"), 0, 1);
        grid.add(quantityField, 1, 1);
        grid.add(new Label("Meals / day"), 0, 2);
        grid.add(mealsField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(javafx.scene.control.ButtonType.OK,
                javafx.scene.control.ButtonType.CANCEL);
        var result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                double parsedQuantity = Double.parseDouble(quantityField.getText().trim());
                int parsedMeals = Integer.parseInt(mealsField.getText().trim());
                farmService.updateFeedingProgram(selected.getZoneCode(), feedField.getText().trim(), parsedQuantity, parsedMeals);
                refresh();
            } catch (RuntimeException e) {
                dialogService.showError("Update Feeding Schedule Failed", e.getMessage());
            }
        }
    }
}
