package ferme.ui.viewmodels;

public class ZoneViewModel {
    private final String code;
    private final String name;
    private final String type;
    private final String status;
    private final int sensorCount;
    private final double productionAverage;

    public ZoneViewModel(String code, String name, String type, String status, int sensorCount,
            double productionAverage) {
        this.code = code;
        this.name = name;
        this.type = type;
        this.status = status;
        this.sensorCount = sensorCount;
        this.productionAverage = productionAverage;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getStatus() {
        return status;
    }

    public int getSensorCount() {
        return sensorCount;
    }

    public double getProductionAverage() {
        return productionAverage;
    }
}
