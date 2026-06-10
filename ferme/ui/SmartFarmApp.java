package ferme.ui;

import ferme.ui.navigation.NavigationController;
import ferme.ui.services.DialogService;
import ferme.ui.services.FileImportService;
import ferme.ui.services.UiFarmService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SmartFarmApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        AppContext context = new AppContext();
        DialogService dialogService = new DialogService(stage);
        UiFarmService farmService = new UiFarmService(context);
        FileImportService fileImportService = new FileImportService(stage);

        FXMLLoader loader = new FXMLLoader(SmartFarmApp.class.getResource("/ferme/ui/fxml/MainLayout.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 1280, 800);
        scene.getStylesheets().add(SmartFarmApp.class.getResource("/ferme/ui/css/app.css").toExternalForm());

        NavigationController navigationController = loader.getController();
        navigationController.init(context, farmService, dialogService, fileImportService);

        stage.setTitle("Smart Farm Manager");
        stage.setScene(scene);
        stage.show();

        navigationController.showFirstRunIfNeeded();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
