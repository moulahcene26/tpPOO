package ferme.models;

public class ReleveNumerique extends Releve {
    private double valeur;
    private String unite;

    public ReleveNumerique(double valeur, String unite, String date, String codeCapteur) {
        super(date, codeCapteur);
        this.valeur = valeur;
        this.unite = unite;
    }

    @Override
    public boolean estGPS() { return false; }

    @Override
    public double getValeur() { return valeur; }

    @Override
    public String getUnite() { return unite; }

    @Override
    public String toString() {
        return "[" + getDate() + "] Capteur " + getCodeCapteur() + " : " + valeur + " " + unite
             + " | " + getNiveau().getLibelle();
    }
}
