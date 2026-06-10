package ferme.ui.components;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Clean metric card — colored dot + muted label, big value. No icons.
 * ┌──────────────────────┐
 * │ ● TOTAL ZONES        │
 * │ 42                   │
 * └──────────────────────┘
 */
public class StatCard extends VBox {
    private final Label valueLabel;
    private final Label titleLabel;
    private final Region dot;

    public StatCard(String title, String value) {
        this(null, title, value, "#10b981");
    }

    public StatCard(String icon, String title, String value, String accentColor) {
        getStyleClass().add("stat-card");
        setAlignment(Pos.CENTER_LEFT);

        String accent = (accentColor == null || accentColor.isEmpty()) ? "#10b981" : accentColor;

        // Colored dot
        dot = new Region();
        dot.setMinSize(7, 7);
        dot.setPrefSize(7, 7);
        dot.setMaxSize(7, 7);
        dot.setStyle("-fx-background-color: " + accent + "; -fx-background-radius: 4;");

        titleLabel = new Label(title);
        titleLabel.getStyleClass().add("stat-label");

        HBox header = new HBox(8, dot, titleLabel);
        header.setAlignment(Pos.CENTER_LEFT);

        valueLabel = new Label(value);
        valueLabel.getStyleClass().add("stat-value");

        getChildren().addAll(header, valueLabel);
    }

    public void setValue(String value) {
        valueLabel.setText(value);
    }

    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    public void setIcon(String icon) {
        // no-op — icons removed
    }

    public void setAccentColor(String color) {
        dot.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 4;");
    }
}
