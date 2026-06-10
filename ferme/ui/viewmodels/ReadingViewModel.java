package ferme.ui.viewmodels;

public class ReadingViewModel {
    private final String sensorCode;
    private final String type;
    private final String value;
    private final String unit;
    private final String date;
    private final String severity;

    public ReadingViewModel(String sensorCode, String type, String value, String unit, String date, String severity) {
        this.sensorCode = sensorCode;
        this.type = type;
        this.value = value;
        this.unit = unit;
        this.date = date;
        this.severity = severity;
    }

    public String getSensorCode() {
        return sensorCode;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public String getUnit() {
        return unit;
    }

    public String getDate() {
        return date;
    }

    public String getSeverity() {
        return severity;
    }
}
