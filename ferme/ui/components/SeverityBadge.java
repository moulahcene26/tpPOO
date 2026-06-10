package ferme.ui.components;

import javafx.scene.control.Label;

public class SeverityBadge extends Label {

    public SeverityBadge() {
        getStyleClass().add("badge");
    }

    public void setSeverity(String severity) {
        getStyleClass().removeAll("badge-success", "badge-warning", "badge-danger", "badge-muted");
        String value = severity == null ? "" : severity.toUpperCase();
        if ("CRITIQUE".equals(value) || "CRITICAL".equals(value)) {
            getStyleClass().add("badge-danger");
        } else if ("AVERTISSEMENT".equals(value) || "WARNING".equals(value)) {
            getStyleClass().add("badge-warning");
        } else if ("NORMAL".equals(value)) {
            getStyleClass().add("badge-success");
        } else {
            getStyleClass().add("badge-muted");
        }
        setText(severity);
    }
}
