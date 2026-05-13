package ferme.models;

import ferme.enums.NiveauGravite;

public class CapteurGPS extends Capteur {
    private int numeroAnimal;
    private double latitude;
    private double longitude;
    private double rayonZoneMetres;

    public CapteurGPS(String code, String codeZone, int numeroAnimal,
                      PositionGPS centreZone, double rayonZoneMetres) {
        this(code, codeZone, numeroAnimal,
                centreZone.getLatitude(), centreZone.getLongitude(), rayonZoneMetres);
    }

    public CapteurGPS(String code, String codeZone, int numeroAnimal,
                      double latitude, double longitude, double rayonZoneMetres) {
        super(code, codeZone, 0, 0);
        this.numeroAnimal = numeroAnimal;
        this.latitude = latitude;
        this.longitude = longitude;
        this.rayonZoneMetres = rayonZoneMetres;
    }

    public int getNumeroAnimal() { return numeroAnimal; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public PositionGPS getCentreZone() { return new PositionGPS(latitude, longitude); }
    public double getRayonZoneMetres() { return rayonZoneMetres; }

    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public void setCentreZone(PositionGPS centre) {
        this.latitude = centre.getLatitude();
        this.longitude = centre.getLongitude();
    }
    public void setRayonZoneMetres(double rayon) { this.rayonZoneMetres = rayon; }

    public boolean estDansZone(PositionGPS position) {
        return getCentreZone().distanceVers(position) <= rayonZoneMetres;
    }

    public NiveauGravite evaluerPositionGPS(PositionGPS position) {
        double distance = getCentreZone().distanceVers(position);
        if (distance > rayonZoneMetres) {
            if (distance > rayonZoneMetres * 1.5) {
                return NiveauGravite.CRITIQUE;
            }
            return NiveauGravite.AVERTISSEMENT;
        }
        return NiveauGravite.NORMAL;
    }

    @Override
    public boolean enregistrerReleve(Releve releve) {
        if (statut == ferme.enums.StatutCapteur.SUSPENDU) {
            System.out.println("  Capteur GPS " + code + " suspendu, relevé ignoré.");
            return false;
        }
        if (!releve.estGPS() || releve.getPosition() == null) {
            System.out.println("  Capteur GPS " + code + " incompatible avec un relevé numérique.");
            return false;
        }

        NiveauGravite niveau = evaluerPositionGPS(releve.getPosition());
        releve.setNiveau(niveau);

        releves.add(releve);
        return true;
    }

    public String getTypeCapteur() { return "GPS"; }
    public String getUnite() { return "coordonnées"; }

    @Override
    public String toString() {
        return "Capteur GPS " + code + " | Animal #" + numeroAnimal
             + " | Zone: " + codeZone + " | Statut: " + statut.getLibelle()
             + " | Centre: (" + latitude + ", " + longitude + ")"
             + " | Rayon: " + rayonZoneMetres + "m";
    }
}
