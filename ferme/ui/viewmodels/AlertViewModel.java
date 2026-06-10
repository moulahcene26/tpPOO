package ferme.ui.viewmodels;

public class AlertViewModel {
    private final int id;
    private final String severity;
    private final String zoneCode;
    private final String sensorCode;
    private final String date;
    private final boolean acknowledged;
    private final String summary;

    public AlertViewModel(int id, String severity, String zoneCode, String sensorCode,
            String date, boolean acknowledged, String summary) {
        this.id = id;
        this.severity = severity;
        this.zoneCode = zoneCode;
        this.sensorCode = sensorCode;
        this.date = date;
        this.acknowledged = acknowledged;
        this.summary = summary;
    }

    public int getId() { return id; }
    public String getSeverity() { return severity; }
    public String getZoneCode() { return zoneCode; }
    public String getSensorCode() { return sensorCode; }
    public String getDate() { return date; }
    public boolean isAcknowledged() { return acknowledged; }
    public String getSummary() { return summary; }

    /** Returns human-readable status for badge rendering */
    public String getStatusText() {
        return acknowledged ? "Acknowledged" : "Active";
    }
}
