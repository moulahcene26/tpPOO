package ferme.models;

public class PositionGPS {
    private double latitude;
    private double longitude;

    public PositionGPS(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public double distanceVers(PositionGPS autre) {
        double dLat = Math.toRadians(autre.latitude - this.latitude);
        double dLon = Math.toRadians(autre.longitude - this.longitude);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(autre.latitude))
                 * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 6371000 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    public String toString() {
        return "(" + latitude + ", " + longitude + ")";
    }
}
