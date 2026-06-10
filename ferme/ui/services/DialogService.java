package ferme.ui.services;

import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class DialogService {
    private final Stage owner;

    public DialogService(Stage owner) {
        this.owner = owner;
    }

    public void showInfo(String title, String message) {
        showAlert(Alert.AlertType.INFORMATION, title, message);
    }

    public void showWarning(String title, String message) {
        showAlert(Alert.AlertType.WARNING, title, message);
    }

    public void showError(String title, String message) {
        showAlert(Alert.AlertType.ERROR, title, message);
    }

    public boolean confirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.OK, ButtonType.CANCEL);
        alert.initOwner(owner);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setTitle(title);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    public Optional<String> promptText(String title, String prompt) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText(prompt);
        return dialog.showAndWait();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.initOwner(owner);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
