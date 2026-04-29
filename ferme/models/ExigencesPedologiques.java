package ferme.models;

public class ExigencesPedologiques {
    private double phMin;
    private double phMax;
    private double humiditeMin;
    private double humiditeMax;

    public ExigencesPedologiques(double phMin, double phMax, double humiditeMin, double humiditeMax) {
        this.phMin = phMin;
        this.phMax = phMax;
        this.humiditeMin = humiditeMin;
        this.humiditeMax = humiditeMax;
    }

    public double getPhMin() { return phMin; }
    public double getPhMax() { return phMax; }
    public double getHumiditeMin() { return humiditeMin; }
    public double getHumiditeMax() { return humiditeMax; }

    public boolean phDansPlage(double ph) {
        return ph >= phMin && ph <= phMax;
    }

    public boolean humiditeDansPlage(double humidite) {
        return humidite >= humiditeMin && humidite <= humiditeMax;
    }

    public String toString() {
        return "pH: [" + phMin + " - " + phMax + "], Humidité: [" + humiditeMin + "% - " + humiditeMax + "%]";
    }
}
