package ferme.ui.viewmodels;

public class CultureViewModel {
    private final String zoneCode;
    private final String name;
    private final String family;
    private final String stage;
    private final String plantingDate;
    private final String harvestDate;
    private final String phRange;
    private final String humidityRange;

    public CultureViewModel(String zoneCode, String name, String family, String stage,
            String plantingDate, String harvestDate,
            String phRange, String humidityRange) {
        this.zoneCode = zoneCode;
        this.name = name;
        this.family = family;
        this.stage = stage;
        this.plantingDate = plantingDate;
        this.harvestDate = harvestDate;
        this.phRange = phRange;
        this.humidityRange = humidityRange;
    }

    public String getZoneCode() {
        return zoneCode;
    }

    public String getName() {
        return name;
    }

    public String getFamily() {
        return family;
    }

    public String getStage() {
        return stage;
    }

    public String getPlantingDate() {
        return plantingDate;
    }

    public String getHarvestDate() {
        return harvestDate;
    }

    public String getPhRange() {
        return phRange;
    }

    public String getHumidityRange() {
        return humidityRange;
    }
}
