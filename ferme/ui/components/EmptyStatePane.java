package ferme.ui.components;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class EmptyStatePane extends VBox {
    private final Label titleLabel;
    private final Label messageLabel;

    public EmptyStatePane(String title, String message) {
        getStyleClass().add("empty-state");
        setAlignment(Pos.CENTER);
        setSpacing(8);
        titleLabel = new Label(title);
        titleLabel.getStyleClass().add("empty-state-title");
        messageLabel = new Label(message);
        messageLabel.getStyleClass().add("empty-state-text");
        getChildren().addAll(titleLabel, messageLabel);
    }
}
