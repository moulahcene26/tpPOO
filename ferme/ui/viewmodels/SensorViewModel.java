package ferme.ui.viewmodels;

public class SensorViewModel {
    private final String code;
    private final String family;
    private final String type;
    private final String zoneCode;
    private final String status;
    private final double min;
    private final double max;
    private final String latestReading;
    private final String latestSeverity;

    public SensorViewModel(String code, String family, String type, String zoneCode,
            String status, double min, double max,
            String latestReading, String latestSeverity) {
        this.code = code;
        this.family = family;
        this.type = type;
        this.zoneCode = zoneCode;
        this.status = status;
        this.min = min;
        this.max = max;
        this.latestReading = latestReading;
        this.latestSeverity = latestSeverity;
    }

    public String getCode() {
        return code;
    }

    public String getFamily() {
        return family;
    }

    public String getType() {
        return type;
    }

    public String getZoneCode() {
        return zoneCode;
    }

    public String getStatus() {
        return status;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public String getLatestReading() {
        return latestReading;
    }

    public String getLatestSeverity() {
        return latestSeverity;
    }
}
