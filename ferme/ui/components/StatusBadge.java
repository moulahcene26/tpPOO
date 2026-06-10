package ferme.ui.components;

import javafx.scene.control.Label;

public class StatusBadge extends Label {

    public StatusBadge() {
        getStyleClass().add("badge");
    }

    public void setStatus(String status) {
        getStyleClass().removeAll("badge-success", "badge-warning", "badge-danger", "badge-muted");
        String value = status == null ? "" : status.toUpperCase();
        if (value.contains("ACTIVE") || value.contains("ACTIF")) {
            getStyleClass().add("badge-success");
        } else if (value.contains("SUSPEND")) {
            getStyleClass().add("badge-muted");
        } else if (value.contains("DEFAILL") || value.contains("DEFECT")) {
            getStyleClass().add("badge-danger");
        } else {
            getStyleClass().add("badge-warning");
        }
        setText(status);
    }
}
