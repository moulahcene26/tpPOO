package ferme.models;

public class ReleveGPS extends Releve {
    private PositionGPS position;

    public ReleveGPS(PositionGPS position, String date, String codeCapteur) {
        super(date, codeCapteur);
        this.position = position;
    }

    @Override
    public boolean estGPS() { return true; }

    @Override
    public PositionGPS getPosition() { return position; }

    @Override
    public String getUnite() { return "GPS"; }

    @Override
    public String toString() {
        return "[" + getDate() + "] Capteur " + getCodeCapteur() + " : Position " + position
             + " | " + getNiveau().getLibelle();
    }
}
