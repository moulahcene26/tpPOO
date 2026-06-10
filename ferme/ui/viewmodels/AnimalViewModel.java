package ferme.ui.viewmodels;

public class AnimalViewModel {
    private final int number;
    private final String species;
    private final String zoneCode;
    private final String livestockType;
    private final int age;
    private final double weight;
    private final String healthStatus;
    private final String lastPosition;

    public AnimalViewModel(int number, String species, String zoneCode, String livestockType,
            int age, double weight, String healthStatus, String lastPosition) {
        this.number = number;
        this.species = species;
        this.zoneCode = zoneCode;
        this.livestockType = livestockType;
        this.age = age;
        this.weight = weight;
        this.healthStatus = healthStatus;
        this.lastPosition = lastPosition;
    }

    public int getNumber() {
        return number;
    }

    public String getSpecies() {
        return species;
    }

    public String getZoneCode() {
        return zoneCode;
    }

    public String getLivestockType() {
        return livestockType;
    }

    public int getAge() {
        return age;
    }

    public double getWeight() {
        return weight;
    }

    public String getHealthStatus() {
        return healthStatus;
    }

    public String getLastPosition() {
        return lastPosition;
    }
}
